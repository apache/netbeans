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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
