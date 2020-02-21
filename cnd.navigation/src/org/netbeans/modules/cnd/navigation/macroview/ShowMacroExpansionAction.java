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
package org.netbeans.modules.cnd.navigation.macroview;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Context menu action that opens Macro Expansion View panel
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.navigation.macroview.ShowMacroExpansionAction", category = "Edit")
@ActionRegistration(lazy = true, displayName = "#CTL_ShowMacroExpansionAction")
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/x-h/Popup/goto", position = 1600),
    @ActionReference(path = "Editors/text/x-c++/Popup/goto", position = 1600),
    @ActionReference(path = "Editors/text/x-c/Popup/goto", position = 1400)})
public final class ShowMacroExpansionAction extends CookieAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Document mainDoc = getDocument(activatedNodes);
        int offset = getOffset(activatedNodes);
        CsmMacroExpansion.showMacroExpansionView(mainDoc, offset);
    }

    private Document getDocument(Node[] activatedNodes) {
        EditorCookie c = activatedNodes[0].getLookup().lookup(EditorCookie.class);
        if (c != null) {
            return CsmUtilities.openDocument(c);
        }
        return null;
    }

    private int getOffset(Node[] activatedNodes) {
        EditorCookie c = activatedNodes[0].getLookup().lookup(EditorCookie.class);
        if (c != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(c);
            if (pane != null ) {
                return pane.getCaret().getDot();
            }
        }
        return 0;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            return ContextUtils.findFile(activatedNodes[0]) != null;
        }
        return false;
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ShowMacroExpansionAction"); // NOI18N
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class<?>[]{
                    EditorCookie.class
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

