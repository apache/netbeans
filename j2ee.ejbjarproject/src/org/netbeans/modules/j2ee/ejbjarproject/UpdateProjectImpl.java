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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tomas Mysik
 */
public class UpdateProjectImpl implements UpdateImplementation {

    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    private static final String INCLUDED_LIBRARY_ELEMENT = "included-library"; //NOI18N
    private static final String MINIMUM_ANT_VERSION_ELEMENT = "minimum-ant-version"; // NOI18N
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration cfg;
    private final GeneratedFilesHelper genFileHelper;
    private boolean alreadyAskedInWriteAccess;
    private Boolean isCurrent;
    private Element cachedElement;

    /**
     * Creates new UpdateHelper
     * @param project
     * @param helper AntProjectHelper
     * @param cfg AuxiliaryConfiguration
     * @param genFileHelper GeneratedFilesHelper
     * @param notifier used to ask user about project update
     */
    UpdateProjectImpl(Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg,
            GeneratedFilesHelper genFileHelper) {
        assert project != null && helper != null && cfg != null && genFileHelper != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
        this.genFileHelper = genFileHelper;
    }

    public boolean isCurrent() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (this) {
                    if (isCurrent == null) {
                        if ((cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/1",true) != null) ||
                        (cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/2",true) != null)) {
                            isCurrent = Boolean.FALSE;
                        } else {
                            isCurrent = Boolean.TRUE;
                        }
                    }
                    return isCurrent;
                }
            }
        }).booleanValue();
    }

    public boolean canUpdate() {
        //Ask just once under a single write access
        if (alreadyAskedInWriteAccess) {
            return false;
        }
        else {
            boolean canUpdate = showUpdateDialog();
            if (!canUpdate) {
                alreadyAskedInWriteAccess = true;
                ProjectManager.mutex().postReadRequest(new Runnable() {
                    public void run() {
                        alreadyAskedInWriteAccess = false;
                    }
                });
            }
            return canUpdate;
        }
    }

    public void saveUpdate(EditableProperties props) throws IOException {
        this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
        if (this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/1",true) != null) { //NOI18N
            //version 1
            this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/1",true); //NOI18N
        } else {
            //version 2
            this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/2",true); //NOI18N
        }
                
        boolean putProps = false;
        
        // AB: fix for #55597: should not update the project without adding the properties
        // update is only done once, so if we don't add the properties now, we don't get another chance to do so
        if (props == null) {
            props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            putProps = true;
        }

        //add properties needed by 4.1 project
        if(props != null) {
            props.put("test.src.dir", "test"); //NOI18N
        }

        if (putProps) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        
        ProjectManager.getDefault().saveProject (this.project);
        this.genFileHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH, UpdateProjectImpl.class.getResource("resources/build-impl.xsl"),
            true);
        synchronized(this) {
            this.isCurrent = Boolean.TRUE;
        }
    }

    public Element getUpdatedSharedConfigurationData() {
        if (cachedElement == null) {
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/1",true);    //NOI18N
            
            int version = 1;
            if (oldRoot == null) {
                version = 2;
                oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-ejbjarproject/2",true);    //NOI18N
            }
            final String ns = version == 1 ? "http://www.netbeans.org/ns/j2ee-ejbjarproject/1" : "http://www.netbeans.org/ns/j2ee-ejbjarproject/2"; //NOI18N 
            
            if (oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                XMLUtil.copyDocument (oldRoot, newRoot, EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE);
                if(version == 1) {
                    //1=>2 upgrade
                    Element sourceRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                    Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id","src.dir");   //NOI18N
                    sourceRoots.appendChild(root);
                    newRoot.appendChild (sourceRoots);
                    Element testRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id","test.src.dir");   //NOI18N
                    testRoots.appendChild (root);
                    newRoot.appendChild (testRoots);
                }
                if(version == 1 || version == 2) {
                    //2=>3 upgrade
                    NodeList libList = newRoot.getElementsByTagNameNS(ns, INCLUDED_LIBRARY_ELEMENT);
                    for (int i = 0; i < libList.getLength(); i++) {
                        if (libList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element library = (Element) libList.item(i);
                            String fileText = XMLUtil.findText(library);
                            if (fileText.startsWith ("libs.")) {
                                String libName = fileText.substring(6, fileText.indexOf(".classpath")); //NOI18N
                                List/*<URL>*/ roots = LibraryManager.getDefault().getLibrary(libName).getContent("classpath"); //NOI18N
                                ArrayList<FileObject> files = new ArrayList<FileObject>();
                                ArrayList<FileObject> dirs = new ArrayList<FileObject>();
                                for (Iterator it = roots.iterator(); it.hasNext();) {
                                    URL rootUrl = (URL) it.next();
                                    FileObject root = org.openide.filesystems.URLMapper.findFileObject (rootUrl);
                                    if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                        root = FileUtil.getArchiveFile (root);
                                    }
                                    if (root != null) {
                                        if (root.isData()) {
                                            files.add(root);
                                        } else {
                                            dirs.add(root);
                                        }
                                    }
                                }
                                if (files.size() > 0) {
                                    library.setAttribute(ATTR_FILES, "" + files.size()); //NOI18N
                                }
                                if (dirs.size() > 0) {
                                    library.setAttribute(ATTR_DIRS, "" + dirs.size()); //NOI18N
                                }
                            }
                        }
                    }
                }
                
                cachedElement = updateMinAntVersion(newRoot, doc);
            }
        }
        return cachedElement;
    }

    public EditableProperties getUpdatedProjectProperties() {
        // XXX no need to update? original comment was: Properties are the same in both j2seproject/1 and j2seproject/2
        return helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
    }

    private static Element updateMinAntVersion (final Element root, final Document doc) {
        NodeList list = root.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,MINIMUM_ANT_VERSION_ELEMENT);
        if (list.getLength() == 1) {
            Element me = (Element) list.item(0);
            list = me.getChildNodes();
            if (list.getLength() == 1) {
                me.replaceChild (doc.createTextNode(EjbJarProjectGenerator.MINIMUM_ANT_VERSION), list.item(0));
                return root;
            }
        }
        assert false : "Invalid project file"; //NOI18N
        return root;
    }
    
    private boolean showUpdateDialog() {
        return DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Confirmation (NbBundle.getMessage(UpdateProjectImpl.class,"TXT_ProjectUpdate",BUILD_NUMBER),
                NbBundle.getMessage(UpdateProjectImpl.class,"TXT_ProjectUpdateTitle"),
                NotifyDescriptor.YES_NO_OPTION)) == NotifyDescriptor.YES_OPTION;
    }
}
