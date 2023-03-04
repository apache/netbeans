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

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * Property editor for String class. Not used directly, but wrapped in
 * StringEditor from editors2 package, allowing to create resource values.
 * 
 * @author Tomas Pavek
 */
public class StringEditor extends PropertyEditorSupport
        implements FormAwareEditor, ExPropertyEditor, DocumentListener, Runnable
{
    private boolean editable = true;   
    private Component customEditor;
    private JTextComponent textComp;
    private boolean htmlText;

    private boolean valueUpdateInvoked;

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        if (!valueUpdateInvoked && textComp != null && textComp.isShowing())
            setValueToCustomEditor();
    }

    @Override
    public void setAsText(String text) {
        setValue(text);
    }

    @Override
    public String getJavaInitializationString () {
        String s = (String) getValue();
        return "\"" + FormUtils.escapeCharactersInString(s) + "\""; // NOI18N
    }

    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    @Override
    public Component getCustomEditor () {
        if (customEditor == null) {
            JTextArea textArea = new JTextArea();
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setColumns(60);
            textArea.setRows(8);
            textArea.getDocument().addDocumentListener(this);
            textArea.getAccessibleContext().setAccessibleName(
                    NbBundle.getBundle(StringEditor.class).getString("ACSN_StringEditorTextArea")); //NOI18N
            textArea.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getBundle(StringEditor.class).getString("ACSD_StringEditorTextArea")); //NOI18N

            JScrollPane scroll = new JScrollPane();
            scroll.setViewportView(textArea);

            JLabel htmlTipLabel = new JLabel(NbBundle.getMessage(StringEditor.class, "StringEditor.htmlTipLabel.text")); // NOI18N

            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutoCreateGaps(true);
            panel.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(scroll)
                        .addComponent(htmlTipLabel))
                    .addContainerGap());
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addContainerGap().addComponent(scroll).addComponent(htmlTipLabel));

            customEditor = panel;
            textComp = textArea;
            htmlTipLabel.setVisible(htmlText);
        }

        textComp.setEditable(editable);
        setValueToCustomEditor();

        return customEditor;
    }

    // FormAwareEditor
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        htmlText = FormUtils.isHTMLTextProperty(property);
    }

    // FormAwareEditor
    @Override
    public void updateFormVersionLevel() {
    }

    // ExPropertyEditor
    @Override
    public void attachEnv(PropertyEnv env) {        
        FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
            if (textComp != null)
                textComp.setEditable(editable);
        }
    }

    private void setValueToCustomEditor() {
        valueUpdateInvoked = true;
        textComp.setText(getAsText());
        valueUpdateInvoked = false;
    }

    // DocumentListener
    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!valueUpdateInvoked) {
            valueUpdateInvoked = true;
            EventQueue.invokeLater(this);
        }
    }

    // DocumentListener
    @Override
    public void removeUpdate(DocumentEvent e) {
        if (!valueUpdateInvoked) {
            valueUpdateInvoked = true;
            EventQueue.invokeLater(this);
        }
    }

    // DocumentListener
    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    // updates value from the custom editor
    @Override
    public void run() {
        if (textComp != null)
            setValue(textComp.getText());
        valueUpdateInvoked = false;
    }

}
