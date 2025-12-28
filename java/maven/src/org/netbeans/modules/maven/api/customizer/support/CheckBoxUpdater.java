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
package org.netbeans.modules.maven.api.customizer.support;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import static org.netbeans.modules.maven.api.customizer.support.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
public abstract class CheckBoxUpdater implements ItemListener, AncestorListener {

    private final JCheckBox component;
    private boolean inherited = false;

    @SuppressWarnings("LeakingThisInConstructor")
    public CheckBoxUpdater(JCheckBox comp) {
        component = comp;
        component.addAncestorListener(this);
    }

    public abstract Boolean getValue();

    public abstract boolean getDefaultValue();

    public abstract void setValue(Boolean value);

    @Messages("MSG_Value_Inherited=Value is inherited from parent POM or default.")
    private void setModelValue() {
        if (inherited) {
            inherited = false;
            component.setFont(component.getFont().deriveFont(Font.BOLD));

            component.setToolTipText(null);
        } else {
            component.setToolTipText(MSG_Value_Inherited()); //NOI18N
            inherited = true;
            component.setFont(component.getFont().deriveFont(Font.PLAIN));
        }
        boolean val = component.isSelected();
        setValue(val == getDefaultValue() ? null : val);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        setModelValue();
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        setCheckBoxValue(getValue(), getDefaultValue(), component);
        component.addItemListener(this);
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        component.removeItemListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    private void setCheckBoxValue(Boolean value, boolean defValue, JCheckBox component) {
        if (value != null) {
            component.setSelected(value);
            component.setToolTipText(null);
            inherited = false;
            component.setFont(component.getFont().deriveFont(Font.BOLD));
        } else {
            component.setSelected(defValue);
            component.setToolTipText(MSG_Value_Inherited());
            inherited = true;
            component.setFont(component.getFont().deriveFont(Font.PLAIN));
        }
    }
}
