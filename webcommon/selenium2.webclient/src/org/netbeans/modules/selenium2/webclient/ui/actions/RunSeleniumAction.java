/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.selenium2.webclient.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Theofanis Oikonomou
 */
@NbBundle.Messages({
    "SeleniumTestFileAction.name=Test File",
    "SeleniumTestFilesAction.name=Test Files",
    "RunSeleniumTestsAction.name=Run Selenium Tests"})
@ActionID(id = "org.netbeans.modules.selenium2.webclient.ui.actions.RunSeleniumAction", category = "Project")
@ActionRegistration(lazy = false, displayName = "#SeleniumTestFileAction.name")
@ActionReferences({
    @ActionReference(path = "Loaders/folder/any/Actions", position = 398), // first seperator is in position 400
    @ActionReference(path = "Loaders/text/javascript/Actions", position = 256),
    @ActionReference(path = "Editors/text/javascript/Popup", position = 811)})
public class RunSeleniumAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(RunSeleniumAction.class.getName(), 1);
    private FileObject[] activatedFOs;

    public RunSeleniumAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunSeleniumAction(Lookup actionContext) {
        activatedFOs = lookupSeleniumTestOnly(actionContext);
        if(activatedFOs != null) {
            if(activatedFOs.length == 1) {
                if(activatedFOs[0].isData()) {
                    putValue(Action.NAME, Bundle.SeleniumTestFileAction_name());
                } else if(activatedFOs[0].isFolder()) {
                    putValue(Action.NAME, Bundle.RunSeleniumTestsAction_name());
                }
            } else {
                boolean onlyFiles = true;
                for (int i = 0; i < activatedFOs.length; i++) {
                    FileObject activatedFO = activatedFOs[i];
                    if (activatedFO.isFolder()) {
                        onlyFiles = false;
                        break;
                    }
                }
                if (onlyFiles) {
                    putValue(Action.NAME, Bundle.SeleniumTestFilesAction_name());
                } else {
                    putValue(Action.NAME, Bundle.RunSeleniumTestsAction_name());
                }
            }
        }
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        setEnabled(activatedFOs != null);
    }

    @Override
    public boolean isEnabled() {
        return activatedFOs != null;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
                if (p == null) {
                    return;
                }
                FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_TEST_SINGLE, Bundle.SeleniumTestFileAction_name(), null).actionPerformed(e);
                SeleniumTestingProvider provider = SeleniumTestingProviders.getDefault().getSeleniumTestingProvider(p, true);
                if (provider != null) {
                    provider.runTests(activatedFOs);
                }
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new RunSeleniumAction(actionContext);
    }
    
    @CheckForNull
    private FileObject[] lookupSeleniumTestOnly(Lookup context) {
        Collection<? extends FileObject> fileObjects = context.lookupAll(FileObject.class);
        if (fileObjects.isEmpty()) {
            return null;
        }
        Project p = null;
        Iterator<? extends FileObject> iterator = fileObjects.iterator();
        while (iterator.hasNext()) {
            FileObject fo = iterator.next();
            Project project = FileOwnerQuery.getOwner(fo);
            if (project == null) {
                return null;
            }
            if(p == null) {
                p = project;
            }
            if(!p.equals(project)) { // selected FileObjects belong to different projects
                return null;
            }
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM);
            if (sourceGroups.length != 1) { // no Selenium Tests Folder set yet
                return null;
            }
            FileObject rootFolder = sourceGroups[0].getRootFolder();
            if (!FileUtil.isParentOf(rootFolder, fo)) { // file in not under Selenium Tests Folder
                return null;
            }
        }
        return fileObjects.toArray(new FileObject[0]);
    }

}
