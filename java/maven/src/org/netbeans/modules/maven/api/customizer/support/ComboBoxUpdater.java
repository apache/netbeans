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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import static org.netbeans.modules.maven.api.customizer.support.Bundle.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public abstract class ComboBoxUpdater<T> implements ActionListener, AncestorListener {

    private JComboBox component;
    private JLabel label;
    
    private boolean inherited = false;
    
    /** Creates a new instance of TextComponentUpdater */
    @SuppressWarnings("LeakingThisInConstructor")
    public ComboBoxUpdater(JComboBox comp, JLabel label) {
        component = comp;
        component.addAncestorListener(this);
        this.label = label;
    }
    
    public abstract T getValue();
    public abstract T getDefaultValue();
    public abstract void setValue(T value);

    private void setModelValue() {
        if (inherited) {
            inherited = false;
//            component.setBackground(DEFAULT);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            
            component.setToolTipText(null);
        }
        @SuppressWarnings("unchecked")
        T val = (T)component.getSelectedItem();
        setValue(val.equals(getDefaultValue()) ? null : val);
        if (val.equals(getDefaultValue())) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    component.removeActionListener(ComboBoxUpdater.this);
                    setComboValue(getValue(), getDefaultValue(), component);
                    component.addActionListener(ComboBoxUpdater.this);
                }
            });
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        setModelValue();
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        setComboValue(getValue(), getDefaultValue(), component);
        component.addActionListener(this);
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        component.removeActionListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }
    
    @Messages("HINT_inherited=Value is inherited from parent POM.")
    private void setComboValue(T value, T projectValue, JComboBox field) {
        if (!Utilities.compareObjects(value, projectValue)) {
            field.setSelectedItem(value != null ? value : field.getModel().getElementAt(0));
            component.setToolTipText(null);
            inherited = false;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else {
            field.setSelectedItem(projectValue != null ? projectValue : field.getModel().getElementAt(0));
//            field.setBackground(INHERITED);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            component.setToolTipText(HINT_inherited());
            inherited = true;
      }
    }

}
