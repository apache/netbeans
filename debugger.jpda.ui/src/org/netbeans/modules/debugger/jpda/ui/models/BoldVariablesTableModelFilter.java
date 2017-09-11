/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Color;
import java.util.Map;
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
    
    private Map variableToValueType = new WeakHashMap ();
    private Map variableToValueValue = new WeakHashMap ();
    private Map variableToValueToString = new WeakHashMap ();
    
    
    
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
    
    private String bold (Object variable, String value, Map map) {
        if (map.containsKey (variable)) {
            String oldValue = (String) map.get (variable);
            if (oldValue == value ||
                oldValue != null && oldValue.equals (value)) {
                
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
        text = text.replaceAll ("&", "&amp;");
        text = text.replaceAll ("<", "&lt;");
        text = text.replaceAll (">", "&gt;");
        sb.append (text);
        sb.append ("</font>");
        if (italics) sb.append ("</i>");
        if (bold) sb.append ("</b>");
        sb.append ("</html>");
        return sb.toString ();
    }
}
