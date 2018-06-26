/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                    final int oldRow = ((Integer)evt.getOldValue()).intValue();
                    final int newRow = ((Integer)evt.getNewValue()).intValue();
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
            final List list1 = Arrays.asList(cmpFields.getCmpFields());
            final List list2 = new LinkedList(Arrays.asList((CmpField[]) element));
            if (list1.size() == list2.size()) {
                list2.removeAll(list1);
                if (list2.size() == 0) {
                    return this;
                }
            }
        }
        return super.getNodeForElement(element);
    }
}
