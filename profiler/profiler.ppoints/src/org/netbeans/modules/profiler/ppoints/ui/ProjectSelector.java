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

package org.netbeans.modules.profiler.ppoints.ui;

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
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Copied from org.netbeans.modules.profiler.v2.ui.ProjectSelector
 * TODO: use a public implementation once available
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProjectSelector_SelectProject=<Select Project>"
})
class ProjectSelector extends LazyComboBox<Lookup.Provider> {
    
    private static final Lookup.Provider EXTRA_ITEM = new Lookup.Provider() {
        public Lookup getLookup() { return Lookup.EMPTY; }
    };
    

    ProjectSelector(String extraItemName) {
        super(new Populator());
        setRenderer(new ProjectNameRenderer(extraItemName));
    }
    
    
    final Lookup.Provider getProject() {
        Lookup.Provider project = (Lookup.Provider)getSelectedItem();
        return project == EXTRA_ITEM ? null : project;
    }
    
    final void setProject(Lookup.Provider project) {
        if (project != null && !ProjectProfilingSupport.get(project).areProfilingPointsSupported())
            project = null;
        setSelectedItem(project == null ? EXTRA_ITEM : project);
    }
    
    void resetProject(Lookup.Provider project) {
        if (getProject() == project) resetModel();
    }
    
    
    // --- Projects populator --------------------------------------------------
    
    static class Populator extends LazyComboBox.Populator<Lookup.Provider> {
        
        protected Lookup.Provider initialProject() {
            return null;
        }

        protected Collection<Lookup.Provider> additionalProjects() {
            return Collections.EMPTY_SET;
        }
        
        protected final Lookup.Provider initial() {
            Lookup.Provider initial = initialProject();
            return initial == null ? EXTRA_ITEM : initial;
        }
        
        protected final Lookup.Provider[] populate() {
            Set<Lookup.Provider> s = new HashSet<>();
            
            for (Lookup.Provider project : ProjectUtilities.getOpenedProjects())
                if (ProjectProfilingSupport.get(project).areProfilingPointsSupported())
                    s.add(project);
            
            for (Lookup.Provider project : additionalProjects())
                if (ProjectProfilingSupport.get(project).areProfilingPointsSupported())
                    s.add(project);

            List<Lookup.Provider> l = new ArrayList<>();
            Lookup.Provider[] pa = s.toArray(new Lookup.Provider[0]);
            l.add(EXTRA_ITEM);
            l.addAll(Arrays.asList(ProjectUtilities.getSortedProjects(pa)));
            return l.toArray(new Lookup.Provider[0]);
        }
    }
    
    
    // --- Project renderer ----------------------------------------------------
    
    private static final class ProjectNameRenderer extends DefaultListCellRenderer {
        
        private Font _plainFont;
        private Font _boldFont;
        
        private Renderer _renderer;
        
        private final String extraItemName;
        
        ProjectNameRenderer(String extraItemName) {
            this.extraItemName = extraItemName;
        }

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
            
            if (value != EXTRA_ITEM) {
                Lookup.Provider p = (Lookup.Provider)value;
                _renderer.setText(ProjectUtilities.getDisplayName(p));
                _renderer.setIcon(ProjectUtilities.getIcon(p));
                boolean main = ProjectUtilities.getMainProject() == value;
                _renderer.setFontEx(main ? boldFont(renderer) : plainFont(renderer));
            } else {
                _renderer.setText(extraItemName);
                _renderer.setIcon(null);
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
