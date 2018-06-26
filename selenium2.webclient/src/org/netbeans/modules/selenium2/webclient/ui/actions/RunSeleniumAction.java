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
        return fileObjects.toArray(new FileObject[fileObjects.size()]);
    }

}
