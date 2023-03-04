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

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.w3c.dom.Element;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 */
public class UpdateProjectImpl implements UpdateImplementation {

    private static final boolean TRANSPARENT_UPDATE = Boolean.getBoolean("webproject.transparentUpdate"); //NOI18N
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    private Boolean isCurrent;
    private final AuxiliaryConfiguration cfg;
    private boolean alreadyAskedInWriteAccess;
    private final Project project;
    private final AntProjectHelper helper;
    private Element cachedElement;
    private static final String TAG_FILE = "file"; //NOI18N
    private static final String TAG_LIBRARY = "library"; //NOI18N
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    
    UpdateProjectImpl (Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg) {
        assert project != null && helper != null && cfg != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
    }
   
    public boolean isCurrent() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (this) {
                    if (isCurrent == null) {
                        isCurrent = cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-earproject/1",true) == null //NOI18N
                                ? Boolean.TRUE : Boolean.FALSE;
                    }
                    return isCurrent;
                }
            }
        });
    }

    public boolean canUpdate() {
        if (TRANSPARENT_UPDATE) {
            return true;
        }
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

    private boolean showUpdateDialog() {
        JButton updateOption = new JButton (NbBundle.getMessage(UpdateProjectImpl.class, "CTL_UpdateOption"));
        return DialogDisplayer.getDefault().notify(
            new NotifyDescriptor (NbBundle.getMessage(UpdateProjectImpl.class,"TXT_ProjectUpdate", BUILD_NUMBER),
                NbBundle.getMessage(UpdateProjectImpl.class,"TXT_ProjectUpdateTitle"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[] {
                    updateOption,
                    NotifyDescriptor.CANCEL_OPTION
                },
                updateOption)) == updateOption;
    }
    
    public void saveUpdate(EditableProperties props) throws IOException {
        helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
        cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-earproject/1",true); //NOI18N
        ProjectManager.getDefault().saveProject (project);
        synchronized(this) {
            isCurrent = Boolean.TRUE;
        }
    }

    public Element getUpdatedSharedConfigurationData() {
        if (cachedElement == null) {
            final String ns  = EarProjectType.PROJECT_CONFIGURATION_NAMESPACE; //NOI18N
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2ee-earproject/1" ,true);    //NOI18N
            if(oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                XMLUtil.copyDocument(oldRoot, newRoot, EarProjectType.PROJECT_CONFIGURATION_NAMESPACE);
                
                //update <web-module-/additional/-libraries/> to <j2ee-module-/additional/-libraries/>
//                NodeList contList = newRoot.getElementsByTagNameNS(ns, "web-module-libraries");
//                contList.item(0).setNodeValue(ArchiveProjectProperties.TAG_WEB_MODULE_LIBRARIES);
//                contList = newRoot.getElementsByTagNameNS(ns, "web-module-additional-libraries");
//                contList.item(0).setNodeValue(ArchiveProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                
                NodeList libList = newRoot.getElementsByTagNameNS(ns, TAG_LIBRARY);
                for (int i = 0; i < libList.getLength(); i++) {
                    if (libList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element library = (Element) libList.item(i);
                        Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                        //remove ${ and } from the beginning and end
                        String webFileText = XMLUtil.findText(webFile);
                        webFileText = webFileText.substring(2, webFileText.length() - 1);
                        if (webFileText.startsWith("libs.")) {
                            String libName = webFileText.substring(5, webFileText.indexOf(".classpath")); //NOI18N
                            @SuppressWarnings("unchecked")
                            List<URL> roots = LibraryManager.getDefault().getLibrary(libName).getContent("classpath"); //NOI18N
                            List<FileObject> files = new ArrayList<FileObject>();
                            List<FileObject> dirs = new ArrayList<FileObject>();
                            for (URL rootUrl : roots) {
                                FileObject root = org.openide.filesystems.URLMapper.findFileObject(rootUrl);
                                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                    root = FileUtil.getArchiveFile(root);
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
                                library.setAttribute(ATTR_FILES, "" + files.size());
                            }
                            if (dirs.size() > 0) {
                                library.setAttribute(ATTR_DIRS, "" + dirs.size());
                            }
                        }
                    }
                }
                cachedElement = newRoot;
            }
        }
        return cachedElement;
    }

    public EditableProperties getUpdatedProjectProperties() {
        return helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
    }

}
