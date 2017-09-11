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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
