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
