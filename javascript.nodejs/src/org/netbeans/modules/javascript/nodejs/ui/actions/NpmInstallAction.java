/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@ActionID(id = "org.netbeans.modules.javascript.nodejs.ui.actions.NpmInstallAction", category = "Build")
@ActionRegistration(displayName = "#NpmInstallAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/package+x-json/Popup", position = 906),
    @ActionReference(path = "Loaders/text/package+x-json/Actions", position = 156),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 170),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 110),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 650),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 750),
})
@NbBundle.Messages("NpmInstallAction.name=npm Install")
public final class NpmInstallAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(NpmInstallAction.class);

    private final Project project;


    public NpmInstallAction() {
        this(null);
    }

    public NpmInstallAction(Project project) {
        this.project = project;
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.NpmInstallAction_name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        final NpmExecutable npm = NpmExecutable.getDefault(project, true);
        if (npm == null) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                NodeJsUtils.logUsageNpmInstall();
                npm.install();
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new NpmInstallAction(NodeJsUtils.getPackageJsonProject(context));
    }

}
