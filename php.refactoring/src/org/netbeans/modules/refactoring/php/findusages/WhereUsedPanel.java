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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.refactoring.php.findusages;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Based on the WhereUsedPanel in Java refactoring by Jan Becicka.
 *
 * @author Jan Becicka, Tor Norbye, Radek Matous
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {

    private final transient WhereUsedSupport usage;
    private final transient ChangeListener parent;

    /**
     * Creates new form WhereUsedPanel
     */
    public WhereUsedPanel(String name, WhereUsedSupport e, ChangeListener parent) {
        setName(new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")).format(
                new Object[]{name})); // NOI18N

        this.usage = e;
        this.parent = parent;
        initComponents();
        searchInComments.setEnabled(false);
        searchInComments.setVisible(false);
        elementComboBox.setRenderer(new ModelElementRenderer());
    }
    private boolean initialized = false;
    private String methodDeclaringSuperClass = null;
    private String methodDeclaringClass = null;

    String getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setupPanels();
            }
        });
        initialized = true;
    }

    private void setupPanels() {
        ModelElement elem = usage.getModelElement();
        assert elem != null;
        String name = usage.getName();
        String clsName = null;
        if (usage.getModelElements().size() == 1) {
            if (elem instanceof FullyQualifiedElement && !(elem instanceof MethodScope)) {
                name = ((FullyQualifiedElement) elem).getFullyQualifiedName().toString();
            }
            if (elem.getInScope() instanceof TypeScope) {
                clsName = ((TypeScope) elem.getInScope()).getFullyQualifiedName().toString();
            }
        } else if (elem.getInScope() instanceof TypeScope) {
            clsName = "?";//NOI18N
        }
        String bKey = bundleKeyForLabel();
        final Set<Modifier> modifiers = usage.getModifiers();
        String lblText = name;
        if (bKey != null) {
            if (clsName != null) {
                lblText = NbBundle.getMessage(WhereUsedPanel.class, bKey, name, clsName);
            } else {
                lblText = NbBundle.getMessage(WhereUsedPanel.class, bKey, name);
            }
        }

        handleElementsCombo(usage.getModelElements());
        remove(classesPanel);
        remove(methodsPanel);
        m_overriders.setVisible(false);
        label.setText(lblText);
        if (usage.getKind() == PhpElementKind.METHOD) {
            add(methodsPanel, BorderLayout.CENTER);
            methodsPanel.setVisible(true);
            m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
            ModelElement modelElement = usage.getModelElement();
            if (modelElement != null) {
                final PhpModifiers phpModifiers = modelElement.getPhpModifiers();
                m_overriders.setVisible(!phpModifiers.isFinal() && !phpModifiers.isPrivate() && !phpModifiers.isStatic());
            }
            if (methodDeclaringSuperClass != null) {
                m_isBaseClass.setVisible(true);
                m_isBaseClass.setSelected(true);
                //Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
            } else {
                m_isBaseClass.setVisible(false);
                m_isBaseClass.setSelected(false);
            }
        } else if (usage.getKind() == PhpElementKind.CLASS) {
            add(classesPanel, BorderLayout.CENTER);
            classesPanel.setVisible(true);
        } else if (usage.getKind() == PhpElementKind.IFACE) {
            add(classesPanel, BorderLayout.CENTER);
            classesPanel.setVisible(true);
        } else {
            remove(classesPanel);
            remove(methodsPanel);
            c_subclasses.setVisible(false);
            m_usages.setVisible(false);
            c_usages.setVisible(false);
            c_directOnly.setVisible(false);
        }
        validate();
    }

    private void handleElementsCombo(List<ModelElement> elements) {
        elementComboBox.setModel(new DefaultComboBoxModel(new Vector<>(elements)));
        elementComboBox.setSelectedIndex(0);
        if (elements.size() == 1) {
            elementLabel.setVisible(false);
            elementComboBox.setVisible(false);
        }
    }

    private String bundleKeyForLabel() {
        String bundleKey = null;
        switch (usage.getKind()) {
            case IFACE:
                bundleKey = "DSC_IfaceUsages";//NOI18N
                break;
            case CLASS:
                bundleKey = "DSC_ClassUsages";//NOI18N
                break;
            case VARIABLE:
                bundleKey = "DSC_VariableUsages"; // NOI18N
                break;
            case FUNCTION:
                bundleKey = "DSC_FuncUsages"; // NOI18N
                break;
            case FIELD:
                bundleKey = "DSC_FieldUsages"; //NOI18N
                break;
            case METHOD:
                bundleKey = "DSC_MethodUsages"; //NOI18N
                break;
            case CONSTANT:
                bundleKey = "DSC_ConstantUsages"; //NOI18N
                break;
            case TYPE_CONSTANT:
                bundleKey = "DSC_ClassConstantUsages"; //NOI18N
                break;
            case USE_ALIAS:
                bundleKey = "DSC_UseAliasUsages"; //NOI18N
                break;
            case TRAIT:
                bundleKey = "DSC_TraitUsages"; //NOI18N
                break;
            default:
                assert false : usage.getKind();
        }

        return bundleKey;
    }

    public WhereUsedSupport getBaseMethod() {
        return usage;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        methodsPanel = new javax.swing.JPanel();
        m_isBaseClass = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        m_overriders = new javax.swing.JCheckBox();
        m_usages = new javax.swing.JCheckBox();
        classesPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        c_subclasses = new javax.swing.JRadioButton();
        c_usages = new javax.swing.JRadioButton();
        c_directOnly = new javax.swing.JRadioButton();
        commentsPanel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        elementLabel = new javax.swing.JLabel();
        elementComboBox = new javax.swing.JComboBox();
        searchInComments = new javax.swing.JCheckBox();

        setFocusTraversalPolicy(new java.awt.FocusTraversalPolicy() {
            public java.awt.Component getDefaultComponent(java.awt.Container focusCycleRoot){
                return m_isBaseClass;
            }//end getDefaultComponent

            public java.awt.Component getFirstComponent(java.awt.Container focusCycleRoot){
                return m_isBaseClass;
            }//end getFirstComponent

            public java.awt.Component getLastComponent(java.awt.Container focusCycleRoot){
                return m_isBaseClass;
            }//end getLastComponent

            public java.awt.Component getComponentAfter(java.awt.Container focusCycleRoot, java.awt.Component aComponent){
                if(aComponent ==  c_subclasses){
                    return c_directOnly;
                }
                if(aComponent ==  c_usages){
                    return c_subclasses;
                }
                if(aComponent ==  searchInComments){
                    return c_usages;
                }
                if(aComponent ==  elementComboBox){
                    return searchInComments;
                }
                return m_isBaseClass;//end getComponentAfter
            }
            public java.awt.Component getComponentBefore(java.awt.Container focusCycleRoot, java.awt.Component aComponent){
                if(aComponent ==  c_directOnly){
                    return c_subclasses;
                }
                if(aComponent ==  c_subclasses){
                    return c_usages;
                }
                if(aComponent ==  c_usages){
                    return searchInComments;
                }
                if(aComponent ==  searchInComments){
                    return elementComboBox;
                }
                return m_isBaseClass;//end getComponentBefore

            }}
        );
        setLayout(new java.awt.BorderLayout());

        methodsPanel.setLayout(new java.awt.GridBagLayout());

        m_isBaseClass.setSelected(true);
        m_isBaseClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_isBaseClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_isBaseClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        methodsPanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(m_overriders, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindOverridingMethods")); // NOI18N
        m_overriders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_overridersActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_overriders, gridBagConstraints);
        m_overriders.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.m_overriders.AccessibleContext.accessibleName")); // NOI18N

        m_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(m_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        m_usages.setMargin(new java.awt.Insets(10, 2, 2, 2));
        m_usages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_usagesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_usages, gridBagConstraints);
        m_usages.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.m_usages.AccessibleContext.accessibleName")); // NOI18N

        add(methodsPanel, java.awt.BorderLayout.CENTER);

        classesPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        classesPanel.add(jPanel2, gridBagConstraints);

        buttonGroup.add(c_subclasses);
        org.openide.awt.Mnemonics.setLocalizedText(c_subclasses, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindAllSubtypes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_subclasses, gridBagConstraints);
        c_subclasses.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.c_subclasses.AccessibleContext.accessibleName")); // NOI18N
        c_subclasses.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.c_subclasses.AccessibleContext.accessibleDescription")); // NOI18N

        buttonGroup.add(c_usages);
        c_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(c_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        c_usages.setMargin(new java.awt.Insets(4, 2, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_usages, gridBagConstraints);
        c_usages.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.c_usages.AccessibleContext.accessibleName")); // NOI18N
        c_usages.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.c_usages.AccessibleContext.accessibleDescription")); // NOI18N

        buttonGroup.add(c_directOnly);
        org.openide.awt.Mnemonics.setLocalizedText(c_directOnly, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindDirectSubtypesOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.c_directOnly.AccessibleContext.accessibleName")); // NOI18N
        c_directOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.c_directOnly.AccessibleContext.accessibleDescription")); // NOI18N

        add(classesPanel, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(label, "DUMMY"); // NOI18N

        elementLabel.setLabelFor(elementComboBox);
        elementLabel.setText(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FromFile")); // NOI18N

        searchInComments.setSelected(((Boolean) NbPreferences.forModule(WhereUsedPanel.class).getBoolean("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        searchInComments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchInCommentsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout commentsPanelLayout = new javax.swing.GroupLayout(commentsPanel);
        commentsPanel.setLayout(commentsPanelLayout);
        commentsPanelLayout.setHorizontalGroup(
            commentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commentsPanelLayout.createSequentialGroup()
                .addGroup(commentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchInComments)
                    .addComponent(label)
                    .addGroup(commentsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(elementLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(elementComboBox, 0, 263, Short.MAX_VALUE)))
                .addContainerGap())
        );
        commentsPanelLayout.setVerticalGroup(
            commentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commentsPanelLayout.createSequentialGroup()
                .addComponent(label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(commentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(elementComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(elementLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchInComments))
        );

        label.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.label.AccessibleContext.accessibleName")); // NOI18N
        label.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.label.AccessibleContext.accessibleDescription")); // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.elementLabel.AccessibleContext.accessibleName")); // NOI18N
        elementLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.elementLabel.AccessibleContext.accessibleDescription")); // NOI18N
        elementComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.elementComboBox.AccessibleContext.accessibleName")); // NOI18N
        elementComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.elementComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        searchInComments.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.searchInComments.AccessibleContext.accessibleName")); // NOI18N
        searchInComments.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.searchInComments.AccessibleContext.accessibleDescription")); // NOI18N

        add(commentsPanel, java.awt.BorderLayout.NORTH);
        commentsPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.commentsPanel.AccessibleContext.accessibleName")); // NOI18N
        commentsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.commentsPanel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "WhereUsedPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        NbPreferences.forModule(WhereUsedPanel.class).putBoolean("searchInComments.whereUsed", b);//GEN-LAST:event_searchInCommentsItemStateChanged
    }

    private void m_isBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_isBaseClassActionPerformed
        parent.stateChanged(null);//GEN-LAST:event_m_isBaseClassActionPerformed
    }

    private void m_overridersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_overridersActionPerformed
        parent.stateChanged(null);//GEN-LAST:event_m_overridersActionPerformed
    }

    private void m_usagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_usagesActionPerformed
        parent.stateChanged(null);//GEN-LAST:event_m_usagesActionPerformed
    }

private void searchInCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchInCommentsActionPerformed
// TODO add your handling code here://GEN-LAST:event_searchInCommentsActionPerformed
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton c_directOnly;
    private javax.swing.JRadioButton c_subclasses;
    private javax.swing.JRadioButton c_usages;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel commentsPanel;
    private javax.swing.JComboBox elementComboBox;
    private javax.swing.JLabel elementLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox m_isBaseClass;
    private javax.swing.JCheckBox m_overriders;
    private javax.swing.JCheckBox m_usages;
    private javax.swing.JPanel methodsPanel;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables

    public boolean isMethodFromBaseClass() {
        return m_isBaseClass.isSelected();
    }

    public boolean isMethodOverriders() {
        return m_overriders.isSelected();
    }

    public boolean isClassSubTypes() {
        return c_subclasses.isSelected();
    }

    public boolean isClassSubTypesDirectOnly() {
        return c_directOnly.isSelected();
    }

    public boolean isMethodFindUsages() {
        return m_usages.isSelected();
    }

    public boolean isClassFindUsages() {
        return c_usages.isSelected();
    }

    public ModelElement getElement() {
        return (ModelElement) elementComboBox.getSelectedItem();
    }

    public @Override
    Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30, orig.height + 80);
    }

    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private static final class ModelElementRenderer extends JLabel implements ListCellRenderer, UIResource {

        private static final long serialVersionUID = 87513687675643214L;

        public ModelElementRenderer() {
            super();
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            if (value instanceof ModelElement) {
                setName("ComboBox.listRenderer"); // NOI18N

                ModelElement element = (ModelElement) value;
                StringBuilder sb = new StringBuilder();
                if (element instanceof FullyQualifiedElement) {
                    sb.append(((FullyQualifiedElement) element).getFullyQualifiedName().toString());
                } else if (element.getInScope() instanceof FullyQualifiedElement) {
                    sb.append(((FullyQualifiedElement) element.getInScope()).getFullyQualifiedName().toString());
                } else {
                    sb.append(element.getName());
                }
                final FileObject fileObject = element.getFileObject();
                if (fileObject != null) {
                    sb.append(" (").append(fileObject.getNameExt()).append(")");//NOI18N
                }
                //String filepath = FileUtil.toFile(element.getFileObject()).getAbsolutePath();
                setText(sb.toString());
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
    }
}
