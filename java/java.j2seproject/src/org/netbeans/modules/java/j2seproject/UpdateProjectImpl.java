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

package org.netbeans.modules.java.j2seproject;

import java.io.IOException;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
 * @author Tomas Zezula
 */
public class UpdateProjectImpl implements UpdateImplementation {

    private static final boolean TRANSPARENT_UPDATE = Boolean.getBoolean("j2seproject.transparentUpdate");
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    private static final String MINIMUM_ANT_VERSION_ELEMENT = "minimum-ant-version";

    private final Project project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration cfg;
    private boolean alreadyAskedInWriteAccess;
    private volatile boolean silentUpdate;
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
    UpdateProjectImpl(Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg) {
        assert project != null && helper != null && cfg != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
    }

    /**
     * Returns true if the project is of current version.
     * @return true if the project is of current version, otherwise false.
     */
    public boolean isCurrent () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (UpdateProjectImpl.this) {
                    if (isCurrent == null) {
                        if ((cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true) != null) ||
                        (cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true) != null)) {
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

    public boolean canUpdate () {
        if (TRANSPARENT_UPDATE || silentUpdate) {
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

    public void setTransparentUpdate(final boolean transparentUpdate) {
        this.silentUpdate = transparentUpdate;
    }

    public void saveUpdate(final EditableProperties props) throws IOException {
        this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true); //NOI18N
        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true); //NOI18N
        ProjectManager.getDefault().saveProject (this.project);
        synchronized(this) {
            this.isCurrent = Boolean.TRUE;
        }
    }

    public synchronized Element getUpdatedSharedConfigurationData () {
        if (cachedElement == null) {
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true);    //NOI18N
            if (oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                XMLUtil.copyDocument (oldRoot, newRoot, J2SEProject.PROJECT_CONFIGURATION_NAMESPACE);
                Element sourceRoots = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                root.setAttribute ("id","src.dir");   //NOI18N
                sourceRoots.appendChild(root);
                newRoot.appendChild (sourceRoots);
                Element testRoots = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                root.setAttribute ("id","test.src.dir");   //NOI18N
                testRoots.appendChild (root);
                newRoot.appendChild (testRoots);
                cachedElement = newRoot;
            } else {
                oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true);    //NOI18N
                if (oldRoot != null) {
                    Document doc = oldRoot.getOwnerDocument();
                    Element newRoot = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                    XMLUtil.copyDocument (oldRoot, newRoot, J2SEProject.PROJECT_CONFIGURATION_NAMESPACE);
                    cachedElement = newRoot;
                }
            }
        }
        if (cachedElement != null) {
            deleteMinAntVersion(cachedElement);
        }
        return cachedElement;
    }

    public synchronized EditableProperties getUpdatedProjectProperties () {
        EditableProperties cachedProperties = this.helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //The javadoc.additionalparam was not in NB 4.0
        ensureValueExists(cachedProperties, ProjectProperties.JAVADOC_ADDITIONALPARAM,"");    //NOI18N
        ensureValueExists(cachedProperties, "build.generated.dir","${build.dir}/generated"); //NOI18N
        ensureValueExists(cachedProperties, "meta.inf.dir","${src.dir}/META-INF"); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); //NOI18N
        ensureValueExists(cachedProperties, ProjectProperties.JAVAC_PROCESSORPATH,"${" + ProjectProperties.JAVAC_CLASSPATH + "}"); //NOI18N
        ensureValueExists(cachedProperties, "javac.test.processorpath","${" + ProjectProperties.JAVAC_TEST_CLASSPATH + "}"); //NOI18N
        return cachedProperties;
    }

    private static void ensureValueExists(EditableProperties prop, String property, String defaultValue) {
        if (prop.get(property)==null) { //NOI18N
            prop.put (property, defaultValue); //NOI18N
        }
    }

    private static void deleteMinAntVersion(final Element root) {
        NodeList list = root.getElementsByTagNameNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,MINIMUM_ANT_VERSION_ELEMENT);
        if (list.getLength() == 1) {
            Node me = list.item(0);
            me.getParentNode().removeChild(me);
        }
    }

    private boolean showUpdateDialog() {
        JButton updateOption = new JButton (NbBundle.getMessage(UpdateProjectImpl.class, "CTL_UpdateOption"));
        updateOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UpdateProjectImpl.class, "AD_UpdateOption"));
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
