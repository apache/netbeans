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
package org.netbeans.modules.refactoring.php.delete;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Subclass of CustomRefactoringPanel representing the
 * Safe Delete refactoring UI
 * @author Bharath Ravikumar
 */
public class SafeDeletePanel extends JPanel implements CustomRefactoringPanel {

    private final transient SafeDeleteRefactoring refactoring;
    private boolean regulardelete;
    private ChangeListener parent;
    private final boolean hasVisibleElements;
    private final String fileName;

    /**
     * Creates new form RenamePanelName
     * @param refactoring The SafeDelete refactoring used by this panel
     * @param selectedElements A Collection of selected elements
     */
    public SafeDeletePanel(SafeDeleteRefactoring refactoring, boolean regulardelete, ChangeListener parent) {
        setName(NbBundle.getMessage(SafeDeletePanel.class,
                regulardelete ? "LBL_SafeDel_Delete" : "LBL_SafeDel")); // NOI18N
        this.refactoring = refactoring;
        final SafeDeleteSupport support = this.refactoring.getRefactoringSource().lookup(SafeDeleteSupport.class);
        this.fileName = support.getFile().getNameExt();
        hasVisibleElements = support.hasVisibleElements();
        this.regulardelete = regulardelete;
        this.parent = parent;
        initComponents();
    }
    private boolean initialized = false;
    private String methodDeclaringClass = null;

    String getMethodDeclaringClass() {
        return methodDeclaringClass;
    }

    /**
     * Initialization method. Creates appropriate labels in the panel.
     */
    @Override
    public void initialize() {
        //This is needed since the checkBox is gets disabled on a
        //repeated invocation of SafeDelete follwing removal of references
        //to the element
        searchInComments.setEnabled(false);
        searchInComments.setVisible(false);

        if (initialized) {
            return;
        }


        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (regulardelete) {
                    safeDelete = new JCheckBox();
                    Mnemonics.setLocalizedText(safeDelete, NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDelCheckBox"));
                    safeDelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                            SafeDeletePanel.class,
                            "SafeDeletePanel.safeDelete.AccessibleContext.accessibleDescription"));
                    safeDelete.setMargin(new java.awt.Insets(2, 14, 2, 2));
                    searchInComments.setEnabled(false);
                    safeDelete.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent evt) {
                            searchInComments.setEnabled(safeDelete.isSelected());
                            parent.stateChanged(null);
                        }
                    });

                    checkBoxes.add(safeDelete, BorderLayout.CENTER);
                    if (!hasVisibleElements) {
                        safeDelete.setVisible(true);
                        safeDelete.setEnabled(false);
                    }
                }

                validate();
            }
        });
        initialized = true;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
    }

    boolean isRegularDelete() {
        return (safeDelete != null && !safeDelete.isSelected()) || !hasVisibleElements;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        checkBoxes = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        checkBoxes.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(label, (regulardelete)
            ? NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_RegularDeleteElement", this.fileName)
            : NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Element", this.fileName));
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        checkBoxes.add(label, java.awt.BorderLayout.NORTH);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/php/delete/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, bundle.getString("LBL_SafeDelInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(2, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        checkBoxes.add(searchInComments, java.awt.BorderLayout.SOUTH);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(checkBoxes, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for deleteInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        refactoring.setCheckInComments(b.booleanValue());
    }//GEN-LAST:event_searchInCommentsItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel checkBoxes;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JCheckBox safeDelete;

    @Override
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30, orig.height + 30);
    }

    /**
     * Indicates whether the element usage must be checked in comments
     * before deleting each element.
     * @return Returns the isSelected() attribute of the
     * underlying check box that controls search in comments
     */
    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
