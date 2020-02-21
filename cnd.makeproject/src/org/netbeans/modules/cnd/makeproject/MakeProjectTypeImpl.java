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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
