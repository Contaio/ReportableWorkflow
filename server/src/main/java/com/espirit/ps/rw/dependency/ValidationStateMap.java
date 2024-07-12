package com.espirit.ps.rw.dependency;


import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.sitestore.PageRef;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class ValidationStateMap {

	private final Map<Object, List<ValidationState>> validationMap = new HashMap<>();
	private final Manager                            manager;


	public ValidationStateMap(final Manager manager) {
		this.manager = manager;
	}


	public boolean add(final Handle handle) {
		if (handle.getKeyObject() != null) {
			if (contains(handle.getKeyObject())) {
				validationMap.get(handle.getKeyObject()).addAll(handle.getValidationStates());
			} else {
				validationMap.put(handle.getKeyObject(), handle.getValidationStates());
			}

			return true;
		} else {
			return false;
		}
	}


	public boolean contains(final Object o) {
		return validationMap.containsKey(o);
	}


	private List<IDProvider> deepSortIDProviders(final List<IDProvider> inList) {
		Map<Integer, List<IDProvider>> idProviderMap = new HashMap<>();
		List<IDProvider>               resultList    = new LinkedList<>();
		int                            maxDeep       = 0;

		// filter null elements out
		// missing permissions can cause containing 'null' elements
		for (IDProvider idProvider : inList.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
			int currentDeep = getDeep(idProvider);

			if (currentDeep > maxDeep) {
				maxDeep = currentDeep;
			}

			if (idProviderMap.containsKey(currentDeep)) {
				idProviderMap.get(currentDeep).add(idProvider);
			} else {
				List<IDProvider> list = new LinkedList<>();
				list.add(idProvider);
				idProviderMap.put(currentDeep, list);
			}
		}

		for (int i = maxDeep; i >= 0; i--) {
			if (idProviderMap.containsKey(i)) {
				resultList.addAll(idProviderMap.get(i));
			}
		}

		return resultList;
	}


	private List<IDProvider> extractIDProvider(final Store.Type type, final boolean onlyWithStates) {
		Set<StoreElement> ignoreSet  = ReportableWorkflowUtil.buildIgnoreSet(manager);
		List<IDProvider>  resultList = new LinkedList<>();

		for (Object element : validationMap.keySet()) {
			if (onlyWithStates && validationMap.get(element).isEmpty()) {
				continue;
			}

			if (element instanceof IDProvider) {
				boolean isIgnored   = ignoreSet.contains(element);
				boolean isStoreType = type.equals(((IDProvider) element).getStore().getType());

				if (!isIgnored && isStoreType) {
					switch (manager.getAction()) {
						case RELEASE:
							if (((IDProvider) element).isReleaseSupported() && !resultList.contains(element)) {
								resultList.add((IDProvider) element);
							}

							break;

						case DELETE:
							if (!resultList.contains(element)) {
								// Should i delete this?
								List<ValidationState> validationStates = validationMap.get(element);
								boolean               shouldBeIgnored  = validationStates.stream().anyMatch(state -> state.getType() == ValidationState.Type.IGNORED_ON_DELETE);
								if (!shouldBeIgnored) {
									resultList.add((IDProvider) element);
								}
							}

							break;
					}
				}
			}
		}

		if (type.equals(Store.Type.SITESTORE)) {
			resultList = sortSiteStoreIDProviders(resultList);
		} else {
			resultList = deepSortIDProviders(resultList);
		}

		return resultList;
	}


	public List<IDProvider> getActionables(final boolean onlyWithStates) {
		List<IDProvider> resultList = new LinkedList<>();

		if (manager.isActionable()) {
			resultList.addAll(extractIDProvider(Store.Type.GLOBALSTORE, onlyWithStates));
			resultList.addAll(extractIDProvider(Store.Type.MEDIASTORE, onlyWithStates));
			resultList.addAll(extractIDProvider(Store.Type.SITESTORE, onlyWithStates));
			resultList.addAll(extractIDProvider(Store.Type.PAGESTORE, onlyWithStates));
			resultList.addAll(extractIDProvider(Store.Type.CONTENTSTORE, onlyWithStates));
		}

		return resultList;
	}


	public ValidationStateList getAllValidationStates(final Collection<ValidationState.Type> without) {
		ValidationState.Type[] typeList  = new ValidationState.Type[without.size()];
		ValidationState.Type[] typeArray = without.toArray(typeList);

		return getAllValidationStates(typeArray);
	}


	public ValidationStateList getAllValidationStates(final ValidationState.Type... without) {
		ValidationStateList resultList = new ValidationStateList();

		if (without == null || without.length == 0) {
			for (List<ValidationState> stateList : validationMap.values()) {
				resultList.addAll(stateList);
			}
		} else {
			for (List<ValidationState> stateList : validationMap.values()) {
				for (ValidationState state : stateList) {
					boolean add = true;

					for (ValidationState.Type type : without) {
						if (state.getType().equals(type)) {
							add = false;
							break;
						}
					}

					if (add) {
						resultList.add(state);
					}
				}
			}
		}

		return resultList;
	}


	private int getDeep(final IDProvider idProvider) {
		int deep = 0;

		IDProvider currentIdProvider = idProvider;
		IDProvider lastIdProvider    = null;

		while (!(currentIdProvider instanceof Store)) {
			if (currentIdProvider == null) {
				if (lastIdProvider == null) {
					Logging.logWarning("Missing initial IDProvider.", getClass());
				} else {
					Logging.logWarning(String.format("IDProvider without parent: #%d", lastIdProvider.getId()), getClass());
				}
				break;
			}
			deep++;
			lastIdProvider = currentIdProvider;
			currentIdProvider = currentIdProvider.getParent();
		}

		return deep;
	}


	public List<ValidationState> getValidationStates(final Collection<ValidationState.Type> with) {
		ValidationState.Type[] typeList  = new ValidationState.Type[with.size()];
		ValidationState.Type[] typeArray = with.toArray(typeList);

		return getValidationStates(typeArray);
	}


	public List<ValidationState> getValidationStates(final ValidationState.Type... with) {
		List<ValidationState> resultList = new LinkedList<>();

		if (with != null && with.length != 0) {
			for (List<ValidationState> stateList : validationMap.values()) {
				for (ValidationState state : stateList) {
					for (ValidationState.Type type : with) {
						if (state.getType().equals(type)) {
							resultList.add(state);
							break;
						}
					}
				}
			}
		}

		return resultList;
	}


	public int numberOfHandles() {
		return validationMap.size();
	}


	public long numberOfValidationStates() {
		long size = 0;

		for (List<ValidationState> list : validationMap.values()) {
			if (list != null) {
				size += list.size();
			}
		}

		return size;
	}


	private List<IDProvider> sortSiteStoreIDProviders(final List<IDProvider> inList) {
		List<IDProvider> idProviderList = new LinkedList<>();
		List<IDProvider> resultList     = new LinkedList<>();

		idProviderList.addAll(inList);

		// PageRef.isStartNode() -> true
		for (IDProvider idProvider : idProviderList) {
			if (idProvider instanceof PageRef && ((PageRef) idProvider).isStartNode()) {
				resultList.add(idProvider);
			}
		}

		idProviderList.removeAll(resultList);

		idProviderList = deepSortIDProviders(idProviderList);
		resultList.addAll(idProviderList);

		return resultList;
	}
}
