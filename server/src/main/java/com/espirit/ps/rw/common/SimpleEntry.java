package com.espirit.ps.rw.common;

import java.util.AbstractMap;

public class SimpleEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
	
	public static final long serialVersionUID = 1L;
	
	
	public SimpleEntry(final K key, final V value) {
		super(key, value);
	}
}
