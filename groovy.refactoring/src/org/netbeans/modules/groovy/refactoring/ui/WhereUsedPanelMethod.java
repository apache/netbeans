/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.refactoring.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.netbeans.modules.groovy.refactoring.utils.FindPossibleMethods;
import org.netbeans.modules.groovy.refactoring.findusages.model.MethodRefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.openide.filesystems.FileObject;

/**
 * Copied from Java Refactoring module and changed with respect to Groovy and
 * CSL specifics.
 * 
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class WhereUsedPanelMethod extends WhereUsedPanel.WhereUsedInnerPanel {
    private static final long serialVersionUID = 1L;


    public WhereUsedPanelMethod() {
        initComponents();
        jComboBox1.setRenderer(new ComboBoxRenderer());
    }


    @Override
    void initialize(final RefactoringElement element) {
        assert (element instanceof MethodRefactoringElement);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initUI(element);
                initClassTypeComboBox((MethodRefactoringElement) element);
            }
        });
    }

    private void initUI(final RefactoringElement element) {
        final Set<Modifier> modifiers = element.getModifiers();
        final Icon labelIcon = UiUtils.getElementIcon(element.getKind(), element.getModifiers());
        final String labelText = element.getShowcase();

        Dimension preferredSize = label.getPreferredSize();
        label.setText(labelText);
        label.setIcon(labelIcon);
        label.setPreferredSize(preferredSize);
        label.setMinimumSize(preferredSize);
        btn_usages.setVisible(!modifiers.contains(Modifier.STATIC));
        btn_overriders.setVisible(!(modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE) || element.getKind() == ElementKind.CONSTRUCTOR));
        btn_usages_overriders.setVisible(btn_usages.isVisible() && btn_overriders.isVisible());
    }

    private void initClassTypeComboBox(final MethodRefactoringElement element) {
        ClassNode methodType = element.getMethodType();
        if (methodType != null) {
            // This happens in normal situation if we are able to interfere method type
            jComboBox1.setModel(new DefaultComboBoxModel(new ClassNode[] {methodType}));
            jComboBox1.setEnabled(false);
        } else {
            final FileObject fo = element.getFileObject();
            final String fqn = element.getNode().getText();
            final String methodName = element.getName();

            final Set<MethodNode> possibleMethods = FindPossibleMethods.findPossibleMethods(fo, fqn, methodName);
            final Set<ClassNode> possibleClasses = new HashSet<ClassNode>();
            for (MethodNode method : possibleMethods) {
                possibleClasses.add(method.getDeclaringClass());
            }
            jComboBox1.setModel(new DefaultComboBoxModel(possibleClasses.toArray()));
            jComboBox1.setEnabled(true);
            element.setMethodType(possibleClasses.iterator().next());
        }
        
        jComboBox1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                element.setMethodType((ClassNode) jComboBox1.getSelectedItem());
            }
        });
    }

    public boolean isMethodFromBaseClass() {
        return jComboBox1.getSelectedIndex() > 0;
    }

    public boolean isMethodOverriders() {
        return btn_overriders.isSelected() || btn_usages_overriders.isSelected();
    }

    public boolean isMethodFindUsages() {
        return btn_usages.isSelected() || btn_usages_overriders.isSelected();
    }

    @Override
    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    @SuppressWarnings("serial")
    private static class ComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {

        public ComboBoxRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            setName("ComboBox.listRenderer"); // NOI18N

            if (value != null && value instanceof ClassNode) {
                ClassNode classNode = ((ClassNode) value);
                setText(ElementUtils.getNameWithoutPackage(classNode));

                if (classNode.isInterface()) {
                    setIcon(UiUtils.getElementIcon(ElementKind.INTERFACE, null));
                } else {
                    setIcon(UiUtils.getElementIcon(ElementKind.CLASS, null));
                }
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

    private static EmptyImageIcon EMPTY_IMAGE_ICON;
    private static Icon getEmptyIcon() {
        if (EMPTY_IMAGE_ICON == null) {
            EMPTY_IMAGE_ICON = new EmptyImageIcon();
        }
        return EMPTY_IMAGE_ICON;
    }
    private static class EmptyImageIcon implements Icon {
        private static final int WIDTH = 16;
        private static final int HEIGHT = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Empty
        }

        @Override
        public int getIconWidth() {
            return WIDTH;
        }

        @Override
        public int getIconHeight() {
            return HEIGHT;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        lbl_usagesof = new javax.swing.JLabel();
        btn_usages = new javax.swing.JRadioButton();
        btn_overriders = new javax.swing.JRadioButton();
        btn_usages_overriders = new javax.swing.JRadioButton();

        label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/groovy/refactoring/resources/warning_16.png"))); // NOI18N
        label.setText("<<Element>>"); // NOI18N

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanelMethod.class).getString("LBL_SearchInComents")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WhereUsedPanelMethod.class, "WhereUsedPanelMethod.jLabel1.text")); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new JLabel[] { label }));

        org.openide.awt.Mnemonics.setLocalizedText(lbl_usagesof, org.openide.util.NbBundle.getMessage(WhereUsedPanelMethod.class, "LBL_UsagesOfElement")); // NOI18N

        buttonGroup1.add(btn_usages);
        btn_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btn_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanelMethod.class, "LBL_FindUsages")); // NOI18N

        buttonGroup1.add(btn_overriders);
        org.openide.awt.Mnemonics.setLocalizedText(btn_overriders, org.openide.util.NbBundle.getMessage(WhereUsedPanelMethod.class, "LBL_FindOverridingMethods")); // NOI18N

        buttonGroup1.add(btn_usages_overriders);
        org.openide.awt.Mnemonics.setLocalizedText(btn_usages_overriders, org.openide.util.NbBundle.getMessage(WhereUsedPanelMethod.class, "LBL_FindUsagesOverridingMethods")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbl_usagesof)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_usages_overriders)
                            .addComponent(btn_usages)
                            .addComponent(searchInComments)
                            .addComponent(btn_overriders))
                        .addGap(0, 33, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_usagesof)
                    .addComponent(label))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchInComments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_usages)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_overriders)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_usages_overriders)
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btn_overriders;
    private javax.swing.JRadioButton btn_usages;
    private javax.swing.JRadioButton btn_usages_overriders;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel label;
    private javax.swing.JLabel lbl_usagesof;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables
}
