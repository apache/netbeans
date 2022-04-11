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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.openide.nodes.PropertySupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

public final class SignalsNodeProp extends PropertySupport<Signals> {
    private final DbgProfile profile;

    public SignalsNodeProp(DbgProfile profile) {
	super("Signals",				// NOI18N
	      Signals.class,
	      Catalog.get("SignalsPropDisplayName"),	// NOI18N
	      Catalog.get("SignalsPropTT"),             // NOI18N
	      true, true);
	this.profile = profile;
    }

    @Override
    public Signals getValue() {
	return profile.signals();
    }

    @Override
    public void setValue(Signals v) {
	profile.signals().assign(v);
    }
	    
    private static PropertyEditor propertyEditor;

    @Override
    public PropertyEditor getPropertyEditor() {
	if (propertyEditor == null)
	    propertyEditor = new SignalsEditor();
	return propertyEditor;
    }

    // interface Node.Property
    @Override
    public boolean supportsDefaultValue() {
	return true;
    }

    // interface Node.Property
    @Override
    public boolean isDefaultValue() {
	return profile.signals().isDefaultValue();
    }

    // interface Node.Property
    @Override
    public void restoreDefaultValue() {
	profile.signals().restoreDefaultValue();
    }

    // interface Node.Property
    @Override
    public String getHtmlDisplayName() {
	if (isDefaultValue()) {
	    return getDisplayName();
	} else {
	    return "<b>" + getDisplayName() + "</b>";	// NOI18N
	}
    }

    @Override
    public Object getValue(String attributeName) {
	if (attributeName.equals("canEditAsText")) // NOI18N
	    return Boolean.FALSE;
	return super.getValue(attributeName);
    }

    private static final class SignalsEditor extends PropertyEditorSupport
				implements ExPropertyEditor {
	private PropertyEnv env;

	public SignalsEditor() {
	}

        @Override
	public void setAsText(String text) {
	}

        @Override
	public String getAsText() {
	    return getValue().toString();
	}

        @Override
	public java.awt.Component getCustomEditor () {

	    // Make a clone because the value edited by the editor might get
	    // discarded in case of cancellation.
	    // The new value will get assigned back to us by
	    // SignalsPanel.propertyChange().

	    Signals signals = (Signals) getValue();
	    SignalsPanel signalsPanel =
		new SignalsPanel(this, env, (Signals)signals.clone());
	    return signalsPanel;
	}

        @Override
	public boolean supportsCustomEditor () {
	    return true;
	}
	
	// interface ExPropertyEditor
        @Override
	public void attachEnv(PropertyEnv env) {
	    this.env = env;
	}
    }
}
