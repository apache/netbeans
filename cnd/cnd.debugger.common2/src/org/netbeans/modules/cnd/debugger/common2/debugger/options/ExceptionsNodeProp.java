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

public final class ExceptionsNodeProp extends PropertySupport<Exceptions> {
    private DbgProfile profile;

    public ExceptionsNodeProp(DbgProfile profile) {
	super("Exceptions",					// NOI18N
	      Exceptions.class,
	      Catalog.get("ExceptionsPropDisplayName"),		// NOI18N
	      Catalog.get("ExceptionsPropTT"),			// NOI18N
	      true, true);
	this.profile = profile;
    }

    @Override
    public Exceptions getValue() {
	return profile.exceptions();
    }

    @Override
    public void setValue(Exceptions v) {
	profile.exceptions().assign(v);
    }
	    
    private static PropertyEditor propertyEditor = null;

    @Override
    public PropertyEditor getPropertyEditor() {
	if (propertyEditor == null)
	    propertyEditor = new ExceptionsEditor();
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
	return profile.exceptions().isDefaultValue();
    }

    // interface Node.Property
    @Override
    public void restoreDefaultValue() {
	profile.exceptions().restoreDefaultValue();
    }

    // interface Node.Property
    @Override
    public String getHtmlDisplayName() {
	if (isDefaultValue()) {
	    return getDisplayName();
	} else {
	    return "<b>" + getDisplayName() + "</b>";   // NOI18N
	}
    }

    @Override
    public Object getValue(String attributeName) {
	if (attributeName.equals("canEditAsText")) // NOI18N
	    return Boolean.FALSE;
	return super.getValue(attributeName);
    }

    private final static class ExceptionsEditor extends PropertyEditorSupport
					  implements ExPropertyEditor {
	private PropertyEnv env;

	public ExceptionsEditor() {
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
	    Exceptions exceptions = (Exceptions) getValue();
	    ExceptionsPanel panel =
		new ExceptionsPanel(this, env, (Exceptions) exceptions.clone());
	    return panel;
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
