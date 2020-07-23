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

package com.jkoolcloud.remora.advices;

import org.tinylog.TaggedLogger;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class TransformationLoggingListener extends AgentBuilder.Listener.Adapter {
	TaggedLogger logger;
	public final static String PREFIX = "[ByteBuddy]";

	public TransformationLoggingListener(TaggedLogger logger) {
		this.logger = logger;
	}

	@Override
	public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
			boolean loaded, DynamicType dynamicType) {
		if (logger == null) {
			System.out.println(BaseTransformers.format(PREFIX + " TRANSFORM {} [{}, {}, loaded={}]",
					typeDescription.getName(), classLoader, module, loaded));
		} else {
			logger.info(PREFIX + " TRANSFORM {} [{}, {}, loaded={}]", typeDescription.getName(), classLoader, module,
					typeDescription);

		}
	}

	@Override
	public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
			Throwable throwable) {

		if (logger == null) {
			System.out.println(BaseTransformers.format(PREFIX + " ERROR {} [{}, {}, loaded={}] \n", typeName,
					classLoader, module, loaded));
			throwable.printStackTrace();
		} else {
			logger.info(throwable, PREFIX + " ERROR {} [{}, {}, loaded={}] \n", typeName, classLoader, module, loaded);
		}

	}

	@Override
	public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
		if (logger != null) {
			logger.trace(PREFIX + " DISCOVERY {} [{}, {}, loaded={}]", typeName, classLoader, module, loaded);

		}
	}

}
