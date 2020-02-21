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

package org.netbeans.modules.cnd.debugger.common2.values;

public final class CountLimit {
    public static final String Keyword_INFINITY =
	Catalog.get("CountLimitKeyword_INFINITY");		// NOI18N

    public static final String Action_INFINITY = Keyword_INFINITY;
    public static final String Action_DISABLE =
	Catalog.get("CountLimitAction_DISABLE");		// NOI18N
    public static final String Action_CURRENT =
	Catalog.get("CountLimitAction_CURRENT");		// NOI18N

    public static final CountLimit infinity = new CountLimit(Action_INFINITY);


    private static final long limit = (0xffffffffL-1);

    private final boolean enable;
    // -1 == infinity, -2 == use current count value, -3 == always stop/disable
    private final long count;
    private final String errorMessage;


    public CountLimit(long count) {
	boolean good = true;
	this.count = count;

	if (count == -2) {
	    enable = true;
	} else if (count == -1) {
	    enable = true;
	} else if (count == -3) {
	    enable = false;
	} else {
	    enable = true;
	    if (count < 0)
		good = false;
	    else if (count > limit)
		good = false;
	}

	if (!good) {
	    errorMessage = Catalog.format("FMT_CountLimit_ERROR", // NOI18N
					  count, 0, limit);
	} else {
	    errorMessage = null;
	}
    }

    /**
     * Validate & interpret 'text' and construct a count-limit
     * An invalid value will have 'errorMessage != null'
     */

    public CountLimit(String text) {
	boolean good = true;

	// Step I
	if (text.equalsIgnoreCase(Action_INFINITY)) {
	    text = "infinity";              // NOI18N
	} else if (text.equals(Action_CURRENT)) {
	    text = "current";              // NOI18N
	} else if (text.equals(Action_DISABLE) ||
		   text.trim().equals("")) { // NOI18N
	    text = null;
	}
//        else {
//	    // Let Node, validateText2(0, do further validation
//	    text = text;
//	}

	// Step II
	if (text == null) {
	    enable = false;
	    count = -3;
	} else if (text.equals("infinity")) {	// NOI18N
	    enable = true;
	    count = -1;
	} else if (text.equals("current")) { // NOI18N
	    enable = true;
	    count = -2;
	} else {
	    enable = true;
	    long preCount = 0;
	    try {
		preCount = Long.parseLong(text);
		if (preCount < 0)
		    good = false;
		else if (preCount > limit)
		    good = false;
	    } catch (NumberFormatException x) {
		good = false;
	    }
	    count = preCount;
	}

	if (!good) {
	    errorMessage = Catalog.format("FMT_CountLimit_ERROR", // NOI18N
					  text, 0, limit);
	} else {
	    errorMessage = null;
	}
    }

    /**
     * If our "value" is Action_CURRENT then use given count as the new limit.
     * Can only be applied _once_!
     */

    public CountLimit possiblySetToCurrentCount(int currentCount) {
	if (count == -2) {
	    return new CountLimit(currentCount);
	} else {
	    return this;
	}
    }

    public long count() {
	return count;
    }

    public boolean isEnabled() {
	return enable;
    }

    public String errorMessage() {
	return errorMessage;
    }

    @Override
    public String toString() {
	if (count == -3)
	    return null;
	else if (count == -1)
	    return Keyword_INFINITY;
	else
	    return Long.toString(count);
    } 
}
