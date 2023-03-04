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
import javax.swing.*;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.editors.StringArrayCustomEditor;
import org.netbeans.modules.form.editors.StringArrayEditor;
import org.openide.util.NbBundle;

/** A simple property editor for ComboBoxModel.
 *
 * @author Tomas Pavek
 */

public class ComboBoxModelEditor extends StringArrayEditor 
       implements NamedPropertyEditor, FormAwareEditor {

    private ComboBoxModel comboModel;
    private RADProperty property;

    @Override
    public void setValue(Object val) {
        if (val instanceof ComboBoxModel) {
            comboModel = (ComboBoxModel) val;
            super.setValue(getDataFromModel(comboModel));
        }
        else if (val instanceof String[]) {
            comboModel = getModelForData((String[])val);
            super.setValue(val);
        }
        else {
            comboModel = getModelForData(new String[0]);
            super.setValue(null);
        }
    }

    @Override
    public Object getValue() {
        return comboModel;
    }

    @Override
    public void setStringArray(String[] value) {
        comboModel = getModelForData(value);
        super.setValue(value);
    }

    @Override
    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    @Override
    public String getJavaInitializationString() {
        if (getStrings(true).equals("")) { // NOI18N
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("new javax.swing.DefaultComboBoxModel"); // NOI18N
        buf.append(FormUtils.getTypeParametersCode(property, false));
        buf.append("(new String[] { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" })"); // NOI18N

        return buf.toString();
    }

    static String[] getDataFromModel(ComboBoxModel model) {
        return ListModelEditor.getDataFromModel(model);
    }

    static ComboBoxModel getModelForData(String[] data) {
        return new DefaultComboBoxModel(data);
    }
    
    /**
     * Returns human-readable name of this property editor.
     * 
     * @return human-readable name of this property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(ComboBoxModelEditor.class).getString("CTL_ComboBoxModelEditor_DisplayName"); // NOI18N
    }

    @Override
    public Component getCustomEditor() {
        return new StringArrayCustomEditor(
                this,
                NbBundle.getMessage(
                    ComboBoxModelEditor.class, 
                    "ComboBoxModelEditor.label.text")
                ); // NOI18N
    }

    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        if (property instanceof RADProperty) {
            this.property = (RADProperty) property;
        }
    }

    @Override
    public void updateFormVersionLevel() {
    }
}
