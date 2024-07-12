package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.client.plugin.dataaccess.DataStream;

import java.util.*;


public class ValidationStateDataStream implements DataStream<ValidationState> {
	
	private final int                              total;
	private final Iterator<ValidationState>        iterator;
	private final ValidationStateDataStreamBuilder builder;
	
	
	public ValidationStateDataStream(final BaseContext context, final List<ValidationState> validationStates, final ValidationStateDataStreamBuilder builder) {
		total = validationStates.size();
		iterator = validationStates.iterator();
		this.builder = builder;
	}
	
	
	@Override
	public void close() {
	}
	
	
	public ValidationStateDataStreamBuilder getBuilder() {
		return builder;
	}
	
	
	@Override
	public List<ValidationState> getNext(final int count) {
		List<ValidationState> validationStates = new LinkedList<>();
		int                   counter          = 0;
		while (iterator.hasNext() && counter < count) {
			validationStates.add(iterator.next());
		}
		return validationStates;
	}
	
	
	@Override
	public int getTotal() {
		return total;
	}
	
	
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}
}