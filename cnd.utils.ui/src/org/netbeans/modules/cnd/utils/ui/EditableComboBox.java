/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
