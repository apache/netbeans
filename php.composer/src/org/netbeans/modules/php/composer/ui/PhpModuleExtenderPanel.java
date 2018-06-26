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
package org.netbeans.modules.php.composer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.composer.output.model.ComposerPackage;
import org.netbeans.modules.php.composer.ui.options.ComposerOptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public class PhpModuleExtenderPanel extends JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final List<ComposerPackage> selectedPackages = Collections.synchronizedList(new ArrayList<ComposerPackage>());
    // @GuardedBy("EDT")
    private final DependenciesPanel dependenciesPanel;
    // @GuardedBy("EDT")
    private final SelectedPackagesModel selectedPackagesModel;


    public PhpModuleExtenderPanel() {
        assert EventQueue.isDispatchThread();

        dependenciesPanel = DependenciesPanel.create();
        selectedPackagesModel = new SelectedPackagesModel(selectedPackages);

        initComponents();
        init();
    }

    private void init() {
        searchPanel.add(dependenciesPanel, BorderLayout.CENTER);
        selectedPackagesList.setModel(selectedPackagesModel);
        selectedPackagesList.setCellRenderer(new ComposerPackageListCellRenderer());
        enableSelectButton();
        enableDeselectButton();
        // listeners
        dependenciesPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                enableSelectButton();
            }
        });
        selectedPackagesModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                processChange();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                processChange();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                processChange();
            }
            private void processChange() {
                enableSelectButton();
                fireChange();
            }
        });
        selectedPackagesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                enableDeselectButton();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public List<ComposerPackage> getSelectedPackages() {
        return new ArrayList<>(selectedPackages);
    }

    void enableSelectButton() {
        selectButton.setEnabled(dependenciesPanel.getComposerPackage() != null);
    }

    void enableDeselectButton() {
        deselectButton.setEnabled(!selectedPackagesList.getSelectedValuesList().isEmpty());
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPanel = new JPanel();
        selectButton = new JButton();
        deselectButton = new JButton();
        selectedLabel = new JLabel();
        optionsLabel = new JLabel();
        selectedPackagesScrollPane = new JScrollPane();
        selectedPackagesList = new JList<ComposerPackage>();
        holderPanel1 = new JPanel();
        holderPanel2 = new JPanel();

        searchPanel.setLayout(new BorderLayout());

        Mnemonics.setLocalizedText(selectButton, ">"); // NOI18N
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(deselectButton, "<"); // NOI18N
        deselectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deselectButtonActionPerformed(evt);
            }
        });

        selectedLabel.setLabelFor(selectedPackagesList);
        Mnemonics.setLocalizedText(selectedLabel, NbBundle.getMessage(PhpModuleExtenderPanel.class, "PhpModuleExtenderPanel.selectedLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(PhpModuleExtenderPanel.class, "PhpModuleExtenderPanel.optionsLabel.text")); // NOI18N
        optionsLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                optionsLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                optionsLabelMousePressed(evt);
            }
        });

        selectedPackagesScrollPane.setViewportView(selectedPackagesList);

        GroupLayout holderPanel1Layout = new GroupLayout(holderPanel1);
        holderPanel1.setLayout(holderPanel1Layout);
        holderPanel1Layout.setHorizontalGroup(
            holderPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 44, Short.MAX_VALUE)
        );
        holderPanel1Layout.setVerticalGroup(
            holderPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        GroupLayout holderPanel2Layout = new GroupLayout(holderPanel2);
        holderPanel2.setLayout(holderPanel2Layout);
        holderPanel2Layout.setHorizontalGroup(
            holderPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 44, Short.MAX_VALUE)
        );
        holderPanel2Layout.setVerticalGroup(
            holderPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchPanel, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(holderPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton)
                    .addComponent(holderPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deselectButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(selectedPackagesScrollPane, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectedLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {deselectButton, holderPanel1, holderPanel2, selectButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(searchPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(holderPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deselectButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(holderPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectedLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedPackagesScrollPane, GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void optionsLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_optionsLabelMouseEntered

    private void optionsLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMousePressed
        OptionsDisplayer.getDefault().open(ComposerOptionsPanelController.getOptionsPath());
    }//GEN-LAST:event_optionsLabelMousePressed

    private void selectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        ComposerPackage composerPackage = dependenciesPanel.getComposerPackage();
        assert composerPackage != null;
        if (new HashSet<>(selectedPackages).add(composerPackage)) {
            selectedPackages.add(composerPackage);
            selectedPackagesModel.fireContentsChanged();
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void deselectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deselectButtonActionPerformed
        List<ComposerPackage> packages = selectedPackagesList.getSelectedValuesList();
        assert !packages.isEmpty();
        selectedPackages.removeAll(packages);
        selectedPackagesModel.fireContentsChanged();
    }//GEN-LAST:event_deselectButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton deselectButton;
    private JPanel holderPanel1;
    private JPanel holderPanel2;
    private JLabel optionsLabel;
    private JPanel searchPanel;
    private JButton selectButton;
    private JLabel selectedLabel;
    private JList<ComposerPackage> selectedPackagesList;
    private JScrollPane selectedPackagesScrollPane;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class SelectedPackagesModel extends AbstractListModel<ComposerPackage> {

        // @GuardedBy("EDT")
        private final List<ComposerPackage> selectedPackages;


        public SelectedPackagesModel(List<ComposerPackage> selectedPackages) {
            assert EventQueue.isDispatchThread();
            this.selectedPackages = selectedPackages;
        }

        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return selectedPackages.size();
        }

        @Override
        public ComposerPackage getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            try {
                return selectedPackages.get(index);
            } catch (IndexOutOfBoundsException ex) {
                // can happen while clearing results
                return null;
            }
        }

        void fireContentsChanged() {
            assert EventQueue.isDispatchThread();
            fireContentsChanged(0, 0, selectedPackages.size());
        }

    }

    private static final class ComposerPackageListCellRenderer implements ListCellRenderer<ComposerPackage> {

        private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();

        @NbBundle.Messages({
            "# {0} - package name",
            "# {1} - package version",
            "ComposerPackageListCellRenderer.label={0} ({1})",
        })
        @Override
        public Component getListCellRendererComponent(JList<? extends ComposerPackage> list, ComposerPackage value, int index, boolean isSelected, boolean cellHasFocus) {
            String label = Bundle.ComposerPackageListCellRenderer_label(value.getName(), value.getVersion());
            return defaultRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
        }

    }


}
