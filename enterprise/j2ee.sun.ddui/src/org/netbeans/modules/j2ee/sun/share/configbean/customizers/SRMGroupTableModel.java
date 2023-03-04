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
/*
 * GroupTableModel.java
 *
 * Created on April 14, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;


/**
 *
 * @author Peter Williams
 */
public class SRMGroupTableModel extends SRMBaseTableModel implements GroupTableModel {
    
    public SRMGroupTableModel(XmlMultiViewDataSynchronizer s, SecurityRoleMapping m) {
        super(s, m);
    }

    /** Model manipulation
     */
    public int addElement(String entry) {
        int index = mapping.addGroupName(entry);
        fireTableRowsInserted(index, index);
        modelUpdatedFromUI();
        return index;
    }
    
    public int replaceElement(String oldEntry, String newEntry) {
        int index = indexOf(oldEntry);
        if(index != -1) {
            mapping.setGroupName(index, newEntry);
            fireTableRowsUpdated(index, index);
            modelUpdatedFromUI();
        }
        return index;
    }
    
    public int removeElement(String entry) {
        int index = indexOf(entry);
        if(index != -1) {
            mapping.removeGroupName(entry);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
            modelUpdatedFromUI();
        }
        return index;
    }

	public void removeElementAt(int index) {
		if(index >= 0 || index < mapping.sizeGroupName())  {
            mapping.removeValue(SecurityRoleMapping.GROUP_NAME, index);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
            modelUpdatedFromUI();
		}
	}
	
	public void removeElements(int[] indices) {
        // !PW FIXME this method has an unwritten requirement that the
        // list of indices passed in is ordered in ascending numerical order.
		if(indices.length > 0) {
            boolean dataChanged = false;
			for(int i = indices.length-1; i >= 0; i--) {
				if(indices[i] >= 0 || indices[i] < mapping.sizeGroupName())  {
                    mapping.removeValue(SecurityRoleMapping.GROUP_NAME, indices[i]);
                    dataChanged = true;
				}
			}
            
            if(dataChanged) {
//                fireTableRowsUpdated(indices[0], indices[indices.length-1]);
                fireTableDataChanged();
                modelUpdatedFromUI();
            }
		}
	}
    
    public boolean contains(String entry) {
        return indexOf(entry) != -1;
    }
    
    public String getElementAt(int rowIndex) {
        String result = null;
        if(rowIndex >= 0 && rowIndex < mapping.sizeGroupName()) {
            result = mapping.getGroupName(rowIndex);
        }
        return result;
    }

    private int indexOf(String entry) {
        String [] names = mapping.getGroupName();
        if(names != null) {
            for(int index = 0; index < names.length; index++) {
                if(Utils.strEquivalent(names[index], entry)) {
                    return index;
                }
            }
        }
        return -1;
    }
    
    /** TableModel interface methods
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        String result = null;
        if(rowIndex >= 0 && rowIndex < mapping.sizeGroupName() && columnIndex == 0) {
            result = mapping.getGroupName(rowIndex);
        } 
        return result;
    }
    
    public int getRowCount() {
        return mapping.sizeGroupName();
    }

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int columnIndex) {
        if(columnIndex == 0) {
            return customizerBundle.getString("LBL_GroupName"); // NOI18N
        }
        return null;
    }

}
