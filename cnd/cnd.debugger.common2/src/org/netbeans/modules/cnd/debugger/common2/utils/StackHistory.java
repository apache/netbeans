/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.ArrayList;
import java.util.List;

/** 
 * Utility to record a history of stack traces.
 * Useful for debugging the call history of a particular method.
 */

public class StackHistory {
    private final List<StackTraceElement[]> history = new ArrayList<StackTraceElement[]>();

    /**
     * Create a snapshot of the current stack trace and add it to the history.
     */
    public void snap() {
	StackTraceElement[] stackTrace =
	    Thread.currentThread().getStackTrace();
	history.add(stackTrace);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	if (history.size() == 0)
	    return "No stack history"; // NOI18N
	for (int sx = 0; sx < history.size(); sx++) {
	    sb.append("Stack " + sx + "------------------\n"); // NOI18N
	    StackTraceElement[] stackTrace = history.get(sx);
	    for (int fx = 0; fx < stackTrace.length; fx++)
		sb.append("\t\t" + stackTrace[fx] + "\n"); // NOI18N
	}
	return sb.toString();
    }
}
