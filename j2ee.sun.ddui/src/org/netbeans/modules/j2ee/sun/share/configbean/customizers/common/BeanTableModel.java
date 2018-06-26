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
 * BeanTableModel.java
 *
 * Created on October 3, 2003, 10:47 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

/**
 * DBeanTableModel.java - table model over the base bean array
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


public abstract class BeanTableModel extends AbstractTableModel {
	/* A class implementation comment can go here. */
	private List children;
	private Object parent;


	// AbstractTableModel methods
	public int getColumnCount() {
		return getColumnNames().length;
	}


	public int getRowCount() {
		if (children != null){
			return children.size();
		} else {
			return 0;
		}
	}


	public String getColumnName(int column) {
		return getColumnNames()[column];
	}


	public boolean isCellEditable(int row, int column) {
		return false;
	}


	// BeanTableModel methods
	protected abstract String[] getColumnNames();

	public abstract Object addRow(Object[] values);

	public abstract void editRow(int row, Object[] values);

	public abstract void removeRow(int row);

	public abstract boolean alreadyExists(Object[] values);

	public abstract Object[] getValues(int row);


	public void setData(Object parent, CommonDDBean[] children) {
		this.parent = parent;
		this.children = new ArrayList();
		
		if (children == null) {
			return;
		}
		
		for(int i = 0;i < children.length; i++) {
			this.children.add(children[i]);
		}
		
		fireTableDataChanged();
	}


	public int getRowWithValue(int column, Object value) {
		for(int row = 0, max = getRowCount(); row < max; row++) {
			Object obj = getValueAt(row, column);
			if (obj.equals(value)) {
				return row;
			}
		}
		return -1;
	}


	protected Object getParent() {
		return parent;
	}


	protected List getChildren() {
		return children;
	}
}
