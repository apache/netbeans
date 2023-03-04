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

package org.netbeans.modules.profiler.v2.ui;

import org.netbeans.lib.profiler.ui.components.LazyComboBox;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProjectSelector_ExternalProcess=External process"
})
public class ProjectSelector extends LazyComboBox<Lookup.Provider> {
    
    public static final Lookup.Provider EXTERNAL_PROCESS = new Lookup.Provider() {
        public Lookup getLookup() { return Lookup.EMPTY; }
    };
    

    public ProjectSelector(Populator populator) {
        super(populator);
        setRenderer(new ProjectNameRenderer());
    }
    
    
    public final Lookup.Provider getProject() {
        Lookup.Provider project = (Lookup.Provider)getSelectedItem();
        return project == EXTERNAL_PROCESS ? null : project;
    }
    
    public final void setProject(Lookup.Provider project) {
        setSelectedItem(project == null ? EXTERNAL_PROCESS : project);
    }
    
    public void resetProject(Lookup.Provider project) {
        if (getProject() == project) resetModel();
    }
    
    
    // --- Projects populator --------------------------------------------------
    
    public static class Populator extends LazyComboBox.Populator<Lookup.Provider> {
        
        protected Lookup.Provider initialProject() {
            return null;
        }

        protected Collection<Lookup.Provider> additionalProjects() {
            return Collections.EMPTY_SET;
        }
        
        protected final Lookup.Provider initial() {
            Lookup.Provider initial = initialProject();
            return initial == null ? EXTERNAL_PROCESS : initial;
        }
        
        protected final Lookup.Provider[] populate() {
            Set<Lookup.Provider> s = new HashSet<>();
            s.addAll(Arrays.asList(ProjectUtilities.getOpenedProjects()));
            s.addAll(additionalProjects());

            List<Lookup.Provider> l = new ArrayList<>();
            Lookup.Provider[] pa = s.toArray(new Lookup.Provider[0]);
            l.add(EXTERNAL_PROCESS);
            l.addAll(Arrays.asList(ProjectUtilities.getSortedProjects(pa)));
            return l.toArray(new Lookup.Provider[0]);
        }
    }
    
    
    // --- Project renderer ----------------------------------------------------
    
    private static final class ProjectNameRenderer extends DefaultListCellRenderer {
        
        private Font _plainFont;
        private Font _boldFont;
        
        private Renderer _renderer;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel renderer = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (_renderer == null) _renderer = new Renderer();
            _renderer.setComponentOrientation(renderer.getComponentOrientation());
            _renderer.setOpaque(renderer.isOpaque());
            _renderer.setForeground(renderer.getForeground());
            _renderer.setBackground(renderer.getBackground());
            _renderer.setEnabled(renderer.isEnabled());
            _renderer.setBorder(renderer.getBorder());
            
            if (value != EXTERNAL_PROCESS) {
                Lookup.Provider p = (Lookup.Provider)value;
                _renderer.setText(ProjectUtilities.getDisplayName(p));
                _renderer.setIcon(ProjectUtilities.getIcon(p));
                boolean main = ProjectUtilities.getMainProject() == value;
                _renderer.setFontEx(main ? boldFont(renderer) : plainFont(renderer));
            } else {
                _renderer.setText(Bundle.ProjectSelector_ExternalProcess());
                _renderer.setIcon(Icons.getIcon(GeneralIcons.JAVA_PROCESS));
                _renderer.setFontEx(plainFont(renderer));
            }

            return _renderer;
        }
        
        private Font plainFont(JLabel renderer) {
            if (_plainFont == null) _plainFont = renderer.getFont().deriveFont(Font.PLAIN);
            return _plainFont;
        }
        
        private Font boldFont(JLabel renderer) {
            if (_boldFont == null) _boldFont = renderer.getFont().deriveFont(Font.BOLD);
            return _boldFont;
        }
        
        // Default renderer doesn't follow font settings in combo (not popup)
        private static class Renderer extends DefaultListCellRenderer {
            public void setFont(Font font) {}
            public void setFontEx(Font font) { super.setFont(font); }
        }
        
    }
    
}
