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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
		Object [] args = { Integer.valueOf(listSize) };
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
