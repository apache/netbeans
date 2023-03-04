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

package org.netbeans.modules.form.actions;

import org.netbeans.modules.form.EditorSupport;
import org.netbeans.modules.form.FormDataObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;

/**
 * Action that invokes reloading of the currently active form. Presented only
 * in contextual menus within the Form Editor.
 *
 * @author Tomas Pavek
 */

public class ReloadAction extends CallableSystemAction {

    private static String name;

    public ReloadAction() {
        setEnabled(true);
    }

    @Override
    public String getName() {
        if (name == null) {
            name = org.openide.util.NbBundle.getBundle(ReloadAction.class)
                     .getString("ACT_ReloadForm"); // NOI18N
        }
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.quickref"); // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public void performAction() {
        WindowManager wm = WindowManager.getDefault();        
        TopComponent activeTC = wm.getRegistry().getActivated();
        if(activeTC==null) {
            return;
        }
        
        Object dobj = activeTC.getLookup().lookup(DataObject.class);
        if (dobj instanceof FormDataObject) {
            FormDataObject formDataObject = (FormDataObject)dobj;
            EditorSupport fes = formDataObject.getFormEditorSupport();
            fes.reloadForm();
        }   
    }
}
