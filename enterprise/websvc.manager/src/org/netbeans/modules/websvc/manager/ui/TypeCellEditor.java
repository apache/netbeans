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

package org.netbeans.modules.websvc.manager.ui;

import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.net.URI;
import java.net.URISyntaxException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import org.openide.ErrorManager;

/**
 *
 * @author  david
 */
public class TypeCellEditor extends DefaultCellEditor implements TableCellEditor {
    Component lastComponent;
    String type;
    private ClassLoader classLoader;

    /** Creates a new instance of TypeCellRenderer */
    public TypeCellEditor(ClassLoader loader) {
        super(new JTextField());
        this.setClickCountToStart(1);
        this.classLoader = loader;
    }

    public void cancelCellEditing() {
        return;
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    /**
     * return the value of the last component.
     */
    public Object getCellEditorValue() {
        if(null == type) {
            return ((JTextField)lastComponent).getText();
        } else {
            if(lastComponent instanceof JTextField) {
                String valueString = ((JTextField)lastComponent).getText();
                Object value = createValue(valueString);
                return value;
            } else if(lastComponent instanceof JComboBox) {
                return ((JComboBox)lastComponent).getSelectedItem();
            } else return null;
        }

    }



    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        /**
         * We need to create the correct editing component for the type of field we have.
         *  JavaSimpleTypes all except Date and Calendar - JTextField()
         *  JavaEnumerationType - JComboBox
         */

        /**
         *  First, we need to get the JavaType for the node of the object to be edited.
         */

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)table.getModel().
                getValueAt(row, 0);
        /**
         * Now depending on the type, create a component to edit/display the type.
         */
        if(null == node.getUserObject()) {
            JTextField txtField = new JTextField();
            txtField.setText((String)value);
            lastComponent = (Component)txtField;

        } else {
            TypeNodeData data = (TypeNodeData)node.getUserObject();
            type = data.getTypeClass();

            if (ReflectionHelper.isSimpleType(type, classLoader)) {
                /**
                 * If the type is boolean or Boolean, create a JComboBox with true,false
                 */
                if (type.equalsIgnoreCase(boolean.class.getName()) ||
                        type.equalsIgnoreCase(Boolean.class.getName())) {
                    JComboBox combo = new JComboBox();
                    lastComponent = (Component)combo;
                    combo.addItem(true);
                    combo.addItem(false);

                    /**
                     * Set the value as the current Enumeration value.
                     */

                    Object parameterValue = data.getTypeValue();

                    combo.setSelectedItem(parameterValue);
                    combo.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            comboActionPerformed(evt);
                        }
                    });
                } else {

                    JTextField txtField = new JTextField();
                    /**
                     * figure out what kind of simple field this is to set the value.
                     */
                    if(null != value) {
                        txtField.setText(value.toString());
                    }
                    lastComponent = (Component)txtField;
                }

            } else if (ReflectionHelper.isEnumeration(type, classLoader)) {
                try {
                    JComboBox combo = new JComboBox();
                    List<String> enumTypes = ReflectionHelper.getEnumerationValues(type, classLoader);
                    for (String enumType : enumTypes) {
                        Object nextEnum = ReflectionHelper.getEnumeration(type, enumType, classLoader);
                        combo.addItem(nextEnum);
                    }
                    lastComponent = combo;

                    combo.setSelectedItem(data.getTypeValue());
                    combo.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            comboActionPerformed(evt);
                        }
                    });
                } catch (WebServiceReflectionException wsre) {
                    Throwable cause = wsre.getCause();
                    ErrorManager.getDefault().notify(cause);
                    ErrorManager.getDefault().log(this.getClass().getName() + ": Error retrieving Enum type on: " + "WebServiceReflectionException=" + cause);
                }
            }
        }

        return lastComponent;
    }

    private void comboActionPerformed(ActionEvent evt) {
        //JComboBox combo = (JComboBox)evt.getSource();
        this.fireEditingStopped();

    }

    private Object createValue(String inValue) {
        Object returnValue = null;
        String currentType = type;

        if(currentType.equalsIgnoreCase("int") ||
        currentType.equalsIgnoreCase("java.lang.Integer")) {
            try {
                returnValue = Integer.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = 0;
            }
        } else if(currentType.equalsIgnoreCase("byte") ||
        currentType.equalsIgnoreCase("java.lang.Byte")) {
            try {
                returnValue = Byte.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = Byte.valueOf("0");
            }
        } else if(currentType.equalsIgnoreCase("boolean") ||
        currentType.equalsIgnoreCase("java.lang.Boolean")) {
            try {
                returnValue = Boolean.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = false;
            }
        } else if(currentType.equalsIgnoreCase("float") ||
        currentType.equalsIgnoreCase("java.lang.Float")) {
            try {
                returnValue = Float.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = Float.valueOf(0);
            }
        } else if(currentType.equalsIgnoreCase("double") ||
        currentType.equalsIgnoreCase("java.lang.Double")) {
            try {
                returnValue = Double.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = Double.valueOf(0);
            }
        } else if(currentType.equalsIgnoreCase("long") ||
        currentType.equalsIgnoreCase("java.lang.Long")) {
            try {
                returnValue = Long.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = Long.valueOf(0);
            }
        } else if(currentType.equalsIgnoreCase("short") ||
        currentType.equalsIgnoreCase("java.lang.Short")) {
            try {
                returnValue = Short.valueOf(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = Short.valueOf(" ");
            }
        } else if(currentType.equalsIgnoreCase("java.lang.String")) {
            returnValue = inValue;
        } else if(currentType.equalsIgnoreCase("java.math.BigDecimal")) {
            try {
                returnValue = new BigDecimal(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = BigDecimal.valueOf(0);
            }
        } else if(currentType.equalsIgnoreCase("java.math.BigInteger")) {
            try {
                returnValue = new BigInteger(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new BigInteger("0");
            }
        } else if(currentType.equalsIgnoreCase("java.net.URI")) {
            try {
                returnValue = new URI(inValue);
            } catch(URISyntaxException uri) {
                try {
                    returnValue = new URI("http://java.sun.com");
                } catch(URISyntaxException uri2) {}
            }
        } else if(currentType.equalsIgnoreCase("java.util.Calendar")) {
            returnValue = Calendar.getInstance();
        } else if(currentType.equalsIgnoreCase("java.util.Date")) {
            try {
                returnValue = DateFormat.getInstance().parse(inValue);
            } catch(ParseException pe) {
                returnValue = new Date();
            }
        }

        return returnValue;

    }

}
