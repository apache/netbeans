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
