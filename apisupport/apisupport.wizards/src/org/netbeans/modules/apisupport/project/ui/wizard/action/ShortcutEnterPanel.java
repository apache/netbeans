/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.action;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Radek Matous
 */
public class ShortcutEnterPanel extends javax.swing.JPanel {
    private final Listener listener = new Listener();
    private final JButton bTab;
    private final JButton bClear;
    
    
    /** Creates new form ShortcutCustomizerPanel */
    public ShortcutEnterPanel() {
        initComponents();
        bTab = new JButton();
        bClear = new JButton();
        loc(bTab, "CTL_Tab");
        loc(bClear, "CTL_Clear");
        tfShortcut.setFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                Collections.<AWTKeyStroke>emptySet()
                );
        tfShortcut.setFocusTraversalKeys(
                KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS,
                Collections.<AWTKeyStroke>emptySet()
                );
        tfShortcut.setFocusTraversalKeys(
                KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS,
                Collections.<AWTKeyStroke>emptySet()
                );
        
        tfShortcut.addKeyListener(listener);        
    }
    
    private String getTitle() {
        return loc("LBL_AddShortcutTitle");
    }
    
    private Object[] getAdditionalOptions() {
        return new Object[] {bClear, bTab};
    }
    
    private String getShortcutText() {
        return tfShortcut.getText();
    }
    
    // --- see defect #217279
    private static final Method keyEvent_getExtendedKeyCode;
    
    static {
        Class eventClass = KeyEvent.class;
        Method m = null;
        try {
            m = eventClass.getMethod("getExtendedKeyCode"); // NOI18N
        } catch (NoSuchMethodException ex) {
            // expected, JDK < 1.7
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        keyEvent_getExtendedKeyCode = m;
    }
    
    static KeyStroke createKeyStroke(KeyEvent e) {
        int code = e.getKeyCode();
        if (keyEvent_getExtendedKeyCode != null) {
            try {
                code = (int)(Integer)keyEvent_getExtendedKeyCode.invoke(e);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return KeyStroke.getKeyStroke(code, e.getModifiers());
    }
    // --- end defect #217279

    static KeyStroke[] showDialog() {        
        Object[] buttons = new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        };
        final ShortcutEnterPanel sepPanel = new ShortcutEnterPanel();
        
        DialogDescriptor descriptor = new DialogDescriptor(sepPanel,sepPanel.getTitle(),
                true,buttons,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null,sepPanel.listener);
        descriptor.setClosingOptions(new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        });
        descriptor.setAdditionalOptions(sepPanel.getAdditionalOptions());
        
        DialogDisplayer.getDefault().notify(descriptor);
        String shortcut = sepPanel.getShortcutText();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION && shortcut != null && shortcut.trim().length() > 0) {
            return WizardUtils.stringToKeyStrokes(shortcut);//NOI18N
            
        } else {
            return null;
        }
    }
    
    private static void loc(Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText(
                    (AbstractButton) c,
                    loc(key)
                    );
        else
            Mnemonics.setLocalizedText(
                    (JLabel) c,
                    loc(key)
                    );
    }
    
    private static String loc(String key) {
        return NbBundle.getMessage(ShortcutEnterPanel.class, key);
    }
    
    private class Listener implements ActionListener, KeyListener {
        
        private KeyStroke backspaceKS = KeyStroke.getKeyStroke
                (KeyEvent.VK_BACK_SPACE, 0);
        private KeyStroke tabKS = KeyStroke.getKeyStroke
                (KeyEvent.VK_TAB, 0);
        
        private String key = "";
        
        public void keyTyped(KeyEvent e) {
            e.consume();
        }
        
        public void keyPressed(KeyEvent e) {
            KeyStroke keyStroke = createKeyStroke(e);
            
            boolean add = e.getKeyCode() != e.VK_SHIFT &&
                    e.getKeyCode() != e.VK_CONTROL &&
                    e.getKeyCode() != e.VK_ALT &&
                    e.getKeyCode() != e.VK_META &&
                    e.getKeyCode() != e.VK_ALT_GRAPH;
            
            if (keyStroke.equals(backspaceKS) && !key.equals("")) {
                // delete last key
                int i = key.lastIndexOf(' ');
                if (i < 0)
                    key = "";
                else
                    key = key.substring(0, i);
                tfShortcut.setText(key);
            } else
                // add key
                addKeyStroke(keyStroke, add);
            
            e.consume();
        }
        
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bClear) {
                key = "";
                tfShortcut.setText(key);
                tfShortcut.requestFocusInWindow();
            } else if (e.getSource() == bTab) {
                addKeyStroke(tabKS, true);
                tfShortcut.requestFocusInWindow();
            }
        }
        
        
        private void addKeyStroke(KeyStroke keyStroke, boolean add) {
            String k = WizardUtils.keyStrokeToString(keyStroke);
            if (key.equals("")) {
                tfShortcut.setText(k);
                if (add) key = k;
            } else {
                tfShortcut.setText(key + " " + k);
                if (add) key += " " + k;
            }
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tfShortcut = new javax.swing.JTextField();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/action/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(tfShortcutLabel, bundle.getString("LBL_Shortcut")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tfShortcutLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfShortcut, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfShortcutLabel)
                    .addComponent(tfShortcut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField tfShortcut;
    private final javax.swing.JLabel tfShortcutLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables
    
}
