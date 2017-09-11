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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbRefactoringContext;
import org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mkozeny
 */
public class NbRefactoringProviderImpl implements NbRefactoringProvider {

    private final Project project;

    NbRefactoringProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public List<ProjectFileRefactoring> getProjectFilesRefactoring(final NbRefactoringContext context) {
        List<ProjectFileRefactoring> result = new ArrayList<ProjectFileRefactoring>();
        NbModuleProject nbmProject = project.getLookup().lookup(NbModuleProject.class);
        if (nbmProject != null) {
            final FileObject buildFileObj = FileUtil.toFileObject(nbmProject.getHelper().resolveFile("build.xml"));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final File buildFile = FileUtil.toFile(buildFileObj);
            synchronized (buildFile) {
                try {
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    final Document buildScript = dBuilder.parse(buildFile);
                    NodeList projectNodes = buildScript.getElementsByTagName("project");
                    if(projectNodes != null) {
                        Node projectNode = projectNodes.item(0);
                        if(projectNode != null) {
                            NamedNodeMap projectAttrs = projectNode.getAttributes();
                            final Node nameAttr = projectAttrs.getNamedItem("name");
                            if (nameAttr != null && nameAttr.getTextContent().equals(context.getOldPackagePath())) {
                                result.add(new ProjectFileRefactoring(buildFileObj) {

                                    @Override
                                    public void performChange() {
                                        try {
                                            buildFileObj.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                                @Override
                                                public void run() throws IOException {
                                                    synchronized (buildFile) {
                                                        nameAttr.setTextContent(context.getNewPackagePath());
                                                        OutputStream os = buildFileObj.getOutputStream();
                                                        try {
                                                            XMLUtil.write(buildScript, os, "UTF-8"); // NOI18N
                                                        } finally {
                                                            os.close();
                                                        }
                                                    }
                                                }
                                            });
                                        } catch (FileStateInvalidException ex) {
                                            Exceptions.printStackTrace(ex);
                                        } catch (IOException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }

                                    @Override
                                    public String getDisplayText() {
                                        return NbBundle.getMessage(NbRefactoringProviderImpl.class, "TXT_ProjectXmlFileElementRename", "project", "name", context.getOldPackagePath());
                                    }

                                });
                            }
                        }
                    }
                } catch (ParserConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            final FileObject projectFileObj = FileUtil.toFileObject(new File(nbmProject.getProjectDirectoryFile(), "nbproject/project.xml"));
            final File projectFile = FileUtil.toFile(projectFileObj);
            synchronized (projectFile) {
                try {
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    final Document projectXmlFile = dBuilder.parse(projectFile);
                    NodeList codeNameBaseNodes = projectXmlFile.getElementsByTagName("code-name-base");
                    if(codeNameBaseNodes != null) {
                        final Node codeNameBaseNode = codeNameBaseNodes.item(0);
                        if(codeNameBaseNode != null) {
                            if (codeNameBaseNode.getTextContent().equals(context.getOldPackagePath())) {
                                result.add(new ProjectFileRefactoring(projectFileObj) {

                                    @Override
                                    public void performChange() {
                                        synchronized (projectFile) {
                                            try {
                                                codeNameBaseNode.setTextContent(context.getNewPackagePath());
                                                OutputStream os = projectFileObj.getOutputStream();
                                                try {
                                                    XMLUtil.write(projectXmlFile, os, "UTF-8"); // NOI18N
                                                } finally {
                                                    os.close();
                                                }
                                            } catch (FileAlreadyLockedException ex) {
                                                Exceptions.printStackTrace(ex);
                                            } catch (IOException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                    }

                                    @Override
                                    public String getDisplayText() {
                                        return NbBundle.getMessage(NbRefactoringProviderImpl.class, "TXT_ProjectXmlFileElementValueRename", "code-name-base", context.getOldPackagePath());
                                    }
                                });
                            }
                        }
                    }
                    NodeList publPkgNodes = projectXmlFile.getElementsByTagName("public-packages");
                    if(publPkgNodes != null) {
                        final Node publPkgListNode = publPkgNodes.item(0);
                        if(publPkgListNode != null) {
                            NodeList publPkgNodeList = publPkgListNode.getChildNodes();
                            for(int i=0; i<publPkgNodeList.getLength(); i++) {
                                if(publPkgNodeList.item(i).getTextContent().equals(context.getOldPackagePath())) {
                                    final Node publPkgNode = publPkgNodeList.item(i);
                                    result.add(new ProjectFileRefactoring(projectFileObj) {

                                        @Override
                                        public void performChange() {
                                            synchronized (projectFile) {
                                                try {
                                                    publPkgNode.setTextContent(context.getNewPackagePath());
                                                    OutputStream os = projectFileObj.getOutputStream();
                                                    try {
                                                        XMLUtil.write(projectXmlFile, os, "UTF-8"); // NOI18N
                                                    } finally {
                                                        os.close();
                                                    }
                                                } catch (FileAlreadyLockedException ex) {
                                                    Exceptions.printStackTrace(ex);
                                                } catch (IOException ex) {
                                                    Exceptions.printStackTrace(ex);
                                                }
                                            }
                                        }

                                        @Override
                                        public String getDisplayText() {
                                            return NbBundle.getMessage(NbRefactoringProviderImpl.class, "TXT_ProjectXmlFileElementValueRename", "package", context.getOldPackagePath());
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (ParserConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return result;
    }

}
