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

package org.netbeans.modules.debugger.jpda.truffle.vars.models;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleVariableImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_VALUE_COLUMN_ID;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableHTMLModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types = {TableModelFilter.class, TableHTMLModelFilter.class}),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ResultsView", types = {TableModelFilter.class, TableHTMLModelFilter.class}),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ToolTipView", types = {TableModelFilter.class, TableHTMLModelFilter.class}),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/WatchesView", types = {TableModelFilter.class, TableHTMLModelFilter.class})
})
public class TruffleVariablesTableModel implements TableModelFilter, TableHTMLModelFilter {
    
    private final JPDADebugger debugger;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    
    public TruffleVariablesTableModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    private static TruffleVariable getTruffleVariable(Object node) {
        if (node instanceof Variable) {
            return TruffleVariableImpl.get((Variable) node);
        } else if (node instanceof TruffleVariable) {
            return (TruffleVariable) node;
        } else {
            return null;
        }
    }

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof TruffleScope) {
            return "";
        }
        TruffleVariable tv = getTruffleVariable(node);
        if (tv != null) {
            switch (columnID) {
                case LOCALS_TYPE_COLUMN_ID:
                case WATCH_TYPE_COLUMN_ID:
                    return tv.getType();
                case LOCALS_VALUE_COLUMN_ID:
                case WATCH_VALUE_COLUMN_ID:
                    return tv.getValue();
                case LOCALS_TO_STRING_COLUMN_ID:
                case WATCH_TO_STRING_COLUMN_ID:
                    Object var = tv.getValue();
                    return String.valueOf(var);
            }
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof TruffleScope) {
            return true;
        }
        if (node instanceof TruffleVariable) {
            if (LOCALS_VALUE_COLUMN_ID.equals(columnID) || WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return !((TruffleVariable) node).isWritable();
            } else {
                return true;
            }
        }
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            boolean success = ((TruffleVariable) node).setValue(debugger, value.toString()) != null;
            if (success) {
                ModelEvent evt = new ModelEvent.NodeChanged(this, node);
                for (ModelListener l : listeners) {
                    l.modelChanged(evt);
                }
            }
        } else {
            original.setValueAt(node, columnID, value);
        }
    }

    @Override
    public boolean hasHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            switch (columnID) {
                case LOCALS_TYPE_COLUMN_ID:
                case WATCH_TYPE_COLUMN_ID:
                    return true;
            }
        }
        return original.hasHTMLValueAt(node, columnID);
    }

    @Override
    public String getHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
            switch (columnID) {
                case LOCALS_TYPE_COLUMN_ID:
                case WATCH_TYPE_COLUMN_ID:
                    TruffleVariable tv = getTruffleVariable(node);
                    if (tv != null) {
                        LanguageName frameLanguage = LanguageName.NONE;
                        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger.getCurrentThread());
                        if (currentPCInfo != null) {
                            TruffleStackFrame selectedStackFrame = currentPCInfo.getSelectedStackFrame();
                            frameLanguage = selectedStackFrame != null ? selectedStackFrame.getLanguage() : LanguageName.NONE;
                        }
                        LanguageName valueLanguage = tv.getLanguage();
                        if (!LanguageName.NONE.equals(valueLanguage) && !frameLanguage.equals(valueLanguage)) {
                            return toHTML(valueLanguage.getName(), tv.getType());
                        }
                    }
        }
        return original.getHTMLValueAt(node, columnID);
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    public static String toHTML(String id, String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder ();
        sb.append("<html>");
        Color color = UIManager.getColor("Table.foreground");
        if (color == null) {
            color = new JTable().getForeground();
        }
        sb.append("<font color=\"#");
        String hexColor = Integer.toHexString ((color.getRGB () & 0xffffff));
        for (int i = hexColor.length(); i < 6; i++) {
            sb.append("0"); // Prepend zeros to length of 6
        }
        sb.append(hexColor);
        sb.append("\">");
        
        sb.append("<font color=\"#808080\">[");
        sb.append(id);
        sb.append("]</font> ");
        
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        sb.append(text);
        sb.append("</font>");
        sb.append("</html>");
        return sb.toString ();
    }
}
