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
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@ActionID(
    category = "Edit",
id = "org.netbeans.modules.java.navigation.actions.ShowMembersAtCaretAction")
@ActionRegistration(
    displayName = "#CTL_ShowMembersAtCaretAction", lazy=false)
@ActionReference(path = "Menu/GoTo/Inspect", position = 1000)
@NbBundle.Messages({
    "CTL_ShowMembersAtCaretAction=&Members",
    "CTL_ShowMembersAtCaretActionPopUp=Inspect Members"
})
public class ShowMembersAtCaretAction extends BaseAction {

    public ShowMembersAtCaretAction() {
        putValue(Action.NAME, Bundle.CTL_ShowMembersAtCaretAction());
        putValue(SHORT_DESCRIPTION, "");
        putValue(ExtKit.TRIMMED_TEXT, Bundle.CTL_ShowMembersAtCaretActionPopUp());
        putValue(BaseAction.POPUP_MENU_TEXT, Bundle.CTL_ShowMembersAtCaretActionPopUp());
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
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        final JavaSource context = getContext(target);
        if (context != null) {
            ShowMembersAction.missingNavigatorAPIHack(evt, context, target);
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
