package com.espirit.ps.rw.dependency;


import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;


/**
 * Maps all calls to internal ArrayList.
 * Method add(e), add(i,e), addAll(c) amd addAll(i,c) track the validation State.
 * clear() resets validation state in addition to clearing the list.
 * !!! Any other method of adding, setting or removing objects is not tracked and hence should be avoided !!!
 */
public class ValidationStateList implements List<ValidationState> {

	private List<ValidationState> validationStateList;


	ValidationStateList() {
		validationStateList = new LinkedList<>();
	}


	@Override
	public boolean add(final ValidationState validationState) {
		if (validationStateList.contains(validationState)) {
			return true;
		} else {
			return validationStateList.add(validationState);
		}
	}


	@Override
	public void add(final int index, final ValidationState element) {
		if (!validationStateList.contains(element)) {
			validationStateList.add(index, element);
		}
	}


	@Override
	public boolean addAll(@NotNull final Collection<? extends ValidationState> c) {
		final boolean addAllResult = validationStateList.addAll(c);
		validationStateList = validationStateList.stream().distinct().collect(Collectors.toList());
		return addAllResult;
	}


	@Override
	public boolean addAll(final int index, @NotNull final Collection<? extends ValidationState> c) {
		final boolean addAllResult = validationStateList.addAll(index, c);
		validationStateList = validationStateList.stream().distinct().collect(Collectors.toList());
		return addAllResult;
	}



	@Override
	public void clear() {
		validationStateList.clear();
	}


	@Override
	public boolean contains(final Object o) {
		return validationStateList.contains(o);
	}


	@Override
	public boolean containsAll(@NotNull final Collection<?> c) {
		return new HashSet<>(validationStateList).containsAll(c);
	}


	@Override
	public ValidationState get(final int index) {
		return validationStateList.get(index);
	}


	@Override
	public int indexOf(final Object o) {
		return validationStateList.indexOf(o);
	}


	@Override
	public boolean isEmpty() {
		return validationStateList.isEmpty();
	}


	@NotNull
	@Override
	public Iterator<ValidationState> iterator() {
		return validationStateList.iterator();
	}


	@Override
	public int lastIndexOf(final Object o) {
		return validationStateList.lastIndexOf(o);
	}


	@NotNull
	@Override
	public ListIterator<ValidationState> listIterator() {
		return validationStateList.listIterator();
	}


	@NotNull
	@Override
	public ListIterator<ValidationState> listIterator(final int index) {
		return validationStateList.listIterator(index);
	}


	@Override
	public boolean remove(final Object o) {
		return validationStateList.remove(o);
	}


	@Override
	public ValidationState remove(final int index) {
		return validationStateList.remove(index);
	}


	@Override
	public boolean removeAll(@NotNull final Collection<?> c) {
		return validationStateList.removeAll(c);
	}


	@Override
	public boolean retainAll(@NotNull final Collection<?> c) {
		return validationStateList.retainAll(c);
	}


	@Override
	public ValidationState set(final int index, final ValidationState element) {
		return validationStateList.set(index, element);
	}


	@Override
	public int size() {
		return validationStateList.size();
	}


	@NotNull
	@Override
	public List<ValidationState> subList(final int fromIndex, final int toIndex) {
		return validationStateList.subList(fromIndex, toIndex);
	}


	@NotNull
	@Override
	public Object[] toArray() {
		return validationStateList.toArray();
	}


	@NotNull
	@Override
	public <T> T[] toArray(@NotNull final T[] a) {
		return validationStateList.toArray(a);
	}
}
