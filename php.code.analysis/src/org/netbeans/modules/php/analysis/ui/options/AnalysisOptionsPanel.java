/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.analysis.ui.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@NbBundle.Messages("AnalysisOptionsPanel.keywords.analysis=analysis")
@OptionsPanelController.Keywords(keywords={"php", "code", "analysis", "code analysis", "#AnalysisOptionsPanel.keywords.analysis"},
        location=UiUtils.OPTIONS_PATH, tabTitle="#AnalysisOptionsPanel.name")
public class AnalysisOptionsPanel extends JPanel {

    private static final long serialVersionUID = -895132465784564654L;
    // @GuardedBy("EDT")
    private final Map<String, AnalysisCategoryPanel> categoryPanels = new HashMap<>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public AnalysisOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();

        init();
    }

    private void init() {
        errorLabel.setText(" "); // NOI18N
        categoriesList.setCellRenderer(new CategoryNameListCellRenderer(categoriesList.getCellRenderer()));
    }

    public void addCategoryPanel(AnalysisCategoryPanel panel) {
        assert EventQueue.isDispatchThread();
        String name = panel.getCategoryName();
        assert !categoryPanels.containsKey(name) : name + " already found in " + categoryPanels;
        ((DefaultListModel) categoriesList.getModel()).addElement(name);
        categoryPanels.put(name, panel);
    }

    public void selectCategoryPanel(String name) {
        assert EventQueue.isDispatchThread();
        assert categoryPanels.containsKey(name) : name + " not found in " + categoryPanels;
        categoriesList.setSelectedValue(name, true);
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @CheckForNull
    public AnalysisCategoryPanel getSelectedPanel() {
        assert EventQueue.isDispatchThread();
        String categoryName = (String) categoriesList.getSelectedValue();
        return categoryPanels.get(categoryName);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        errorLabel = new JLabel();
        categoriesScrollPane = new JScrollPane();
        categoriesList = new JList<String>();
        categoryPanel = new JPanel();

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        categoriesList.setModel(new DefaultListModel());
        categoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoriesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                categoriesListValueChanged(evt);
            }
        });
        categoriesScrollPane.setViewportView(categoriesList);

        categoryPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(categoriesScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(categoryPanel, GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(errorLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(categoryPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(errorLabel))
                    .addComponent(categoriesScrollPane))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void categoriesListValueChanged(ListSelectionEvent evt) {//GEN-FIRST:event_categoriesListValueChanged
        categoryPanel.removeAll();
        AnalysisCategoryPanel selectedPanel = getSelectedPanel();
        if (selectedPanel != null) {
            categoryPanel.add(selectedPanel, BorderLayout.CENTER);
        }
        categoryPanel.revalidate();
        categoryPanel.repaint();
        fireChange();
    }//GEN-LAST:event_categoriesListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JList<String> categoriesList;
    private JScrollPane categoriesScrollPane;
    private JPanel categoryPanel;
    private JLabel errorLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class CategoryNameListCellRenderer implements ListCellRenderer<String> {

        private final ListCellRenderer<? super String> defaultCellRenderer;


        CategoryNameListCellRenderer(ListCellRenderer<? super String> defaultCellRenderer) {
            assert defaultCellRenderer != null;
            this.defaultCellRenderer = defaultCellRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            // is there a beter way to simply add a padding?
            value += "   "; // NOI18N
            return defaultCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

}
