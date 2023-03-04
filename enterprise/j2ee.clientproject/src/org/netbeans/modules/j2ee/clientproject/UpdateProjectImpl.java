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

package org.netbeans.modules.j2ee.clientproject;

import java.io.IOException;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Tomas Mysik
 */
public class UpdateProjectImpl implements UpdateImplementation {

    private static final boolean TRANSPARENT_UPDATE = Boolean.getBoolean("carproject.transparentUpdate");
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    private static final String MINIMUM_ANT_VERSION_ELEMENT = "minimum-ant-version"; // NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration cfg;
    private boolean alreadyAskedInWriteAccess;
    private boolean isCurrent;
    private Element cachedElement;

    /**
     * Creates new UpdateHelper
     * @param project
     * @param helper AntProjectHelper
     * @param cfg AuxiliaryConfiguration
     * @param genFileHelper GeneratedFilesHelper
     * @param notifier used to ask user about project update
     */
    UpdateProjectImpl(Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg) {
        assert project != null && helper != null && cfg != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
    }

    public boolean isCurrent() {
        /*
        if (this.isCurrent == null) {
            if ((this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true) != null) ||
                (this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true) != null)) {
                this.isCurrent = Boolean.FALSE;
            } else {
                this.isCurrent = Boolean.TRUE;
            }
        }
        return isCurrent.booleanValue();
         */
        //there are no other versions yet => we can always return true
        return true;
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

    public void saveUpdate(EditableProperties props) throws IOException {
        this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true); //NOI18N
        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true); //NOI18N
        ProjectManager.getDefault().saveProject (this.project);
        synchronized(this) {
            this.isCurrent = Boolean.TRUE;
        }
    }

    public Element getUpdatedSharedConfigurationData() {
        if (cachedElement == null) {
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true);    //NOI18N
            if (oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS (AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                XMLUtil.copyDocument (oldRoot, newRoot, AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE);
                Element sourceRoots = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                Element root = doc.createElementNS (AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                root.setAttribute ("id","src.dir");   //NOI18N
                sourceRoots.appendChild(root);
                newRoot.appendChild (sourceRoots);
                Element testRoots = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                root = doc.createElementNS (AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                root.setAttribute ("id","test.src.dir");   //NOI18N
                testRoots.appendChild (root);
                newRoot.appendChild (testRoots);                
                cachedElement = updateMinAntVersion (newRoot, doc);
            } else {
                oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true);    //NOI18N
                if (oldRoot != null) {
                    Document doc = oldRoot.getOwnerDocument();
                    Element newRoot = doc.createElementNS (AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                    XMLUtil.copyDocument (oldRoot, newRoot, AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE);
                    cachedElement = updateMinAntVersion (newRoot, doc);
                }
            }
        }
        return cachedElement;
    }

    public EditableProperties getUpdatedProjectProperties() {
        EditableProperties cachedProperties = this.helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //The javadoc.additionalparam was not in NB 4.0
        if (cachedProperties.get (AppClientProjectProperties.JAVADOC_ADDITIONALPARAM)==null) {
            cachedProperties.put (AppClientProjectProperties.JAVADOC_ADDITIONALPARAM,"");    //NOI18N
        }
        if (cachedProperties.get ("build.generated.dir")==null) { //NOI18N
            cachedProperties.put ("build.generated.dir","${build.dir}/generated"); //NOI18N
        }
         if (cachedProperties.get (AppClientProjectProperties.META_INF)==null) { //NOI18N
            cachedProperties.put (AppClientProjectProperties.META_INF,"${src.dir}/conf"); //NOI18N
        }
        return cachedProperties;
    }

    private static Element updateMinAntVersion (final Element root, final Document doc) {
        NodeList list = root.getElementsByTagNameNS (AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,MINIMUM_ANT_VERSION_ELEMENT);
        if (list.getLength() == 1) {
            Element me = (Element) list.item(0);
            list = me.getChildNodes();
            if (list.getLength() == 1) {
                me.replaceChild (doc.createTextNode(AppClientProjectGenerator.MINIMUM_ANT_VERSION), list.item(0));
                return root;
            }
        }
        assert false : "Invalid project file"; //NOI18N
        return root;
    }
    
    private boolean showUpdateDialog() {
        JButton updateOption = new JButton (NbBundle.getMessage(UpdateProjectImpl.class, "CTL_UpdateOption"));
        updateOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UpdateHelper.class, "AD_UpdateOption"));
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
}
