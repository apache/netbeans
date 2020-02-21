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

package org.netbeans.modules.cnd.debugger.gdb2.mi;

import java.util.*;
import java.util.regex.*;

/**
 * A representation of a "menu" issued by gdb unto the console.
 * It typically looks like this:
 <pre>
 </pre>
 */

public class MIUserInteraction {
    private final boolean empty;
    private final String[] items;
    private int cancelIndex = -1;
    private int allIndex = -1;
    private int firstChoice = -1;

    MIUserInteraction(String consoleStream) {
	if (consoleStream == null) {
	    empty = true;
	    items = new String[0];
	    return;
	} else {
	    empty = false;
	}

	//
	// Break console stream into a list of lines
	//
	List<String> lines = new ArrayList<String>();
	java.util.StringTokenizer st = new StringTokenizer(consoleStream, "\n"); // NOI18N
	while (st.hasMoreTokens()) {
	    lines.add(st.nextToken());
	}

	if (Log.MI.ui) {
	    System.out.printf("USER INTERACTION LINES:\n"); // NOI18N
	    for (String line : lines)
		System.out.printf("\t'%s'\n", line); // NOI18N
	}

	//
	// parse each line into the choices
	//
	Pattern p = Pattern.compile("\\[(\\d+)\\] (.+)"); // NOI18N
	List<String> itemList = new ArrayList<String>();
	int cx = 0;
	if (Log.MI.ui)
	    System.out.printf("PARSED based on '%s'\n", p.pattern()); // NOI18N
	for (String line : lines) {
	    // "[<number>] <stuff>"
	    Matcher m = p.matcher(line);
	    if (m.matches()) {
		if (Log.MI.ui)
		    System.out.printf("'%s' '%s'\n", m.group(1), m.group(2)); // NOI18N
		String choice = m.group(2);
		if ("cancel".equals(choice)) { // NOI18N
		    cancelIndex = Integer.parseInt(m.group(1));
		    continue;
		} else if ("all".equals(choice)) { // NOI18N
		    allIndex = java.lang.Integer.parseInt(m.group(1));
		    continue;
		} else {
		    if (firstChoice == -1)
			firstChoice = java.lang.Integer.parseInt(m.group(1))
;
		    itemList.add(choice);
		}

	    } else {
		if (Log.MI.ui)
		    System.out.printf("No match\n"); // NOI18N
	    }
	}

	items = new String[itemList.size()];
	itemList.toArray(items);
    }

    public boolean isEmpty() {
	return empty;
    }

    public boolean hasCancel() {
	return cancelIndex != -1;
    }

    public boolean hasAll() {
	return allIndex != -1;
    }

    public int cancelChoice() {
	return cancelIndex;
    }

    public int allChoice() {
	return allIndex;
    }

    public int firstChoice() {
	return firstChoice;
    }

    public String[] items() {
	return items;
    }
}
