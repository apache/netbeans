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
package org.netbeans.modules.javascript.bower.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.exec.BowerExecutable;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.netbeans.modules.javascript.bower.util.BowerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

@ActionID(id = "org.netbeans.modules.javascript.bower.ui.actions.BowerInstallAction", category = "Build")
@ActionRegistration(displayName = "#BowerInstallAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/bower+x-json/Popup", position = 905),
    @ActionReference(path = "Loaders/text/bower+x-json/Actions", position = 155),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 175),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 115),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 660),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 760)
})
@NbBundle.Messages("BowerInstallAction.name=Bower Install")
public final class BowerInstallAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(BowerInstallAction.class);

    private final Project project;


    public BowerInstallAction() {
        this(null);
    }

    public BowerInstallAction(Project project) {
        this.project = project;
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.BowerInstallAction_name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        final BowerExecutable bower = BowerExecutable.getDefault(project, true);
        if (bower == null) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                BowerUtils.logUsageBowerInstall();
                bower.install();
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        Project contextProject = context.lookup(Project.class);
        BowerJson bowerJson = null;
        if (contextProject != null) {
            // project action
            bowerJson = new BowerJson(contextProject.getProjectDirectory());
        } else {
            // package.json directly
            FileObject file = context.lookup(FileObject.class);
            if (file == null) {
                DataObject dataObject = context.lookup(DataObject.class);
                if (dataObject != null) {
                    file = dataObject.getPrimaryFile();
                }
            }
            if (file != null) {
                bowerJson = new BowerJson(file.getParent());
            }
        }
        if (bowerJson == null) {
            return this;
        }
        if (!bowerJson.exists()) {
            return this;
        }
        if (bowerJson.getDependencies().isEmpty()) {
            return this;
        }
        return new BowerInstallAction(contextProject != null ? contextProject : FileOwnerQuery.getOwner(Utilities.toURI(bowerJson.getFile())));
    }

}
