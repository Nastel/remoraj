package com.jkoolcloud.remora.advices;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the advice that should not create {@link com.jkoolcloud.remora.core.EntryDefinition}, instead it should poll
 * one from stack and add the required parameters
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransparentAdvice {
}
