/*
 * Copyright 2019-2020 NASTEL TECHNOLOGIES, INC.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jkoolcloud.remora.core.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

public interface TypePropertyAggregator<T> {

	void collect(T object, EntryDefinition entryDefinition, String prefix);

	static String name(String prefix, String name) {
		if (prefix == null) {
			return name;
		} else {
			return prefix + "_" + name;
		}
	}

	enum Default {
		COLLECTION(Collection.class, CollectionPropertyAggregator.class), //

		OBJECT_TO_STRING(Object.class, ObjectToStringPropertyAggregator.class);

		private TypePropertyAggregator<?> value;
		Map<Class<?>, TypePropertyAggregator<?>> aggregators = new HashMap<>();

		Default(Class<? extends Object> collectionClass,
				Class<? extends TypePropertyAggregator<?>> collectionPropertyAggregatorClass) {
			try {
				value = collectionPropertyAggregatorClass.newInstance();
				aggregators.put(collectionClass, value);
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}

		<T> TypePropertyAggregator<? extends Object> getAggregator(T object) {
			@SuppressWarnings("unlikely-arg-type")
			TypePropertyAggregator<?> aggregator = aggregators
					.get(aggregators.keySet().stream().filter(key -> key.isInstance(object)).findFirst());
			if (aggregator == null) {
				return OBJECT_TO_STRING.value;
			}
			return aggregator;
		}
	}

	class CollectionPropertyAggregator implements TypePropertyAggregator<Collection<?>> {

		@Override
		public void collect(Collection<?> object, EntryDefinition entryDefinition, String prefix) {
			entryDefinition.addPropertyIfExist(name(prefix, "SIZE"), object.size());
		}
	}

	class ObjectToStringPropertyAggregator implements TypePropertyAggregator<Object> {

		@RemoraConfig.Configurable
		private int maxArgumentLength;

		@Override
		public void collect(Object object, EntryDefinition ed, String prefix) {
			String value = String.valueOf(object);
			if (value.length() > maxArgumentLength) {
				ed.addPropertyIfExist(prefix, value.substring(0, maxArgumentLength));
			} else {
				ed.addPropertyIfExist(prefix, value);
			}
		}
	}
}
