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
package org.netbeans.modules.bugtracking.commons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author Ondrej Vrabec
 */
public final class CollapsibleSectionPanel extends javax.swing.JPanel implements FocusListener {

    final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
    final boolean isNimbus = "Nimbus".equals( UIManager.getLookAndFeel().getID() );
    final boolean isAqua = "Aqua".equals( UIManager.getLookAndFeel().getID() );
    
    /**
     * Creates new form CollapsibleSectionPanel
     */
    public CollapsibleSectionPanel () {
        initComponents();
        setColors();
        sectionButton1.addFocusListener(this);
    }

    private void setColors() {
        Color c = getHeaderBackground();
        headerPanel.setBackground(c);
        setForeground(headerPanel);
    }

    private void setForeground(Container container) {
        for (Component cmp : container.getComponents()) {
            Color c = sectionButton1.getForeground();
            if(cmp instanceof LinkButton) {
                LinkButton lb = (LinkButton)cmp;
                if (sectionButton1.isFocusOwner()) {
                    lb.setAlternativeLinkColor(c);
                } else {
                    lb.setAlternativeLinkColor(null);                    
                }
                lb.setForeground(lb.getLinkColor());
            } else if(cmp instanceof JButton) {
                ((JButton) cmp).setForeground(c);
            } else if(cmp instanceof JLabel) {
                ((JLabel) cmp).setForeground(c);
            } else if(cmp instanceof Container) {
                setForeground((Container) cmp);
            }
        }        
    }

    public void setLabel (String label) {
        sectionButton1.setText(label);
    }

    public String getLabel () {
        return sectionButton1.getText();
    }

    public void setContent (JComponent content) {
        ((GroupLayout) getLayout()).replace(this.content, content);
        this.content = content;
    }

    public AbstractButton getLabelComponent () {
        return sectionButton1;
    }

    public boolean isExpanded () {
        return sectionButton1.isSelected();
    }

    public void setExpanded (boolean expanded) {
        sectionButton1.setSelected(expanded);
        sectionButtonStateChanged();
    }

    public void setActions (Action[] sectionActions) {
        ActionsBuilder builder = new ActionsBuilder(actionsPanel, this);
        for (Action action : sectionActions) {
            builder.addAction(action);
        }
        builder.finish();
    }

    @Override
    public void focusGained (FocusEvent e) {
        focusEvent(e);
    }

    @Override
    public void focusLost (FocusEvent e) {
        focusEvent(e);
    }

    private Color getHeaderBackground () {
        if (sectionButton1.isFocusOwner()) {
            if (isGTK || isNimbus) {
                return UIManager.getColor("Tree.selectionBackground"); //NOI18N
            }
            return UIManager.getColor("PropSheet.selectedSetBackground"); //NOI18N
        } else {
            if (isAqua) {
                Color defBk = UIManager.getColor("NbExplorerView.background");
                if (null == defBk) {
                    defBk = Color.gray;
                }
                return new Color(defBk.getRed() - 10, defBk.getGreen() - 10, defBk.getBlue() - 10);
            }
            Color bkColor;
            if (isGTK || isNimbus) {
                bkColor = new Color(UIManager.getColor("Menu.background").getRGB());//NOI18N
            } else {
                bkColor = UIManager.getColor("PropSheet.setBackground"); //NOI18N
            }
            // hack for high-contrast black
            Color c = sectionButton1.getForeground();
            if (c != null && (c.getRed() >= 240 || c.getGreen() >= 240 || c.getBlue() >= 240)
                    && bkColor != null && (bkColor.getRed() >= 192 || bkColor.getGreen() >= 192 || bkColor.getBlue() >= 192)) {
                bkColor = Color.darkGray;
            }
            return bkColor;
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

        headerPanel = new javax.swing.JPanel();
        actionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        javax.swing.GroupLayout dummyContentPanelLayout = new javax.swing.GroupLayout(dummyContentPanel);
        dummyContentPanel.setLayout(dummyContentPanelLayout);
        dummyContentPanelLayout.setHorizontalGroup(
            dummyContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 388, Short.MAX_VALUE)
        );
        dummyContentPanelLayout.setVerticalGroup(
            dummyContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        headerPanel.setBackground(getHeaderBackground());

        sectionButton1.setSelected(true);
        sectionButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sectionButton1ActionPerformed(evt);
            }
        });

        actionsPanel.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, " "); // NOI18N

        javax.swing.GroupLayout actionsPanelLayout = new javax.swing.GroupLayout(actionsPanel);
        actionsPanel.setLayout(actionsPanelLayout);
        actionsPanelLayout.setHorizontalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1)
        );
        actionsPanelLayout.setVerticalGroup(
            actionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionsPanelLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel1))
        );

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sectionButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(actionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(actionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sectionButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dummyContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(dummyContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sectionButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sectionButton1ActionPerformed
        sectionButtonStateChanged();
    }//GEN-LAST:event_sectionButton1ActionPerformed

    private void sectionButtonStateChanged () {
        content.setVisible(sectionButton1.isSelected());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private final javax.swing.JPanel dummyContentPanel = new javax.swing.JPanel();
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private final org.netbeans.modules.bugtracking.commons.TransparentSectionButton sectionButton1 = new org.netbeans.modules.bugtracking.commons.TransparentSectionButton();
    // End of variables declaration//GEN-END:variables
    private JComponent content = dummyContentPanel;

    private void focusEvent (FocusEvent e) {
        if (sectionButton1 == e.getSource()) {
            setColors();
        }
    }

    private static class ActionsBuilder {
        private final GroupLayout.SequentialGroup horizontalSeqGroup;
        private final GroupLayout.ParallelGroup verticalParallelGroup;
        private boolean notEmpty = false;
        private final FocusListener focusListener;

        public ActionsBuilder (JPanel panel, FocusListener listener) {
            this.focusListener = listener;
            panel.removeAll();
            GroupLayout layout = (GroupLayout) panel.getLayout();
            horizontalSeqGroup = layout.createSequentialGroup();
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(horizontalSeqGroup)
            );
            verticalParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(verticalParallelGroup)
            );
        }

        private void addAction (Action action) {
            String name = (String) action.getValue(Action.NAME);
            LinkButton btn = new LinkButton(name);
            btn.addActionListener(action);
            btn.addFocusListener(focusListener);
            
            if (notEmpty) {
                JLabel separator = new javax.swing.JLabel();
                separator.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 0, 2, 0),
                        BorderFactory.createLineBorder(Color.BLACK, 1)
                ));
                horizontalSeqGroup
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(separator)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                verticalParallelGroup
                    .addComponent(separator, GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
            }
            
            horizontalSeqGroup
                    .addComponent(btn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            verticalParallelGroup
                    .addComponent(btn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            notEmpty = true;
        }

        private void finish () {
            horizontalSeqGroup.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        }
    }

}
