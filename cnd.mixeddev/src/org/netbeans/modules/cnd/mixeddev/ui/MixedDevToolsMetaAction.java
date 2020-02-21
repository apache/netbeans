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
package org.netbeans.modules.cnd.mixeddev.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.wizard.GenerateProjectAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.mixeddev.ui.MixedDevToolsMetaAction", category = "MixedDevelopment")
@ActionRegistration(displayName = "unused-name", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "UI/ToolActions")
})
public class MixedDevToolsMetaAction extends AbstactDynamicMenuAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(MixedDevToolsMetaAction.class.getName(), 1);

    public MixedDevToolsMetaAction() {
        super(RP, NbBundle.getMessage(MixedDevUtils.class, "Editors/text/x-java/Popup/MixedDevelopment")); // NOI18N
    }

    @Override
    protected Action[] createActions(Lookup actionContext) {
        Collection<? extends Node> nodes = actionContext.lookupAll(Node.class);
        if (nodes == null || nodes.size() != 1) {
            return new Action[0];
        }
        return new Action[]{GenerateProjectAction.INSTANCE};
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Action action = super.createContextAwareInstance(actionContext);
        Collection<? extends Node> nodes = actionContext.lookupAll(Node.class);
        if (nodes == null || nodes.size() != 1) {
            action.setEnabled(false);
        } else {
            Node n = nodes.iterator().next();
            if (n == null) {
                action.setEnabled(false);
            } else {
                FileObject fobj = n.getLookup().lookup(FileObject.class);
                if (fobj == null || JavaSource.forFileObject(fobj) == null) {
                    action.setEnabled(false);
                }
            }
        }
        return action;
    }
    
}
