/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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
            Set<Lookup.Provider> s = new HashSet();
            
            for (Lookup.Provider project : ProjectUtilities.getOpenedProjects())
                if (ProjectProfilingSupport.get(project).areProfilingPointsSupported())
                    s.add(project);
            
            for (Lookup.Provider project : additionalProjects())
                if (ProjectProfilingSupport.get(project).areProfilingPointsSupported())
                    s.add(project);

            List<Lookup.Provider> l = new ArrayList();
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
        
        final private String extraItemName;
        
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
