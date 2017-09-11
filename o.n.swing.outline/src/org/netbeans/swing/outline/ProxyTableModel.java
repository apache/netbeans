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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.swing.outline;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/** A TableModel which is driven by a RowModel - the RowModel
 * supplies row contents, based on nodes suppled by the tree
 * column of an OutlineModel.  This model supplies the additional
 * rows of the TableModel to the OutlineModel.
 *
 * @author  Tim Boudreau
 */
final class ProxyTableModel implements TableModel {
    private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private RowModel rowmodel;
    private OutlineModel outlineModel;
    /** Creates a new instance of ProxyTableModel that will use the supplied
     * RowModel to produce its values.  */
    public ProxyTableModel(RowModel rowmodel) {
        this.rowmodel = rowmodel;
    }
    
    /** Set the OutlineModel that will be used to find nodes for
     * rows.  DefaultOutlineModel will do this in its constructor. */
    void setOutlineModel (OutlineModel mdl) {
        this.outlineModel = mdl;
    }
    
    /** Get the outline model used to provide column 0 nodes to the
     * RowModel for setting the values.  */
    OutlineModel getOutlineModel () {
        return outlineModel;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return rowmodel.getColumnClass(columnIndex);
    }
    
    @Override
    public int getColumnCount() {
        return rowmodel.getColumnCount();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return rowmodel.getColumnName(columnIndex);
    }
    
    @Override
    public int getRowCount() {
        //not interesting, will never be called - the outline model
        //handles this
        return -1;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        if (node == null) {
            assert false : "Some node should exist on row " + rowIndex + " and on column " + columnIndex + ", but was null.";
            return null;
        }
        return rowmodel.getValueFor(node, columnIndex);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        return rowmodel.isCellEditable (node, columnIndex);
    }
    
    @Override
    public synchronized void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
    
    @Override
    public synchronized void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }
    
    private void fire (TableModelEvent e) {
        TableModelListener[] l;
        synchronized (this) {
            l = new TableModelListener[listeners.size()];
            l = listeners.toArray (l);
        }
        for (int i=0; i < l.length; i++) {
            l[i].tableChanged(e);
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        rowmodel.setValueFor (node, columnIndex, aValue);
        TableModelEvent e = new TableModelEvent (this, rowIndex, rowIndex, 
            columnIndex);
        fire(e);
    }
    
    /** Get the object that will be passed to the RowModel to fetch values
     * for the given row. 
     * @param row The row we need the tree node for */
    private Object getNodeForRow(int row) {
        return getOutlineModel().getValueAt(row, 0);
    }    

    
}
