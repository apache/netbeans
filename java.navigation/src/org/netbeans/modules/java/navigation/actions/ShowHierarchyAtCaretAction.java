/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.java.navigation.hierarchy.HierarchyTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "Edit",
id = "org.netbeans.modules.java.navigation.actions.ShowHierarchyAtCaretAction")
@ActionRegistration(
    displayName = "#CTL_ShowHierarchyAtCaretAction", lazy=false)
@ActionReference(path = "Menu/GoTo/Inspect", position = 2100)
@Messages({
    "CTL_ShowHierarchyAtCaretAction=&Hierarchy",
    "CTL_ShowHierarchyAtCaretActionPopUp=Inspect Hierarchy"
})
public final class ShowHierarchyAtCaretAction extends BaseAction {

    public ShowHierarchyAtCaretAction() {
        putValue(Action.NAME, Bundle.CTL_ShowHierarchyAtCaretAction());        
        putValue(SHORT_DESCRIPTION, "");
        putValue(ExtKit.TRIMMED_TEXT, Bundle.CTL_ShowHierarchyAtCaretActionPopUp());
        putValue(BaseAction.POPUP_MENU_TEXT, Bundle.CTL_ShowHierarchyAtCaretActionPopUp());
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        final JTextComponent tc = EditorRegistry.lastFocusedComponent();
        if (tc == null || !tc.isShowing() || getContext(tc) == null) {
             return false;
        }
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        final JavaSource context = getContext(target);
        if (context != null) {
            HierarchyTopComponent htc = HierarchyTopComponent.findDefault();
            htc.setContext(context, target);
            htc.open();
            htc.requestActive();
        }
    }


    private JavaSource getContext(JTextComponent target) {
        final Document doc = Utilities.getDocument(target);
        if (doc == null) {
            return null;
        }
        return JavaSource.forDocument(doc);
    }
}
