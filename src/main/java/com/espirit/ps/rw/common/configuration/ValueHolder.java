package com.espirit.ps.rw.common.configuration;

interface ValueHolder<T> {
	
	T getValue();
	
	void setValue(final T value);
}
