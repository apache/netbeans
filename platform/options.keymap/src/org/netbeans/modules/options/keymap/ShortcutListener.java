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
package org.netbeans.modules.options.keymap;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * KeyListener trasforming keystrokes to human-readable and displaying them
 * inside given textfield
 * @author Max Sauer
 */
public class ShortcutListener implements KeyListener {

    private JTextField textField;
    private boolean enterConfirms;

    /**
     * Creates new instance
     * @param enterConfirms whether ENTER keystroke should be taken as
     * confirmation or displayed in the same way as other shortcuts
     */
    public ShortcutListener(boolean enterConfirms) {
//        this.textField = textField;
        this.enterConfirms = enterConfirms;
    }

    private KeyStroke backspaceKS = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
    private KeyStroke enterKS = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private String key = ""; //NOI18N

    /**
     * Clears cached shortcut text representation
     */
    public void clear() {
        key = "";
    }

    public void keyTyped(KeyEvent e) {
        e.consume();
    }
    
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
                int ecode = (int)(Integer)keyEvent_getExtendedKeyCode.invoke(e);
                if (ecode != KeyEvent.VK_UNDEFINED) {
                    code = ecode;
                }
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

    public void keyPressed(KeyEvent e) {
        assert (e.getSource() instanceof JTextField);

        if(((e.getModifiers() & (KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.META_MASK)) == 0) &&
                (e.getKeyCode() == KeyEvent.VK_DOWN || 
                e.getKeyCode() == KeyEvent.VK_UP ||
                e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            return ;
        }
        
        textField = (JTextField) e.getSource();
        KeyStroke keyStroke = createKeyStroke(e);

        boolean add = e.getKeyCode() != KeyEvent.VK_SHIFT &&
                e.getKeyCode() != KeyEvent.VK_CONTROL &&
                e.getKeyCode() != KeyEvent.VK_ALT &&
                e.getKeyCode() != KeyEvent.VK_META &&
                e.getKeyCode() != KeyEvent.VK_ALT_GRAPH;

        if (!(enterConfirms && keyStroke.equals(enterKS))) {
            if (keyStroke.equals(backspaceKS) && !key.equals("")) {
                // delete last key
                int i = key.lastIndexOf(' '); //NOI18N
                if (i < 0) {
                    key = ""; //NOI18N
                } else {
                    key = key.substring(0, i);
                }
                textField.setText(key);
            } else {
                // add key
                addKeyStroke(keyStroke, add);
            }

            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    private void addKeyStroke(KeyStroke keyStroke, boolean add) {
        String s = Utilities.keyToString(keyStroke, true);
        KeyStroke mappedStroke = Utilities.stringToKey(s);
        if (!keyStroke.equals(mappedStroke)) {
            return;
        }
        String k = KeyStrokeUtils.getKeyStrokeAsText(keyStroke);
        // check if the text can be mapped back
        if (key.equals("")) { //NOI18N
            textField.setText(k);
            if (add)
                key = k;
        } else {
            textField.setText(key + " " + k); //NOI18N
            if (add)
                key += " " + k; //NOI18N
        }
    }
}
