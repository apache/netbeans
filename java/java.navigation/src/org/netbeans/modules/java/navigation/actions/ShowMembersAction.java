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
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.java.navigation.ClassMemberPanelUI;
import org.netbeans.spi.navigator.NavigatorHandler;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Zezula
 */
@ActionID(
    category = "Edit",
id = "org.netbeans.modules.java.navigation.actions.ShowMembersAction")
@ActionRegistration(
    displayName = "#CTL_ShowMembersAction", lazy=false)
@ActionReference(path = "Menu/GoTo/Inspect", position = 1100)
@NbBundle.Messages("CTL_ShowMembersAction=&File Members")
public class ShowMembersAction extends AbstractAction {

    private static final String FORM_VIEW_ID = "form";  //NOI18N

    public ShowMembersAction() {
        putValue(Action.NAME, Bundle.CTL_ShowMembersAction());
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
        if (context != null) {
            missingNavigatorAPIHack(ev, context, null);
        }
    }

    static void missingNavigatorAPIHack(
            @NonNull final ActionEvent ev,
            @NonNull final JavaSource context,
            @NullAllowed final JTextComponent target) {
        final Action openNavigator = FileUtil.getConfigObject(
                "Actions/Window/org-netbeans-modules-navigator-ShowNavigatorAction.instance",
                Action.class);
        if (openNavigator != null) {
            openNavigator.actionPerformed(ev);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    NavigatorHandler.activateNavigator();
                    final Collection<? extends NavigatorPanel> panels = getPanels(context);
                    NavigatorPanel cmp = null;
                    for (NavigatorPanel panel : panels) {
                        if (panel.getComponent().getClass() == ClassMemberPanelUI.class) {
                            cmp = panel;
                            break;
                        }
                    }
                    if (cmp != null) {
                        NavigatorHandler.activatePanel(cmp);
                        ((ClassMemberPanelUI)cmp.getComponent()).setContext(context, target);
                    }
                }
            });
        }
    }

    private static Collection<? extends NavigatorPanel> getPanels(@NonNull final JavaSource context) {
        final Collection<? extends FileObject> files = context.getFileObjects();
        assert files.size() == 1;
        return Lookups.forPath(
            String.format(
                "Navigator/Panels/%s/",  //NOI18N
                files.iterator().next().getMIMEType())).lookupAll(NavigatorPanel.class);
    }


    private JavaSource getContext() {
        FileObject fo = Utilities.actionsGlobalContext().lookup(FileObject.class);
        if (fo == null) {
            final DataObject dobj = Utilities.actionsGlobalContext().lookup(DataObject.class);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        if (fo == null) {
            return null;
        }
        TopComponent.Registry regs = WindowManager.getDefault().getRegistry();
        final TopComponent tc = regs.getActivated();
        final MultiViewHandler h = tc == null ?
                null :
                MultiViews.findMultiViewHandler(tc);
        if (h != null && FORM_VIEW_ID.equals(h.getSelectedPerspective().preferredID())) {
            //Form view does not support Members View
            return null;
        }
        return JavaSource.forFileObject(fo);
    }
}
