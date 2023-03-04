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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.openide.util.NbBundle;

/**
 * @author pfiala
 */
public class SelectMethodsNode extends EjbSectionNode {
    
    private EntityHelper.Queries queries;
    
    SelectMethodsNode(SectionNodeView sectionNodeView, EntityHelper.Queries queries) {
        super(sectionNodeView, true, queries, Utils.getBundleMessage("LBL_CmpSelects"), Utils.ICON_BASE_MISC_NODE);
        this.queries = queries;
    }
    
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        final SelectMethodsTableModel model = queries.getSelectMethodsTableModel();
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(), model) {

            protected void editCell(final int row, final int column) {
                if (!model.editRow(row)){
                    getSectionNodeView().getErrorPanel().setError(
                            new org.netbeans.modules.xml.multiview.Error(org.netbeans.modules.xml.multiview.Error.TYPE_WARNING,
                            NbBundle.getMessage(SelectMethodsNode.class, "TXT_MethodNotFound"), getTable()));
                }
            }
            
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
            }
            
            
            public void focusData(Object element) {
                if (element instanceof Query) {
                    final int row = queries.getSelectMethodRow((Query) element);
                    if (row >= 0) {
                        getTable().getSelectionModel().setSelectionInterval(row, row);
                    }
                }
                
            }
        };
        innerTablePanel.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                
            }
            public void focusLost(FocusEvent e) {
                innerTablePanel.getSectionView().getErrorPanel().clearError();
            }
        });
        
        return innerTablePanel;
    }
    
    public SectionNode getNodeForElement(Object element) {
        if (element instanceof Query) {
            if (queries.getSelectMethodRow((Query) element) >= 0) {
                return this;
            }
        }
        return super.getNodeForElement(element);
    }
    
}
