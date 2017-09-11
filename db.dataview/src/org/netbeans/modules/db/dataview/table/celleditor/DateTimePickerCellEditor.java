/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
