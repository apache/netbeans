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

package org.netbeans.modules.profiler.options.ui.v2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilerOptionsContainer_Categories=&Categories:"
})
public class ProfilerOptionsContainer extends ProfilerOptionsPanel {
    
    private List<ProfilerOptionsPanel> panels; // only accessed in EDT
    private CategoriesListModel categoriesModel;
    private CategoriesSelectionModel categoriesSelection;
    
    private static int scrollIncrement = 20;
    
    private JPanel content;
    
    
    public ProfilerOptionsContainer() {
        initUI();
    }
    
    
    // --- API -----------------------------------------------------------------
    
    public String getDisplayName() { return null; }
    
    public void storeTo(final ProfilerIDESettings settings) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (panels != null)
                    for (ProfilerOptionsPanel panel : panels)
                        panel.storeTo(settings);
            }
        });
    }
    
    public void loadFrom(ProfilerIDESettings settings) {
        int sel = categoriesSelection.getLeadSelectionIndex();
        if (sel == -1) sel = 0;
        
        if (panels == null) panels = new ArrayList();
        else panels.clear();
        
        panels.addAll(Lookup.getDefault().lookupAll(ProfilerOptionsPanel.class));
        categoriesModel.changed();
        
        for (ProfilerOptionsPanel panel : panels) panel.loadFrom(settings);
        
        if (categoriesModel.getSize() > 0) categoriesSelection.setSelectionInterval(sel, sel);
    }
    
    public boolean equalsTo(ProfilerIDESettings settings) {
        if (panels != null)
            for (ProfilerOptionsPanel panel : panels)
                if (!panel.equalsTo(settings)) return false;
        return true;
    }
    
    
    // --- Implementation ------------------------------------------------------
    
    private void initUI() {
        categoriesModel = new CategoriesListModel();
        categoriesSelection = new CategoriesSelectionModel();
        
        scrollIncrement = new JCheckBox("XXX").getPreferredSize().height; // NOI18N
        
        JList<ProfilerOptionsPanel> categoriesList = new JList(categoriesModel) {
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                dim.width = Math.max(dim.width + 20, 140);
                return dim;
            }
        };
        categoriesList.setVisibleRowCount(0);
        categoriesList.setSelectionModel(categoriesSelection);
        categoriesList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String panelName = " " + ((ProfilerOptionsPanel)value).getDisplayName() + " "; // NOI18N
                return super.getListCellRendererComponent(list, panelName, index, isSelected, cellHasFocus);
            }
        });
        
        JScrollPane categoriesScroll = new JScrollPane(categoriesList);
        
        JLabel categoriesLabel = new JLabel();
        categoriesLabel.setHorizontalAlignment(JLabel.LEADING);
        Mnemonics.setLocalizedText(categoriesLabel, Bundle.ProfilerOptionsContainer_Categories());
        categoriesLabel.setLabelFor(categoriesList);
        int labelOffset = 6;
        
        JPanel categoriesPanel = new JPanel(new BorderLayout(0, labelOffset));
        categoriesPanel.add(categoriesLabel, BorderLayout.NORTH);
        categoriesPanel.add(categoriesScroll, BorderLayout.CENTER);
        
        content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(categoriesLabel.getPreferredSize().height + labelOffset, 11, 0, 0));
        content.setMinimumSize(new Dimension(0, 0));
        content.setPreferredSize(new Dimension(0, 0));
        
        setLayout(new BorderLayout());
        add(categoriesPanel, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }
    
    private void panelSelected(final int index) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                content.removeAll();
                content.add(createPanelScroll(panels.get(index)), BorderLayout.CENTER);
                content.revalidate();
                content.repaint();
            }
        });
    }
    
    private JScrollPane createPanelScroll(ProfilerOptionsPanel panel) {
        enlargeBorder(panel, 0, 0, 0, 5);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(scrollIncrement);
        scroll.getVerticalScrollBar().setBlockIncrement((int)(content.getHeight() * 0.8d));
        scroll.getHorizontalScrollBar().setUnitIncrement(scrollIncrement);
        scroll.getHorizontalScrollBar().setBlockIncrement((int)(content.getWidth() * 0.8d));
        return scroll;
    }
    
    private static void enlargeBorder(JComponent c, int t, int l, int b, int r) {
        Border current = c.getBorder();
        Border larger = BorderFactory.createEmptyBorder(t, l, b, r);
        if (current == null) c.setBorder(larger);
        else c.setBorder(BorderFactory.createCompoundBorder(larger, current));
    }
    
    
    // --- Data model ----------------------------------------------------------
    
    private class CategoriesListModel extends AbstractListModel<ProfilerOptionsPanel> {
        
        public int getSize() {
            return panels == null ? 0 : panels.size();
        }
        
        public ProfilerOptionsPanel getElementAt(int index) {
            return panels == null ? null : panels.get(index);
        }
        
        void changed() {
            if (panels != null) super.fireContentsChanged(this, 0, panels.size());
        }
        
    }
    
    
    // --- Selection Model -----------------------------------------------------
    
    private class CategoriesSelectionModel extends DefaultListSelectionModel {
        
        CategoriesSelectionModel() { setSelectionMode(SINGLE_SELECTION); }
        
        public void clearSelection() {}
        
        public void removeSelectionInterval(int index0, int index1) {}
        
        protected void fireValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
            super.fireValueChanged(firstIndex, lastIndex, isAdjusting);
            if (!isAdjusting) panelSelected(getLeadSelectionIndex());
        }
        
    }
    
}
