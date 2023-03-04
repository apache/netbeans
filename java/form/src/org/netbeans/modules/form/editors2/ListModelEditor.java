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

/** A simple property editor for ListModel.
 *
 * @author Tomas Pavek
 */

public class ListModelEditor extends StringArrayEditor implements NamedPropertyEditor, FormAwareEditor {

    private ListModel listModel;
    private RADProperty property;

    @Override
    public void setValue(Object val) {
        if (val instanceof ListModel) {
            listModel = (ListModel) val;
            super.setValue(getDataFromModel(listModel));
        }
        else if (val instanceof String[]) {
            listModel = getModelForData((String[])val);
            super.setValue(val);
        }
        else {
            listModel = getModelForData(new String[0]);
            super.setValue(null);
        }
    }

    @Override
    public Object getValue() {
        return listModel;
    }

    @Override
    public void setStringArray(String[] value) {
        listModel = getModelForData(value);
        super.setValue(value);
    }

    @Override
    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    @Override
    public String getJavaInitializationString() {
        if (getStrings(true).equals("")) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("new javax.swing.AbstractListModel"); // NOI18N
        buf.append(FormUtils.getTypeParametersCode(property, true));
        buf.append("() {\n"); // NOI18N
        buf.append("String[] strings = { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" };\n"); // NOI18N
        buf.append("public int getSize() { return strings.length; }\n"); // NOI18N
        buf.append("public ");
        String typeParam = property != null ? FormUtils.getTypeParameters(property.getRADComponent()) : null;
        buf.append("String".equals(typeParam) ? typeParam : "Object"); // NOI18N
        buf.append(" getElementAt(int i) { return strings[i]; }\n"); // NOI18N
        buf.append("}"); // NOI18N

        return buf.toString();
    }

    static String[] getDataFromModel(ListModel model) {
        String[] data = new String[model.getSize()];
        for (int i=0; i < data.length; i++) {
            Object obj = model.getElementAt(i);
            data[i] = obj instanceof String ? (String) obj : ""; // NOI18N
        }
        return data;
    }

    static ListModel getModelForData(String[] data) {
        DefaultListModel model = new DefaultListModel();
        for (int i=0; i < data.length; i++)
            model.addElement(data[i]);
        return model;
    }

    /**
     * Returns human-readable name of this property editor.
     * 
     * @return human-readable name of this property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(ListModelEditor.class).getString("CTL_ListModelEditor_DisplayName"); // NOI18N
    }
    
    @Override
    public Component getCustomEditor() {
        return new StringArrayCustomEditor(
                this,
                NbBundle.getMessage(
                    ListModelEditor.class,
                    "ListModelEditor.label.text")
                );  // NOI18N
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
