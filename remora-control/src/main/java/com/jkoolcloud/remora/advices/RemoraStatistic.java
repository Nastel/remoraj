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

import java.util.concurrent.atomic.AtomicLong;

public class RemoraStatistic {
	private AtomicLong eventCreateCount = new AtomicLong();
	private AtomicLong invokeCount = new AtomicLong();
	private AtomicLong errorCount = new AtomicLong();

	public long getEventCreateCount() {
		return eventCreateCount.get();
	}

	public long getInvokeCount() {
		return invokeCount.get();
	}

	public long getErrorCount() {
		return errorCount.get();
	}

	public long incEventCreateCount() {
		return eventCreateCount.incrementAndGet();
	}

	public long incInvokeCount() {
		return invokeCount.incrementAndGet();
	}

	public long incErrorCount() {
		return errorCount.incrementAndGet();
	}

}
