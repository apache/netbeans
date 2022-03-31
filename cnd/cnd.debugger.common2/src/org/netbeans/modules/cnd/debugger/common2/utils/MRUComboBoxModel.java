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

import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

/**
 * MostRecentlyUsed ComboBoxModel which provides history for an editable
 * text field.
 * add() will add or move an existing item to the top.
 */

public final class MRUComboBoxModel extends DefaultComboBoxModel {

    public MRUComboBoxModel(String[] initial) {
	super(initial);
    }

    public MRUComboBoxModel(Vector initial) {
	super(initial);
    }
    
    public MRUComboBoxModel() {
	super();
    }
    
    /** Add a given string to the address combo box */
    public void add(String item) {
	int num = getSize();
	
	for (int i = 0; i < num; i++) {
	    String comboItem = (String)getElementAt(i);
	    if (comboItem.equals(item)) {
		// Already there; move to the top
		if (i != 0) {
		    removeElementAt(i);
		    insertElementAt(item, 0);
		}
		// All done
		return;
	    }
	}
	insertElementAt(item, 0);
    }
}
