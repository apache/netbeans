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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 * @author Tomas Zezula
 * @author Petr Somol
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category="Application", position=200)
public class JWSCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_WEBSTART = "WebStart"; // NOI18N
    
    private static final Map<String, JWSProjectProperties> projectProperties = new HashMap<String, JWSProjectProperties>();
    
    public JWSCompositeCategoryProvider() {}
    
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        boolean fxOverride = false;
        final Project project = context.lookup(Project.class);
        if (project != null) {
            final J2SEPropertyEvaluator j2sepe = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            fxOverride = JWSProjectProperties.isTrue(j2sepe.evaluator().getProperty("javafx.enabled")); //NOI18N
        }
        if(!fxOverride) {
            ProjectCustomizer.Category c = ProjectCustomizer.Category.create(CAT_WEBSTART,
                    NbBundle.getMessage(JWSCompositeCategoryProvider.class, "LBL_Category_WebStart"), null); //NOI18N
            c.setOkButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JWSProjectProperties prop = JWSProjectProperties.getInstanceIfExists(project.getLookup());
                        if(prop != null) {
                            projectProperties.put(project.getProjectDirectory().getPath(), prop);
                        }
                    }
                }
            });
            c.setStoreListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JWSProjectProperties prop = projectProperties.get(project.getProjectDirectory().getPath());
                        if(prop != null) {
                            JWSProjectPropertiesUtils.updateJnlpExtensionAndSave(prop, project);
                        }
                        projectProperties.remove(project.getProjectDirectory().getPath());
                    }
                }
            });
            c.setCloseListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JWSProjectProperties.cleanup(project.getLookup());
                    }
                }
            });
            return c;
        }
        return null;
    }
    
    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return new JWSCustomizerPanel(JWSProjectProperties.getInstance(context));
    }

}
