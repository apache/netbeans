/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * ListMapping.java
 *
 * Created on January 9, 2004, 3:05 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

/** Class that associates a list with a calculated string.  This is so a field of
 *  a combobox or table that actually represents an entire list can have a nice
 *  text field displayed.  The default toString() on ArrayList is not sufficient.
 *  This class could be generalized to Collection if necessary, though additional
 *  typed accessors would be necessary (e.g. valueAsList(), valueAsCollection(),
 *  etc.)  It is expected that the underlying list may be changed during this
 *  object's lifetime, and between calls to toString().
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class ListMapping {
	
	// Standard resource bundle to use for non-property list fields
	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	private final String formatPattern = bundle.getString("LBL_SizeOfListText");	// NOI18N
	
	private List theList;
	private String displayText;
	private int listSize;

	public ListMapping(List l) {
		theList = l;
		displayText = null;
		listSize = 0;
	}

	public String toString() {
		if(textOutOfDate()) {
			buildDisplayText();
		}
		
		return displayText;
	}
	
	private void buildDisplayText() {
		listSize = (theList != null) ? theList.size() : 0;
		Object [] args = { listSize};
		displayText = MessageFormat.format(formatPattern, args);
	}
	
	private boolean textOutOfDate() {
		// Rebuild display Text if text is null or if size of list has changed.
		if(displayText == null) {
			return true;
		}
		
		int newListSize = 0;
		if(theList != null) {
			newListSize = theList.size();
		}
		
		if(listSize != newListSize) {
			return true;
		}
		
		return false;
	}

	public List getList() {
		return theList;
	}
}
