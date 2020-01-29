/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.jkoolcloud.remora.advices;

import static org.junit.Assert.*;

import java.lang.instrument.Instrumentation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import org.tinylog.configuration.Configuration;

import com.jkoolcloud.remora.core.CallStack;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class BaseTransformersTest {

	static TaggedLogger logger = null;

	@BeforeClass
	public static void configureLogger() {
		Configuration.set("writerTEST", "console");
		Configuration.set("writerTEST.stream", "out");

		logger = Logger.tag("TEST");
	}

	@Test
	public void getEntryDefinitionEDpresent() {
		EntryDefinition ed = new EntryDefinition(BaseTransformers.class);
		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger);
		assertEquals(ed, returned);
	}

	@Test
	public void getEntryDefinitionEDpresentNTA() {
		EntryDefinition ed = new EntryDefinition(BaseTransformers.class);
		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger);
		assertEquals(ed, returned);
	}

	@Test
	public void getEntryDefinitionNonTrasperentAdviceStartingStack() {
		EntryDefinition ed = null;
		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger);
		assertNotNull(returned);
	}

	@Test
	public void getEntryDefinitionTrasperentAdviceStartingStack() {
		EntryDefinition ed = null;
		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, TransparentAdvice.class, logger);
		assertNotNull(returned);
	}

	@Test
	public void getEntryDefinitionNonTrasperentAdviceContinueStack() {
		EntryDefinition ed = null;
		BaseTransformers.stackThreadLocal.set(new CallStack<>(logger));
		EntryDefinition stack1 = new EntryDefinition(GeneralAdvice.class);
		BaseTransformers.stackThreadLocal.get().push(stack1);
		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger);
		assertNotNull(returned);
		assertNotEquals(stack1, returned);
	}

	@Test
	public void getEntryDefinitionTrasperentAdviceContinueStack() {
		EntryDefinition ed = null;
		assertNull(BaseTransformers.stackThreadLocal.get());
		BaseTransformers.stackThreadLocal.set(new CallStack<>(logger));
		EntryDefinition stack1 = new EntryDefinition(TransparentAdviceInstance.class);
		BaseTransformers.stackThreadLocal.get().push(stack1);
		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger);
		assertNotNull(returned);
		assertEquals(stack1, returned);
		assertFalse(stack1.isTransparent());
		assertEquals(stack1.getAdviceClass(), NonTransparentAdviceInstance.class.getSimpleName());
	}

	@Test
	public void getEntryDefinitionCompleteTest() {
		EntryDefinition ed = null;
		BaseTransformers.stackThreadLocal.set(new CallStack<>(logger));
		EntryDefinition stack1 = new EntryDefinition(NonTransparentAdviceInstance.class); // Service call
		stack1.addProperty("PARAM1", "PARAM");
		BaseTransformers.stackThreadLocal.get().push(stack1);

		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger); // setParam
		returned.addProperty("PARAM1", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned);
		EntryDefinition returned1 = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger); // setParam
		returned.addProperty("PARAM2", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned1);
		EntryDefinition returned2 = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger); // setParam
		returned.addProperty("PARAM3", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned2);
		EntryDefinition returned3 = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger); // execute
		returned.addProperty("PARAM4", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned3);

		assertNotNull(returned);
		assertFalse(returned3.isTransparent());
		assertEquals(returned3.getAdviceClass(), NonTransparentAdviceInstance.class.getSimpleName());
		assertNotNull(returned3.getProperties().get("PARAM1"));
		assertNotNull(returned3.getProperties().get("PARAM2"));
		assertNotNull(returned3.getProperties().get("PARAM3"));
		assertNotNull(returned3.getProperties().get("PARAM4"));
	}

	@Test
	public void getEntryDefinitionCompleteTest2() {
		EntryDefinition ed = null;
		BaseTransformers.stackThreadLocal.set(new CallStack<>(logger));
		EntryDefinition stack1 = new EntryDefinition(NonTransparentAdviceInstance.class); // Service call
		stack1.addProperty("PARAM1", "PARAM");
		BaseTransformers.stackThreadLocal.get().push(stack1);

		EntryDefinition returned = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger); // setParam
		returned.addProperty("PARAM1", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned);
		EntryDefinition returned1 = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger); // setParam
		returned.addProperty("PARAM2", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned1);
		EntryDefinition returned2 = BaseTransformers.getEntryDefinition(ed, TransparentAdviceInstance.class, logger); // setParam
		returned.addProperty("PARAM3", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned2);
		EntryDefinition returned3 = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger); // execute
		returned.addProperty("PARAM4", "TEST");
		BaseTransformers.stackThreadLocal.get().push(returned3);

		EntryDefinition returned4 = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger); // execute
																															// chained
		BaseTransformers.stackThreadLocal.get().push(returned4);

		EntryDefinition returned5 = BaseTransformers.getEntryDefinition(ed, NonTransparentAdviceInstance.class, logger); // execute
																															// chained
		BaseTransformers.stackThreadLocal.get().push(returned5);

		BaseTransformers.doFinally(logger); // execute chained
		BaseTransformers.doFinally(logger); // execute chained
		BaseTransformers.doFinally(logger); // execute
		BaseTransformers.stackThreadLocal.get().pop(); // setParam
		BaseTransformers.stackThreadLocal.get().pop(); // setParam
		BaseTransformers.stackThreadLocal.get().pop(); // setParam

		BaseTransformers.doFinally(logger); // ServiceCall

		assertNotNull(returned);
		assertFalse(returned3.isTransparent());
		assertEquals(returned3.getAdviceClass(), NonTransparentAdviceInstance.class.getSimpleName());
		assertNotNull(returned3.getProperties().get("PARAM1"));
		assertNotNull(returned3.getProperties().get("PARAM2"));
		assertNotNull(returned3.getProperties().get("PARAM3"));
		assertNotNull(returned3.getProperties().get("PARAM4"));
	}

	public static class NonTransparentAdviceInstance extends BaseTransformers {

		@Override
		public ElementMatcher<TypeDescription> getTypeMatcher() {
			return null;
		}

		@Override
		public AgentBuilder.Transformer getAdvice() {
			return null;
		}

		@Override
		protected AgentBuilder.Listener getListener() {
			return null;
		}

		@Override
		public void install(Instrumentation inst) {

		}

		@Override
		public String getName() {
			return null;
		}
	}

	@TransparentAdvice
	public static class TransparentAdviceInstance extends BaseTransformers {

		@Override
		public ElementMatcher<TypeDescription> getTypeMatcher() {
			return null;
		}

		@Override
		public AgentBuilder.Transformer getAdvice() {
			return null;
		}

		@Override
		protected AgentBuilder.Listener getListener() {
			return null;
		}

		@Override
		public void install(Instrumentation inst) {

		}

		@Override
		public String getName() {
			return null;
		}
	}
}