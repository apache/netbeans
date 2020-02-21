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
