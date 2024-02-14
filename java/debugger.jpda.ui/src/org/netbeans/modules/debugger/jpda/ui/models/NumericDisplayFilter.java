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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.api.debugger.jpda.*;
import org.openide.util.Exceptions;
import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;

/**
 * Implements the "Display As Decimal/Hexadecimal/Octal/Binary/Char"
 * option for numeric variables.
 * Provides the popup action and filters displayed values.
 *
 * @author Maros Sandor, Jan Jancura, Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="WatchesView", types=TableModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types={ NodeActionsProviderFilter.class, TableModelFilter.class },
                                 position=800),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types={ NodeActionsProviderFilter.class, TableModelFilter.class },
                                 position=800),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types={ NodeActionsProviderFilter.class, TableModelFilter.class },
                                 position=800),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types={ NodeActionsProviderFilter.class, TableModelFilter.class },
                                 position=800)
})
public class NumericDisplayFilter implements TableModelFilter, 
NodeActionsProviderFilter, Constants {

    enum NumericDisplaySettings { DECIMAL, HEXADECIMAL, OCTAL, BINARY, CHAR, TIME }

    private final Map<Variable, NumericDisplaySettings>   variableToDisplaySettings = new HashMap<Variable, NumericDisplaySettings>();
    private HashSet     listeners;

    
    // TableModelFilter ........................................................

    public Object getValueAt (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        if ( (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID ||
              columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID) && 
            node instanceof Variable && 
            isIntegralType ((Variable) node)
        ) {
            if (node instanceof JPDAWatch) {
                JPDAWatch w = (JPDAWatch) node;
                String e = w.getExceptionDescription ();
                if (e == null) {
                    if (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
                        columnID == Constants.LOCALS_VALUE_COLUMN_ID) {
                        VariablesTableModel.setErrorValueMsg(w, null);
                    } else {
                        VariablesTableModel.setErrorToStringMsg(w, null);
                    }
                }
            }
            Variable var = (Variable) node;
            NumericDisplaySettings nds = variableToDisplaySettings.get (var);
            if (nds == null && var instanceof Field) {
                Variable parent = null;
                try {
                    java.lang.reflect.Method pvm = var.getClass().getMethod("getParentVariable");
                    pvm.setAccessible(true);
                    parent = (Variable) pvm.invoke(var);
                } catch (IllegalAccessException ex) {
                } catch (IllegalArgumentException ex) {
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (NoSuchMethodException ex) {
                } catch (SecurityException ex) {
                }
                nds = variableToDisplaySettings.get(parent);
            }
            return getValue(var, nds);
        }
        return original.getValueAt (node, columnID);
    }

    public boolean isReadOnly (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        return original.isReadOnly(node, columnID);
    }

    public void setValueAt (
        TableModel original, 
        Object node, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        if ( (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID ||
              columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID) && 
            node instanceof Variable && 
            isIntegralType ((Variable) node) &&
            value instanceof String
        ) {
            Variable var = (Variable) node;
            value = setValue (
                var, 
                (NumericDisplaySettings) variableToDisplaySettings.get (var),
                (String) value
            );
        }
        original.setValueAt(node, columnID, value);
    }

    public void addModelListener (ModelListener l) {
        HashSet newListeners = (listeners == null) ? 
            new HashSet () : (HashSet) listeners.clone ();
        newListeners.add (l);
        listeners = newListeners;
    }

    public void removeModelListener (ModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove (l);
        listeners = newListeners;
    }

    
    // NodeActionsProviderFilter ...............................................

    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        if (!(node instanceof Variable)) return original.getActions(node);
        Action [] actions;
        try {
            actions = original.getActions(node);
        } catch (UnknownTypeException e) {
            actions = new Action[0];
        }
        List myActions = new ArrayList();
        if (node instanceof Variable) {
            Variable var = (Variable) node;
            if (isIntegralTypeOrArray(var)) {
                myActions.add(new DisplayAsAction((Variable) node));
            }
        }
        myActions.addAll(Arrays.asList(actions));
        return (Action[]) myActions.toArray(new Action[0]);
    }

    
    // other methods ...........................................................
    
    private static int getChar(String toString) {
        // Remove the surrounding apostrophes first:
        toString = toString.substring(1, toString.length() - 1);
        char c = toString.charAt(0);
        return c & 0xFFFF;
    }
    
    private Object getValue (Variable var, NumericDisplaySettings settings) {
        if (settings == null) return var.getValue ();
        String type = var.getType ();
        try {
            switch (settings) {
            case DECIMAL:
                if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return Integer.toString(c);
                } else {
                    return var.getValue ();
                }
            case HEXADECIMAL:
                if ("int".equals (type))
                    return "0x" + Integer.toHexString (
                        Integer.parseInt (var.getValue ())
                    );
                else
                if ("short".equals (type)) {
                    String rv = Integer.toHexString(Short.parseShort(var.getValue()));
                    if (rv.length() > 4) rv = rv.substring(rv.length() - 4);
                    return "0x" + rv;
                } else if ("byte".equals(type)) {
                    String rv = Integer.toHexString(Byte.parseByte(var.getValue()));
                    if (rv.length() > 2) rv = rv.substring(rv.length() - 2);
                    return "0x" + rv;
                } else if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return "0x" + Integer.toHexString(c);
                } else {//if ("long".equals(type)) {
                    return "0x" + Long.toHexString (
                        Long.parseLong (var.getValue ())
                    );
                }
            case OCTAL:
                if ("int".equals (type))
                    return "0" + Integer.toOctalString (
                        Integer.parseInt (var.getValue ())
                    );
                else
                if ("short".equals(type)) {
                    short s = Short.parseShort(var.getValue());
                    int i = s & 0xFFFF;
                    String rv = Integer.toOctalString(i);
                    return "0" + rv;
                } else
                if ("byte".equals(type)) {
                    byte b = Byte.parseByte(var.getValue());
                    int i = b & 0xFF;
                    String rv = Integer.toOctalString(i);
                    return "0" + rv;
                } else if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return "0" + Integer.toOctalString(c);
                } else {//if ("long".equals(type)) {
                    return "0" + Long.toOctalString (
                        Long.parseLong (var.getValue ())
                    );
                }
            case BINARY:
                if ("int".equals(type))
                    return Integer.toBinaryString(Integer.parseInt(var.getValue()));
                else if ("short".equals(type)) {
                    String rv = Integer.toBinaryString(Short.parseShort(var.getValue()));
                    if (rv.length() > 16) rv = rv.substring(rv.length() - 16);
                    return rv;
                } else if ("byte".equals(type)) {
                    String rv = Integer.toBinaryString(Byte.parseByte(var.getValue()));
                    if (rv.length() > 8) rv = rv.substring(rv.length() - 8);
                    return rv;
                } else if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return Integer.toBinaryString(c);
                } else {//if ("long".equals(type)) {
                    return Long.toBinaryString (Long.parseLong (var.getValue ()));
                }
            case CHAR:
                if ("char".equals(type)) {
                    return var.getValue ();
                }
                return "'" + new Character (
                    (char) Integer.parseInt (var.getValue ())
                ) + "'";
            case TIME:
                if ("long".equals(type)) {
                    return new Date(Long.parseLong(var.getValue ())).toString();
                }
            default:
                return var.getValue ();
            }
        } catch (NumberFormatException nfex) {
            return nfex.getLocalizedMessage();
        }
    }

    private Object setValue (Variable var, NumericDisplaySettings settings, String origValue) {
        if (settings == null) return origValue;
        String type = var.getType ();
        try {
            switch (settings) {
            case BINARY:
                if ("int".equals(type))
                    return Integer.toString(Integer.parseInt(origValue, 2));
                else if ("short".equals(type)) {
                    return Short.toString(Short.parseShort(origValue, 2));
                } else if ("byte".equals(type)) {
                    return Byte.toString(Byte.parseByte(origValue, 2));
                } else if ("char".equals(type)) {
                    return "'"+Character.toString((char) Integer.parseInt(origValue, 2))+"'";
                } else {//if ("long".equals(type)) {
                    return Long.toString(Long.parseLong(origValue, 2))+"l";
                }
            default:
                return origValue;
            }
        } catch (NumberFormatException nfex) {
            return nfex.getLocalizedMessage();
        }
    }
    
    private boolean isIntegralType (Variable v) {
        if (!VariablesTreeModelFilter.isEvaluated(v)) {
            return false;
        }
        
        String type = v.getType ();
        return "int".equals (type) || 
            "char".equals (type) || 
            "byte".equals (type) || 
            "long".equals (type) || 
            "short".equals (type);
    }

    private boolean isIntegralTypeOrArray(Variable v) {
        if (!VariablesTreeModelFilter.isEvaluated(v)) {
            return false;
        }

        String type = removeArray(v.getType());
        return "int".equals (type) ||
            "char".equals (type) ||
            "byte".equals (type) ||
            "long".equals (type) ||
            "short".equals (type);
    }

    private static String removeArray(String type) {
        if (type.length() > 0 && type.endsWith("[]")) { // NOI18N
            return type.substring(0, type.length() - 2);
        } else {
            return type;
        }
    }

    private class DisplayAsAction extends AbstractAction 
    implements Presenter.Popup {

        private Variable variable;
        private String type;

        public DisplayAsAction(Variable variable) {
            this.variable = variable;
            this.type = removeArray(variable.getType());
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu 
                (NbBundle.getMessage(NumericDisplayFilter.class, "CTL_Variable_DisplayAs_Popup"));

            JRadioButtonMenuItem decimalItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Decimal",       // NOI18N
                    NumericDisplaySettings.DECIMAL
            );
            JRadioButtonMenuItem hexadecimalItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Hexadecimal",   // NOI18N
                    NumericDisplaySettings.HEXADECIMAL
            );
            JRadioButtonMenuItem octalItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Octal",         // NOI18N
                    NumericDisplaySettings.OCTAL
            );
            JRadioButtonMenuItem binaryItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Binary",        // NOI18N
                    NumericDisplaySettings.BINARY
            );
            JRadioButtonMenuItem charItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Character",     // NOI18N
                    NumericDisplaySettings.CHAR
            );
            JRadioButtonMenuItem timeItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Time",          // NOI18N
                    NumericDisplaySettings.TIME
            );

            NumericDisplaySettings lds = (NumericDisplaySettings) 
                variableToDisplaySettings.get (variable);
            if (lds != null) {
                switch (lds) {
                case DECIMAL:
                    decimalItem.setSelected (true);
                    break;
                case HEXADECIMAL:
                    hexadecimalItem.setSelected (true);
                    break;
                case OCTAL:
                    octalItem.setSelected (true);
                    break;
                case BINARY:
                    binaryItem.setSelected (true);
                    break;
                case CHAR:
                    charItem.setSelected (true);
                    break;
                case TIME:
                    timeItem.setSelected (true);
                    break;
                }
            } else {
                if ("char".equals(type)) {
                    charItem.setSelected(true);
                } else {
                    decimalItem.setSelected (true);
                }
            }

            displayAsPopup.add (decimalItem);
            displayAsPopup.add (hexadecimalItem);
            displayAsPopup.add (octalItem);
            displayAsPopup.add (binaryItem);
            displayAsPopup.add (charItem);
            if ("long".equals(type)) {
                displayAsPopup.add (timeItem);
            }
            return displayAsPopup;
        }

        private void onDisplayAs (NumericDisplaySettings how) {
            NumericDisplaySettings lds = (NumericDisplaySettings) 
                variableToDisplaySettings.get (variable);
            if (lds == null) {
                if ("char".equals(type)) {
                    lds = NumericDisplaySettings.CHAR;
                } else {
                    lds = NumericDisplaySettings.DECIMAL;
                }
            }
            if (lds == how) return;
            variableToDisplaySettings.put (variable, how);
            fireModelChanged ();
        }
        
        private void fireModelChanged () {
            if (listeners == null) return;
            ModelEvent evt = new ModelEvent.TableValueChanged(this, variable, null);
            for (Iterator i = listeners.iterator (); i.hasNext ();) {
                ModelListener listener = (ModelListener) i.next ();
                listener.modelChanged (evt);
            }
        }

        private class DisplayAsMenuItem extends JRadioButtonMenuItem {

            public DisplayAsMenuItem(final String message, final NumericDisplaySettings as) {
                super(new AbstractAction(NbBundle.getMessage(NumericDisplayFilter.class, message)) {
                        public void actionPerformed (ActionEvent e) {
                            onDisplayAs (as);
                        }
                    });
            }

        }

    }

    
}
