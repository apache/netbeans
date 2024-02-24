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

package org.netbeans.modules.form.editors2;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.*;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple property editor for <code>TableColumnModel</code>.
 *
 * @author Jan Stola
 */
public class TableColumnModelEditor extends PropertyEditorSupport
        implements NamedPropertyEditor, FormCodeAwareEditor, XMLPropertyEditor {

    /** Property being edited. */
    private RADProperty property;
    
    /**
     * Retruns display name of this property editor. 
     * 
     * @return diaplay name of this property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "TableColumnModelEditor"); // NOI18N
    }

    /**
     * Sets context of the property editor. 
     * 
     * @param formModel form model.
     * @param property property being edited.
     */
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.property = (RADProperty)property;
    }

    /**
     * Raise form version to 6.0 - this editor is available since NB 6.0.
     */
    @Override
    public void updateFormVersionLevel() {
        property.getPropertyContext().getFormModel()
                .raiseVersionLevel(FormModel.FormVersion.NB60, FormModel.FormVersion.NB60);
    }

    /**
     * Determines whether this property editor supports custom editing. 
     * 
     * @return <code>true</code>.
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns custom editor.
     * 
     * @return custom editor.
     */
    @Override
    public Component getCustomEditor() {
        JLabel label = new JLabel(NbBundle.getMessage(getClass(), "TableColumnModelEditor_Customizer")); // NOI18N
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
        String msg = NbBundle.getMessage(TableColumnModelEditor.class, "TableColumnModelEditor_TableColumnModel"); // NOI18N
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, rectangle.x, rectangle.y + (rectangle.height - fm.getHeight())/2 + fm.getAscent());
    }

    @Override
    public String getSourceCode() {
        RADComponent comp = property.getRADComponent();
        CodeVariable var = comp.getCodeExpression().getVariable();
        String varName = (var == null) ? null : var.getName();
        String readMethod = property.getPropertyDescriptor().getReadMethod().getName();
        String getter = readMethod + "()"; // NOI18N
        if (varName != null) {
            getter = varName + '.' + getter;
        }

        Object value = getValue();
        if (value instanceof FormTableColumnModel) {
            FormTableColumnModel columnModel = (FormTableColumnModel)value;
            StringBuilder code = new StringBuilder();

            // selection model
            int selectionModel = columnModel.getSelectionModel();
            if (selectionModel > 0) {
                code.append(getter).append(".getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel."); // NOI18N
                switch (selectionModel) {
                    case 1: code.append("SINGLE_SELECTION"); break; // NOI18N
                    case 2: code.append("SINGLE_INTERVAL_SELECTION"); break; // NOI18N
                    case 3: code.append("MULTIPLE_INTERVAL_SELECTION"); break; // NOI18N
                }
                code.append(");\n"); // NOI18N
            }

            // columns
            List<FormTableColumn> columns = columnModel.getColumns();
            String checkColumns = "if (" + getter + ".getColumnCount() > 0) {\n"; // NOI18N
            for (int i=0; i<columns.size(); i++) {
                FormTableColumn column = columns.get(i);
                String columnGetter = getter + ".getColumn(" + i + ")"; // NOI18N
                if (!column.isResizable()) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setResizable(").append(Boolean.toString(column.isResizable())).append(");\n"); // NOI18N
                }
                if (column.getMinWidth() != -1) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setMinWidth(").append(Integer.toString(column.getMinWidth())).append(");\n"); // NOI18N
                }
                if (column.getPrefWidth() != -1) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setPreferredWidth(").append(Integer.toString(column.getPrefWidth())).append(");\n"); // NOI18N
                }
                if (column.getMaxWidth() != -1) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setMaxWidth(").append(Integer.toString(column.getMaxWidth())).append(");\n"); // NOI18N
                }
                FormProperty prop = column.getTitle();
                if (prop.isChanged()) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setHeaderValue(").append(prop.getJavaInitializationString()).append(");\n"); // NOI18N
                }
                prop = column.getEditor();
                if (prop.isChanged()) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setCellEditor(").append(prop.getJavaInitializationString()).append(");\n"); // NOI18N
                }
                prop = column.getRenderer();
                if (prop.isChanged()) {
                    if (checkColumns != null) {
                        code.append(checkColumns);
                        checkColumns = null;
                    }
                    code.append(columnGetter).append(".setCellRenderer(").append(prop.getJavaInitializationString()).append(");\n"); // NOI18N
                }
            }
            if (checkColumns == null) {
                code.append("}\n"); // NOI18N
            }

            return (code.length() == 0) ? null : code.toString();
        } else {
            return null;
        }
    }

    private static final String XML_TABLE_COLUMN_MODEL = "TableColumnModel"; // NOI18N
    private static final String ATTR_SELECTION_MODEL = "selectionModel"; // NOI18N
    private static final String XML_COLUMN = "Column"; // NOI18N
    private static final String ATTR_RESIZABLE = "resizable"; // NOI18N
    private static final String ATTR_WIDTH_MIN = "minWidth"; // NOI18N
    private static final String ATTR_WIDTH_PREF = "prefWidth"; // NOI18N
    private static final String ATTR_WIDTH_MAX = "maxWidth"; // NOI18N
    private static final String XML_TITLE = "Title"; // NOI18N
    private static final String XML_EDITOR = "Editor"; // NOI18N
    private static final String XML_RENDERER = "Renderer"; // NOI18N
    private static final String ATTR_PROP_EDITOR = "editor"; // NOI18N
    private static final String ATTR_VALUE = "value"; // NOI18N
    private static final String ATTR_RESOURCE_KEY = "resourceKey"; // NOI18N
    private static final String ATTR_NO_RESOURCE = "noResource"; // NOI18N

    @Override
    public void readFromXML(Node element) throws IOException {
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        String selectionModelTxt = attributes.getNamedItem(ATTR_SELECTION_MODEL).getNodeValue();
        int selectionModel = Integer.parseInt(selectionModelTxt);
        FormTableColumnModel model = new FormTableColumnModel(property);
        model.setSelectionModel(selectionModel);

        NodeList nodes = element.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (XML_COLUMN.equals(node.getNodeName())) {
                FormTableColumn column = new FormTableColumn(property, model.getColumns().size());
                
                org.w3c.dom.NamedNodeMap colAttrs = node.getAttributes();
                String resizableTxt = colAttrs.getNamedItem(ATTR_RESIZABLE).getNodeValue();
                String minWidthTxt = colAttrs.getNamedItem(ATTR_WIDTH_MIN).getNodeValue();
                String prefWidthTxt = colAttrs.getNamedItem(ATTR_WIDTH_PREF).getNodeValue();
                String maxWidthTxt = colAttrs.getNamedItem(ATTR_WIDTH_MAX).getNodeValue();
                boolean resizable = Boolean.parseBoolean(resizableTxt);
                int minWidth = Integer.parseInt(minWidthTxt);
                int prefWidth = Integer.parseInt(prefWidthTxt);
                int maxWidth = Integer.parseInt(maxWidthTxt);
                column.setResizable(resizable);
                column.setMinWidth(minWidth);
                column.setPrefWidth(prefWidth);
                column.setMaxWidth(maxWidth);

                NodeList subNodes = node.getChildNodes();
                for (int j=0; j<subNodes.getLength(); j++) {
                    Node subNode = subNodes.item(j);
                    String nodeName = subNode.getNodeName();
                    if (XML_TITLE.equals(nodeName)) {
                        loadProperty(column.getTitle(), subNode);
                    } else if (XML_EDITOR.equals(nodeName)) {
                        loadProperty(column.getEditor(), subNode);
                    } else if (XML_RENDERER.equals(nodeName)) {
                        loadProperty(column.getRenderer(), subNode);
                    }
                }

                model.getColumns().add(column);
            }
        }
        setValue(model);
    }

    private void loadProperty(FormProperty property, Node node) {
        org.w3c.dom.NamedNodeMap attributes = node.getAttributes();
        Node valueNode = attributes.getNamedItem(ATTR_VALUE);
        Node noResourceNode = attributes.getNamedItem(ATTR_NO_RESOURCE);
        Node resourceKeyNode = attributes.getNamedItem(ATTR_RESOURCE_KEY);
        FormModel formModel = property.getPropertyContext().getFormModel();
        try {
            if (resourceKeyNode != null) {
                String resourceKey = resourceKeyNode.getNodeValue();
                Object value = ResourceSupport.findResource(formModel, resourceKey, property.getValueType());
                property.setValue(value);
            } else if (valueNode != null) {
                property.setValue(valueNode.getNodeValue());
            } else {
                NodeList nodes = node.getChildNodes();
                for (int i=0; i<nodes.getLength(); i++) {
                    Node subNode = nodes.item(i);
                    if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                        String propEdName = attributes.getNamedItem(ATTR_PROP_EDITOR).getNodeValue();
                        XMLPropertyEditor xmlPropEd = null;
                        if (propEdName.equals(RADConnectionPropertyEditor.class.getName())) {
                            xmlPropEd = new RADConnectionPropertyEditor(property.getValueType());
                            ((FormAwareEditor)xmlPropEd).setContext(formModel, property);
                        } else {
                            Class propEdClass = PersistenceObjectRegistry.loadClass(propEdName,
                                FormEditor.getFormDataObject(formModel).getFormFile());
                            Object propEd = propEdClass.getDeclaredConstructor().newInstance();
                            if (propEd instanceof XMLPropertyEditor) {
                                xmlPropEd = (XMLPropertyEditor)propEd;
                                if (propEd instanceof FormAwareEditor) {
                                    ((FormAwareEditor)propEd).setContext(formModel, property);
                                }
                            }
                        }
                        if (xmlPropEd != null) {
                            xmlPropEd.readFromXML(subNode);
                            property.setValue(new FormProperty.ValueWithEditor(xmlPropEd.getValue(), xmlPropEd));                            
                        }
                        break;
                    }
                }
            }
            if (noResourceNode != null) {
                if (Boolean.parseBoolean(noResourceNode.getNodeValue())) {
                    ResourceSupport.setExcludedProperty(property, true);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    @Override
    public Node storeToXML(Document doc) {
        Object value = getValue();
        org.w3c.dom.Element el = null;
        if (value instanceof FormTableColumnModel) {
            FormTableColumnModel model = (FormTableColumnModel)value;
            el = doc.createElement(XML_TABLE_COLUMN_MODEL);
            el.setAttribute(ATTR_SELECTION_MODEL, Integer.toString(model.getSelectionModel()));
            
            for (FormTableColumn column : model.getColumns()) {
                org.w3c.dom.Element columnEl = doc.createElement(XML_COLUMN);
                columnEl.setAttribute(ATTR_RESIZABLE, Boolean.toString(column.isResizable()));
                columnEl.setAttribute(ATTR_WIDTH_MIN, Integer.toString(column.getMinWidth()));
                columnEl.setAttribute(ATTR_WIDTH_PREF, Integer.toString(column.getPrefWidth()));
                columnEl.setAttribute(ATTR_WIDTH_MAX, Integer.toString(column.getMaxWidth()));
                
                org.w3c.dom.Element titleEl = doc.createElement(XML_TITLE);
                org.w3c.dom.Element editorEl = doc.createElement(XML_EDITOR);
                org.w3c.dom.Element rendererEl = doc.createElement(XML_RENDERER);
                storeProperty(doc, titleEl, column.getTitle());
                storeProperty(doc, editorEl, column.getEditor());
                storeProperty(doc, rendererEl, column.getRenderer());
                columnEl.appendChild(titleEl);
                columnEl.appendChild(editorEl);
                columnEl.appendChild(rendererEl);
                el.appendChild(columnEl);
            }
        }
        return el;
    }

    private void storeProperty(Document doc, org.w3c.dom.Element element, FormProperty property) {
        if (property.isChanged()) {
            PropertyEditor editor = property.getCurrentEditor();
            if (editor instanceof XMLPropertyEditor) {
                try {
                    editor.setValue(property.getValue());
                    element.setAttribute(ATTR_PROP_EDITOR, editor.getClass().getName());
                    Node node = ((XMLPropertyEditor)editor).storeToXML(doc);
                    if (node != null) {
                        element.appendChild(node);
                    }
                } catch (IllegalAccessException iaex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, iaex.getMessage(), iaex);
                } catch (InvocationTargetException itex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, itex.getMessage(), itex);
                }
            } else {
                try {
                    Object value = property.getValue();
                    String resourceKey = null;
                    if (value instanceof ResourceValue) {
                        resourceKey = ((ResourceValue)value).getKey();
                    } else {
                        if (value instanceof String) {
                            element.setAttribute(ATTR_VALUE, (String)value);
                        } else {
                            System.err.println("Unable to store " + property); // NOI18N
                        }
                    }
                    if (resourceKey != null) {
                        element.setAttribute(ATTR_RESOURCE_KEY, resourceKey);
                    }
                } catch (IllegalAccessException iaex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, iaex.getMessage(), iaex);
                } catch (InvocationTargetException itex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, itex.getMessage(), itex);
                }
            }
        }
        boolean noResource = ResourceSupport.isResourceableProperty(property)
            && ResourceSupport.isExcludedProperty(property);
        if (noResource) {
            element.setAttribute(ATTR_NO_RESOURCE, Boolean.TRUE.toString());
        }
    }

    public static class FormTableColumnModel extends FormDesignValueAdapter {
        private FormProperty property;
        private int selectionModel;
        private List<FormTableColumn> columns;

        public FormTableColumnModel(FormProperty property) {
            this.property = property;
            columns = new LinkedList<FormTableColumn>();
        }

        public void setSelectionModel(int selectionModel) {
            this.selectionModel = selectionModel;
        }

        public int getSelectionModel() {
            return selectionModel;
        }

        public List<FormTableColumn> getColumns() {
            return columns;
        }

        @Override
        public Object getDesignValue() {
            Object value = null;
            try {
                value = property.getTargetValue();
                if (value instanceof TableColumnModel) {
                    TableColumnModel columnModel = (TableColumnModel)value;
                    designValue(columnModel);
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, itex.getMessage(), itex);
            }
            return value;
        }

        @Override
        public Object getDesignValue(Object target) {
            TableColumnModel columnModel = null;
            if (target instanceof javax.swing.JTable) {
                columnModel = ((javax.swing.JTable)target).getColumnModel();
                designValue(columnModel);
            }
            return columnModel;
        }

        private void designValue(TableColumnModel columnModel) {
            int selectionModel = getSelectionModel();
            switch (selectionModel) {
                case 0: break; // not allowed - default value
                case 1: columnModel.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION); break;
                case 2: columnModel.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION); break;
                case 3: columnModel.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); break;
            }
            for (int i=0; i<columns.size() && i<columnModel.getColumnCount(); i++) {
                FormTableColumn formColumn = columns.get(i);
                TableColumn column = columnModel.getColumn(i);
                column.setResizable(formColumn.isResizable());
                int width = (formColumn.getMinWidth() == -1) ? defaultColumn().getMinWidth() : formColumn.getMinWidth();
                column.setMinWidth(width);
                width = (formColumn.getPrefWidth() == -1) ? defaultColumn().getPreferredWidth() : formColumn.getPrefWidth();
                column.setPreferredWidth(width);
                width = (formColumn.getMaxWidth() == -1) ? defaultColumn().getMaxWidth() : formColumn.getMaxWidth();
                column.setMaxWidth(width);
                FormProperty prop = formColumn.getTitle();
                try {
                    if (prop.isChanged()) {
                        Object h = prop.getRealValue();
                        if (h instanceof String) {
                            column.setHeaderValue(h);
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
                prop = formColumn.getEditor();
                try {
                    if (prop.isChanged()) {
                        Object editor = prop.getRealValue();
                        if (editor instanceof TableCellEditor) {
                            column.setCellEditor((TableCellEditor)editor);
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
                prop = formColumn.getRenderer();
                try {
                    if (prop.isChanged()) {
                        Object renderer = prop.getRealValue();
                        if (renderer instanceof TableCellRenderer) {
                            column.setCellRenderer((TableCellRenderer)renderer);
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private TableColumn defaultColumn;
        private TableColumn defaultColumn() {
            if (defaultColumn == null) {
                defaultColumn = new TableColumn();
            }
            return defaultColumn;
        }

    }

    public static class FormTableColumn {
        private int index; // just a suffix of names of sub-properties
        private int minWidth;
        private int prefWidth;
        private int maxWidth;
        private boolean resizable;
        private Property title;
        private Property editor;
        private Property renderer;

        public FormTableColumn(RADProperty prop, int index) {
            this.index = index;
            minWidth = -1;
            prefWidth = -1;
            maxWidth = -1;
            resizable = true;
            title = new Property(prop, "title"+index, String.class, null, null); // NOI18N
            editor = new Property(prop, "editor"+index, TableCellEditor.class, null, null); // NOI18N
            renderer = new Property(prop, "renderer"+index, TableCellRenderer.class, null, null); // NOI18N
        }

        public int getIndex() {
            return index;
        }

        public int getMinWidth() {
            return minWidth;
        }

        public void setMinWidth(int minWidth) {
            this.minWidth = minWidth;
        }

        public int getPrefWidth() {
            return prefWidth;
        }

        public void setPrefWidth(int prefWidth) {
            this.prefWidth = prefWidth;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public boolean isResizable() {
            return resizable;
        }

        public void setResizable(boolean resizable) {
            this.resizable = resizable;
        }

        public FormProperty getTitle() {
            return title;
        }

        public FormProperty getEditor() {
            return editor;
        }

        public FormProperty getRenderer() {
            return renderer;
        }
    }

    static class Property extends FormProperty {
        private Object value;

        Property(RADProperty prop, String name, Class type, String displayName, String description) {
            super(new FormPropertyContext.SubProperty(prop), name, type, displayName, description);
            prop.getRADComponent().setPropertyListener(this);
        }

        @Override
        public Object getTargetValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }

        @Override
        public void setTargetValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.value = value;
        }
    }

}
