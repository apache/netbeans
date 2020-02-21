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
