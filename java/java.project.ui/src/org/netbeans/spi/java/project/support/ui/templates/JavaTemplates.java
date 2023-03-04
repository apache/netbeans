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

package org.netbeans.spi.java.project.support.ui.templates;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.project.ui.JavaTargetChooserPanel;
import org.netbeans.modules.java.project.ui.NewJavaFileWizardIterator;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.WizardDescriptor;
import org.netbeans.api.templates.TemplateRegistration;

/**
 * Default implementations of Java-specific template UI.
 * @author Jesse Glick
 */
public class JavaTemplates {

    private JavaTemplates() {}
    
    /**
     * Create a Java-oriented target chooser suitable for templates which are Java
     * sources or otherwise intended to reside in a Java package.
     * The user is prompted to choose a package location for the new file and a (base) name.
     * @param project the project which the template will be created in
     * @param folders a list of possible Java package roots to create the new file in (must be nonempty)
     * @return a wizard panel prompting the user to choose a name and package
     * @throws IllegalArgumentException if folders is empty
     */
    public static WizardDescriptor.Panel<WizardDescriptor> createPackageChooser(Project project, SourceGroup[] folders) throws IllegalArgumentException {
        return createPackageChooser(project, folders, null);
    }

    // friend API method used from templatesui module
    static WizardDescriptor.Panel<WizardDescriptor> createPackageChooser(Object project, String type) throws Exception {
        Project p = (Project) project;
        Sources src = ProjectUtils.getSources(p);
        SourceGroup[] groups = src.getSourceGroups(type);
        return JavaTemplates.createPackageChooser(p, groups);
    }
    
    /**
     * Create a Java-oriented target chooser suitable for templates which are Java
     * sources or otherwise intended to reside in a Java package.
     * The user is prompted to choose a package location for the new file and a (base) name.
     * Resulting panel can be decorated with additional panel. Which will
     * be placed below the standard package chooser.
     * @param project the project which the template will be created in
     * @param folders a list of possible Java package roots to create the new file in (must be nonempty)
     * @param bottomPanel panel which should be placed underneth the default chooser
     * @return a wizard panel prompting the user to choose a name and package
     * @throws IllegalArgumentException if folders is empty
     */
    public static WizardDescriptor.Panel<WizardDescriptor> createPackageChooser(Project project, SourceGroup[] folders,
            WizardDescriptor.Panel<WizardDescriptor> bottomPanel) throws IllegalArgumentException {
        return createPackageChooser(project, folders, bottomPanel, false);
    }
    
    /**
     * Create a Java-oriented target chooser suitable for templates which are Java
     * sources or otherwise intended to reside in a Java package.
     * The user is prompted to choose a package location for the new file and a (base) name;
     * this method allows to specify whether a valid (non-empty) package is required.
     * Resulting panel can be decorated with additional panel. Which will
     * be placed below the standard package chooser.
     * @param project the project which the template will be created in
     * @param folders a list of possible Java package roots to create the new file in (must be nonempty)
     * @param bottomPanel panel which should be placed underneth the default chooser
     * @param validPackageRequired indicates whether a only a valid (non-empty) package is accepted
     * @return a wizard panel prompting the user to choose a name and package
     * @throws IllegalArgumentException if folders is empty
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    public static WizardDescriptor.Panel<WizardDescriptor> createPackageChooser(Project project, SourceGroup[] folders, 
        WizardDescriptor.Panel<WizardDescriptor> bottomPanel, boolean validPackageRequired) throws IllegalArgumentException {
        if (folders.length == 0) {
            throw new IllegalArgumentException("No folders selected"); // NOI18N
        }
        return new JavaTargetChooserPanel(project, folders, bottomPanel, NewJavaFileWizardIterator.Type.FILE, validPackageRequired);
    } 
    
    /**
     * Creates new WizardIterator containing standard Package chooser.
     * Sample usage:
     * <pre>
     * &#64;TemplateRegistration(..., iconBase=JavaTemplates.JAVA_ICON)
     * public static WizardDescriptor.InstantiatingIterator&lt;?> myTemplates() {
     *     return JavaTemplates.createJavaTemplateIterator();
     * }
     * </pre>
     * @return WizardIterator consisting of one panel containing package chooser
     * @see TemplateRegistration
     * @see #JAVA_ICON
     */
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createJavaTemplateIterator() {
        return new NewJavaFileWizardIterator ();
    }

    /**
     * Icon for Java Class templates.
     * @see #createJavaTemplateIterator
     * @since 1.40
     */
    //@StaticResource
    public static final String JAVA_ICON = "org/netbeans/spi/java/project/support/ui/templates/class.png";
    /**
     * Icon for Java Enum templates.
     * @see #createJavaTemplateIterator
     * @since 1.60
     */
    //@StaticResource
    public static final String ENUM_ICON = "org/netbeans/spi/java/project/support/ui/templates/enum.png";

    /**
     * Icon for Java Interface templates.
     * @see #createJavaTemplateIterator
     * @since 1.60
     */
    //@StaticResource
    public static final String INTERFACE_ICON = "org/netbeans/spi/java/project/support/ui/templates/interface.png";

    /**
     * Icon for Java Annotation Type templates.
     * @see #createJavaTemplateIterator
     * @since 1.60
     */
    //@StaticResource
    public static final String ANNOTATION_TYPE_ICON = "org/netbeans/spi/java/project/support/ui/templates/annotation.png";
}
