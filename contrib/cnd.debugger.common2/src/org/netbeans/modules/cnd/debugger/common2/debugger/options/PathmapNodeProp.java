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

public final class PathmapNodeProp extends PropertySupport<Pathmap> {
    private DbgProfile profile;

    public PathmapNodeProp(DbgProfile profile, String dispName) {
	super("Pathmap",					// NOI18N
	      Pathmap.class,
	      Catalog.get(dispName),		// NOI18N
	      Catalog.get("PathmapPropTT"),			// NOI18N
	      true, true);
	this.profile = profile;
    }

    @Override
    public Pathmap getValue() {
	return profile.pathmap();
    }

    @Override
    public void setValue(Pathmap v) {
	profile.pathmap().assign(v);
    }
	    
    private static PropertyEditor propertyEditor;

    @Override
    public PropertyEditor getPropertyEditor() {
	if (propertyEditor == null)
	    propertyEditor = new PathmapEditor();
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
	return profile.pathmap().isDefaultValue();
    }

    // interface Node.Property
    @Override
    public void restoreDefaultValue() {
	profile.pathmap().restoreDefaultValue();
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

    private final static class PathmapEditor extends PropertyEditorSupport
				       implements ExPropertyEditor {
	private PropertyEnv env;

	public PathmapEditor() {
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
	    Pathmap pathmap = (Pathmap) getValue();
	    PathmapPanel panel =
		new PathmapPanel(this, env, (Pathmap) pathmap.clone());
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
