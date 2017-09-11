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

package org.netbeans.modules.java.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.java.freeform.LookupProviderImpl;
import org.netbeans.modules.java.freeform.jdkselection.JdkConfiguration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", position=400)
public class ClasspathCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("classpath", //NOI18N
                    NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Classpath"), null);
            return cat;
        }
        return null;
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        assert acc != null;
        assert project != null;
        
        final JdkConfiguration jdkConf = new JdkConfiguration(project, acc.getHelper(), acc.getEvaluator());
        
        final ClasspathPanel panel = new ClasspathPanel(jdkConf);
        final JavaPlatform initialPlatform = (JavaPlatform) panel.javaPlatform.getSelectedItem();
        ProjectModel pm = context.lookup(ProjectModel.class);
        assert pm != null;
        panel.setModel(pm);
        pm.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                panel.updateControls();
            }
        });
        category.setOkButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JavaPlatform p = (JavaPlatform) panel.javaPlatform.getSelectedItem();
                if (p != initialPlatform) {
                    try {
                        jdkConf.setSelectedPlatform(p);
                    } catch (IOException x) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
                    }
                }
            }
        });
        return panel;
        
    }

}
