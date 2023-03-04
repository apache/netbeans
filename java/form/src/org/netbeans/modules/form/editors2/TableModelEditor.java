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

package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import javax.swing.table.*;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
* A property editor for javax.swing.table.TableModel.
* @author Ian Formanek, Tomas Pavek
*/

public class TableModelEditor implements PropertyEditor, XMLPropertyEditor,
                                         ExPropertyEditor, NamedPropertyEditor {

    public TableModelEditor() {
        support = new PropertyChangeSupport (this);
    }

    @Override
    public Object getValue () {
        return table;
    }

    @Override
    public void setValue (Object value) {
        table = new NbTableModel ((TableModel) value);
        support.firePropertyChange ("", null, null); // NOI18N
    }

    @Override
    public String getAsText () {
        return null;
    }

    @Override
    public void setAsText (String string) {
    }

    @Override
    public String getJavaInitializationString () {
        TableModel m = (TableModel) getValue ();
        StringBuilder titlesSB = new StringBuilder ();
        int i = m.getColumnCount ();
        int j = m.getRowCount ();
        titlesSB.append ("{\n\t\t"); // NOI18N
        if (i > 0) {
            String s = m.getColumnName (0);
            s = (s==null) ? "" : s.replace("\"", "\\\""); // NOI18N
            titlesSB.append ("\"").append (s).append ('"'); // NOI18N
            for (int k = 1; k < i; k++) {
                String s1 = m.getColumnName (k);
                s1 = (s1==null) ? "" : s1.replace("\"", "\\\""); // NOI18N
                titlesSB.append (", \"").append (s1).append ('"'); // NOI18N
            }
        }
        titlesSB.append ("\n\t}"); // NOI18N

        boolean generateTypes = false;
        StringBuilder typesSB = new StringBuilder ();
        typesSB.append ("{\n\t\t"); // NOI18N
        if (i > 0) {
            typesSB.append (m.getColumnClass (0).getName ()).append (".class"); // NOI18N
            if (m.getColumnClass (0) != Object.class)
                generateTypes = true;
            for (int k = 1; k < i; k++) {
                if (m.getColumnClass (k) != Object.class)
                    generateTypes = true;
                typesSB.append (", ").append (m.getColumnClass (k).getName ()).append (".class"); // NOI18N
            }
        }
        typesSB.append ("\n\t}"); // NOI18N

        boolean generateEditable = false;
        StringBuilder editableSB = new StringBuilder ();
        editableSB.append ("{\n\t\t"); // NOI18N
        if (i > 0) {
            editableSB.append (m.isCellEditable (0, 0));
            if (!m.isCellEditable (0, 0)) generateEditable = true;
            for (int k = 1; k < i; k++) {
                if (!m.isCellEditable (0, k)) generateEditable = true;
                editableSB.append (", ").append (m.isCellEditable (0, k)); // NOI18N
            }
        }
        editableSB.append ("\n\t}"); // NOI18N

        StringBuilder dataSB = new StringBuilder ();
        dataSB.append ("{\n\t\t"); // NOI18N
        if (j > 0) {
            for (int l = 0; l < j; l++) {
                if (l != 0)
                    dataSB.append (",\n\t\t"); // NOI18N
                if (i == 0) {
                    dataSB.append ("{}"); // NOI18N
                } else {
                    Object obj = m.getValueAt (l, 0);
                    dataSB.append ('{').append (getAsString (obj));
                    for (int i1 = 1; i1 < i; i1++) {
                        obj = m.getValueAt (l, i1);
                        dataSB.append (", ").append (getAsString (obj)); // NOI18N
                    }
                    dataSB.append ('}');
                }
            }
        }
        dataSB.append ("\n\t}"); // NOI18N
        if (generateEditable || generateTypes) {
            return
                "new javax.swing.table.DefaultTableModel(\n" + // NOI18N
                "\tnew Object [][] " + dataSB.toString() + ",\n" + // NOI18N
                "\tnew String [] " + titlesSB.toString() + "\n" + // NOI18N
                ") {\n" + // NOI18N
                (generateTypes ? (
                     "\tClass[] types = new Class [] " + typesSB.toString() + ";\n") : // NOI18N
                 "") + // NOI18N
                (generateEditable ? (
                     "\tboolean[] canEdit = new boolean [] " + editableSB.toString() + ";\n") : // NOI18N
                 "") + // NOI18N
                (generateTypes ? (
                     "\n" + // NOI18N
                     "\tpublic Class getColumnClass(int columnIndex) {\n" + // NOI18N
                     "\t\treturn types [columnIndex];\n" + // NOI18N
                     "\t}\n") : // NOI18N
                 "") + // NOI18N
                (generateEditable ? (
                     "\n" + // NOI18N
                     "\tpublic boolean isCellEditable(int rowIndex, int columnIndex) {\n" + // NOI18N
                     "\t\treturn canEdit [columnIndex];\n" + // NOI18N
                     "\t}\n") : // NOI18N
                 "") + // NOI18N
                "}"; // NOI18N
        } else {
            return
                "new javax.swing.table.DefaultTableModel(\n" + // NOI18N
                "\tnew Object [][] " + dataSB.toString() + ",\n" + // NOI18N
                "\tnew String [] " + titlesSB.toString() + "\n" + // NOI18N
                ")"; // NOI18N
        }
    }

    @Override
    public String[] getTags () {
        return null;
    }

    @Override
    public boolean isPaintable () {
        return true;
    }

    @Override
    public void paintValue (Graphics g, Rectangle rectangle) {
        String msg = NbBundle.getMessage(TableModelEditor.class, "MSG_TableModel"); // NOI18N
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, rectangle.x, rectangle.y + (rectangle.height - fm.getHeight())/2 + fm.getAscent());
    }

    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    @Override
    public Component getCustomEditor () {
        return new CustomTableModelEditor(this, env);
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    // ------------
    // XMLPropertyEditor implementation

    private static final String XML_TABLE = "Table"; // NOI18N
    private static final String XML_COLUMN = "Column"; // NOI18N
    private static final String XML_DATA = "Data"; // NOI18N

    private static final String ATTR_COLUMN_COUNT = "columnCount"; // NOI18N
    private static final String ATTR_ROW_COUNT = "rowCount"; // NOI18N
    private static final String ATTR_TITLE = "title"; // NOI18N
    private static final String ATTR_TYPE = "type"; // NOI18N
    private static final String ATTR_EDITABLE = "editable"; // NOI18N
    private static final String ATTR_VALUE = "value"; // NOI18N

    @Override
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element tableEl = doc.createElement(XML_TABLE);

        int colCount = table.getColumnCount();
        int rowCount = table.getRowCount();

        tableEl.setAttribute(ATTR_COLUMN_COUNT, Integer.toString(colCount));
        tableEl.setAttribute(ATTR_ROW_COUNT, Integer.toString(rowCount));

        for (int i=0; i < colCount; i++) {
            NbTableModel.ColumnItem column = table.getColumnItem(i);
            org.w3c.dom.Element columnEl = doc.createElement(XML_COLUMN);
            columnEl.setAttribute(ATTR_TITLE, column.title);
            columnEl.setAttribute(ATTR_TYPE, column.type.getName());
            columnEl.setAttribute(ATTR_EDITABLE, column.editable ? "true":"false"); // NOI18N

            boolean anyData = false;
            for (int j=0; j < rowCount; j++)
                if (column.rows.get(j) != null) {
                    anyData = true;
                    break;
                }

            if (anyData)
                for (int j=0; j < rowCount; j++) {
                    org.w3c.dom.Element dataEl = doc.createElement(XML_DATA);
                    dataEl.setAttribute(ATTR_VALUE, valueToString(column.rows.get(j)));
                    columnEl.appendChild(dataEl);
                }

            tableEl.appendChild(columnEl);
        }

        return tableEl;
    }

    @Override
    public void readFromXML(org.w3c.dom.Node element) throws IOException {
        if (!XML_TABLE.equals(element.getNodeName()))
            throw new IOException(getReadingErrorMessage()); // NOI18N

        org.w3c.dom.NamedNodeMap tableAttr = element.getAttributes();
        if (tableAttr == null)
            return;

        IOException ioex = null;
        org.w3c.dom.Node node;

        // first read columnCount and rowCount attributes
        int columnCount = -1;
        int rowCount = -1;

        node = tableAttr.getNamedItem(ATTR_COLUMN_COUNT);
        if (node != null) {
            try {
                columnCount = Integer.parseInt(node.getNodeValue());
            }
            catch (java.lang.NumberFormatException e) {
                ioex = new IOException(getReadingErrorMessage());
                org.openide.ErrorManager.getDefault().annotate(ioex, e);
            }
        }

        node = tableAttr.getNamedItem(ATTR_ROW_COUNT);
        if (node != null) {
            try {
                rowCount = Integer.parseInt(node.getNodeValue());
            }
            catch (java.lang.NumberFormatException e) {
                if (ioex == null)
                   ioex = new IOException(getReadingErrorMessage());
                org.openide.ErrorManager.getDefault().annotate(ioex, e);
            }
        }

        if (columnCount < 0 || rowCount < 0) {
            if (ioex == null)
               ioex = new IOException(getReadingErrorMessage());
            throw ioex;
        }

        java.util.List<NbTableModel.ColumnItem> columns = new ArrayList<NbTableModel.ColumnItem>(columnCount);

        // go through the column data nodes and read the columns
        org.w3c.dom.NodeList columnNodes = element.getChildNodes();
        for (int i=0, cCount=columnNodes.getLength(); i < cCount; i++) {
            org.w3c.dom.Node cNode = columnNodes.item(i);
            if (!XML_COLUMN.equals(cNode.getNodeName()))
                continue;

            org.w3c.dom.NamedNodeMap columnAttr = cNode.getAttributes();
            if (columnAttr == null)
                continue;

            // get title, type and editable attributes
            String title = null;
            Class type = null;
            Boolean editable = null;

            node = columnAttr.getNamedItem(ATTR_TITLE);
            if (node != null)
                title = node.getNodeValue();

            node = columnAttr.getNamedItem(ATTR_TYPE);
            if (node != null) {
                try {
                    type = Class.forName(node.getNodeValue());
                }
                catch (Exception e) {
                    ioex = new IOException(getReadingErrorMessage());
                    org.openide.ErrorManager.getDefault().annotate(ioex, e);
                }
            }

            node = columnAttr.getNamedItem(ATTR_EDITABLE);
            if (node != null)
                editable = Boolean.valueOf(node.getNodeValue());

            if (title == null || type == null || editable == null) {
                if (ioex == null)
                   ioex = new IOException(getReadingErrorMessage());
                throw ioex;
            }

            java.util.List<Object> columnData = new ArrayList<Object>(rowCount);

            // read the column data
            org.w3c.dom.NodeList dataNodes = cNode.getChildNodes();
            for (int j=0, dCount=dataNodes.getLength(); j < dCount; j++) {
                org.w3c.dom.Node dNode = dataNodes.item(j);
                if (!XML_DATA.equals(dNode.getNodeName()))
                    continue;

                org.w3c.dom.NamedNodeMap dataAttr = dNode.getAttributes();
                if (dataAttr == null)
                    continue;

                // get the value attribute
                Object value = null;
                node = dataAttr.getNamedItem(ATTR_VALUE);
                if (node != null) {
                    try {
                        value = stringToValue(node.getNodeValue(), type);
                    }
                    catch (IllegalArgumentException e) {
                        ioex = new IOException(getReadingErrorMessage());
                        org.openide.ErrorManager.getDefault().annotate(ioex, e);
                        throw ioex;
                    }
                }

                columnData.add(value);
            }

            // check the row count
            if (columnData.size() != rowCount) {
                if (columnData.isEmpty())
                    for (int ii=0; ii < rowCount; ii++)
                        columnData.add(null);
                else
                    throw new IOException(getReadingErrorMessage());
            }

            // create the column
            columns.add(new NbTableModel.ColumnItem(title,
                                                    type,
                                                    editable.booleanValue(),
                                                    columnData));
        }

        // check the column count
        if (columns.size() != columnCount)
            throw new IOException(getReadingErrorMessage());

        // create the model instance
        table = new NbTableModel(columns, rowCount);
    }

    // -----------

    private static ResourceBundle getBundle() {
        return NbBundle.getBundle(TableModelEditor.class);
    }

    private static String getReadingErrorMessage() {
        return getBundle().getString("ERR_InvalidXMLFormat"); // NOI18N
    }

    private static String valueToString(Object value) {
        if (value instanceof Integer || value instanceof Short
                || value instanceof Byte || value instanceof Long
                || value instanceof Float || value instanceof Double
                || value instanceof Boolean || value instanceof Character)
            return value.toString();

        if (value instanceof String)
            return (String)value;

        if (value == null)
            return "null"; // NOI18N

        return null; // is not a primitive type
    }

    private static Object stringToValue(String encoded, Class type) {
        if ("null".equals(encoded)) // NOI18N
            return null;

        if (type == Object.class)
            return encoded;

        if (Integer.class.isAssignableFrom(type) || Integer.TYPE.equals(type))
            return Integer.valueOf(encoded);
        if (Short.class.isAssignableFrom(type) || Short.TYPE.equals(type))
            return Short.valueOf(encoded);
        if (Byte.class.isAssignableFrom(type) || Byte.TYPE.equals(type))
            return Byte.valueOf(encoded);
        if (Long.class.isAssignableFrom(type) || Long.TYPE.equals(type))
            return Long.valueOf(encoded);
        if (Float.class.isAssignableFrom(type) || Float.TYPE.equals(type))
            return Float.valueOf(encoded);
        if (Double.class.isAssignableFrom(type) || Double.TYPE.equals(type))
            return Double.valueOf(encoded);
        if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE.equals(type))
            return Boolean.valueOf(encoded);
        if (Character.class.isAssignableFrom(type) || Character.TYPE.equals(type))
            return new Character(encoded.charAt(0));
        if (String.class.isAssignableFrom(type))
            return encoded;

        throw new IllegalArgumentException();
    }

    static String getAsString (Object o) {
        if (o == null) return "null"; // NOI18N
        
        if (o instanceof String)
            return "\"" + ((String)o).replace("\"", "\\\"") + "\""; // NOI18N

        String s = o.getClass ().getName ();
        int g = s.lastIndexOf ('.');
        if (g >= 0) s = s.substring (g + 1, s.length ());

        String cast = ""; // NOI18N
        if (o instanceof Byte)
            cast = "(byte) ";   // NOI18N
        else if (o instanceof Short)
            cast = "(short) ";  // NOI18N
        
        if(s.equals("Double") || s.equals("Float")) { // NOI18N
            String os = o.toString();
            if(os.equals("Infinity")) { // NOI18N
                o = s + ".POSITIVE_INFINITY"; // NOI18N
            } else if(os.equals("-Infinity")) { // NOI18N
                o = s + ".NEGATIVE_INFINITY"; // NOI18N
            } else if(os.equals("NaN")) { // NOI18N
                o = s + ".NaN"; // NOI18N
            }
        }
        
        return " new " + s + "(" + cast + o + ")"; // NOI18N
    }

    static Object getDefaultValue (Class c) {
        return null;
    }
    
    // NamedPropertyEditor implementation
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString("CTL_TableModelEditor_DisplayName"); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // NbTableModel

    public static class NbTableModel extends AbstractTableModel implements Externalizable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6843008677521167210L;

        java.util.List<ColumnItem> columns;
        int rowCount;

        transient boolean alwaysEditable = false;

        /** For externalization only */
        public NbTableModel() {
        }

        public NbTableModel(String[] titles, Class[] types, boolean[] editable) {
            this(titles,types,editable,4);
        }

        public NbTableModel(String[] titles, Class[] types, boolean[] editable, int rowCount) {
            this.rowCount = rowCount;
            columns = new ArrayList<ColumnItem>(titles.length);
            for (int i=0; i < titles.length; i++)
                columns.add(new ColumnItem(titles[i],types[i],editable[i],rowCount));
        }

        public NbTableModel(TableModel createFrom) {
            ResourceBundle bundle = getBundle();
            if (createFrom == null) {
                rowCount = 4; // 4 rows and 4 columns by default
                columns = new ArrayList<ColumnItem>(20);
                for (int i=0; i < 4; i++)
                    columns.add(new ColumnItem(bundle.getString("CTL_Title")+" "+Integer.toString(i+1),
                                               Object.class, true, rowCount));
            } else {
                rowCount = createFrom.getRowCount();
                int colCount = createFrom.getColumnCount();
                columns = new ArrayList<ColumnItem>(colCount);

                if (createFrom instanceof NbTableModel) {
                    NbTableModel model = (NbTableModel) createFrom;

                    for (int i=0; i < colCount; i++) {
                        ColumnItem ci = model.columns.get(i);
                        columns.add(new ColumnItem(ci));
                    }
                } else {
                    for (int i=0; i < colCount; i++) {
                        ColumnItem ci = new ColumnItem(createFrom.getColumnName(i),
                                                       createFrom.getColumnClass(i),
                                                       true, rowCount);
                        for (int j=0; j < rowCount; j++)
                            ci.rows.set(j, createFrom.getValueAt(j,i));
                        columns.add(ci);
                    }
                }
            }
        }

        NbTableModel(java.util.List<ColumnItem> columns, int rowCount) {
            this.columns = columns;
            this.rowCount = rowCount;
        }

        // from AbstractTableModel
        @Override
        public Class getColumnClass(int i) {
            ColumnItem ci = columns.get(i);
            return ci.type;
        }

        public void setColumnClass(int i, Class type) {
            ColumnItem ci = columns.get(i);
            ci.type = type;
        }

        // from AbstractTableModel
        @Override
        public String getColumnName(int i) {
            ColumnItem ci = columns.get(i);
            return ci.title;
        }

        public void setColumnName(int i, String title) {
            ColumnItem ci = columns.get(i);
            ci.title = title;
        }

        // from TableModel
        @Override
        public int getRowCount() {
            return rowCount;
        }

        // from TableModel
        @Override
        public int getColumnCount() {
            return columns.size();
        }

        public boolean isColumnEditable(int i) {
            // this method returns design time settings - doesn't reflect alwaysEditable
            ColumnItem ci = columns.get(i);
            return ci.editable;
        }

        // from AbstractTableModel
        @Override
        public boolean isCellEditable(int i, int j) {
            // this is used by a real table - alwaysEditable true means that table
            // is fully editable (no matter the design settings)
            if (alwaysEditable) return true;
            ColumnItem ci = columns.get(j);
            return ci.editable;
        }

        public void setColumnEditable(int i, boolean editable) {
            ColumnItem ci = columns.get(i);
            ci.editable = editable;
        }

        // from TableModel
        @Override
        public Object getValueAt(int row, int column) {
            ColumnItem ci = columns.get(column);
            return ci.rows.get(row);
        }

        // from AbstractTableModel
        @Override
        public void setValueAt(Object obj, int row, int column) {
            ColumnItem ci = columns.get(column);
            ci.rows.set(row, obj);
            fireTableCellUpdated(row, column);
        }

        private ColumnItem getColumnItem(int i) {
            return columns.get(i);
        }

        void setRowCount(int newRowCount) {
            if (newRowCount == rowCount) return;

            for (int i=0, n=columns.size(); i < n; i++) {
                java.util.List<Object> rows = columns.get(i).rows;
                if (newRowCount > rowCount) {
                    for (int nr = newRowCount-rowCount; nr > 0; nr--)
                        rows.add(null);
                }
                else { // newRowCount < rowCount
                    for (int rn = rowCount - newRowCount; rn > 0; rn--)
                        rows.remove(newRowCount + rn - 1);
                }
            }

            int rc = rowCount;
            rowCount = newRowCount;

            if (newRowCount > rc)
                fireTableRowsInserted(rc, newRowCount - 1);
            else // newRowCount < rc
                fireTableRowsDeleted(newRowCount, rc - 1);
        }

        // adds one row at the index
        public void addRow(int index) {
            if (index >= 0 && index <= rowCount) {
                for (int i=0, n=columns.size(); i < n; i++)
                    columns.get(i).rows.add(index, null);

                rowCount++;
                fireTableRowsInserted(index, index);
            }
        }

        // removes one row from index
        public void removeRow(int index) {
            if (index >= 0 && index < rowCount) {
                for (int i=0, n=columns.size(); i < n; i++)
                    columns.get(i).rows.remove(index);

                rowCount--;
                fireTableRowsDeleted(index, index);
            }
        }

        public void moveRow(int fromIndex, int toIndex) {
            if (columns.size() > 0
                    && fromIndex >= 0 && fromIndex < rowCount
                    && toIndex >= 0 && toIndex < rowCount
                    && fromIndex != toIndex) {

                for (int i=0, n=columns.size(); i < n; i++) {
                    java.util.List<Object> rows = columns.get(i).rows;
                    Object obj = rows.get(toIndex);
                    rows.set(toIndex, rows.get(fromIndex));
                    rows.set(fromIndex, obj);
                }
                fireTableStructureChanged();
            }
        }

        void setColumnCount(int newColumnCount) {
            ResourceBundle bundle = getBundle();
            int colCount = columns.size();
            if (newColumnCount == colCount) return;

            if (newColumnCount > colCount) {
                for (int nc=newColumnCount-colCount; nc > 0; nc--)
                    columns.add(new ColumnItem(bundle.getString("CTL_Title")+" "+Integer.toString(newColumnCount-nc+1),
                                               Object.class, true, rowCount));
            } else { // newColumnCount < colCount
                for (int cn=colCount-newColumnCount; cn > 0; cn--)
                    columns.remove(newColumnCount + cn - 1);
            }
            
            fireTableStructureChanged();
        }

        // adds one column at index
        public void addColumn(int index) {
            if (index >=0 && index <= columns.size()) {
                columns.add(index, new ColumnItem(getBundle().getString("CTL_Title")+" "+Integer.toString(index+1),
                                                  Object.class, true, rowCount));
                // rename default titles
                for (int i=index+1, n=columns.size(); i < n; i++) {
                    ColumnItem ci = columns.get(i);
                    renameDefaultColumnTitle(ci, i, i+1);
                }
                fireTableStructureChanged();
            }
        }

        // removes one column at index
        public void removeColumn(int index) {
            if (index >=0 && index < columns.size()) {
                columns.remove(index);

                // rename default titles
                for (int i=index, n=columns.size(); i < n; i++) {
                    ColumnItem ci = columns.get(i);
                    renameDefaultColumnTitle(ci, i+2, i+1);
                }
                fireTableStructureChanged();
            }
        }

        public void moveColumn(int fromIndex,int toIndex) {
            if (fromIndex >= 0 && fromIndex < columns.size()
                    && toIndex >= 0 && toIndex < columns.size()
                    && fromIndex != toIndex) {

                ColumnItem ciFrom = columns.get(fromIndex);
                ColumnItem ciTo = columns.get(toIndex);

                // also rename default titles
                renameDefaultColumnTitle(ciFrom, fromIndex+1, toIndex+1);
                renameDefaultColumnTitle(ciTo, toIndex+1, fromIndex+1);

                columns.set(toIndex, ciFrom);
                columns.set(fromIndex, ciTo);
                fireTableStructureChanged();
            }
        }

        // renames default column title according to new column's position
        // e.g. with params 2 and 3 - "Title 2" is renamed to "Title 3"
        private static void renameDefaultColumnTitle(ColumnItem ci, int fromIndex, int toIndex) {
            String fromStr = getBundle().getString("CTL_Title")+" "+Integer.toString(fromIndex);
            if (fromStr.equals(ci.title))
                ci.title = getBundle().getString("CTL_Title")+" "+Integer.toString(toIndex);
        }

        @Override
        public void writeExternal(ObjectOutput oo) throws IOException {
            // backward compatibility must be ensured... (table model was implemented using arrays sooner)
            oo.writeInt(rowCount);
            int colCount = columns.size();
            oo.writeInt(colCount);

            String[] titles = new String[colCount];
            boolean[] editable = new boolean[colCount];
            for (int i=0; i < colCount; i++) {
                ColumnItem ci = columns.get(i);
                titles[i] = ci.title;
                editable[i] = ci.editable;
            }
            oo.writeObject(titles);
            oo.writeObject(editable);
            for (int i=0; i < colCount; i++) {
                ColumnItem ci = columns.get(i);
                oo.writeObject(ci.type.getName());
            }

            for (int i=0; i < rowCount; i++)
                for (int j=0; j < colCount; j++) {
                    ColumnItem ci = columns.get(j);
                    if (ci.rows.get(i) instanceof Serializable)
                        oo.writeObject(ci.rows.get(i));
                    else
                        oo.writeObject(null);
                }
        }

        @Override
        public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            // reading is in the previous format (when table model was implemented using arrays)
            rowCount = oi.readInt();
            int colCount = oi.readInt();

            columns = new ArrayList<ColumnItem>(colCount);

            String[] titles = (String[]) oi.readObject();
            boolean[] editable = (boolean[]) oi.readObject();

            for (int i=0; i < colCount; i++)
                columns.add(new ColumnItem(titles[i],Class.forName((String)oi.readObject()),
                                           editable[i], rowCount));

            for (int i=0; i < rowCount; i++)
                for (int j=0; j < colCount; j++) {
                    ColumnItem ci = columns.get(j);
                    ci.rows.set(i, oi.readObject());
                }
        }

        // helper class for holding data of a column
        private static class ColumnItem {
            String title;
            Class type;
            boolean editable;
            java.util.List<Object> rows; // values in the column

            ColumnItem(String title, Class type, boolean editable, int rowCount) {
                this.title = title;
                this.type = type;
                this.editable = editable;
                rows = new ArrayList<Object>(rowCount);
                for (int i=0; i < rowCount; i++)
                    rows.add(null);
            }

            ColumnItem(String title, Class type, boolean editable, java.util.List<Object> data) {
                this.title = title;
                this.type = type;
                this.editable = editable;
                rows = data;
            }

            ColumnItem(ColumnItem ci) {
                this.title = ci.title;
                this.type = ci.type;
                this.editable = ci.editable;
                int rowCount = ci.rows.size();
                rows = new ArrayList<Object>(rowCount);

                for (int i=0; i < rowCount; i++)
                    rows.add(ci.rows.get(i));
            }
        } // Class ColumnItem
    } // Class NbTableModel

    // -----------------------------------------------------------------------------
    // private variables

    private NbTableModel table;
    private PropertyChangeSupport support;
    private PropertyEnv env;

}
