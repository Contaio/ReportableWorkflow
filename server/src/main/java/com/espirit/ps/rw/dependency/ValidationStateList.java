package com.espirit.ps.rw.dependency;


import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * Maps all calls to internal ArrayList.
 * Method add(e), add(i,e), addAll(c) amd addAll(i,c) track the validation State.
 * clear() resets validation state in addition to clearing the list.
 * !!! Any other method of adding, setting or removing objects is not tracked and hence should be avoided !!!
 */
public class ValidationStateList implements List<ValidationState> {
	
	private List<ValidationState> validationStateList;
	private boolean               valid;
	
	
	ValidationStateList() {
		validationStateList = new LinkedList<>();
		valid = true;
	}
	
	
	@Override
	public boolean add(final ValidationState validationState) {
		checkValidity(validationState);
		return validationStateList.add(validationState);
	}
	
	
	@Override
	public void add(final int index, final ValidationState element) {
		checkValidity(element);
		validationStateList.add(index, element);
	}
	
	
	@Override
	public boolean addAll(@NotNull final Collection<? extends ValidationState> c) {
		checkValidity(c);
		return validationStateList.addAll(c);
	}
	
	
	@Override
	public boolean addAll(final int index, @NotNull final Collection<? extends ValidationState> c) {
		checkValidity(c);
		return validationStateList.addAll(index, c);
	}
	
	
	private void checkValidity(final ValidationState state) {
		valid = valid && !state.isPreventing();
	}
	
	
	private void checkValidity(final Collection<? extends ValidationState> states) {
		states.forEach(this::checkValidity);
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
		return validationStateList.containsAll(c);
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
	
	
	public boolean isValid() {
		return valid;
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
