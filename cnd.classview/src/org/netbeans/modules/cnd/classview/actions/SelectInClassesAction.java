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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.classview.actions;

import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.classview.ClassViewTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.classview.actions.SelectInClassesAction", category = "Window/SelectDocumentNode")
@ActionRegistration(lazy = true, displayName = "#CTL_NavigateSelectInClasses")
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "DS-9"),
    @ActionReference(path = "Menu/GoTo", position = 2750)})
public class SelectInClassesAction extends CookieAction {
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        CsmOffsetableDeclaration decl = ContextUtils.getContext(activatedNodes);
        if (decl != null){
            ClassViewTopComponent view = ClassViewTopComponent.findDefault();
            if (!view.isOpened()) {
                view.open();
            }
            view.requestActive();
            view.selectInClasses(decl);
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem item = super.getPopupPresenter();
        item.setText( NbBundle.getMessage(SelectInClassesAction.class, "CTL_SelectInClasses")); // NOI18N
        return item;
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(SelectInClassesAction.class, "CTL_NavigateSelectInClasses"); // NOI18N
    }
    
    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
            DataObject.class
        };
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
