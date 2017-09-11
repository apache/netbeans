/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.modules.options.keymap;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.MessageFormat;
import java.util.Collections;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Jancura
 */
public class ShortcutsDialog extends javax.swing.JPanel {
    public static final String PROP_SHORTCUT_VALID = "ShortcutsDialog.PROP_SHORTCUT_VALID"; //NOI18N
    
    private Listener listener = null;
    private JButton bTab = new JButton ();
    private JButton bClear = new JButton ();
    private ShortcutsFinder f = null;
    private boolean shortcutValid = false;
    
    void init(ShortcutsFinder f) {
        this.f = f;
        loc (lShortcut, "Shortcut"); //NOI18N
        lConflict.setForeground (Color.red);
        loc (bTab, "CTL_Tab"); //NOI18N
        bTab.getAccessibleContext().setAccessibleName(loc("AN_Tab")); //NOI18N
        bTab.getAccessibleContext().setAccessibleDescription(loc("AD_Tab")); //NOI18N
        loc (bClear, "CTL_Clear"); //NOI18N
        bClear.getAccessibleContext().setAccessibleName(loc("AN_Clear")); //NOI18N
        bClear.getAccessibleContext().setAccessibleDescription(loc("AD_Clear")); //NOI18N
        tfShortcut.setFocusTraversalKeys (
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, 
            Collections.<AWTKeyStroke>emptySet()
        );
        tfShortcut.setFocusTraversalKeys (
            KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, 
            Collections.<AWTKeyStroke>emptySet()
        );
        tfShortcut.getAccessibleContext().setAccessibleName(loc("AN_Shortcut")); //NOI18N
        tfShortcut.getAccessibleContext().setAccessibleDescription(loc("AD_Shortcut")); //NOI18N
        lShortcut.setDisplayedMnemonic(loc("CTL_Shortcut_Mnemonic").charAt(0));
//        tfShortcut.setFocusTraversalKeys (
//            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, 
//            Collections.EMPTY_SET
//        );
        tfShortcut.setFocusTraversalKeys (
            KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, 
            Collections.<AWTKeyStroke>emptySet()
        );
        listener = new Listener ();
        tfShortcut.addKeyListener(listener);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ShortcutsDialog.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }
    
    /** Creates new form ShortcutsDialog1 */
    public ShortcutsDialog() {
        initComponents();
    }

    Listener getListener() {
        return listener;
    }

    public javax.swing.JLabel getLShortcut() {
        return lShortcut;
    }

    public javax.swing.JTextField getTfShortcut() {
        return tfShortcut;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lShortcut = new javax.swing.JLabel();
        tfShortcut = new javax.swing.JTextField();
        lConflict = new javax.swing.JLabel();

        lShortcut.setLabelFor(tfShortcut);
        lShortcut.setText("Shortcut:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lConflict, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lShortcut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfShortcut, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lShortcut)
                    .addComponent(tfShortcut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lConflict)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ShortcutsDialog.class, "AN_ShortcutsDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ShortcutsDialog.class, "AD_ShortcutsDialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lConflict;
    private javax.swing.JLabel lShortcut;
    private javax.swing.JTextField tfShortcut;
    // End of variables declaration//GEN-END:variables

    
        class Listener implements ActionListener, KeyListener {

            private KeyStroke backspaceKS = KeyStroke.getKeyStroke 
                (KeyEvent.VK_BACK_SPACE, 0);
            private KeyStroke tabKS = KeyStroke.getKeyStroke 
                (KeyEvent.VK_TAB, 0);
            
            private String key = ""; //NOI18N

            public void keyTyped (KeyEvent e) {
                e.consume ();
            }

            public void keyPressed (KeyEvent e) {
                KeyStroke keyStroke = ShortcutListener.createKeyStroke(e);
                
                boolean add = e.getKeyCode () != KeyEvent.VK_SHIFT &&
                              e.getKeyCode () != KeyEvent.VK_CONTROL &&
                              e.getKeyCode () != KeyEvent.VK_ALT &&
                              e.getKeyCode () != KeyEvent.VK_META &&
                              e.getKeyCode () != KeyEvent.VK_ALT_GRAPH;
                
                if (keyStroke.equals (backspaceKS) && !key.equals ("")) {
                    // delete last key
                    int i = key.lastIndexOf (' '); //NOI18N
                    if (i < 0) {
                        key = ""; //NOI18N
                    } else {
                        key = key.substring (0, i);
                    }
                    getTfShortcut().setText (key);
                } else {
                    // add key
                    addKeyStroke (keyStroke, add);
                }
                if (add) {
                    updateWarning();
                } else {
                    setShortcutValid(false);
                }
                e.consume ();
            }

            public void keyReleased (KeyEvent e) {
                e.consume ();
            }
            
            public void actionPerformed (ActionEvent e) {
                if (e.getSource () == getBClear()) {
                    key = ""; //NOI18N
                    getTfShortcut().setText (key);
                } else 
                if (e.getSource () == getBTab()) {
                    addKeyStroke (tabKS, true);
                }
                updateWarning();
            }
            
            private void updateWarning () {
                String text = getTfShortcut().getText();
                ShortcutAction action = f.findActionForShortcut(text);
                if (action != null) {
                    lConflict.setText (MessageFormat.format (
                        loc ("Shortcut_Conflict"), //NOI18N
                        new Object[] {action.getDisplayName ()}
                    ));
                    setShortcutValid(true);
                } else {
                    lConflict.setText (""); //NOI18N
                    setShortcutValid(text != null && text.length() > 0);
                }
            }
            
            private void addKeyStroke (KeyStroke keyStroke, boolean add) {
                String k = KeyStrokeUtils.getKeyStrokeAsText (keyStroke);
                if (key.equals ("")) { //NOI18N
                    getTfShortcut().setText (k);
                    if (add) key = k;
                } else {
                    getTfShortcut().setText (key + " " + k); //NOI18N
                    if (add) key += " " + k; //NOI18N
                }
            }
        }

    public JButton getBTab() {
        return bTab;
    }

    public JButton getBClear() {
        return bClear;
    }
    
    public boolean isShortcutValid() {
        return shortcutValid;
    }
    
    private void setShortcutValid(boolean valid) {
        if (valid != shortcutValid) {
            shortcutValid = valid;
            firePropertyChange(PROP_SHORTCUT_VALID, !shortcutValid, shortcutValid);
        }
    }
}
