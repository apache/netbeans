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

import java.awt.Color;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.swing.JTable;
import javax.swing.UIManager;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableHTMLModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;



/**
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=TableHTMLModelFilter.class,
                                 position=100),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=TableHTMLModelFilter.class,
                                 position=100),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=TableHTMLModelFilter.class,
                                 position=100),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types=TableHTMLModelFilter.class,
                                 position=100)
})
public class BoldVariablesTableModelFilter implements TableHTMLModelFilter,
Constants {
    
    private Map<Object, String> variableToValueType = new WeakHashMap<>();
    private Map<Object, String> variableToValueValue = new WeakHashMap<>();
    private Map<Object, String> variableToValueToString = new WeakHashMap<>();
    
    
    
    @Override
    public boolean hasHTMLValueAt(TableHTMLModel original, Object row, String columnID) throws UnknownTypeException {
        return true;
    }

    @Override
    public String getHTMLValueAt(TableHTMLModel original, Object row, String columnID) throws UnknownTypeException {
        if (original.hasHTMLValueAt(row, columnID)) {
            return original.getHTMLValueAt(row, columnID);
        }
        Object result = original.getValueAt (row, columnID);
        if ( LOCALS_TYPE_COLUMN_ID.equals (columnID) ||
             WATCH_TYPE_COLUMN_ID.equals (columnID)
        ) {
            return bold (row, (String) result, variableToValueType);
        }
        if (LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
            WATCH_VALUE_COLUMN_ID.equals (columnID)) {
            
            if (result instanceof Variable) {
                Variable var = (Variable) result;
                Object mirror = VariablesTableModel.getMirrorFor(var);
                if (mirror == null) {
                    String value = var.getValue();
                    return bold (row, value, variableToValueValue);
                } else {
                    if ("java.lang.String".equals(var.getType())) {             // NOI18N
                        String value = var.getValue();
                        value = adjustEscaped(value);
                        return bold (row, value, variableToValueValue);
                    }
                    // No HTML value, there's a special property editor that manages the value.
                    return null;
                }
            } else {
                if (result == null || result instanceof String) {
                    return bold (row, (String) result, variableToValueValue);
                } else {
                    // No HTML value, there's a special property editor that manages the value.
                    return null;
                }
            }
        }
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            return bold (row, (String) result, variableToValueToString);
        }
        return original.getHTMLValueAt(row, columnID);
    }
    
    private static String adjustEscaped(String text) {
        text = text.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"), "\\\\\\\\");
        StringBuffer sb = null;
        int j = 0;
        int n = text.length();
        boolean quotes = n > 1 && text.startsWith("\"") && text.endsWith("\"");
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            String replacement = null;
            if (c == '\n') {
                replacement = "\\n";
            } else if (c == '\r') {
                replacement = "\\r";
            } else if (c == '\f') {
                replacement = "\\f";
            } else if (c == '\b') {
                replacement = "\\b";
            } else if (c == '\t') {
                replacement = "\\t";
            } else if (c == '\f') {
                replacement = "\\f";
            } else if (c == '\'') {
                replacement = "\\\'";
            } else if (c == '\"') {
                if (!quotes || (i != 0) && i != (n - 1)) {
                    replacement = "\\\"";
                }
            }
            if (replacement != null) {
                if (sb == null) {
                    sb = new StringBuffer(text.substring(0, i));
                } else {
                    sb.append(text.substring(j, i));
                }
                sb.append(replacement);
                j = i+1;
            }
        }
        if (sb == null) {
            return text;
        } else {
            sb.append(text.substring(j));
            return sb.toString();
        }
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    @Override
    public void addModelListener (ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    @Override
    public void removeModelListener (ModelListener l) {
    }
    
    private String bold (Object variable, String value, Map<Object, String> map) {
        if (map.containsKey (variable)) {
            String oldValue = (String) map.get (variable);
            if (Objects.equals(oldValue, value)) {
                return toHTML (value, false, false, null);
            }
            map.put (variable, value);
            return toHTML (value, true, false, null);
        } else {
            map.put (variable, value);
            return toHTML (value, false, false, null);
        }
    }
    
    public static String toHTML (
        String text,
        boolean bold,
        boolean italics,
        Color color
    ) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder ();
        sb.append ("<html>");
        if (bold) sb.append ("<b>");
        if (italics) sb.append ("<i>");
        if (color == null) {
            color = UIManager.getColor("Table.foreground");
            if (color == null) {
                color = new JTable().getForeground();
            }
        }
        sb.append ("<font color=\"#");
        String hexColor = Integer.toHexString ((color.getRGB () & 0xffffff));
        for (int i = hexColor.length(); i < 6; i++) {
            sb.append("0"); // Prepend zeros to length of 6
        }
        sb.append(hexColor);
        sb.append ("\">");
        text = text.replace ("&", "&amp;");
        text = text.replace ("<", "&lt;");
        text = text.replace (">", "&gt;");
        sb.append (text);
        sb.append ("</font>");
        if (italics) sb.append ("</i>");
        if (bold) sb.append ("</b>");
        sb.append ("</html>");
        return sb.toString ();
    }
}
