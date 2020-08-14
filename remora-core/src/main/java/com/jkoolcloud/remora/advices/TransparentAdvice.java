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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the advice that should not create {@link com.jkoolcloud.remora.core.EntryDefinition}, instead it should poll
 * one from stack and add the required parameters. This is useful for a particular methods there is no need to create
 * {@link com.jkoolcloud.remora.core.EntryDefinition} but to collect particular properties, i.e. CallableStatement
 * instrumentation needs to collect setInt, setText etc. bus you don't want to have an event for every of these, but one
 * event containing all set parameters.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransparentAdvice {
}
