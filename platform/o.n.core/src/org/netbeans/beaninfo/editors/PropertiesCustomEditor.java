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

package org.netbeans.beaninfo.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A custom editor for Properties.
 */
public class PropertiesCustomEditor extends JPanel implements DocumentListener {

    private PropertiesEditor editor;
    private JEditorPane editorPane;
    private JTextField warnings;
    
    public PropertiesCustomEditor(PropertiesEditor ed) {
        editor = ed;
        initComponents ();
        Properties props = (Properties) editor.getValue ();
        if (props == null) props = new Properties ();
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        try {
            props.store (baos, ""); // NOI18N
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        try {
            // Remove all comments from text.
            editorPane.setText(baos.toString("ISO-8859-1").replaceAll("(?m)^#.*" + System.getProperty("line.separator"), "")); // NOI18N
        } catch (UnsupportedEncodingException x) {
            throw new AssertionError(x);
        }
        HelpCtx.setHelpIDString (this, PropertiesCustomEditor.class.getName ());
        
        editorPane.getAccessibleContext().setAccessibleName(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACS_PropertiesEditorPane"));
        editorPane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACSD_PropertiesEditorPane"));
        editorPane.getDocument().addDocumentListener(this);
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACSD_CustomPropertiesEditor"));
    }
    
    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {}

    private void change() {
        Properties v = new Properties();
        boolean loaded = false;
        try {
            v.load(new ByteArrayInputStream(editorPane.getText().getBytes(StandardCharsets.ISO_8859_1)));
            loaded = true;
        } catch (Exception x) { // IOException, IllegalArgumentException, maybe others
            Color c = UIManager.getColor("nb.errorForeground"); // NOI18N
            if (c != null) {
                warnings.setForeground(c);
            }
            warnings.setText(x.toString());
        }
        if (loaded) {
            editor.setValue(v);
            if (Pattern.compile("^#", Pattern.MULTILINE).matcher(editorPane.getText()).find()) { // #20996
                Color c = UIManager.getColor("nb.warningForeground"); // NOI18N
                if (c != null) {
                    warnings.setForeground(c);
                }
                warnings.setText(NbBundle.getMessage(PropertiesCustomEditor.class, "WARN_PropertiesComments"));
            } else {
                warnings.setText(null);
            }
        }
    }

    public @Override Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        editorPane = new JEditorPane();
        editorPane.setContentType("text/x-properties"); // NOI18N
        // XXX pretty arbitrary! No way to set by rows & columns??
        editorPane.setPreferredSize(new Dimension(200, 100));
        add(new JScrollPane(editorPane), BorderLayout.CENTER);

        warnings = new JTextField(30);
        warnings.setEditable(false);
        add(warnings, BorderLayout.SOUTH);
    }
}
