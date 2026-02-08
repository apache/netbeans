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
package org.netbeans.modules.php.blade.editor.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * 
 * @author bhaidu
 */
public class BladeComponentsCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String COMPONENTS_CUSTOMIZER = "components_customizer"; // NOI18N

    @Override
    public ProjectCustomizer.Category createCategory(Lookup lkp) {
        return ProjectCustomizer.Category.create(COMPONENTS_CUSTOMIZER,
                NbBundle.getMessage(BladeComponentsCustomizerProvider.class,
                        "LBL_ComponentsConfig"), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        assert project != null;
        
        BladeComponents panel =  new BladeComponents(project);
        category.setOkButtonListener(new Listener(panel));
        return panel;
    }

    private class Listener implements ActionListener {
        private final BladeComponents panel;
        public Listener(BladeComponents panel){
           this.panel = panel; 
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            this.panel.storeData();
        }
        
    }
}
