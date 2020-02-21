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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.openide.nodes.PropertySupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

public class LoadobjNodeProp extends PropertySupport<Loadobjs> {
    private RtcProfile rtcProfile;

    public LoadobjNodeProp() {
	super("Loadobj", Loadobjs.class, Catalog.get("LoadobjPropDisplayName"), Catalog.get("LoadobjPropTT"), true, true); // FIXUP
    }
    public LoadobjNodeProp(RtcProfile rtcProfile) {
	super("Loadobj", Loadobjs.class, Catalog.get("LoadobjPropDisplayName"), Catalog.get("LoadobjPropTT"), true, true); // FIXUP
	this.rtcProfile = rtcProfile;
    }

    public Loadobjs getValue() {
	return rtcProfile.getLoadobjs();
    }

    public void setValue(Loadobjs v) {
	rtcProfile.getLoadobjs().assign(v);
    }
	    
    @Override
    public PropertyEditor getPropertyEditor() {
	return new LoadobjEditor((Loadobjs)rtcProfile.getLoadobjs().clone(), rtcProfile);
    }

    @Override
    public Object getValue(String attributeName) {
	if (attributeName.equals("canEditAsText")) // NOI18N
	    return Boolean.FALSE;
	return super.getValue(attributeName);
    }
}

class LoadobjEditor extends PropertyEditorSupport implements ExPropertyEditor {
    private final RtcProfile rtcProfile;
    private final Loadobjs loadobj;
    private PropertyEnv env;

    public LoadobjEditor(Loadobjs loadobj, RtcProfile rtcProfile) {
	this.loadobj = loadobj;
	this.rtcProfile = rtcProfile;
    }

    @Override
    public void setAsText(String text) {
    }

    @Override
    public String getAsText() {
	return loadobj.toString();
    }

    @Override
    public java.awt.Component getCustomEditor () {
	boolean access_enabled = rtcProfile.getOptions().byType(RtcOption.RTC_ACCESS_ENABLE).isEnabled();
	LoadobjsPanel panel = new LoadobjsPanel(this, env, access_enabled);
	panel.initValues(loadobj);
	return panel;
    }

    @Override
    public boolean supportsCustomEditor () {
	return true;
    }
    
    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }
}
