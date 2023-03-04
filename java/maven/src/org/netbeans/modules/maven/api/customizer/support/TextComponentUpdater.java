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

package org.netbeans.modules.maven.api.customizer.support;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import static org.netbeans.modules.maven.api.customizer.support.Bundle.*;

/**
 *
 * @author mkleint
 */
public abstract class TextComponentUpdater implements DocumentListener, AncestorListener {
    
    private JTextComponent component;
    private JLabel label;
    
    private boolean inherited = false;
    
    /** Creates a new instance of TextComponentUpdater */
    public TextComponentUpdater(JTextComponent comp, JLabel label) {
        component = comp;
        component.addAncestorListener(this);
        this.label = label;
    }
    
    public abstract String getValue();
    public abstract String getDefaultValue();
    public abstract void setValue(String value);

    private void setModelValue() {
        if (inherited) {
            inherited = false;
//            component.setBackground(DEFAULT);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            component.setToolTipText(null);
        }
        setValue(component.getText().trim().length() == 0 ? null : component.getText());
        if (component.getText().trim().length() == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    component.getDocument().removeDocumentListener(TextComponentUpdater.this);
                    setTextFieldValue(getValue(), getDefaultValue(), component);
                    component.getDocument().addDocumentListener(TextComponentUpdater.this);
                }
            });
        }
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        setModelValue();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        setModelValue();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        setModelValue();
    }
    

    @Override
    public void ancestorAdded(AncestorEvent event) {
        setTextFieldValue(getValue(), getDefaultValue(), component);
        component.getDocument().addDocumentListener(this);
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        component.getDocument().removeDocumentListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }
    
    private void setTextFieldValue(String value, String projectValue, JTextComponent field) {
        if (value != null) {
            field.setText(value);
            component.setToolTipText(null);
            inherited = false;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else if (projectValue != null) {
            field.setText(projectValue);
            field.setSelectionEnd(projectValue.length());
            field.setSelectionStart(0);
//            field.setBackground(INHERITED);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            component.setToolTipText(MSG_Value_Inherited());
            inherited = true;
        } else {
            field.setText("");//NOI18N
            component.setToolTipText(null);
            inherited = false;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
    }
    
}
