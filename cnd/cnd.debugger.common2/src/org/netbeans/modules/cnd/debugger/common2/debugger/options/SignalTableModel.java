/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
