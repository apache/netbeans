/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
