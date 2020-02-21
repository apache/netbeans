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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import javax.swing.table.AbstractTableModel;

/**
 * was: based on utils.ListTableModel.
 */
final class SignalTableModel extends AbstractTableModel {

    private static final String ignored =
	Catalog.get("Signal_Ignored");		// NOI18N
    private static final String ignoredDefault =
	Catalog.get("Signal_IgnoredDefault");	// NOI18N
    private static final String caught =
	Catalog.get("Signal_Caught");		// NOI18N
    private static final String caughtDefault =
	Catalog.get("Signal_CaughtDefault");	// NOI18N

    private Signals signals;
    
    private static final int colCount = 4;

    private static final String[] columnName = new String[] {
	Catalog.get("Signal_Number"),	// NOI18N
	Catalog.get("Signal_Signal"),	// NOI18N
	Catalog.get("Signal_Desc"),	// NOI18N
	Catalog.get("Signal_Handled"),	// NOI18N
    };
    
    private boolean haveSignals() {
	return signals != null;
    }


    public SignalTableModel() {
	super();
    }

    // interface TableModel
    @Override
    public int getRowCount() {
	if (haveSignals()) {
	    return signals.count();
	} else {
	    return 1;
	}
    }

    // interface TableModel
    @Override
    public int getColumnCount() {
	return colCount;
    }

    // interface TableModel
    @Override
    public String getColumnName(int columnIndex) {
	return columnName[columnIndex];
    }

    // interface TableModel
    @Override
    public Class<?> getColumnClass(int columnIndex) {
	/* Don't want integer's left justified
	if (columnIndex == 0) {
	    return Integer.class;
	} else {
	*/
	return String.class;
    }

    // interface TableModel
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (!haveSignals() || rowIndex == 8 /* signal KILL */) {
	    // No signals: no editing
	    return false;
	} else {
	    // Only the "handled" column is editable
	    return (columnIndex == 3);
	}
    }
	
    // interface TableModel
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (!haveSignals()) {
	    if (columnIndex == 2)
		return Catalog.get("NoSigsUntilDebug"); // NOI18N
	    else
		return "";
	}

	Signals.InitialSignalInfo signal = signals.getSignal(rowIndex);

	switch(columnIndex) {
	    case 0:
		return new Integer(signal.signo());
	    case 1:
		return signal.name();
	    case 2:
		return signal.description();
	    case 3:
		if (signal.isCaught()) {
		    if (signal.isCaughtByDefault()) {
			return caughtDefault;
		    } else {
			return caught;
		    }
		} else {
		    if (signal.isCaughtByDefault()) {
			return ignored;
		    } else {
			return ignoredDefault;
		    }
		}
	    default:
		return "";
	}
	
    }

    // interface TableModel
    @Override
    public void setValueAt(Object newValue, int rowIndex, int columnIndex) {

	Signals.InitialSignalInfo signal = signals.getSignal(rowIndex);
	if (signal == null)
	    return;

	switch (columnIndex) {
	    case 0:
	    case 1:
	    case 2:
	    default:
		assert false : "SetValueAt() for bad column " + columnIndex;
		break;
	    case 3:
		String newString = newValue.toString();
		if (ignored.equals(newString)) {
		    signal.setCaught(false);
		} else if (caught.equals(newString)) {
		    signal.setCaught(true);
		} else {
		    assert Catalog.get("Signal_Default").	// NOI18N
			equals(newString);
		    signal.resetCaught();
		}
		signals.checkSignal();
		break;
	}
	fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * The signal list has changed.
     */
    public void signalsUpdated(Signals signals) {

	if (signals != null) {
	    assert signals.isClone() :
		"SignalTableModel.signalsUpdated() not getting a clone"; // NOI18N
	}

	// remove existing rows
	if (haveSignals()) {
	    fireTableRowsDeleted(0, this.signals.count());
	}

	this.signals = signals;
	
	if (signals == null) {
	    return;
	}

	fireTableRowsInserted(0, signals.count());
    }

    public boolean isDefaultValue(int rowIndex) {
	if (!haveSignals()) {
	    return true;
	} else {
	    Signals.InitialSignalInfo s = signals.getSignal(rowIndex);
	    if (s == null)
		return true;
	    if (s.isCaught()) {
		return s.isCaughtByDefault();
	    } else {
		return !s.isCaughtByDefault();
	    }
	}
    }
}
