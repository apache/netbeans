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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.webservices.ui.panels.ProjectFileExplorer;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Roderico Cruz
 */
public class JaxWsGenWSDLImpl implements JaxWsGenWSDLCookie {

    private Project project;
    private String serviceName;

    public JaxWsGenWSDLImpl(Project project, String serviceName) {
        this.project = project;
        this.serviceName = serviceName;
    }

    public void generateWSDL() throws IOException {
        ProjectFileExplorer projectExplorer = new ProjectFileExplorer();
        DialogDescriptor descriptor = new DialogDescriptor(projectExplorer,
                NbBundle.getMessage(JaxWsGenWSDLImpl.class, "TTL_GenCopyWSDL"));
        projectExplorer.setDescriptor(descriptor);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
        if (descriptor.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
            try {
                ExecutorTask wsimportTask =
                        ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsgen-" + serviceName}, null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            if (!projectExplorer.dontCopy()) {
                JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
                AntProjectHelper helper = wss.getAntProjectHelper();
                EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String propValue = props.get("build.generated.sources.dir");  //NOI18N
                PropertyEvaluator evaluator = helper.getStandardPropertyEvaluator();
                String buildGenDir = evaluator.evaluate(propValue);
                String relativePath = buildGenDir + File.separator + "jax-ws"+ File.separator + "resources"; //NOI18N
                relativePath = Pattern.compile("\\\\").matcher(relativePath).replaceAll("/");   //relativePath  should not have backslashes
                FileObject wsdlDir = project.getProjectDirectory().getFileObject(relativePath);
                if (wsdlDir != null && wsdlDir.getChildren().length > 0) {
                    FileObject[] wsdlArtifacts = wsdlDir.getChildren();
                    FileObject selectedFolder = projectExplorer.getSelectedFolder().getPrimaryFile();
                    for (int i = 0; i < wsdlArtifacts.length; i++) {
                        boolean overwrite = true;
                        try {
                            FileObject wsdlArtifact = wsdlArtifacts[i];
                            FileObject testFO = selectedFolder.getFileObject(wsdlArtifact.getNameExt());
                            if (testFO != null) {
                                NotifyDescriptor.Confirmation notifyDescriptor =
                                        new NotifyDescriptor.Confirmation(NbBundle.getMessage(JaxWsGenWSDLImpl.class, "MSG_FILE_EXISTS", testFO.getNameExt(),
                                        selectedFolder.getName()), NotifyDescriptor.YES_NO_OPTION);   //NOI18N
                                DialogDisplayer.getDefault().notify(notifyDescriptor);
                                if (notifyDescriptor.getValue() == NotifyDescriptor.YES_OPTION) {
                                    FileLock lock = null;
                                    try {
                                        lock = testFO.lock();
                                        testFO.delete(lock);
                                    } finally {
                                        if (lock != null) {
                                            lock.releaseLock();
                                        }
                                    }
                                } else {
                                    overwrite = false;
                                }

                            }
                            if (overwrite) {
                                FileUtil.copyFile(wsdlArtifact, selectedFolder, wsdlArtifact.getName());
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                } else {
                    String mes = NbBundle.getMessage(JaxWsGenWSDLImpl.class, "ERROR_WSDL_NOT_FOUND");
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
        }
    }
}
