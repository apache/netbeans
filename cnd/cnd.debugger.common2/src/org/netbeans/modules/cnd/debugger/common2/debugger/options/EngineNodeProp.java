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

import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Collection;

import org.openide.nodes.PropertySupport;

public class EngineNodeProp extends PropertySupport<EngineType> {
    private EngineProfile profile;
    private EngineEditor engineEditor;

    public EngineNodeProp(EngineProfile profile) {
	super("EngineType", // NOI18N
	      EngineType.class,
	      Catalog.get("EnginePropDisplayName"), // NOI18N
	      Catalog.get("EnginePropTT"), // NOI18N
	      true,
	      true);
	this.profile = profile;
    }

    @Override
    public EngineType getValue() {
	return profile.getEngineType();
    }

    @Override
    public void setValue(EngineType v) {
	profile.setEngineType(v);
    } 

    @Override
    public PropertyEditor getPropertyEditor() {
	// NB provides a default EnumPropertyEditor but it only works with 
	// enum names and won't work if we override toString().
	if (engineEditor == null)
	    engineEditor = new EngineEditor();
	return engineEditor;
    }

    @Override
    public Object getValue(String attributeName) {
	return super.getValue(attributeName);
    }

    private class EngineEditor extends PropertyEditorSupport {

	public EngineEditor() {
	}

        @Override
	public void setAsText(String text) {
            if (IpeUtils.isEmpty(text)) {
                EngineEditor.super.setValue(null);
            } else {
                EngineEditor.super.setValue(EngineTypeManager.getEngineTypeByDisplayName(text));
            }
	}

        @Override
	public String getAsText() {
	    return profile.getEngineType().getDisplayName();
	}

        @Override
	public String[] getTags() {
            Collection<EngineType> engineTypes = EngineTypeManager.getEngineTypes(true);

	    String[] tags = new String[engineTypes.size()];
            int i=0;
            for (EngineType engine : engineTypes) {
                tags[i++] = engine.getDisplayName();
            }
	    return tags;
	}
    }
}
