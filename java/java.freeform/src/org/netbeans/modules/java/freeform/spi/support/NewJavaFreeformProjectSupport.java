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

package org.netbeans.modules.java.freeform.spi.support;

import java.io.IOException;
import org.netbeans.modules.java.freeform.ui.ClasspathWizardPanel;
import org.netbeans.modules.java.freeform.ui.NewJ2SEFreeformProjectWizardIterator;
import org.netbeans.modules.java.freeform.ui.OutputWizardPanel;
import org.netbeans.modules.java.freeform.ui.ProjectModel;
import org.netbeans.modules.java.freeform.ui.SourceFoldersPanel;
import org.netbeans.modules.java.freeform.ui.SourceFoldersWizardPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;

/**
 * Support for Java New Project Wizard. These methods are typically used by the
 * freeform project extension which want to instantiate also Java development
 * support in project.
 * <div class="nonnormative">
 * <p>
 * Typical usage of these methods is:
 * </p>
 * <ol>
 * <li>create implementation of {@link org.openide.WizardDescriptor.InstantiatingIterator}
 *   with your wizard panels and add panels created by {@link #createJavaPanels}
 *   method</li>
 * <li>in implementation of {@link org.openide.WizardDescriptor.Iterator.hasNext}
 *   method call also {@link #enableNextButton}</li>
 * <li>in implementation of {@link org.openide.WizardDescriptor.InstantiatingIterator.instantiate}
 *   method call in addition also {@link #instantiateJavaPanels}</li>
 * <li>do not forget to call {@link #uninitializeJavaPanels} in your 
 *   {@link org.openide.WizardDescriptor.InstantiatingIterator.uninitialize}
 *    to clean up Java panels</li>
 * </ol>
 * </div>
 *
 * @author  David Konecny
 */
public class NewJavaFreeformProjectSupport {

    /** List of initial source folders. Type: List of String pair: [source path, its display name]*/
    public static final String PROP_EXTRA_JAVA_SOURCE_FOLDERS = "sourceFolders"; // <List<String,String>> NOI18N
    
    private NewJavaFreeformProjectSupport() {
    }
    
    /**
     * Returns array of standard Java panels suitable for new project wizard.
     * Panel gathers info about Java source folders and their classpath.
     */
    public static WizardDescriptor.Panel[] createJavaPanels() {
        return new WizardDescriptor.Panel[]{new SourceFoldersWizardPanel(), new ClasspathWizardPanel(), new OutputWizardPanel() };
    }

    /**
     * There is special logic in Java panels that Sources panel should enable 
     * Next button only when at least one source folder was specified. Wizard
     * iterator which is using panels created by createJavaPanels() method 
     * should always call this method in hasNext() method.
     */
    public static boolean enableNextButton(WizardDescriptor.Panel panel) {
        if (panel instanceof SourceFoldersWizardPanel) {
            SourceFoldersPanel sfp = (SourceFoldersPanel)panel.getComponent();
            if (!sfp.hasSomeSourceFolder()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Update project with information gathered in Java panels, that is
     * add Java support to project. The method must to be called under 
     * ProjectManager.writeMutex.
     */
    public static void instantiateJavaPanels(AntProjectHelper helper, WizardDescriptor wiz) throws IOException {
        ProjectModel pm = (ProjectModel)wiz.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        ProjectModel.instantiateJavaProject(helper, pm);
    }
    
    /**
     * Uninitialize Java panels after wizard was instantiated.
     */
    public static void uninitializeJavaPanels(WizardDescriptor wiz) {
        wiz.putProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS, null);
        wiz.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL, null);
    }
    
}
