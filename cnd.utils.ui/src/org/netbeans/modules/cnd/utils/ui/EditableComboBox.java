/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.utils.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Editable combo box with memory on 5 previous values
 */
public class EditableComboBox extends JComboBox {

    private String storageKey;
    private Preferences prefs;
    private static final String LIST_DELIMITER = "*"; // NOI18N

    public EditableComboBox() {
        setEditable(true);
    }

    /**
     * Set preferences as storage
     * @param key
     * @param prefs
     */
    public void setStorage(String key, Preferences prefs) {
        storageKey = key;
        this.prefs = prefs;
    }

    /**
     * Read combo box state from storage
     * @param path is initial combo box value
     */
    public void read(String path) {
        List<String> list = new ArrayList<>();
        list.add(path);
        String old = null;
        if (prefs != null) {
            old = prefs.get(storageKey, ""); // NOI18N
        }
        if (old == null) {
            old = "";
        }
        StringTokenizer st = new StringTokenizer(old, LIST_DELIMITER); // NOI18N
        int history = 5;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!s.isEmpty() && !list.contains(s)) {
                list.add(s);
                history--;
                if (history == 0) {
                    break;
                }
            }
        }
        DefaultComboBoxModel rootModel = new DefaultComboBoxModel(list.toArray());
        setModel(rootModel);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 35; i++) {
            buf.append("w"); // NOI18N
        }
        setPrototypeDisplayValue(buf.toString());
    }

    /**
     * Store combo box state in the storage
     */
    public void store() {
        List<String> list = new ArrayList<>();
        String text = getTextImpl();
        if (!text.isEmpty()) {
            list.add(text);
        }
        for (int i = 0; i < getModel().getSize(); i++) {
            String s = getModel().getElementAt(i).toString();
            if (!s.isEmpty() && !list.contains(s)) {
                list.add(s);
            }
        }
        StringBuilder buf = new StringBuilder();
        for (String s : list) {
            if (buf.length() > 0) {
                buf.append(LIST_DELIMITER);
            }
            buf.append(s);
        }
        if (prefs != null) {
            prefs.put(storageKey, buf.toString()); // NOI18N
        }
    }

    /**
     * Get field text.
     * Override this method to add additional expansion possibilities.
     * 
     * @return field text
     */
    public String getText() {
        return getTextImpl();
    }

    /**
     * Get original field text.
     * 
     * @return field text
     */
    protected final String getTextImpl() {
        if (editor != null) {
            Component component = editor.getEditorComponent();
            if (component instanceof JTextField) {
                return ((JTextField) component).getText();
            }
        }
        if (getSelectedItem() != null) {
            return getSelectedItem().toString();
        }
        return null;
    }

    /**
     * Set current text
     * @param path update field
     */
    public void setText(String path) {
        setSelectedItem(path);
    }

    /**
     * Add action and document listeners
     * @param listener
     */
    public void addChangeListener(final ActionListener listener){
        //addActionListener(listener);
        Component component = editor.getEditorComponent();
        if (component instanceof JTextField) {
            ((JTextField)component).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    listener.actionPerformed(null);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    listener.actionPerformed(null);
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    listener.actionPerformed(null);
                }
            });
        }
    }
}
