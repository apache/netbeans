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
package org.netbeans.modules.python.editor.refactoring.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.python.editor.refactoring.PythonRefUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.python.editor.refactoring.RefactoringModule;
import javax.swing.JPanel;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.python.editor.refactoring.PythonElementCtx;
import org.openide.util.Exceptions;

/**
 * Based on the WhereUsedPanel in Java refactoring by Jan Becicka.
 * @author Jan Becicka
 * @author  Tor Norbye
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {
    private final transient PythonElementCtx element;
    private PythonElementCtx newElement;
    private final transient ChangeListener parent;

    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, PythonElementCtx e, ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")); // NOI18N
        this.element = e;
        this.parent = parent;
        initComponents();
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
        Source source = Source.create(element.getFileObject());
        UserTask task = new UserTask() {

            // TODO - handle methods in modules!!!!
            private String getClassName(PythonElementCtx element) {
                return element.getDefClass();
            }

            /**
             * @todo For method calls, try to figure out the call type with the type analyzer
             */
            @Override
            public void run(ResultIterator iter) throws Exception {
                String m_isBaseClassText = null;
                final String labelText;
                Set<Modifier> modif = new HashSet<>();
                // TODO - resolve elements against the current info?
                if (element.getKind() == ElementKind.METHOD) {
                    if (element.getElement() != null) {
                        modif = element.getElement().getModifiers();
                    }
                    String methodName = element.getName();
                    String className = getClassName(element);
                    String displayClassName = /*PythonIndex.UNKNOWN_CLASS.equals(className) ? "&lt;Unknown&gt;" :*/ className;
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_MethodUsages", methodName, displayClassName);

                    methodDeclaringClass = className;

//                    IndexedMethod method = PythonRefUtils.getOverridingMethod(element, info);
//                    if (method != null) {
//                        m_isBaseClassText =
//                                new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOfBaseClass")).format(
//                                new Object[] {
//                            methodDeclaringSuperClass = method.getIn()
//                        }
//                        );
//                        newElement = new PythonElementCtx(method);
//                    }
                } else if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.MODULE) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ClassUsages", element.getName()); // NOI18N
                } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                    String methodName = element.getName();
                    String className = getClassName(element);
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ConstructorUsages", methodName, className); // NOI18N
                } else if (element.getKind() == ElementKind.FIELD) {
                    String fieldName = element.getName();
                    String className = getClassName(element);
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_FieldUsages", fieldName, className); // NOI18N
                } else {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_VariableUsages", element.getName()); // NOI18N
                }

                final Set<Modifier> modifiers = modif;
                final String isBaseClassText = m_isBaseClassText;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        remove(classesPanel);
                        remove(methodsPanel);
                        // WARNING for now since this feature is not ready yet
                        //label.setText(labelText);
                        String combinedLabelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_WhereUsedWarningInDevelopment", labelText);
                        label.setText(combinedLabelText);
                        if (element.getKind() == ElementKind.METHOD) {
                            add(methodsPanel, BorderLayout.CENTER);
                            methodsPanel.setVisible(true);
                            m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
                            // TODO - worry about frozen?                            
                            m_overriders.setVisible(/*!modifiers.contains(Modifier.FINAL) ||*/modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE));
                            if (methodDeclaringSuperClass != null) {
                                m_isBaseClass.setVisible(true);
                                m_isBaseClass.setSelected(true);
                                Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
                            } else {
                                m_isBaseClass.setVisible(false);
                                m_isBaseClass.setSelected(false);
                            }
                        } else if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.MODULE)) {
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
                });
            }
        };
        try {
            ParserManager.parse(Collections.singleton(source), task);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        initialized = true;
    }

    public PythonElementCtx getBaseMethod() {
        return newElement;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        jPanel3 = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();

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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/python/editor/refactoring/ui/Bundle"); // NOI18N
        m_isBaseClass.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_isBaseClass")); // NOI18N

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
        m_overriders.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_overriders")); // NOI18N

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
        m_usages.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_usages")); // NOI18N

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
        c_subclasses.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_subclasses")); // NOI18N

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
        c_usages.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_usages")); // NOI18N

        buttonGroup.add(c_directOnly);
        org.openide.awt.Mnemonics.setLocalizedText(c_directOnly, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindDirectSubtypesOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_directOnly")); // NOI18N

        add(classesPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        jPanel3.add(searchInComments, java.awt.BorderLayout.CENTER);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(jPanel3, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.whereUsed", b);
    }//GEN-LAST:event_searchInCommentsItemStateChanged

    private void m_isBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_isBaseClassActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_isBaseClassActionPerformed

    private void m_overridersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_overridersActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_overridersActionPerformed

    private void m_usagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_usagesActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_usagesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton c_directOnly;
    private javax.swing.JRadioButton c_subclasses;
    private javax.swing.JRadioButton c_usages;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
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

    @Override
    public Dimension getPreferredSize() {
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
}

