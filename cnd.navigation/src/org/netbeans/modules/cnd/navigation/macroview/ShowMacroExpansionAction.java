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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

