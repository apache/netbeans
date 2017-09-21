/**
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
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import org.jdesktop.swingx.JXDatePicker;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.JXDateTimePicker;
import org.netbeans.modules.db.dataview.util.TimestampType;

public class DateTimePickerCellEditor extends AbstractCellEditor implements TableCellEditor {

    private boolean editable = true;
    private JXDateTimePicker datePicker;
    private DateFormat dateFormat;
    private ActionListener pickerActionListener;
    private boolean ignoreAction;
    private JTable table;

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

        // JW: the copy is used to synchronize .. can 
        // we use something else?
        this.dateFormat = dateFormat != null ? dateFormat : new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN);
        datePicker = new JXDateTimePicker();
        // default border crushes the editor/combo
        datePicker.getEditor().setBorder(
                BorderFactory.createEmptyBorder(0, 1, 0, 1));
        // should be fixed by j2se 6.0
        datePicker.setFont(UIManager.getDefaults().getFont("TextField.font"));
        if (dateFormat != null) {
            datePicker.setFormats(dateFormat);
        }
        datePicker.addActionListener(getPickerActionListener());
    }

    @Override
    public Timestamp getCellEditorValue() {
        return datePicker.getDateTime();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        return super.isCellEditable(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        ignoreAction = true;
        boolean canCommit = commitChange();
        ignoreAction = false;
        if (canCommit) {
            datePicker.setDateTime(null);
            return super.stopCellEditing();
        }
        return false;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.table = table;
        ignoreAction = true;
        datePicker.setDateTime(getValueAsTimestamp(value));

        ignoreAction = false;
        return datePicker;
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

    protected boolean commitChange() {
        try {
            datePicker.commitEdit();
            return true;
        } catch (ParseException e) {
        }
        return false;
    }

    public DateFormat[] getFormats() {
        return datePicker.getFormats();
    }

    public void setFormats(DateFormat... formats) {
        datePicker.setFormats(formats);
    }

    private ActionListener getPickerActionListener() {
        if (pickerActionListener == null) {
            pickerActionListener = createPickerActionListener();
        }
        return pickerActionListener;
    }

    protected ActionListener createPickerActionListener() {
        ActionListener l = new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // avoid duplicate trigger from
                // commit in stopCellEditing
                if (ignoreAction) {
                    return;
                }
                terminateEdit(e);
            }

            private void terminateEdit(final ActionEvent e) {
                if ((e != null) && (JXDatePicker.COMMIT_KEY.equals(e.getActionCommand()))) {
                    stopCellEditing();
                } else {
                    cancelCellEditing();
                }
            }
        };
        return l;
    }

    public void addKeyListener(KeyListener kl) {
        datePicker.addKeyListener(kl);
    }
}
