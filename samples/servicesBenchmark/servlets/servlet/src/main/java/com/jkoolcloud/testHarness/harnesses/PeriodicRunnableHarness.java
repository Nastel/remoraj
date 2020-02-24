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

package com.jkoolcloud.testHarness.harnesses;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class PeriodicRunnableHarness implements Runnable, Collection<HarnessResult> {
	public static final int RESULT_SIZE = 100;

	private Harness harness;
	private CircularFifoQueue<HarnessResult> results = new CircularFifoQueue<>(RESULT_SIZE);

	public PeriodicRunnableHarness(Harness harness) {
		this.harness = harness;
	}

	@Override
	public void run() {
		HarnessResult result;
		try {
			result = harness.call();
			results.offer(result);
		} catch (Exception e) {
			results.offer(new ExceptionResult(e));
		}
	}

	@Override
	public int size() {
		return results.size();
	}

	@Override
	public boolean isEmpty() {
		return results.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return results.contains(o);
	}

	@Override
	public Iterator<HarnessResult> iterator() {
		return results.iterator();
	}

	@Override
	public Object[] toArray() {
		return results.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return results.toArray(a);
	}

	@Override
	public boolean add(HarnessResult harnessResult) {
		return results.add(harnessResult);
	}

	@Override
	public boolean remove(Object o) {
		return results.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return results.contains(c);
	}

	@Override
	public boolean addAll(Collection<? extends HarnessResult> c) {
		return results.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return results.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return results.retainAll(c);
	}

	@Override
	public void clear() {
		results.clear();
	}

}
