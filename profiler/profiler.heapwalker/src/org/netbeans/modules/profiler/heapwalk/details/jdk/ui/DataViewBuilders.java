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
package org.netbeans.modules.profiler.heapwalk.details.jdk.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.ComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.JComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.Utils.InstanceBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;

/**
 *
 * @author Jiri Sedlacek
 */
final class DataViewBuilders {
    
    // Make sure subclasses are listed before base class if using isSubclassOf
    static ComponentBuilder getBuilder(Instance instance, Heap heap) {
        if (DetailsUtils.isSubclassOf(instance, JComboBox.class.getName())) {
            return new JComboBoxBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JList.class.getName())) {
            return new JListBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JTree.class.getName())) {
            return new JTreeBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JTable.class.getName())) {
            return new JTableBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JTableHeader.class.getName())) {
            return new JTableHeaderBuilder(instance, heap);
        }
        return null;
    }
    
    
    private static class JComboBoxBuilder extends JComponentBuilder<JComboBox> {
        
        private final boolean isEditable;
        private final String selectedObject;
        
        JComboBoxBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            isEditable = DetailsUtils.getBooleanFieldValue(instance, "isEditable", false);
            
            String _selectedObject = null;
            Object _dataModel = instance.getValueOfField("dataModel");
            if (_dataModel instanceof Instance) {
                Instance dataModel = (Instance)_dataModel;
                if (DetailsUtils.isSubclassOf(dataModel, DefaultComboBoxModel.class.getName())) {
                    Object _selected = dataModel.getValueOfField("selectedObject");
                    if (_selected instanceof Instance) {
                        Instance selected = (Instance)_selected;
                        _selectedObject = DetailsUtils.getInstanceString(selected, heap);
                        if (_selectedObject == null)
                            _selectedObject = BrowserUtils.getSimpleType(selected.getJavaClass().getName()) +
                                             " #" + selected.getInstanceNumber();
                    }
                }
            }
            selectedObject = _selectedObject;
        }
        
        protected void setupInstance(JComboBox instance) {
            super.setupInstance(instance);
            
            instance.setEditable(isEditable);
            if (selectedObject != null) instance.addItem(selectedObject);
        }
        
        protected JComboBox createInstanceImpl() {
            return new JComboBox();
        }
        
    }
    
    private static class DefaultListModelBuilder extends InstanceBuilder<DefaultListModel> {
        
        private final List<String> model;
        
        DefaultListModelBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            model = new ArrayList<>();
            
            Object _delegate = instance.getValueOfField("delegate");
            if (_delegate instanceof Instance) {
                Instance delegate = (Instance)_delegate;
                Object _elementData = delegate.getValueOfField("elementData");
                if (_elementData instanceof ObjectArrayInstance) {
                    int size = DetailsUtils.getIntFieldValue(delegate, "elementCount", 0);
                    if (size > 0) { // TODO: should read up to 'size' elements
                        ObjectArrayInstance elementData = (ObjectArrayInstance)_elementData;
                        for (Object _item : elementData.getValues()) {
                            if (_item instanceof Instance) {
                                Instance item = (Instance)_item;
                                String ytem = DetailsUtils.getInstanceString(item, heap);
                                if (ytem == null)
                                    ytem = BrowserUtils.getSimpleType(item.getJavaClass().getName()) +
                                           " #" + item.getInstanceNumber();
                                model.add(ytem);
                            }
                        }
                    }
                }
            }
        }
        
        static DefaultListModelBuilder fromField(Instance instance, String field, Heap heap) {
            Object model = instance.getValueOfField(field);
            if (!(model instanceof Instance)) return null;
            if (!DetailsUtils.isSubclassOf((Instance)model, DefaultListModel.class.getName())) return null;
            return new DefaultListModelBuilder((Instance)model, heap);
        }
        
        protected void setupInstance(DefaultListModel instance) {
            super.setupInstance(instance);
            
            for (String item : model) instance.addElement(item);
        }
        
        protected DefaultListModel createInstanceImpl() {
            return new DefaultListModel();
        }
        
    }
    
    private static class JListBuilder extends JComponentBuilder<JList> {
        
        private final DefaultListModelBuilder dataModel;
        
        JListBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            dataModel = DefaultListModelBuilder.fromField(instance, "dataModel", heap);
        }
        
        protected void setupInstance(JList instance) {
            super.setupInstance(instance);
            
            ListModel model = dataModel == null ? null : dataModel.createInstance();
            if (model != null) instance.setModel(model);
        }
        
        protected JList createInstanceImpl() {
            return new JList();
        }
        
    }
    
    private static class JTreeBuilder extends JComponentBuilder<JTree> {
        
        private final boolean editable;
        
        JTreeBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            editable = DetailsUtils.getBooleanFieldValue(instance, "editable", false);
        }
        
        protected void setupInstance(JTree instance) {
            super.setupInstance(instance);
            
            instance.setEditable(editable);
        }
        
        protected JTree createInstanceImpl() {
            return new JTree(new Object[0]);
        }
        
    }
    
    private static class TableColumnBuilder extends InstanceBuilder<TableColumn> {
        private final int modelIndex;
        private final int width;
        private final String headerValue;
        
        TableColumnBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            modelIndex = DetailsUtils.getIntFieldValue(instance, "modelIndex", 0);
            width = DetailsUtils.getIntFieldValue(instance, "width", 75);
            
            String _headerValue = Utils.getFieldString(instance, "headerValue");
            headerValue = _headerValue != null ? _headerValue : "Col " + (modelIndex + 1);
        }
        
        protected void setupInstance(TableColumn instance) {
            super.setupInstance(instance);
            
            instance.setHeaderValue(headerValue);
        }
        
        protected TableColumn createInstanceImpl() {
            return new TableColumn(modelIndex, width, null, null);
        }
    }
    
    private static class TableColumnModelBuilder extends InstanceBuilder<DefaultTableColumnModel> {
        
        private final List<TableColumnBuilder> tableColumns;
        private final int columnMargin;
        
        TableColumnModelBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            tableColumns = new ArrayList<>();
            columnMargin = DetailsUtils.getIntFieldValue(instance, "columnMargin", 1);
            
            Object _columns = instance.getValueOfField("tableColumns");
            if (_columns instanceof Instance) {
                Instance columns = (Instance)_columns;
                Object _elementData = columns.getValueOfField("elementData");
                if (_elementData instanceof ObjectArrayInstance) {
                    int size = DetailsUtils.getIntFieldValue(columns, "elementCount", 0);
                    if (size > 0) { // TODO: should read up to 'size' elements
                        ObjectArrayInstance elementData = (ObjectArrayInstance)_elementData;
                        for (Object column : elementData.getValues()) {
                            if (column instanceof Instance)
                                tableColumns.add(new TableColumnBuilder((Instance)column, heap));
                        }
                    }
                }
            }
        }
        
        static TableColumnModelBuilder fromField(Instance instance, String field, Heap heap) {
            Object model = instance.getValueOfField(field);
            if (!(model instanceof Instance)) return null;
            if (!DetailsUtils.isSubclassOf((Instance)model, DefaultTableColumnModel.class.getName())) return null;
            return new TableColumnModelBuilder((Instance)model, heap);
        }
        
        protected void setupInstance(DefaultTableColumnModel instance) {
            super.setupInstance(instance);
            
            for (TableColumnBuilder builder : tableColumns)
                instance.addColumn(builder.createInstance());
            instance.setColumnMargin(columnMargin); 
        }
        
        protected DefaultTableColumnModel createInstanceImpl() {
            return new DefaultTableColumnModel();
        }
        
    }
    
    private static class JTableBuilder extends JComponentBuilder<JTable> {
        
        private final TableColumnModelBuilder columnModel;
        
        JTableBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            columnModel = TableColumnModelBuilder.fromField(instance, "columnModel", heap);
        }
        
        protected JTable createInstanceImpl() {
            return new JTable();
        }
        
        protected Component createPresenterImpl(JTable instance) {
            TableColumnModel _columnModel = columnModel == null ? null : columnModel.createInstance();
            if (_columnModel == null || _columnModel.getColumnCount() == 0) {
                TableColumn column = new TableColumn(0, instance.getWidth());
                column.setHeaderValue("Table");
                _columnModel = new DefaultTableColumnModel();
                _columnModel.addColumn(column);
            }
            instance.setColumnModel(_columnModel);
            instance.setPreferredScrollableViewportSize(instance.getSize());
            
            return new JScrollPane(instance);
        }
        
    }
    
    private static class JTableHeaderBuilder extends JComponentBuilder<JTableHeader> {
        
        private final TableColumnModelBuilder columnModel;
        
        JTableHeaderBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            columnModel = TableColumnModelBuilder.fromField(instance, "columnModel", heap);
        }
        
        protected void setupInstance(JTableHeader instance) {
            super.setupInstance(instance);
            
            TableColumnModel _columnModel = columnModel == null ? null : columnModel.createInstance();
            if (_columnModel == null || _columnModel.getColumnCount() == 0) {
                TableColumn column = new TableColumn(0, instance.getWidth());
                column.setHeaderValue("Table");
                _columnModel = new DefaultTableColumnModel();
                _columnModel.addColumn(column);
            }
            instance.setColumnModel(_columnModel);
        }
        
        protected JTableHeader createInstanceImpl() {
            return new JTableHeader();
        }
        
    }
    
}
