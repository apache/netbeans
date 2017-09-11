/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2sedeploy.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2sedeploy.NativeBundleType;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Build",
        id = "org.netbeans.modules.java.j2seproject.ui.CreateBundleAction")
@ActionRegistration(
        displayName = "#CTL_CreateBundleAction",
        lazy = false)
@Messages("CTL_CreateBundleAction=Package as")
@ActionReferences({
    @ActionReference(
        position = 650,
        path = "Projects/org-netbeans-modules-java-j2seproject/Actions")
})
public final class CreateBundleAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private final ActionProvider actionProvider;
    private final Lookup context;

    public CreateBundleAction() {
        this.actionProvider = null;
        this.context = null;
        init();
        setEnabled(false);
    }

    private CreateBundleAction(
            @NonNull final ActionProvider actionProvider,
            @NonNull final Lookup context) {
        Parameters.notNull("actionProvider", actionProvider); //NOI18N
        Parameters.notNull("context", context);               //NOI18N
        this.actionProvider = actionProvider;
        this.context = context;
        init();
    }

    private void init() {
        putValue(NAME, Bundle.CTL_CreateBundleAction());
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(@NonNull final ActionEvent e) {
        //Container - nothing to do
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        final Project project = actionContext.lookup(Project.class);
        if (project == null) {
            return this;
        }
        final ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        return !supportsImages(ap, actionContext) ?
            this :
            new CreateBundleAction(ap, actionContext);
    }

    @Override    
    public JMenuItem getPopupPresenter() {
        final JMenu m = new JMenu(this);
        m.putClientProperty(
            DynamicMenuContent.HIDE_WHEN_DISABLED,
            getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
        if (actionProvider != null) {
            assert context != null;
            for (NativeBundleType nbt : NativeBundleType.getSupported()) {
                m.add(new JMenuItem(new PackageAction(
                    nbt,
                    actionProvider,
                    context)));
            }
        }
        return m;
    }

    private static boolean supportsImages(
        @NullAllowed final ActionProvider ap,
        @NonNull final Lookup ctx) {
        if (ap == null) {
            return false;
        }
        String found = null;
        for (String action : ap.getSupportedActions()) {
            if (NativeBundleType.forCommand(action)!= null) {
                found = action;
                break;
            }
        }
        if (found == null) {
            return false;
        }
        return ap.isActionEnabled(found, ctx);
    }

    private final class PackageAction extends AbstractAction {

        private final ActionProvider ap;
        private final String command;
        private final Lookup context;
        
        PackageAction(
            @NonNull final NativeBundleType nativeBundleType,
            @NonNull final ActionProvider ap,
            @NonNull final Lookup context) {
            Parameters.notNull("nativeBundleType", nativeBundleType);         //NOI18N
            Parameters.notNull("ap", ap);             //NOI18N
            Parameters.notNull("context", context);   //NOI18N
            putValue(NAME, nativeBundleType.getDisplayName());
            this.ap = ap;
            this.command = nativeBundleType.getCommand();
            this.context = context;
        }

        @Override
        public void actionPerformed(@NonNull final ActionEvent e) {
            if (ap.isActionEnabled(command, context)) {
                ap.invokeAction(command, context);
            }
        }
    }
}
