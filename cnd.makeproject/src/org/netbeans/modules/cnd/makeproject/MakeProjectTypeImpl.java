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
package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory for simple Make projects.
 * 
 */
public final class MakeProjectTypeImpl implements MakeProjectType {

    private static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/make-project-private/1"; // NOI18N
    public static final String MAKE_DEP_PROJECTS = "make-dep-projects"; // NOI18N
    public static final String MAKE_DEP_PROJECT = "make-dep-project"; // NOI18N
    public static final String SOURCE_ENCODING_TAG = "sourceEncoding"; // NOI18N
    public final static String SOURCE_ROOT_ELEMENT = "sourceRootElem"; // NOI18N
    public final static String CONFIGURATION_LIST_ELEMENT = "confList"; // NOI18N
    public final static String CONFIGURATION_ELEMENT = "confElem"; // NOI18N
    public final static String CONFIGURATION_NAME_ELEMENT = "name"; // NOI18N
    public final static String CONFIGURATION_TYPE_ELEMENT = "type"; // NOI18N
    public final static String FORMATTING_STYLE_ELEMENT = "formatting"; // NOI18N
    public final static String FORMATTING_STYLE_PROJECT_ELEMENT = "project-formatting-style"; // NOI18N
    public final static String C_FORMATTING_STYLE_ELEMENT = "c-style"; // NOI18N
    public final static String CPP_FORMATTING_STYLE_ELEMENT = "cpp-style"; // NOI18N
    public final static String HEADER_FORMATTING_STYLE_ELEMENT = "header-style"; // NOI18N
    public final static String CLANG_FORMAT_STYLE_ELEMENT = "clang-format-style"; // NOI18N
    public final static String CUSTOMIZERID_ELEMENT = "customizerid"; // NOI18N
    public final static String ACTIVE_CONFIGURATION_TYPE_ELEMENT = "activeConfTypeElem"; // NOI18N
    public final static String ACTIVE_CONFIGURATION_INDEX_ELEMENT = "activeConfIndexElem"; // NOI18N
    public final static String ACTIVE_CONFIGURATION_CUSTOMIZERID = "activeConfCustomizerid"; // NOI18N
    
    public static final String TYPE_APPLICATION_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-managed.png"; // NOI18N
    public static final String TYPE_DB_APPLICATION_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-database.png"; // NOI18N
    public static final String TYPE_DYNAMIC_LIB_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-managed-dynamic.png"; // NOI18N
    public static final String TYPE_STATIC_LIB_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-managed-static.png"; // NOI18N
    public static final String TYPE_QT_APPLICATION_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-Qt.png"; // NOI18N
    public static final String TYPE_QT_DYNAMIC_LIB_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-Qt-dynamic.png"; // NOI18N
    public static final String TYPE_QT_STATIC_LIB_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-Qt-static.png"; // NOI18N
    public static final String TYPE_MAKEFILE_ICON = "org/netbeans/modules/cnd/makeproject/resources/projects-unmanaged.png"; // NOI18N

    /**
     * Do nothing, just a service.
     * public for testing
     */
    public MakeProjectTypeImpl() {
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public Icon getIcon(Element element) {
        Element conf = XMLUtil.findElement(element, "configuration", MakeBasedProjectFactorySingleton.PROJECT_NS); // NOI18N
        if (conf != null) {
            NodeList type = conf.getElementsByTagName(CONFIGURATION_TYPE_ELEMENT);
            if (type != null && type.getLength() > 0) {
                Node item = type.item(0);
                if (item != null) {
                    try {
                        switch (Integer.parseInt(item.getTextContent())) {
                            case MakeConfiguration.TYPE_MAKEFILE:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_MAKEFILE_ICON, false);
                            case MakeConfiguration.TYPE_APPLICATION:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_APPLICATION_ICON, false);
                            case MakeConfiguration.TYPE_DB_APPLICATION:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_DB_APPLICATION_ICON, false);
                            case MakeConfiguration.TYPE_DYNAMIC_LIB:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_DYNAMIC_LIB_ICON, false);
                            case MakeConfiguration.TYPE_STATIC_LIB:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_STATIC_LIB_ICON, false);
                            case MakeConfiguration.TYPE_QT_APPLICATION:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_QT_APPLICATION_ICON, false);
                            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_QT_DYNAMIC_LIB_ICON, false);
                            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                                return ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_QT_STATIC_LIB_ICON, false);
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
        }
        return ImageUtilities.loadImageIcon(MakeConfigurationDescriptor.ICON, true);
    }
    
    public Project createProject(MakeProjectHelper helper) throws IOException {
        return new MakeProjectImpl(helper);
    }

    @Override
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }

    @Override
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }

    /**
     * Get the path in the system filesystem where other modules could place
     * objects to include them in the projects' lookup
     * @return A path in the system filesystem
     */
    public String getLookupMergerPath() {
        return projectLayerPath() + "/Lookup"; //NOI18N
    }

    /**
     * System filesystem path for modules to place Node factories to include additional
     * nodes under this project
     * @return A path
     */
    public String nodeFactoryPath() {
        return projectLayerPath() + "/Nodes"; //NOI18N
    }

    /**
     * System fs path for other modules to add children to the Important Files subnode
     * @return A path
     */
    public String importantFilesPath() {
        return projectLayerPath() + "/ImportantFiles"; //NOI18N
    }

    /**
     * System fs path for other modules to add customizer panels
     * @return A path
     */
    public String customizerPath() {
        return projectLayerPath() + "/Customizer"; //NOI18N
    }

    /**
     * System fs path for other modules to add make project specific actions
     * @return A path
     */
    public String projectActionsPath() {
        return projectLayerPath() + "/Actions"; //NOI18N
    }

    /**
     * System fs path for other modules to add make project folders' specific actions
     * @return A path
     */
    @Override
    public String folderActionsPath() {
        return projectLayerPath() + "/ActionsFolder"; //NOI18N
    }

    /**
     * System fs path for other modules to add make project external folders' specific actions
     * @return A path
     */
    @Override
    public String extFolderActionsPath() {
        return projectLayerPath() + "/ActionsExtFolder"; //NOI18N
    }

    private String projectLayerPath() {
        return "Projects/" + PROJECT_TYPE; //NOI18N
    }
    

    public static String projectMetadataFactoryPath(String customizerId) {
        return "Projects/" + (customizerId == null ?  PROJECT_TYPE : customizerId) + "/" + ProjectMetadataFactory.LAYER_PATH; //NOI18N
    }    
}
