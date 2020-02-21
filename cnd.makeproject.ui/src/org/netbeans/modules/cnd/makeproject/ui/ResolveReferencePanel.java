/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * ResolveReferencePanel.java
 *
 * Created on 09.06.2011, 20:33:07
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import org.netbeans.modules.cnd.makeproject.ui.BrokenLinks.BrokenLink;
import org.netbeans.modules.cnd.makeproject.ui.BrokenLinks.Solution;
import org.openide.util.NbBundle;

/**
 *
 */
public class ResolveReferencePanel extends javax.swing.JPanel implements ActionListener {
    private final List<BrokenLink> brokenLinks;

    /** Creates new form ResolveReferencePanel */
    public ResolveReferencePanel(List<BrokenLink> brokenLinks) {
        initComponents();
        this.brokenLinks = brokenLinks;
        initUI();
    }
    
    private void initUI() {
        int iy = 0;
        GridBagConstraints gridBagConstraints;
        for(BrokenLink error : brokenLinks) {
            JLabel problem = new JLabel(NbBundle.getBundle(ResolveReferencePanel.class).getString("Link_Problem_Text")); // NOI18N
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = iy++;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(6, 4, 0, 0);
            add(problem, gridBagConstraints);
            
            JTextArea problemText = new JTextArea();
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = iy++;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, 4, 0, 0);
            add(problemText, gridBagConstraints);
            problemText.setText(error.getProblem());
            problemText.setBackground(this.getBackground());
            problemText.setEditable(false);
            problemText.setOpaque(false);                
            
            JSeparator separator = new JSeparator();
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = iy++;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            add(separator, gridBagConstraints);

            JLabel solutions = new JLabel(NbBundle.getBundle(ResolveReferencePanel.class).getString("Link_Solution_Text")); // NOI18N
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = iy++;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(0, 4, 0, 0);
            add(solutions, gridBagConstraints);

            int i = 1;
            for(Solution solution : error.getSolutions()) {
                StringBuilder buf = new StringBuilder();
                buf.append(i);
                buf.append(". "); // NOI18N
                buf.append(solution.getDescription());
                
                JTextArea solutionText = new JTextArea();
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = iy;
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new Insets(4, 4, 0, 0);
                add(solutionText, gridBagConstraints);
                solutionText.setText(buf.toString());
                solutionText.setBackground(this.getBackground());
                solutionText.setEditable(false);
                solutionText.setOpaque(false);                
             
                JButton button = new JButton(NbBundle.getBundle(ResolveReferencePanel.class).getString("Link_Resolve_Text"));
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = iy++;
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new Insets(4, 4, 0, 4);
                add(button, gridBagConstraints);
                if (solution.resolve() == null) {
                    button.setEnabled(false);
                } else {
                    button.putClientProperty("Solution", solution); // NOI18N
                    button.addActionListener(this);
                }
                i++;
                
                separator = new JSeparator();
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = iy++;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                add(separator, gridBagConstraints);
            }
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() instanceof JButton) {
            JButton button = (JButton) ae.getSource();
            Solution solution = (Solution) button.getClientProperty("Solution"); // NOI18N
            solution.resolve().run();
            button.setEnabled(false);
        }
    }
}
