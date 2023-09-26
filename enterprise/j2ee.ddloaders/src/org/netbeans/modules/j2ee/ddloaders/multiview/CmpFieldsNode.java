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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.ListSelectionModel;

/**
 * @author pfiala
 */
public class CmpFieldsNode extends EjbSectionNode {

    private EntityHelper.CmpFields cmpFields;

    CmpFieldsNode(SectionNodeView sectionNodeView, EntityHelper.CmpFields cmpFields) {
        super(sectionNodeView, true, cmpFields, Utils.getBundleMessage("LBL_CmpFields"), Utils.ICON_BASE_MISC_NODE);
        this.cmpFields = cmpFields;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        final CmpFieldsTableModel model = cmpFields.getCmpFieldsTableModel();
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(), model) {
            protected void editCell(final int row, final int column) {
                model.editRow(row);
            }

            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if (source == key) {
                    model.refreshView();
                    scheduleRefreshView();
                }
            }

            public void focusData(Object element) {
                if (element instanceof CmpField) {
                    final int row = cmpFields.getFieldRow((CmpField) element);
                    if (row >= 0) {
                        getTable().getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        };
        cmpFields.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt != null && CmpFieldHelper.PROPERTY_FIELD_ROW_CHANGED.equals(evt.getPropertyName())) {
                    final ListSelectionModel selectionModel = innerTablePanel.getTable().getSelectionModel();
                    final int selectedRow = selectionModel.getLeadSelectionIndex();
                    model.refreshView();
                    final int oldRow = ((Integer)evt.getOldValue());
                    final int newRow = ((Integer)evt.getNewValue());
                    if (selectedRow == oldRow) {
                        selectionModel.setSelectionInterval(newRow, newRow);
                    }
                }
            }
        });
        return innerTablePanel;

    }

    public SectionNode getNodeForElement(Object element) {
        if (element instanceof CmpField) {
            if (cmpFields.getFieldRow((CmpField) element) >= 0) {
                return this;
            }
        } else if (element instanceof CmpField[]) {
            final List<CmpField> list1 = Arrays.asList(cmpFields.getCmpFields());
            final List<CmpField> list2 = new LinkedList<>(Arrays.asList((CmpField[]) element));
            if (list1.size() == list2.size()) {
                list2.removeAll(list1);
                if (list2.isEmpty()) {
                    return this;
                }
            }
        }
        return super.getNodeForElement(element);
    }
}
