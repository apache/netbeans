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

package org.netbeans.modules.form.editors2;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.ResourceSupport;
import org.netbeans.modules.form.ResourceWrapperEditor;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Property editor wrapping StringEditor in a resource editor, allowing to
 * produce resource values (ResourceValue) from strings.
 * 
 * @author Tomas Pavek
 */
public class StringEditor extends ResourceWrapperEditor implements NamedPropertyEditor {

    private JCheckBox noI18nCheckbox;

    public StringEditor() {
        super(new org.netbeans.modules.form.editors.StringEditor());
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(StringEditor.class, "StringEditor_DisplayName"); // NOI18N
    }

    @Override
    public String getJavaInitializationString() {
        String javaStr = super.getJavaInitializationString();
        if (getValue() instanceof String
            && ResourceSupport.isResourceableProperty(property)
            && ResourceSupport.isExcludedProperty(property))
        {   // intentionally excluded from resourcing/internationalization - add NOI18N comment
            javaStr = "*/\n\\1NOI18N*/\n\\0" + javaStr; // NOI18N
            // */\n\\1 is a special code mark for line comment
            // */\n\\0 is a special code mark to indicate that a real code follows
        }
        return javaStr;
    }

    @Override
    public Component getCustomEditor() {
        Component customEd = super.getCustomEditor();
        if (noI18nCheckbox != null) {
            noI18nCheckbox.setSelected(ResourceSupport.isExcludedProperty(property));
        }
        return customEd;
    }

    @Override
    protected Component createCustomEditorGUI(Component resourcePanelGUI) {
        if (resourcePanelGUI == null && ResourceSupport.isResourceableProperty(property)) {
            // not usable for full resourcing, only for internationalization
            // add a NOI18N checkbox so the user can mark the property as not to be internationalized
            Component customEd = delegateEditor.getCustomEditor();
            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            noI18nCheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(noI18nCheckbox, NbBundle.getMessage(StringEditor.class, "CTL_NOI18NCheckBox")); // NOI18N
            noI18nCheckbox.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getBundle(
                        StringEditor.class).getString("ACD_NOI18NCheckBox")); //NOI18N
            
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(customEd)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap().addComponent(noI18nCheckbox).addContainerGap()));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(customEd).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(noI18nCheckbox));
            return panel;
        }
        else {
            noI18nCheckbox = null;
            return super.createCustomEditorGUI(resourcePanelGUI);
        }
    }

    // called when OK button is pressed in the custom editor dialog
    @Override
    public void vetoableChange(PropertyChangeEvent ev) throws PropertyVetoException {
        super.vetoableChange(ev);
        if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())
            && resourcePanel == null && noI18nCheckbox  != null)
        {   // no resourcing, just internationalizing
            // mark the property excluded if the NOI18N checkbox is checked
            ResourceSupport.setExcludedProperty(property, noI18nCheckbox.isSelected());
        }
    }
}
