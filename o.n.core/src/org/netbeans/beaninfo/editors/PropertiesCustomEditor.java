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

package org.netbeans.beaninfo.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
            v.load(new ByteArrayInputStream(editorPane.getText().getBytes("ISO-8859-1")));
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
