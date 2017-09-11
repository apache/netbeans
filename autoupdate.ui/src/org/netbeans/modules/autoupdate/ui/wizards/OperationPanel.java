/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.actions.Installer;

/**
 *
 * @author  Jiri Rechtacek
 */
public final class OperationPanel extends javax.swing.JPanel {
    
    static final String RUN_ACTION = "run-action";
    static final String RUN_IN_BACKGROUND = "run-in-background";
    
    private final boolean runInBackground;
    
    public OperationPanel (boolean allowRunInBackground) {
        this(allowRunInBackground, false);
    }
    
    public OperationPanel (boolean allowRunInBackground, boolean runInBackground) {
        assert (runInBackground && allowRunInBackground) || ! runInBackground;
        this.runInBackground = runInBackground;
        initComponents ();
        rbRestartNow.setSelected (true);
        cbRunInBackground.setVisible (allowRunInBackground);
        if (allowRunInBackground) {
            cbRunInBackground.setSelected (false);
        }
        setRestartButtonsVisible (false);
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        Installer.RP.post(new Runnable () {
            @Override
            public void run () {
                if (runInBackground) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            firePropertyChange (RUN_IN_BACKGROUND, null, Boolean.TRUE);
                        }
                    });
                }
                firePropertyChange (RUN_ACTION, null, Boolean.TRUE);
            }
        }, 200);
    }
    
    public void waitAndSetProgressComponents (final JLabel mainLabel, final JComponent progressComponent, final JLabel detailLabel) {
        if (SwingUtilities.isEventDispatchThread ()) {
            setProgressComponents (mainLabel, progressComponent, detailLabel);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    setProgressComponents (mainLabel, progressComponent, detailLabel);
                }
            });
        }
    }
    
    public void setRestartButtonsVisible (final boolean visible) {
        if (SwingUtilities.isEventDispatchThread ()) {
            rbRestartLater.setVisible (visible);
            rbRestartNow.setVisible (visible);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    rbRestartLater.setVisible (visible);
                    rbRestartNow.setVisible (visible);
                }
            });
        }
    }
    
    public boolean restartNow () {
        return rbRestartNow.isSelected ();
    }

    public void hideRunInBackground () {
        if (SwingUtilities.isEventDispatchThread ()) {
            cbRunInBackground.setVisible (false);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    cbRunInBackground.setVisible (false);
                }
            });
        }
    }
    
    private void setProgressComponents (JLabel mainLabel, JComponent progressComponent, JLabel detailLabel) {
        assert pProgress != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        mainLabel.setPreferredSize (new Dimension (0, 20));
        detailLabel.setPreferredSize (new Dimension (0, 20));
        progressComponent.setPreferredSize (new Dimension (0, 20));
        pProgress.removeAll ();
        pProgress.add (mainLabel, BorderLayout.NORTH);
        pProgress.add (progressComponent, BorderLayout.CENTER);
        pProgress.add (detailLabel, BorderLayout.SOUTH);
        pAboveSpace.setVisible(true);
        revalidate ();
    }
    
    public void setBody (final String msg, final String text) {
        if (SwingUtilities.isEventDispatchThread ()) {
            setBodyInEQ (msg, text);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    setBodyInEQ (msg, text);
                }
            });
        }
    }
    
    public void setBody (final String msg, final Set<UpdateElement> updateElements) {
        final List<UpdateElement> elements = new ArrayList<UpdateElement> (updateElements);
        
        Collections.sort(elements, new Comparator<UpdateElement>() {

            @Override
                public int compare(UpdateElement o1, UpdateElement o2) {
                    return Collator.getInstance().compare(o1.getDisplayName(), o2.getDisplayName());
                }
            });
        
        if (SwingUtilities.isEventDispatchThread ()) {
            setBodyInEQ (msg, elements);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    setBodyInEQ (msg, elements);
                }
            });
        }
    }
    
    private void setBodyInEQ (String msg, List<UpdateElement> elements) {
        pProgress.removeAll ();
        pProgress.add (getTitleComponent (msg), BorderLayout.NORTH);
        pContainerList2.add(getElementsComponent(elements), BorderLayout.CENTER);
        pProgress.add(pContainerList1, BorderLayout.CENTER);
        pAboveSpace.setVisible(false);
        revalidate ();
    }
    
    private void setBodyInEQ (String msg, String elements) {
        pProgress.removeAll ();
        pProgress.add (getTitleComponent (msg), BorderLayout.NORTH);
        pContainerList2.add(getElementsComponent(elements), BorderLayout.CENTER);
        pProgress.add (pContainerList1, BorderLayout.CENTER);
        pAboveSpace.setVisible(false);
        revalidate ();
    }
    
    private JComponent getTitleComponent (String msg) {
        JTextArea area = new JTextArea (msg);
        area.setWrapStyleWord (true);
        area.setLineWrap (true);
        area.setEditable (false);
        area.setOpaque (false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setBackground(new Color(0, 0, 0, 0));
        area.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        return area;
    }
    
    private JComponent getElementsComponent (List<UpdateElement> elements) {
        StringBuilder body = new StringBuilder ();
        for (UpdateElement el : elements) {
             body.append(el.getDisplayName ()).append("<br>"); // NOI18N
        }
        return getElementsComponent(body.toString());
    }
    
    private JComponent getElementsComponent (String msg) {
        JTextPane area = new JTextPane ();
        area.setEditable (false);
        area.setContentType ("text/html"); // NOI18N
        area.setText (msg);
        area.setOpaque (false);
        area.setBackground(new Color(0, 0, 0, 0));
        area.putClientProperty( JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );
        return area;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgRestartButtons = new javax.swing.ButtonGroup();
        pContainerList1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pContainerList2 = new javax.swing.JPanel();
        pAboveSpace = new javax.swing.JPanel();
        pProgress = new javax.swing.JPanel();
        pbPlaceHolder = new javax.swing.JProgressBar();
        lMainLabel = new javax.swing.JLabel();
        lDetailLabel = new javax.swing.JLabel();
        rbRestartNow = new javax.swing.JRadioButton();
        rbRestartLater = new javax.swing.JRadioButton();
        cbRunInBackground = new javax.swing.JCheckBox();

        pContainerList2.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(pContainerList2);

        javax.swing.GroupLayout pContainerList1Layout = new javax.swing.GroupLayout(pContainerList1);
        pContainerList1.setLayout(pContainerList1Layout);
        pContainerList1Layout.setHorizontalGroup(
            pContainerList1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 156, Short.MAX_VALUE)
            .addGroup(pContainerList1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
        );
        pContainerList1Layout.setVerticalGroup(
            pContainerList1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 144, Short.MAX_VALUE)
            .addGroup(pContainerList1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
        );

        pAboveSpace.setOpaque(false);

        javax.swing.GroupLayout pAboveSpaceLayout = new javax.swing.GroupLayout(pAboveSpace);
        pAboveSpace.setLayout(pAboveSpaceLayout);
        pAboveSpaceLayout.setHorizontalGroup(
            pAboveSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );
        pAboveSpaceLayout.setVerticalGroup(
            pAboveSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 65, Short.MAX_VALUE)
        );

        pProgress.setOpaque(false);
        pProgress.setLayout(new java.awt.BorderLayout());

        pbPlaceHolder.setPreferredSize(new java.awt.Dimension(0, 20));
        pProgress.add(pbPlaceHolder, java.awt.BorderLayout.CENTER);
        pProgress.add(lMainLabel, java.awt.BorderLayout.NORTH);
        pProgress.add(lDetailLabel, java.awt.BorderLayout.SOUTH);

        bgRestartButtons.add(rbRestartNow);
        org.openide.awt.Mnemonics.setLocalizedText(rbRestartNow, org.openide.util.NbBundle.getMessage(OperationPanel.class, "InstallUnitWizardModel_Buttons_RestartNow")); // NOI18N
        rbRestartNow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRestartNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRestartNowActionPerformed(evt);
            }
        });

        bgRestartButtons.add(rbRestartLater);
        org.openide.awt.Mnemonics.setLocalizedText(rbRestartLater, org.openide.util.NbBundle.getMessage(OperationPanel.class, "InstallUnitWizardModel_Buttons_RestartLater")); // NOI18N
        rbRestartLater.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRestartLater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRestartLaterActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbRunInBackground, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.cbRunInBackground.text")); // NOI18N
        cbRunInBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbRunInBackgroundActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .addComponent(pAboveSpace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbRunInBackground)
                    .addComponent(rbRestartNow, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .addComponent(rbRestartLater, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pAboveSpace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbRunInBackground)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbRestartNow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbRestartLater)
                .addContainerGap(45, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void rbRestartLaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRestartLaterActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_rbRestartLaterActionPerformed

private void rbRestartNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRestartNowActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_rbRestartNowActionPerformed

private void cbRunInBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRunInBackgroundActionPerformed
    firePropertyChange (RUN_IN_BACKGROUND, null, Boolean.TRUE);
}//GEN-LAST:event_cbRunInBackgroundActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgRestartButtons;
    private javax.swing.JCheckBox cbRunInBackground;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lDetailLabel;
    private javax.swing.JLabel lMainLabel;
    private javax.swing.JPanel pAboveSpace;
    private javax.swing.JPanel pContainerList1;
    private javax.swing.JPanel pContainerList2;
    private javax.swing.JPanel pProgress;
    private javax.swing.JProgressBar pbPlaceHolder;
    private javax.swing.JRadioButton rbRestartLater;
    private javax.swing.JRadioButton rbRestartNow;
    // End of variables declaration//GEN-END:variables
    
}
