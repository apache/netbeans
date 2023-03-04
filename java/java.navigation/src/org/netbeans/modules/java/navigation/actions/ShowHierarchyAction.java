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
package org.netbeans.modules.java.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.navigation.hierarchy.HierarchyTopComponent;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(
    category = "Edit",
id = "org.netbeans.modules.java.navigation.actions.ShowHierarchyAction")
@ActionRegistration(
    displayName = "#CTL_ShowHierarchyAction", lazy=false)
@ActionReference(path = "Menu/GoTo/Inspect", position = 2200)
@Messages("CTL_ShowHierarchyAction=File H&ierarchy")
public final class ShowHierarchyAction extends AbstractAction {

    public ShowHierarchyAction() {
        putValue(Action.NAME, Bundle.CTL_ShowHierarchyAction());
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        return getContext() != null;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final JavaSource context = getContext();
        assert context != null;
        HierarchyTopComponent htc = HierarchyTopComponent.findDefault();
        htc.setContext(context);
        htc.open();
        htc.requestActive();
    }


    private JavaSource getContext() {
        FileObject fo = Utilities.actionsGlobalContext().lookup(FileObject.class);
        if (fo == null) {
            DataObject dobj = Utilities.actionsGlobalContext().lookup(DataObject.class);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        if (fo == null) {
            return null;
        }
        return JavaSource.forFileObject(fo);
    }
}
