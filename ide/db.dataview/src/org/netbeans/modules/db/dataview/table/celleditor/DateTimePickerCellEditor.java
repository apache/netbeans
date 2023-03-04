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
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.netbeans.modules.db.dataview.table.ResultSetTableCellEditor;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.TimestampType;

public class DateTimePickerCellEditor extends ResultSetTableCellEditor {

    private Timestamp initialValue;
    private DateFormat dateFormat;

    public DateTimePickerCellEditor() {
        this(new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN));
    }

    /**
     * Instantiates an editor with the given dateFormat. If
     * null, the datePickers default is used.
     * 
     * @param dateFormat
     */
    public DateTimePickerCellEditor(DateFormat dateFormat) {
        super(new JTextField());
        this.dateFormat = dateFormat != null ? dateFormat : new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN);
    }

    @Override
    public Timestamp getCellEditorValue() {
        Date parsedTimestamp = TimestampType.doParse((String) super.getCellEditorValue());
        if(parsedTimestamp != null) {
            return new Timestamp(parsedTimestamp.getTime());
        } else {
            return initialValue;
        }
    }
    
    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        Component c = super.getTableCellEditorComponent(table, "", isSelected, row, column);
        JTextField jtf = (JTextField) c;
        initialValue = getValueAsTimestamp(value);
        jtf.setText(dateFormat.format(initialValue));
        if (suppressEditorBorder) {
            jtf.setBorder(BorderFactory.createEmptyBorder());
        }
        return c;
    }

    protected Timestamp getValueAsTimestamp(Object value) {
        if (isEmpty(value) || DataViewUtils.isSQLConstantString(value, null)) {
            return new Timestamp(System.currentTimeMillis());
        } else if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) value).getTime());
        } else if (value instanceof java.util.Calendar) {
            return new Timestamp(((java.util.Calendar) value).getTime().getTime());
        } else if (value instanceof Long) {
            return new Timestamp((Long) value);
        } else if (value instanceof String) {
            try {
                return new Timestamp(dateFormat.parse((String) value).getTime());
            } catch (ParseException e) {
                //mLogger.log(Level.SEVERE, e.getMessage(), e.getMessage());
            }
        }

        return new Timestamp(System.currentTimeMillis());
    }

    protected boolean isEmpty(Object value) {
        return value == null || value instanceof String && ((String) value).length() == 0;
    }
    
    public void addKeyListener(KeyListener kl) {
        ((JTextField) getComponent()).addKeyListener(kl);
    }
}
