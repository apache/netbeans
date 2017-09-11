/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.hints.projects.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.netbeans.spi.editor.hints.projects.ProjectSettings;
import org.netbeans.spi.editor.hints.projects.support.StandardProjectSettings.Standard;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages("CAP_Hints=Hints")
class ProjectCustomizer implements CompositeCategoryProvider {
    
    private final String customizersFolder;

    public ProjectCustomizer(String customizersFolder) {
        this.customizersFolder = customizersFolder;
    }
    
    @Override
    public Category createCategory(Lookup context) {
        Project prj = context.lookup(Project.class);
        
        if (prj == null) return null;
        
        Standard settings = findStandardSettings(prj.getLookup());
        
        if (settings == null) return null;
        
        return Category.create("editor.hints", Bundle.CAP_Hints(), null);
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        final Project prj = context.lookup(Project.class);
        
        assert prj != null;
        
        final ProjectHintSettingPanel settingsPanel = new ProjectHintSettingPanel(findStandardSettings(prj.getLookup()), customizersFolder);
        
        final ToolPreferences[] toSave = new ToolPreferences[1];
        
        category.setOkButtonListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                toSave[0] = settingsPanel.commit();
            }
        });
        
        category.setStoreListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    if (toSave[0] != null) {
                        toSave[0].save();
                        toSave[0] = null;
                    }
                    
                    ProjectManager.getDefault().saveProject(prj);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
        return settingsPanel;
    }
    
    private static Standard findStandardSettings(Lookup where) {
        ProjectSettings settings = where.lookup(ProjectSettings.class);
        
        return settings instanceof Standard ? (Standard) settings : null;
    }
    
}
