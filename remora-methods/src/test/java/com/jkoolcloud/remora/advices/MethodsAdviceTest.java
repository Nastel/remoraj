package com.jkoolcloud.remora.advices;

import static junit.framework.TestCase.assertTrue;

import java.util.Arrays;

import org.junit.Test;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class MethodsAdviceTest {

	@Test
	public void testFillClassAndMethodList() throws NoSuchMethodException {
		String[] interceptionEntries = { "com.nastel.bank.DbUtils.getUserId()",
				"com.nastel.bank.DbUtils.getBalance()" };
		MethodsAdvice bma = new MethodsAdvice();
		bma.fillClassAndMethodList(Arrays.asList(interceptionEntries).stream());

		assertTrue(bma.instrumentedClasses.contains("com.nastel.bank.DbUtils"));
		assertTrue(bma.classAndMethodList.contains("com.nastel.bank.DbUtils.getUserId"));
		assertTrue(bma.classAndMethodList.contains("com.nastel.bank.DbUtils.getBalance"));
	}
}
