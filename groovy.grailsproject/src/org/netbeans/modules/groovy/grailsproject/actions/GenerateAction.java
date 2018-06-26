/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.util.concurrent.Callable;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class GenerateAction extends NodeAction {

    private static final String DOMAIN_DIR = "grails-app/domain"; // NOI18N

    private final String command;

    private final String actionName;

    public GenerateAction(String command, String actionName) {
        this.command = command;
        this.actionName = actionName;
    }

    protected void performAction(Node[] activatedNodes) {
        final GrailsPlatform runtime = GrailsPlatform.getDefault();
        if (!runtime.isConfigured()) {
            ConfigurationSupport.showConfigurationWarning(runtime);
            return;
        }

        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);

        GrailsProject prj = (GrailsProject) FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());
        FileObject domainDir = prj.getProjectDirectory().getFileObject(DOMAIN_DIR);
        if (domainDir == null) {
            return;
        }

        String relativePath = FileUtil.getRelativePath(domainDir, dataObject.getPrimaryFile());
        if (relativePath == null) {
            return;
        }

        // replace slashes and cut off the extension
        StringBuilder builder = new StringBuilder(relativePath.replace('/', '.')); // NOI18N
        builder.setLength(builder.length() - dataObject.getPrimaryFile().getNameExt().length());
        builder.append(dataObject.getPrimaryFile().getName());

        ProjectInformation inf = prj.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(prj), builder.toString()); // NOI18N

        ExecutionDescriptor descriptor = prj.getCommandSupport().getDescriptor(command);

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    public String getName() {
        return actionName;
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);

        if (dataObject == null || dataObject.getFolder() == null) {
            return false;
        }

        Project prj = FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());
        if (prj == null) {
            return false;
        }
        FileObject domainDir = prj.getProjectDirectory().getFileObject(DOMAIN_DIR);
        if (domainDir == null) {
            return false;
        }

        return FileUtil.isParentOf(domainDir, dataObject.getPrimaryFile());
    }
}

