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

