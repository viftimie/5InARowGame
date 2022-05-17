package com.fiveInARow.utils;

import java.util.ArrayList;
import java.util.List;

public class Pool<T> {
	
	public interface PoolObjectFactory<T> {
		public T createObject();
	}
	
	private final List<T> freeObjects;
	private final PoolObjectFactory<T> factory;
	private final int maxSize;
	
	public Pool(PoolObjectFactory<T> factory, int maxSize) {
		this.factory = factory;
		this.maxSize = maxSize;
		this.freeObjects = new ArrayList<T>(maxSize);
	}
	
	public T newObject() {
		T object = null;
		if (freeObjects.size() == 0)
			object = factory.createObject();
		else
			object = freeObjects.remove(0);
		return object;
	}
	
	public void free(T object) {
		if (freeObjects.size() == maxSize)
			freeObjects.remove(0);
		freeObjects.add(object);
	}
	
	public void freeAll(List<T> objects) {
		freeObjects.clear();
		int size=objects.size();
		
		if(size>maxSize)
			for(int i=size-maxSize;i<size;i++)
				free(objects.get(i));
		else
			for(int i=0;i<size;i++)
				free(objects.get(i));
	}
	
}
