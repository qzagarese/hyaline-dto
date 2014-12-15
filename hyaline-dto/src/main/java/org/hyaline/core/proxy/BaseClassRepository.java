package org.hyaline.core.proxy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hyaline.core.ClassRepository;

public class BaseClassRepository implements ClassRepository<String, Class<?>>{

	private Map<String, Class<?>> repo = new HashMap<String, Class<?>>();
	
	@Override
	public int size() {
		return repo.size();
	}

	@Override
	public boolean isEmpty() {
		return repo.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return repo.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return repo.containsValue(value);
	}

	@Override
	public Class<?> get(Object key) {
		return repo.get(key);
	}

	@Override
	public Class<?> put(String key, Class<?> value) {
		return repo.put(key, value);
	}

	@Override
	public Class<?> remove(Object key) {
		return repo.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Class<?>> m) {
		repo.putAll(m);
	}

	@Override
	public void clear() {
		repo.clear();
	}

	@Override
	public Set<String> keySet() {
		return repo.keySet();
	}

	@Override
	public Collection<Class<?>> values() {
		return repo.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Class<?>>> entrySet() {
		return repo.entrySet();
	}

}
