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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.openide.util.NbBundle;

/**
 * Panel for choosing a <em>friend</em>.
 *
 * @author  Martin Krauskopf
 */
public class AddFriendPanel extends JPanel {
    
    static final String VALID_PROPERTY = "isPanelValid"; // NOI18N
    
    boolean valid = false;
    
    /** Creates new form AddFriendPanel */
    public AddFriendPanel(final SingleModuleProperties props) {
        initComponents();
        // helps prevents flickering
        friends.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"); // NOI18N
        Component editorComp = friends.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) {
                    checkValidity();
                }
            });
        }
        friends.setEnabled(true);
        friends.setModel(UIUtil.createComboWaitModel());
        friends.setSelectedIndex(-1);
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                final String[] friendCNBs = props.getAvailableFriends();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        DefaultComboBoxModel model = new DefaultComboBoxModel();
                        for (int i = 0; i < friendCNBs.length; i++) {
                            model.addElement(friendCNBs[i]);
                        }
                        friends.setEnabled(false);
                        String cachedItem = friends.getEditor().getItem().toString();
                        friends.setModel(model);
                        friends.getEditor().setItem(cachedItem);
                        friends.setEnabled(true);
                        friends.requestFocus();
                        checkValidity();
                        // data are loaded lets LayoutManager do its work
                        friends.setPrototypeDisplayValue(null);
                        Window w = SwingUtilities.getWindowAncestor(AddFriendPanel.this);
                        if (w != null && w.getWidth() < w.getPreferredSize().getWidth()) {
                            w.pack();
                        }
                    }
                });
            }
        });
    }
    
    private void checkValidity() {
        String cnb = getFriendCNB();
        if (cnb.length() == 0) {
            setErrorMessage(NbBundle.getMessage(AddFriendPanel.class, "MSG_FriendMayNotBeBlank"));
        } else if (!ApisupportAntUtils.isValidJavaFQN(cnb)) {
            setErrorMessage(NbBundle.getMessage(AddFriendPanel.class, "MSG_FriendIsNotValidCNB"));
        } else {
            setErrorMessage(null);
        }
    }
    
    String getFriendCNB() {
        return friends.getEditor().getItem().toString().trim();
    }
    
    private void setErrorMessage(String errMessage) {
        this.errorMessage.setText(errMessage == null ? " " : errMessage);
        boolean valid = errMessage == null;
        if (this.valid != valid) {
            this.valid = valid;
            firePropertyChange(AddFriendPanel.VALID_PROPERTY, !valid, valid);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        friendsTxt = new javax.swing.JLabel();
        friends = new javax.swing.JComboBox();
        errorMessage = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        friendsTxt.setLabelFor(friends);
        org.openide.awt.Mnemonics.setLocalizedText(friendsTxt, org.openide.util.NbBundle.getMessage(AddFriendPanel.class, "LBL_FriendModule"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(friendsTxt, gridBagConstraints);

        friends.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(friends, gridBagConstraints);

        errorMessage.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(errorMessage, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel errorMessage;
    public javax.swing.JComboBox friends;
    public javax.swing.JLabel friendsTxt;
    // End of variables declaration//GEN-END:variables
    
}
