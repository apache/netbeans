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
/*
 * GroupTableModel.java
 *
 * Created on April 14, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.Collections;
import java.util.List;

import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Williams
 */
public abstract class SecurityMappingTableModel extends AbstractTableModel {
    
    protected final ResourceBundle customizerBundle = NbBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle"); // NOI18N
    
    /** Number of columns in table.
     */
    private final int numColumns;
    
    /** List of some type of Object.
     */
    private final List itemList;
    
    protected SecurityMappingTableModel(List p, int columns) {
        assert p != null;
        
        itemList = p;
        numColumns = columns;
    }

    /** Model manipulation
     */
    protected int addElement(Object entry) {
        itemList.add(entry);
        int index = itemList.size();
        fireTableRowsInserted(index, index);
        return index;
    }
    
    protected int replaceElement(Object oldEntry, Object newEntry) {
        int index = itemList.indexOf(oldEntry);
        if(index != -1) {
            itemList.set(index, newEntry);
            fireTableRowsUpdated(index, index);
        }
        return index;
    }
    
    protected int removeElement(Object entry) {
        int index = itemList.indexOf(entry);
        if(index != -1) {
            itemList.remove(index);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
        }
        return index;
    }
    
	public void removeElementAt(int index) {
		if(index >= 0 || index < itemList.size())  {
			itemList.remove(index);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
		}
	}
	
	public void removeElements(int[] indices) {
        // !PW FIXME this method has an unwritten requirement that the
        // list of indices passed in is ordered in ascending numerical order.
		if(indices.length > 0) {
			for(int i = indices.length-1; i >= 0; i--) {
				if(indices[i] >= 0 || indices[i] < itemList.size())  {
					itemList.remove(indices[i]);
				}
			}
//            fireTableRowsUpdated(indices[0], indices[indices.length-1]);
            fireTableDataChanged();
		}
	}
    
    protected boolean contains(Object entry) {
        return itemList.contains(entry);
    }
    
    protected Object getRowElement(int rowIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < itemList.size()) {
            result = itemList.get(rowIndex);
        }
        return result;
    }
    
    /** TableModel interface methods
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < itemList.size() && columnIndex >= 0 && columnIndex < numColumns) {
            result = getColumnValueFromRow(itemList.get(rowIndex), columnIndex);
        } 
        return result;
    }
    
    public int getRowCount() {
        return itemList.size();
    }

    public int getColumnCount() {
        return numColumns;
    }

    public abstract String getColumnName(int column);

    /** SecurityMappingTableModel methods
     */
    /** This method is passed the entry for a given row and returns the value
     *  of the specified colum element within that row.  Used by superclass to
     *  implement getValueAt().  columnIndex has already been range checked, but
     *  rowEntry could be null if this table allows null values.
     */
    protected abstract Object getColumnValueFromRow(Object rowEntry, int columnIndex);

}
