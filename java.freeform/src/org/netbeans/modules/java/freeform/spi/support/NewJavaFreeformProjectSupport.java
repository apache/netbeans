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
