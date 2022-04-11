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

import java.beans.PropertyEditorSupport;

import org.openide.ErrorManager;


/**
 * Editor to support asynchronous property editing and validating.
 * This works together with props.EditUndo
 * When a user commits text a copy of it is kept in 'pending' while
 * setValue() ultimately sends the value asynchronously to be validated.
 * Meanwhile getValue() will be returning the newly entered 'pending'
 * value until the async validator gets back and the property setter
 * fires a property change event which resets 'pending'.
 */

public class AsyncEditor extends PropertyEditorSupport {
    private String pending = null;

    public AsyncEditor() {
	addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
	    public void propertyChange(java.beans.PropertyChangeEvent evt)  {
		clearPending();
	    }
	} );
    } 

    private void clearPending() {
	pending = null;
    }

    protected void notePending(String newText) {
	// Since the Node property _getter_ will still return the old value 
	// trick getAsText() into returning the value the user has just entered.
	// Once the node has its value updated by the engine, propertyChange()
	// gets called and stops the trickery by nulling 'pending'.
	pending = newText;
    }

    /**
     * Throw an annotated IllegalArgumentException so NB shows it to the
     * user in a stylized manner.
     *
     * To be called from subclasses setAsText if needed.
     */
    protected void badValue(String msg) {
	IllegalArgumentException iae = new IllegalArgumentException(msg);
	ErrorManager.getDefault().annotate(iae,
					   ErrorManager.USER,
					   "The other message",	// NOI18N
					   null, null, null);
	throw iae;
    }

    @Override
    public void setAsText(String newText) {
	// Called when an edit is commited through user action
	// System.out.println("StringEditor.setAsText \"" + newText + "\"");

	// Propagate the new value to the node property setter which
	// will forward the property to the engine.
	setValue(newText);

	// setValue() will cause the property change listener above to get
	// called and reset 'pending' so set 'pending' after setting the
	// value.
	notePending(newText);
    }

    @Override
    public String getAsText() {

	// LATER
	// The following should not be neccessary anymore
	// however verifying that things are still functioning is hard

	if (pending != null)
	    return pending;

	/*
	 * The default NB text display will show "null" for null Strings.
	 * We prefer empty fields.
	 */

	Object o = getValue();
	if (o != null) {
	    String str = o.toString();
	    if (str != null)
		return str;
	}
	return "";		// NOI18N
    }
}
