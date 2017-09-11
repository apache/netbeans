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
package org.netbeans.modules.refactoring.spi.impl;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;

/**
 *
 * @author  Jan Becicka
 */
public class ErrorPanel extends javax.swing.JPanel {

    private static ImageIcon fatalImage = null, nonFatalImage = null;

    private RefactoringUI ui;
    /** Creates new form ErrorPanel */
    public ErrorPanel(RefactoringUI ui) {
        this.ui = ui;
        initComponents();
        headLine.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background")); // NOI18N
        setPreferredSize(new Dimension(510, 200));
    }
    
    public ErrorPanel(Problem problem, RefactoringUI ui) {
        this(ui);
        setProblems(problem);
    }
    
    public void setProblems(Problem problem) {
        errors.removeAll();
        int i = 0;
        ProblemComponent.initButtonSize(problem);
        boolean single = problem.getNext()==null;
        while (problem != null) {
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i++;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints.weightx = 1.0;
            
            ProblemComponent c = new ProblemComponent(problem, ui, single);
            errors.add(c, gridBagConstraints);
            
            problem = problem.getNext();
            
            if (i%2 == 1)
                c.setLightBackground();
            else 
                c.setDarkBackground();
        }

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = i;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        JPanel jp = new JPanel();
        errors.add(jp, gridBagConstraints);
    }
    
    static ImageIcon getFatalErrorIcon() {
        if (fatalImage == null) {
            fatalImage = new ImageIcon(ErrorPanel.class.getResource("/org/netbeans/modules/refactoring/api/resources/error.png")); //NOI18N
        }
        return fatalImage;
    }
    
    static ImageIcon getNonfatalErrorIcon() {
        if (nonFatalImage == null) {
            nonFatalImage = new ImageIcon(ErrorPanel.class.getResource("/org/netbeans/modules/refactoring/api/resources/warning.png")); //NOI18N
        }
        return nonFatalImage;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();
        listScrollPane = new javax.swing.JScrollPane();
        errors = new ScrollableJPanel();
        explanationPanel = new javax.swing.JPanel();
        fatalError = new javax.swing.JLabel();
        nonFatalError = new javax.swing.JLabel();
        headLine = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        listPanel.setLayout(new java.awt.BorderLayout());

        errorLabel.setLabelFor(errors);
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("LBL_ErrorsList")); // NOI18N
        listPanel.add(errorLabel, java.awt.BorderLayout.NORTH);

        listScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        errors.setLayout(new java.awt.GridBagLayout());
        listScrollPane.setViewportView(errors);

        listPanel.add(listScrollPane, java.awt.BorderLayout.CENTER);

        add(listPanel, java.awt.BorderLayout.CENTER);

        explanationPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        fatalError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/refactoring/api/resources/error.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(fatalError, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("LBL_FatalError")); // NOI18N
        fatalError.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 20));
        explanationPanel.add(fatalError);

        nonFatalError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/refactoring/api/resources/warning.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(nonFatalError, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("LBL_NonFatalError")); // NOI18N
        explanationPanel.add(nonFatalError);

        add(explanationPanel, java.awt.BorderLayout.SOUTH);

        headLine.setEditable(false);
        headLine.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        headLine.setFont(errorLabel.getFont());
        headLine.setLineWrap(true);
        headLine.setText(org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("LBL_ErrorPanelDescription")); // NOI18N
        headLine.setWrapStyleWord(true);
        headLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 10, 1));
        add(headLine, java.awt.BorderLayout.NORTH);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle"); // NOI18N
        headLine.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_ErrorPanelName")); // NOI18N
        headLine.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ErrorPanel.class, "ACSD_ErrorPanelDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(ErrorPanel.class).getString("ACSD_ErrorPanelName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ErrorPanel.class).getString("ACSD_ErrorPanelDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel errors;
    private javax.swing.JPanel explanationPanel;
    private javax.swing.JLabel fatalError;
    private javax.swing.JTextArea headLine;
    private javax.swing.JPanel listPanel;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JLabel nonFatalError;
    // End of variables declaration//GEN-END:variables
    
}
