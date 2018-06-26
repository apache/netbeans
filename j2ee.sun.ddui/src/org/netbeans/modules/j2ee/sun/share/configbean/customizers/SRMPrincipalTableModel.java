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

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.share.PrincipalNameMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;


/**
 *
 * @author Peter Williams
 */
public class SRMPrincipalTableModel extends SRMBaseTableModel implements PrincipalTableModel {
    
    /** Number of columns in table.
     */
    private final int numColumns;
    
    public SRMPrincipalTableModel(XmlMultiViewDataSynchronizer s, SecurityRoleMapping m, int columns) {
        super(s, m);
        
        numColumns = columns;
    }

    /** Model manipulation
     */
    public int addElement(PrincipalNameMapping entry) {
        int index = mapping.addPrincipalName(entry.getPrincipalName());
        try {
            mapping.setPrincipalNameClassName(index, entry.getClassName());
        } catch(VersionNotSupportedException ex) {
        }
        fireTableRowsInserted(index, index);
        modelUpdatedFromUI();
        return index;
    }
    
    public int replaceElement(PrincipalNameMapping oldEntry, PrincipalNameMapping newEntry) {
        int index = indexOf(oldEntry);
        if(index != -1) {
            mapping.setPrincipalName(index, newEntry.getPrincipalName());
            try {
                mapping.setPrincipalNameClassName(index, newEntry.getClassName());
            } catch(VersionNotSupportedException ex) {
            }
            fireTableRowsUpdated(index, index);
            modelUpdatedFromUI();
        }
        return index;
    }
    
    public int removeElement(PrincipalNameMapping entry) {
        int index = indexOf(entry);
        if(index != -1) {
            mapping.removePrincipalName(entry.getPrincipalName());
            modelUpdatedFromUI();
        }
        return index;
    }

	public void removeElementAt(int index) {
		if(index >= 0 || index < mapping.sizePrincipalName())  {
            mapping.removeValue(SecurityRoleMapping.PRINCIPAL_NAME, index);
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
				if(indices[i] >= 0 || indices[i] < mapping.sizePrincipalName())  {
                    mapping.removeValue(SecurityRoleMapping.PRINCIPAL_NAME, indices[i]);
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
    
    public boolean contains(PrincipalNameMapping entry) {
        return indexOf(entry) != -1;
    }
    
    public PrincipalNameMapping getElementAt(int rowIndex) {
        PrincipalNameMapping result = null;
        if(rowIndex >= 0 && rowIndex < mapping.sizePrincipalName()) {
            try {
                result = new PrincipalNameMapping(mapping.getPrincipalName(rowIndex),
                        mapping.getPrincipalNameClassName(rowIndex));
            } catch(VersionNotSupportedException ex) {
                result = new PrincipalNameMapping(mapping.getPrincipalName(rowIndex));
            }
            modelUpdatedFromUI();
        }
        return result;
    }

    private int indexOf(PrincipalNameMapping entry) {
        String [] names = mapping.getPrincipalName();
        if(names != null) {
            for(int index = 0; index < names.length; index++) {
                if(Utils.strEquivalent(names[index], entry.getPrincipalName())) {
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
        if(rowIndex >= 0 && rowIndex < mapping.sizePrincipalName() && columnIndex >= 0 && columnIndex < numColumns) {
            if(columnIndex == 0) {
                result = mapping.getPrincipalName(rowIndex);
            } else {
                try {
                    result = mapping.getPrincipalNameClassName(rowIndex);
                } catch(VersionNotSupportedException ex) {
                }
            }
        } 
        return result;
    }
    
    public int getRowCount() {
        return mapping.sizePrincipalName();
    }

    public int getColumnCount() {
        return numColumns;
    }

    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return customizerBundle.getString("LBL_PrincipalName"); // NOI18N
            case 1:
                return customizerBundle.getString("LBL_ClassName"); // NOI18N
        }
        return null;
    }

}
