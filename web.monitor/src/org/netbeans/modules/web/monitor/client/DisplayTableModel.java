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

package org.netbeans.modules.web.monitor.client;

import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 * DisplayTableModel.java
 *
 *
 * Created: Fri Jan 25 13:37:39 2002
 *
 * @author Ana von Klopp
 * @version
 */
public class DisplayTableModel extends AbstractTableModel {

    private Object[][] data = null;
    private boolean editable, editableNames;
    private int numCols = 3;

    private static final boolean debug = false;

    public DisplayTableModel(Object[][] data, 
			boolean editable,
			boolean editableNames) {
	this.data = data;
	this.editable = editable;
	this.editableNames = editableNames;
    }
    
    public String getColumnName(int col) { 
	return ""; // NOI18N
	//return headers[col].toString(); 
    }
	
    public int getRowCount() { return data.length; }
    public int getColumnCount() { return numCols; }
    public Object getValueAt(int row, int col) { 
	return data[row][col]; 
    }
    public boolean isCellEditable(int row, int col) { 
	if(editable) {
	    if(col == 0) return editableNames;
	    if(col == 1) return true;
	    if(col == 2) return true;
	}
	if(col == 2) return true; 
	return false; 
    }
    
    public void setValueAt(Object value, int row, int col) {
	data[row][col] = value;
	fireTableCellUpdated(row, col);
    }

    void log(String s) {
	System.out.println("DisplayTableModel::" + s);  // NOI18N
    }
    
} // DisplayTableModel

