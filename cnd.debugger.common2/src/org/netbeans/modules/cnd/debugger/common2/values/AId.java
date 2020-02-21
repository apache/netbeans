/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
