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

package org.netbeans.modules.db.dataview.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Blob;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.db.dataview.output.SQLConstant;
import org.netbeans.modules.db.dataview.util.ColorHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.DateType;
import org.netbeans.modules.db.dataview.util.FormatStringValue;
import org.netbeans.modules.db.dataview.util.LobHelper;
import org.netbeans.modules.db.dataview.util.StringValue;
import org.netbeans.modules.db.dataview.util.TimeType;
import org.netbeans.modules.db.dataview.util.TimestampType;

/**
 * @author Ahimanikya Satapathy
 */
public class ResultSetCellRenderer extends DefaultTableCellRenderer {

    protected static final FormatStringValue DATETIME_TO_STRING = new FormatStringValue(new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN));
    protected static final FormatStringValue TIME_TO_STRING = new FormatStringValue(new SimpleDateFormat (TimeType.DEFAULT_FOMAT_PATTERN));
    protected static final FormatStringValue Date_TO_STRING = new FormatStringValue(new SimpleDateFormat (DateType.DEFAULT_FOMAT_PATTERN));
    
    private final TableCellRenderer NULL_RENDERER = new NullObjectCellRenderer();
    private final TableCellRenderer DEFAULT_RENDERER = new SQLConstantsCellRenderer();
    private final TableCellRenderer NUMBER_RENDERER = new NumberObjectCellRenderer();
    private final TableCellRenderer BOOLEAN_RENDERER = new BooleanCellRenderer();
    private final TableCellRenderer CELL_FOCUS_RENDERER = new CellFocusCustomRenderer();
    private final TableCellRenderer BLOB_RENDERER = new BlobCellRenderer();
    private final TableCellRenderer CLOB_RENDERER = new ClobCellRenderer();

    private StringValue stringValue = null;
    
    public ResultSetCellRenderer() {
        this(new StringValue() {

            @Override
            public String getString(Object o) {
                return o == null ? "null" : o.toString(); // NOI18N
            }
        });
    }

    public ResultSetCellRenderer(StringValue converter) {
        super();
        stringValue = converter;
    }
    
    public ResultSetCellRenderer(StringValue converter, float alignment) {
        this(converter);
        ((JLabel) this).setAlignmentX(alignment);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (null == value) {
            return NULL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (value instanceof Number) {
            return NUMBER_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (DataViewUtils.isSQLConstantString(value, null)) {
            Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setTableCellToolTip(c, value);
            return c;            
        } else if (value instanceof Boolean) {
            return BOOLEAN_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (value instanceof Blob) {
            return BLOB_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (value instanceof Clob) {
            return CLOB_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
            if(stringValue != null) {
                value = stringValue.getString(value);
            }
            Component c = CELL_FOCUS_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setTableCellToolTip(c, value);
            return c;
        }
    }

    protected void setTableCellToolTip(Component c, Object value) {
        if (c instanceof JComponent) {
            String text = null;
            if( value instanceof String ) {
                text = (String) value;
            } else if (value != null) {
                text = value.toString();
            }
            if(text == null) {
                ((JComponent) c).setToolTipText(null);
            } else {
                int limit = Math.min(255, text.length());
                String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0 width=40><tr><td>";
                tooltip += DataViewUtils.escapeHTML(text.substring(0, limit))
                        .replace("\n", "<br>")
                        .replace(" ", "&nbsp;");
                if(text.length() > 255) {
                    tooltip += "<br><br>&hellip;";
                }
                tooltip += "</td></tr></table></html>";
                ((JComponent) c).setToolTipText(tooltip);
            }
        }
    }
}

class BooleanCellRenderer extends CellFocusCustomRenderer {

    private JCheckBox cb;

    public BooleanCellRenderer() {
        super();
        cb = new JCheckBox();
        cb.setOpaque(true);
        cb.setHorizontalAlignment(0);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        cb.setSelected((Boolean) value);
        return cb;
    }
}

class NumberObjectCellRenderer extends CellFocusCustomRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
        ((JLabel) c).setToolTipText(value.toString());
        return c;
    }
}

class NullObjectCellRenderer extends SQLConstantsCellRenderer {

    static final String NULL_LABEL = "<NULL>"; // NOI18N

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return super.getTableCellRendererComponent(table, NULL_LABEL, isSelected, hasFocus, row, column);
    }
}

class SQLConstantsCellRenderer extends CellFocusCustomRenderer {

    private static final Color foregroundColor = ColorHelper.getTableSqlconstantForeground();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object resolvedValue;
        if(value instanceof SQLConstant) {
            resolvedValue = "<" + ((SQLConstant) value).toString() + ">";
        } else {
            resolvedValue = value;
        }
        Component c = super.getTableCellRendererComponent(table, resolvedValue, isSelected, hasFocus, row, column);
        c.setFont(new Font(c.getFont().getFamily(), Font.ITALIC, 9));
        ((JLabel) c).setToolTipText(resolvedValue.toString());
        if (!isSelected) {
            c.setForeground(foregroundColor);
        }

        return c;
    }
}

class BlobCellRenderer extends SQLConstantsCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!(value instanceof Blob)) {
            throw new IllegalArgumentException("BlobCellRenderer can only be used for Blobs");
        }
        return super.getTableCellRendererComponent(table, LobHelper.blobToString((Blob) value), isSelected, hasFocus, row, column);
    }
}

class ClobCellRenderer extends CellFocusCustomRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!(value instanceof Clob)) {
            throw new IllegalArgumentException(
                    "ClobCellRenderer can only be used for Clobs");     //NOI18N
        }
        Clob clobValue = (Clob) value;

        Component renderer = super.getTableCellRendererComponent(table,
                LobHelper.clobToString(clobValue), isSelected, hasFocus, row, column);
        if (renderer instanceof JComponent) {
            ((JComponent) renderer).setToolTipText(LobHelper.clobToDescription(clobValue));
        }
        return renderer;
    }
}
