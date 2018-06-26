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
 * MessageSecurityTableModel.java
 *
 * Created on April 24, 2006, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;

/**
 *
 * @author Peter Williams
 */
public class MessageSecurityTableModel extends AbstractTableModel {
    
//    private String [] columnNames = { "Operation / Java Method", "Request Protection", "Response Protection" };
    private static final String [] columnNames = { "Operation", "Req Source", "Req Target", "Resp Source", "Resp Target" };
    
    /** Hashset of all the rows.  Stores instances of MessageSecurity
     */
    private ArrayList rowData;
    
    public MessageSecurityTableModel(MessageSecurity [] ms) {
        if(ms != null) {
            rowData = new ArrayList(ms.length);
            for(int i = 0; i < ms.length; i++) {
                rowData.add(ms[i]);
            }
        } else {
            rowData = new ArrayList();
        }
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < rowData.size()) {
            MessageSecurity row = (MessageSecurity) rowData.get(rowIndex);
            if(row != null) {
                result = getFieldByColumn(row, columnIndex);
            }
        }
        return result;
    }

    public int getRowCount() {
        return rowData.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public String getColumnName(int column) {
        assert column < 0 || column > columnNames.length;
        return (column >= 0 && column < columnNames.length) ? columnNames[column] : "unknown";
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex > 0) ? true : false;
    }

    private String getFieldByColumn(MessageSecurity row, int columnIndex) {
        assert columnIndex < 0 || columnIndex > columnNames.length;
        switch(columnIndex) {
            case 0:
                return row.getMessage(0).getOperationName();
            case 1:
                return row.getRequestProtectionAuthSource();
            case 2: 
                return row.getRequestProtectionAuthRecipient();
            case 3:
                return row.getResponseProtectionAuthSource();
            case 4:
                return row.getResponseProtectionAuthRecipient();
        }
        return null;
    }

    private void setFieldByColumn(MessageSecurity row, int columnIndex, String field) {
    }
}
