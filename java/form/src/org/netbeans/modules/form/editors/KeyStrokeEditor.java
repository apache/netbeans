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

package org.netbeans.modules.form.editors;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;

import org.netbeans.modules.form.NamedPropertyEditor;

import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Property editor for KeyStroke.
 * 
 * @author Jan Stola
 */
public class KeyStrokeEditor extends PropertyEditorSupport
        implements XMLPropertyEditor, NamedPropertyEditor {
    private String TXT_CTRL;
    private String TXT_ALT;
    private String TXT_SHIFT;
    private String TXT_META;

    public KeyStrokeEditor() {
        ResourceBundle bundle = org.openide.util.NbBundle.getBundle(KeyStrokeEditor.class);
        TXT_CTRL = bundle.getString("CTL_CtrlAsText"); // NOI18N
        TXT_SHIFT = bundle.getString("CTL_ShiftAsText"); // NOI18N
        TXT_ALT = bundle.getString("CTL_AltAsText"); // NOI18N
        TXT_META = bundle.getString("CTL_MetaAsText"); // NOI18N
    }

    @Override
    public String getJavaInitializationString() {
        KeyStroke key =(KeyStroke) getValue();
        int mods = key.getModifiers();
        StringBuilder modsText = new StringBuilder();

        if (0 !=(mods
                 &(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK))) {
            if (0 !=(mods & InputEvent.ALT_DOWN_MASK))
                modsText.append("java.awt.event.InputEvent.ALT_DOWN_MASK"); // NOI18N
            if (0 !=(mods & InputEvent.SHIFT_DOWN_MASK)) {
                if (modsText.length() > 0)
                    modsText.append(" | "); // NOI18N
                modsText.append("java.awt.event.InputEvent.SHIFT_DOWN_MASK"); // NOI18N
            }
            if (0 !=(mods & InputEvent.CTRL_DOWN_MASK)) {
                if (modsText.length() > 0)
                    modsText.append(" | "); // NOI18N
                modsText.append("java.awt.event.InputEvent.CTRL_DOWN_MASK"); // NOI18N
            }
            if (0 !=(mods & InputEvent.META_DOWN_MASK)) {
                if (modsText.length() > 0)
                    modsText.append(" | "); // NOI18N
                modsText.append("java.awt.event.InputEvent.META_DOWN_MASK"); // NOI18N
            }
        }
        else
            modsText.append("0"); // NOI18N

        return "javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent." // NOI18N
            + getVirtualkeyName(key.getKeyCode()) + ", " + modsText.toString() + ")"; // NOI18N
    }

    @Override
    public String getAsText() {
        KeyStroke key = (KeyStroke) getValue();
        return key != null ? keyStrokeAsString(key, true) : "null"; // NOI18N
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || "".equals(text) || "null".equals(text)) { // NOI18N
            setValue(null);
            return;
        }

        KeyStroke key = keyStrokeFromString(text, true);
        if (key == null) {
            throw new IllegalArgumentException("Unrecognized key: " + text); // NOI18N
        } else {
            setValue(key);
        }
    }

    private static String getVirtualkeyName(int keycode) {
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int modifiers = f.getModifiers();
            if (Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && Modifier.isFinal(modifiers)
                && f.getType() == Integer.TYPE
                && f.getName().startsWith("VK_")) { // NOI18N
                try {
                    if (f.getInt(KeyEvent.class) == keycode) {
                        return f.getName();
                    }
                }
                catch (IllegalAccessException ex) {
                    ex.printStackTrace(); // should not happen
                }
            }
        }
        return null;
    }

    private KeyStroke keyStrokeFromString(String s, boolean i18ned) {
        StringTokenizer st = new StringTokenizer(s, "+"); // NOI18N
        String token;
        int mods = 0;
        int keycode = 0;

        String alt = i18ned ? TXT_ALT : "Alt"; // NOI18N
        String shift = i18ned ? TXT_SHIFT : "Shift"; // NOI18N
        String ctrl = i18ned ? TXT_CTRL : "Ctrl"; // NOI18N
        String meta = i18ned ? TXT_META : "Meta"; // NOI18N

        while (st.hasMoreTokens() &&(token = st.nextToken()) != null) {
            if (alt.equalsIgnoreCase(token)) {
                mods |= InputEvent.ALT_DOWN_MASK;
            } else if (shift.equalsIgnoreCase(token)) {
                mods |= InputEvent.SHIFT_DOWN_MASK;
            } else if (ctrl.equalsIgnoreCase(token)) {
                mods |= InputEvent.CTRL_DOWN_MASK;
            } else if (meta.equalsIgnoreCase(token)) {
                mods |= InputEvent.META_DOWN_MASK;
            } else {
                String keycodeName = "VK_" + token.toUpperCase(); // NOI18N
                try {
                    keycode = KeyEvent.class.getField(keycodeName).getInt(KeyEvent.class);
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }
        if (keycode != 0) {
            return KeyStroke.getKeyStroke(keycode, mods);
        } else {
            return null;
        }
    }

    private String keyStrokeAsString(KeyStroke key, boolean i18ned) {
        String alt = i18ned ? TXT_ALT : "Alt"; // NOI18N
        String shift = i18ned ? TXT_SHIFT : "Shift"; // NOI18N
        String ctrl = i18ned ? TXT_CTRL : "Ctrl"; // NOI18N
        String meta = i18ned ? TXT_META : "Meta"; //NOI18N

        StringBuilder buf = new StringBuilder();
        int mods = key.getModifiers();
        int modMasks[] = { InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
                           InputEvent.ALT_DOWN_MASK, InputEvent.META_DOWN_MASK };
        String modMaskStrings[] = { shift, ctrl, alt, meta};

        for (int i = 0; i < modMasks.length; i++) {
            if ((mods & modMasks[i]) != 0) {
                buf.append(modMaskStrings[i]);
                buf.append("+"); // NOI18N
            }
        }
        String keyName = getVirtualkeyName(key.getKeyCode());
        if (keyName != null) {
            buf.append(keyName.substring(3));
        }
        return buf.toString();
    }

    //
    // XMLPropertyEditor
    //

    public static final String XML_KEYSTROKE = "KeyStroke"; // NOI18N
    public static final String ATTR_KEY = "key"; // NOI18N

    @Override
    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_KEYSTROKE.equals(element.getNodeName())) {
            throw new java.io.IOException();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            String value = attributes.getNamedItem(ATTR_KEY).getNodeValue();
            KeyStroke key = keyStrokeFromString(value, false);
            if (key == null)
                throw new IllegalArgumentException();
            else
                setValue(key);
        }
        catch (Exception e) {
            throw new java.io.IOException();
        }
    }

    @Override
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        KeyStroke key = (KeyStroke) getValue();
        String str = key != null ? keyStrokeAsString(key, false) : "null"; // NOI18N

        org.w3c.dom.Element el = doc.createElement(XML_KEYSTROKE);
        el.setAttribute(ATTR_KEY, str);
        return el;
    }

    //
    // custom editor
    //

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public java.awt.Component getCustomEditor() {
        return new CustomEditor();
    }
    
    // NamedPropertyEditor implementation
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString("CTL_KeyStrokeEditor_DisplayName"); // NOI18N
    }

    private static String[] _virtualKeys;

    private class CustomEditor extends JPanel
    {
        private KeyGrabberField _keyGrabber;
        private JCheckBox _ctrl, _alt, _shift, _meta;
        private JComboBox _virtualKey;

        CustomEditor() {
            setLayout(new GridBagLayout());
            
            ResourceBundle bundle =
                org.openide.util.NbBundle.getBundle(KeyStrokeEditor.class);

            JLabel virtualKeyLabel = new JLabel();
            Mnemonics.setLocalizedText(virtualKeyLabel, bundle.getString("CTL_VirtualKey")); // NOI18N

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.insets = new Insets(12, 12, 5, 12);
            add(virtualKeyLabel, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 0;
            gbc.insets = new Insets(12, 0, 5, 11);
            add(_virtualKey = new JComboBox(), gbc);
            _virtualKey.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_VirtualKey")); // NOI18N

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            _ctrl = new JCheckBox();
            Mnemonics.setLocalizedText(_ctrl, bundle.getString("CTL_Ctrl")); // NOI18N
            _ctrl.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_CtrlKey")); // NOI18N
            panel.add(_ctrl);
            _alt = new JCheckBox();
            Mnemonics.setLocalizedText(_alt, bundle.getString("CTL_Alt")); // NOI18N
            _alt.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_AltKey")); // NOI18N
            panel.add(_alt);
            _shift = new JCheckBox();
            Mnemonics.setLocalizedText(_shift, bundle.getString("CTL_Shift")); // NOI18N
            _shift.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_ShiftKey")); // NOI18N
            panel.add(_shift);
            _meta = new JCheckBox();
            Mnemonics.setLocalizedText(_meta, bundle.getString("CTL_Meta")); // NOI18N
            _meta.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_MetaKey")); // NOI18N
            panel.add(_meta);
            virtualKeyLabel.setLabelFor(_virtualKey);
            
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.insets = new Insets(12, 0, 5, 12);
            add(panel, gbc);
            
            JLabel keyStrokeLabel = new JLabel();
            Mnemonics.setLocalizedText(keyStrokeLabel, bundle.getString("CTL_KeyStroke")); // NOI18N

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.insets = new Insets(0, 12, 0, 12);
            add(keyStrokeLabel, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 0;
            gbc.insets = new Insets(0, 0, 0, 11);
            add(_keyGrabber = new KeyGrabberField(), gbc);
            _keyGrabber.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_KeyStroke")); // NOI18N
            keyStrokeLabel.setLabelFor(_keyGrabber);

            _keyGrabber.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    setAsText(_keyGrabber.getText());
                }
            });
            
            getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_KeyStrokeCustomEditor")); // NOI18N

            // fill in virtual key list

            if (_virtualKeys == null) {
                java.util.List<String> list = new ArrayList<String>();

                Field[] fields = KeyEvent.class.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    int modifiers = f.getModifiers();
                    if (Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers)
                        && f.getType() == Integer.TYPE
                        && f.getName().startsWith("VK_")) { // NOI18N
                        list.add(f.getName());
                    }
                }
                _virtualKeys = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    _virtualKeys[i] = list.get(i);
                }
            }
            _virtualKey.addItem(""); // NOI18N
            for (int i = 0; i < _virtualKeys.length; i++)
                _virtualKey.addItem(_virtualKeys[i]);

            KeyStroke key =(KeyStroke) getValue();
            if (key != null)
                setKeyStroke(key);

            // listeners

            ItemListener il = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    virtualKeyChanged();
                }
            };
            _virtualKey.addItemListener(il);
            _ctrl.addItemListener(il);
            _alt.addItemListener(il);
            _shift.addItemListener(il);
            _meta.addItemListener(il);
        }

        java.awt.Component getKeyGrabber() {
            return _keyGrabber;
        }

        private void setKeyStroke(KeyStroke key) {
            _ctrl.setSelected(0 !=(InputEvent.CTRL_DOWN_MASK & key.getModifiers()));
            _alt.setSelected(0 !=(InputEvent.ALT_DOWN_MASK & key.getModifiers()));
            _shift.setSelected(0 !=(InputEvent.SHIFT_DOWN_MASK & key.getModifiers()));
            _meta.setSelected(0 !=(InputEvent.META_DOWN_MASK & key.getModifiers()));

            int keycode = key.getKeyCode();
            String keyName = getVirtualkeyName(keycode);
            if (keyName != null) {
                _virtualKey.setSelectedItem(keyName);
                _keyGrabber.setText(getAsText());
            }
        }

        private void virtualKeyChanged() {
            String keyName =(String) _virtualKey.getSelectedItem();
            if ("".equals(keyName)) { // NOI18N
                _keyGrabber.setText(""); // NOI18N
                setValue(null);
                return;
            }

            try {
                Field f = KeyEvent.class.getDeclaredField(keyName);
                int keycode = f.getInt(KeyEvent.class);
                int mods = 0;
                if (_ctrl.isSelected())
                    mods |= InputEvent.CTRL_DOWN_MASK;
                if (_shift.isSelected())
                    mods |= InputEvent.SHIFT_DOWN_MASK;
                if (_alt.isSelected())
                    mods |= InputEvent.ALT_DOWN_MASK;
                if (_meta.isSelected())
                    mods |= InputEvent.META_DOWN_MASK;

                setValue(KeyStroke.getKeyStroke(keycode, mods));
                _keyGrabber.setText(getAsText());
            }
            catch (NoSuchFieldException ex) {
                ex.printStackTrace(); // should not happen
            }
            catch (IllegalAccessException ex) {
                ex.printStackTrace(); // should not happen
            }
        }

        private class KeyGrabberField extends JTextField {
            @Override
            protected void processKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB)
                    super.processKeyEvent(e);
                else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    int keycode = e.getKeyCode();
                    if (keycode != KeyEvent.VK_CONTROL
                        && keycode != KeyEvent.VK_ALT
                        && keycode != KeyEvent.VK_SHIFT
                        && keycode != KeyEvent.VK_META) {
                        KeyStroke key = createKeyStroke(e);
                        setKeyStroke(key);
                    }
                    e.consume();
                }
            }
        }
    }

    // --- see defect #217279
    // JDK-specific translation of KeyEvent to KeyStroke, method is only available
    // in JDK 1.7+
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
    // --- end defect #217279
    
}
