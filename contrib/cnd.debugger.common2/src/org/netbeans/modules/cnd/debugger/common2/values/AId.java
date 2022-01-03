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

import java.math.BigInteger;

/**
 * thread and lwps are collectively known as "active entities" 
 * so their Id's are AId's.
 */

public class AId {
    private String id;
    public String errorMessage;

    private static boolean isDecimalNumber(String s) {
	long n = 0;
	try {
	    n = Long.parseLong(s);
	} catch(NumberFormatException x) {
	    return false;
	}

	// we don't like negative numbers either
	if (n < 0)
	    return false;

	return true;
    }

    private static boolean isLongDecimalNumber(String s) {
	try {
            // we don't like negative numbers either
            if (s.startsWith("-")) {// NOI18N
                return false;
            }
            BigInteger bi = new BigInteger(s);
            if (bi.bitLength() > 64) {
                // cannot be represented by java long
                return false;
            }
	} catch(NumberFormatException x) {
	    return false;
	}

	return true;
    }

    /**
     * Validate & interpret 'text' per the additional flags.
     * An invalid value will have 'errorMessage != null'
     */

    public AId(String text, boolean isLwp, boolean isJava) {
	errorMessage = null;

	if (text == null) {
	    id = null;
	    return;
	}

	text = text.trim();

	if (text.length() == 0) {
	    id = null;
	    return;
	}

	String numberPart = null;

	if (isLwp) {
	    if (text.startsWith("l@") || text.startsWith("L@")) {// NOI18N
		try {
		    numberPart = text.substring(2);
		} catch (IndexOutOfBoundsException x) {
		    // should be impossible
		}
	    } else {
		numberPart = text;
	    }
	    if (!isDecimalNumber(numberPart)) {
		errorMessage = Catalog.get("MSG_AId_MalformedLwp"); // NOI18N
		return;
	    }
	    id = "l@" + Integer.parseInt(numberPart); // NOI18N

	} else {
	    if (!isJava) {
		if (text.startsWith("t@") || text.startsWith("T@")) {// NOI18N
		    try {
			numberPart = text.substring(2);
		    } catch (IndexOutOfBoundsException x) {
			// should be impossible
		    }
		} else {
		    numberPart = text;
		}
		if (!isLongDecimalNumber(numberPart)) {
		    errorMessage = Catalog.get("MSG_AId_MalformedThread"); // NOI18N
		    return;
		}
                BigInteger big = new BigInteger(numberPart);
                long lid = big.longValue();
                if (lid < 0) {
                    BigInteger max = BigInteger.ONE.shiftLeft(64);
                    BigInteger bi = BigInteger.valueOf(lid);
                    id = "t@" + bi.add(max).toString(); // NOI18N
                } else {
                    id = "t@" + Long.toString(lid); // NOI18N
                }
	    } else {
		// Java thread names are strings so can by _anything_
		id = text;
	    }
	}
    }

    @Override
    public String toString() {
	return id;
    } 
}
