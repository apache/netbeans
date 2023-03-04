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

import java.io.IOException;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import org.netbeans.modules.form.FormCodeAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.netbeans.modules.form.editors.EnumEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Property editor for <code>JTable.selectionModel</code> property. It provides tags
 * that correspond to valid values of <code>JTable.setSelectionMode()</code>.
 *
 * @author Jan Stola
 */
public class JTableSelectionModelEditor extends EnumEditor implements FormCodeAwareEditor, XMLPropertyEditor {
    /** Property being edited. */
    private FormProperty property;

    /**
     * Creates new <code>JTableSelectionModelEditor</code>.
     */
    public JTableSelectionModelEditor() {
        super(new Object[] {
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_DEFAULT"), // NOI18N
            new Object(),
            "", // NOI18N
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_SINGLE_SELECTION"), // NOI18N
            createListSelectionModel(ListSelectionModel.SINGLE_SELECTION),
            "ListSelectionModel.SINGLE_SELECTION", // NOI18N
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_SINGLE_INTERVAL_SELECTION"), // NOI18N
            createListSelectionModel(ListSelectionModel.SINGLE_INTERVAL_SELECTION),
            "ListSelectionModel.SINGLE_INTERVAL_SELECTION", // NOI18N
            NbBundle.getMessage(JTableSelectionModelEditor.class, "JTableSelectionModelEditor_MULTIPLE_INTERVAL_SELECTION"), // NOI18N
            createListSelectionModel(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),
            "ListSelectionModel.MULTIPLE_INTERVAL_SELECTION", // NOI18N
        }, false);
    }

    private static ListSelectionModel createListSelectionModel(int selectionMode) {
        DefaultListSelectionModel model = new DefaultListSelectionModel();
        model.setSelectionMode(selectionMode);
        return model;
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof ListSelectionModel)) {
            value = property.getDefaultValue();
        }
        super.setValue(value);
    }

    @Override
    public String getSourceCode() {
        String code = super.getJavaInitializationString();
        if (code != null) {
            RADProperty prop = (RADProperty)this.property;
            RADComponent comp = prop.getRADComponent();
            CodeVariable var = comp.getCodeExpression().getVariable();            
            String varName = (var == null) ? null : var.getName(); // NOI18N
            varName = (varName == null) ? "" : varName + "."; // NOI18N
            code = varName + "setSelectionMode(" + code + ");\n"; // NOI18N
        }        
        return code;
    }

    /**
     * Sets context of the property editor. 
     * 
     * @param formModel form model.
     * @param property property being edited.
     */
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.property = property;
    }

    /**
     * Raise form version to 6.1 - this editor is available since NB 6.1.
     */
    @Override
    public void updateFormVersionLevel() {
        property.getPropertyContext().getFormModel()
                .raiseVersionLevel(FormModel.FormVersion.NB61, FormModel.FormVersion.NB61);
    }

    private static final String XML_TABLE_SELECTION_MODEL = "JTableSelectionModel"; // NOI18N
    private static final String ATTR_SELECTION_MODE = "selectionMode"; // NOI18N

    @Override
    public void readFromXML(Node element) throws IOException {
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        Object[] values = getEnumerationValues();
        Object value;
        Node node = attributes.getNamedItem(ATTR_SELECTION_MODE);
        int selectionMode = Integer.valueOf(node.getNodeValue()).intValue();
        switch (selectionMode) {
            case ListSelectionModel.SINGLE_SELECTION: value = values[4]; break;
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION: value = values[7]; break;
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION: value = values[10]; break;
            default: value = values[1]; break;
        }
        setValue(value);
    }

    @Override
    public Node storeToXML(Document doc) {
        Object value = getValue();
        int selectionMode = -1;
        Object[] values = getEnumerationValues();
        if (values[4].equals(value)) {
            selectionMode = ListSelectionModel.SINGLE_SELECTION;
        } else if (values[7].equals(value)) {
            selectionMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION;
        } else if (values[10].equals(value)) {
            selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        }
        org.w3c.dom.Element el = null;
        el = doc.createElement(XML_TABLE_SELECTION_MODEL);
        el.setAttribute(ATTR_SELECTION_MODE, Integer.toString(selectionMode));
        return el;
    }

}
