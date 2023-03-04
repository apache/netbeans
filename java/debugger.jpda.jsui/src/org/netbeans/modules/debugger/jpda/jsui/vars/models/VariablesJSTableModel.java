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

package org.netbeans.modules.debugger.jpda.jsui.vars.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.js.vars.DebuggerSupport;
import org.netbeans.modules.debugger.jpda.js.vars.JSVariable;
import org.netbeans.modules.debugger.jpda.js.vars.ScopeVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import static org.netbeans.spi.debugger.ui.Constants.*;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/LocalsView",  types = TableModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/ResultsView", types = TableModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/ToolTipView", types = TableModelFilter.class, position = 850),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/WatchesView", types = TableModelFilter.class, position = 850)
})
public class VariablesJSTableModel implements TableModelFilter {
    
    private final JPDADebugger debugger;
    
    public VariablesJSTableModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof JPDAWatch && !isEnabled((JPDAWatch) node)) {
            return original.getValueAt(node, columnID);
        }
        if (node instanceof JSWatchVar) {
            JSWatchVar jswv = (JSWatchVar) node;
            JSVariable jsVar = jswv.getJSVar();
            if (jsVar != null) {
                node = jsVar;
            } else {
                node = jswv.getWatch();
            }
        }
        if (node instanceof JSVariable) {
            JSVariable jsVar = (JSVariable) node;
            switch (columnID) {
                case LOCALS_TYPE_COLUMN_ID:
                case WATCH_TYPE_COLUMN_ID:
                    return "";
                case LOCALS_VALUE_COLUMN_ID:
                case WATCH_VALUE_COLUMN_ID:
                case LOCALS_TO_STRING_COLUMN_ID:
                case WATCH_TO_STRING_COLUMN_ID:
                    return jsVar.getValue();
            }
        } else if (node instanceof ScopeVariable) {
            return "";
        } else if (node instanceof ObjectVariable) {
            switch (columnID) {
                case LOCALS_TYPE_COLUMN_ID:
                case WATCH_TYPE_COLUMN_ID:
                    return "";
                case LOCALS_VALUE_COLUMN_ID:
                case WATCH_VALUE_COLUMN_ID:
                case LOCALS_TO_STRING_COLUMN_ID:
                case WATCH_TO_STRING_COLUMN_ID:
                    if (node instanceof JPDAWatch) {
                        String excDescr = ((JPDAWatch) node).getExceptionDescription();
                        if (excDescr != null) {
                            int i = excDescr.indexOf('\n');
                            if (i > 0) {
                                excDescr = excDescr.substring(0, i);
                            }
                            return excDescr;
                        }
                    }
                    return DebuggerSupport.getVarValue(debugger, (ObjectVariable) node);
            }
        }
        return original.getValueAt(node, columnID);
    }
    
    private boolean isEnabled(JPDAWatch jw) {
        try {
            // This is clearly missing in the APIs:
            Method getWatchMethod = jw.getClass().getMethod("getWatch");
            getWatchMethod.setAccessible(true);
            Watch w = (Watch) getWatchMethod.invoke(jw);
            return w.isEnabled();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return true;
        }
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof JSVariable || node instanceof ScopeVariable || node instanceof JSWatchVar) {
            return true;
        }
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        if (node instanceof JSVariable || node instanceof JSWatchVar) {
            return ;
        }
        original.setValueAt(node, columnID, value);
    }

    @Override
    public void addModelListener(ModelListener l) {
        
    }

    @Override
    public void removeModelListener(ModelListener l) {
        
    }
    
}
