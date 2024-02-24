// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
//</editor-fold>

package org.netbeans.modules.payara.common.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

public abstract class BasePanel extends JPanel {

    protected abstract String getPrefix();

    protected abstract List<Component> getDataComponents();

    public final void initializeUI() {
        for (Component c : getDataComponents()) {
            c.setEnabled(false);
        }
    }

    /** this is likely to be called off the awt thread
     *
     * @param name
     * @param data
     */
    public void initializeData(final String name, final Map<String, String> data) {
        Mutex.EVENT.readAccess(new Runnable(){

            @Override
            public void run() {
                for (Component c : getDataComponents()) {
                    c.setEnabled(true);
                    // fill in the blanks...
                    String compName = c.getName();
                    if (compName != null) {
                        // construct the key
                        String key = getPrefix() + name + "." + compName;
                        String value = data.get(key);
                        if (null == value) {
                            value = NbBundle.getMessage(this.getClass(), "ERR_DATA_NOT_FOUND", key);
                        } else {
                            c.setName(key); // for writing the field value back to the server
                        }
                        if (c instanceof JComboBox) {
                            final JComboBox jcb = (JComboBox) c;
                            new ComboBoxSetter(jcb, value, data).run();
                        } else if (c instanceof JTextComponent) {
                            final JTextComponent jtc = (JTextComponent) c;
                            new TextFieldSetter(jtc, value).run();
                        } else if (c instanceof AbstractButton) {
                            AbstractButton ab = (AbstractButton) c;
                            new ButtonSetter(ab, value).run();
                        } else if (c instanceof JTable) {
                            JTable table = (JTable) c;
                            new TableSetter(name, table, data).run();
                        }
                    }
                }
            }
        });

    }

    public final Map<String,String> getData() {
        Map<String,String> retVal = new HashMap<String,String>(getDataComponents().size());
        for (Component c : getDataComponents()) {
            // fill in the blanks...
            String compName = c.getName();
            if (compName != null) {
                // construct the key
                String key = compName;
                if (c instanceof JComboBox) {
                    final JComboBox jcb = (JComboBox) c;
                    retVal.put(key, (String) jcb.getSelectedItem());
                } else if (c instanceof JTextComponent) {
                    final JTextComponent jtc = (JTextComponent) c;
                    retVal.put(key, jtc.getText());
                } else if (c instanceof AbstractButton) {
                    AbstractButton ab = (AbstractButton) c;
                    retVal.put(key, Boolean.toString(ab.isSelected()));
                } else if (c instanceof JTable) {
                    JTable table = (JTable) c;
                    TableCellEditor tce = table.getCellEditor();
                    if (null != tce) {
                        tce.stopCellEditing();
                    }
                    Object tm = table.getModel();
                    if (tm instanceof DataTableModel) {
                        DataTableModel model = (DataTableModel) tm;
                        retVal.putAll(model.getData());
                    }
                }
            }
        }
        return retVal;
    }


    static class ComboBoxSetter implements Runnable {
        private JComboBox jcb;
        private Map<String,String> data;
        private String value;

        private ComboBoxSetter(JComboBox jcb, String value, Map<String, String> data) {
            this.jcb = jcb;
            this.data = data;
            this.value = value;
        }

        public void run() {
            // build the allowed values
            String allowedRegEx = jcb.getActionCommand();
            DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<>();
            Pattern p = Pattern.compile(allowedRegEx);
            Set<String> keys = data.keySet();
            //String pushPrefix = null;
            for (String k : keys) {
                Matcher test = p.matcher(k);
                if (test.matches()) {
                    dcbm.addElement(data.get(k));
                }
            }
            jcb.setModel(dcbm);
            jcb.setSelectedItem(value);
        }
        
    }

    static class TextFieldSetter implements Runnable {
        private JTextComponent jtc;
        private String value;

        TextFieldSetter(JTextComponent jtc, String value) {
            this.jtc = jtc;
            this.value = value;
        }
        public void run() {
            jtc.setText(value);
        }
    }

    static class ButtonSetter implements Runnable {
        private AbstractButton button;
        private String value;
        ButtonSetter(AbstractButton button, String value) {
            this.button = button;
            this.value = value;
        }

        public void run() {
            button.setSelected(Boolean.parseBoolean(value));
        }
    }

    static class TableSetter implements Runnable {
        private JTable table;
        private Map<String, String> data;
        private String name;
        TableSetter(String name, JTable table, Map<String,String> data) {
            this.table = table;
            this.data = data;
            this.name = name;
        }

        public void run() {
            // build the row data
            String[] specComp = table.getName().split("\\.");
            int colCount = specComp.length - 1;
            if (0 >= colCount) {
                // probably should log something here, too...
                return;
            }
            List<String[]> l = new ArrayList<String[]>();
            // old style
            Pattern pattern = Pattern.compile(".*\\."+name+"\\."+specComp[0]+"\\..*\\."+specComp[1]);
            Set<String> keys = data.keySet();
            String pushPrefix = null;
            for (String k : keys) {
                Matcher test = pattern.matcher(k);
                if (test.matches()) {
                    if (null == pushPrefix) {
                        int dex = k.lastIndexOf(specComp[0]);
                        pushPrefix = k.substring(0,dex);
                    }
                    String[] aRow = new String[colCount];
                    int dex = k.lastIndexOf(".");
                    String partialKey = k.substring(0, dex);
                    aRow[0] = data.get(k);
                    for (int i = 2 ; i < colCount+1; i++) {
                        aRow[i-1] = data.get(partialKey+"."+specComp[i]);
                        if (null == aRow[i-1]) aRow[i-1] = "";
                    }
                    l.add(aRow);
                }
            }
            if (l.size() > 0) {
                table.setModel(new AttributedPropertyTableModel(l.toArray(new String[0][]), specComp, pushPrefix));
            } else {
                // this data is from a post beta build...
                pattern = Pattern.compile(".*\\." + name + "\\." + specComp[0] + "\\..*");
                pushPrefix = null;
                for (String k : keys) {
                Matcher test = pattern.matcher(k);
                if (test.matches()) {
                        if (null == pushPrefix) {
                            int dex = k.lastIndexOf(specComp[0]);
                            pushPrefix = k.substring(0, dex);
                        }
                        String[] aRow = new String[colCount];
                        int dex = k.lastIndexOf(".");
                        String propName = k.substring(dex + 1);
                        aRow[0] = propName;
                        aRow[1] = data.get(k);
                        l.add(aRow);
                    }
                }
                if (l.size() > 0) {
                    table.setModel(new NameValueTableModel(l.toArray(new String[0][]), specComp, pushPrefix));
                }
            }
        }
    }

    abstract static class DataTableModel extends AbstractTableModel {

        public abstract Map<String, String> getData();

        private String[][] rowData;
        private String pushPrefix;
        private String[] names;
        private String[] specComp;

        DataTableModel(String[][] rowData, String[] specComp, String pushPrefix) {
            this.rowData = rowData;
            this.specComp = specComp;
            this.pushPrefix = pushPrefix;
            if (rowData.length > 0) {
                int colCount = rowData[0].length;
                names = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    try {
                        names[i] = NbBundle.getMessage(this.getClass(),
                                "column-title." + specComp[0] + "." + specComp[i + 1]);
                    } catch (MissingResourceException mre) {
                        // TODO -- log the MRE
                        names[i] = specComp[i + 1];
                    }
                }
            } 
        }

        public int getRowCount() {
            return rowData.length;
        }

        public int getColumnCount() {
            return rowData.length == 0 ? 0 : rowData[0].length;
        }

        @Override
        public String getColumnName(int i) {
            if (i > -1 && i < getColumnCount())
                return names[i];
            return "";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return rowData[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object arg0, int arg1, int arg2) {
            super.setValueAt(arg0, arg1, arg2);
            rowData[arg1][arg2] = (String) arg0;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        public String getPushPrefix() {
            return pushPrefix;
        }
        
        public String getSpecComp(int i) {
            return specComp[i];
        }
    }

    static class AttributedPropertyTableModel extends DataTableModel {
        AttributedPropertyTableModel(String[][] rowData, String[] specComp, String pushPrefix) {
            super(rowData,specComp,pushPrefix);
        }
        
        public Map<String,String> getData() {
            Map<String,String> retVal = new HashMap<String,String>(getRowCount()*(getColumnCount()-1));
            for (int i = 0; i < getRowCount(); i++) {
                String key = getPushPrefix() + getSpecComp(0)+"."+
                        getValueAt(i,0)+".";
                for (int j = 1; j < getColumnCount(); j++) {
                    key += getSpecComp(j+1);
                    retVal.put(key, (String) getValueAt(i,j));
                }
            }
            return retVal;
        }
    }

    static class NameValueTableModel extends DataTableModel {

        NameValueTableModel(String[][] rowData, String[] specComp, String pushPrefix) {
            super(rowData,specComp,pushPrefix);
        }

        public Map<String, String> getData() {
            Map<String, String> retVal = new HashMap<String, String>(getRowCount() * (getColumnCount() - 1));
            for (int i = 0; i < getRowCount(); i++) {
                String key = getPushPrefix() + getSpecComp(0) + "." +
                        getValueAt(i, 0); //+".";
                retVal.put(key, (String) getValueAt(i, 1));
            }
            return retVal;
        }
    }

    public static class Error extends BasePanel {
        public Error() {
        }
        protected String getPrefix() {
            return "";
        }
        protected List<Component> getDataComponents() {
            return Collections.emptyList();
        }

        @Override
        public void initializeData(String name, Map<String, String> data) {
            return;
        }
    }
}
