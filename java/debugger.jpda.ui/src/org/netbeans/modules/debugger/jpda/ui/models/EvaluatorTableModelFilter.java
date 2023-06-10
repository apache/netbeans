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


import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executor;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import static org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL.CHILDREN;
import static org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL.DISPLAY_NAME;
import static org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL.SHORT_DESCRIPTION;
import static org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL.VALUE;
import static org.netbeans.spi.viewmodel.AsynchronousModelFilter.CURRENT_THREAD;

@DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                             types = { TableModelFilter.class, AsynchronousModelFilter.class },
                             position=330)
public class EvaluatorTableModelFilter implements TableModelFilter, AsynchronousModelFilter {

    private final Executor rp;
    private final Collection<ModelListener> listeners = new HashSet<>();

    public EvaluatorTableModelFilter(ContextProvider lookupProvider) {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
                lookupFirst(null, JPDADebugger.class);
        rp = debugger.getRequestProcessor();
    }

    @Override
    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }

    @Override
    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }

    @Override
    @SuppressWarnings("ConvertToStringSwitch")
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem) {
            DefaultHistoryItem item = (DefaultHistoryItem) node;
            if (Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return item.getToStringValue();
            } else if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return item.getType();
            } else if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
                return item.getValue();
            }
            return ""; // NOI18N
        }
        if (node instanceof EvaluatorTreeModel.HistoryNode) {
            return "";
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem || node instanceof EvaluatorTreeModel.HistoryNode) {
            return true;
        }
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem || node instanceof EvaluatorTreeModel.HistoryNode) {
            return;
        }
        original.setValueAt(node, columnID, value);
    }

    @Override
    public Executor asynchronous(Executor exec, CALL asynchCall, Object object) {
        switch (asynchCall) {
            case VALUE:
            case CHILDREN:
            case SHORT_DESCRIPTION:
                return rp;
            case DISPLAY_NAME:
                return CURRENT_THREAD;
        }
        return null; // ??
    }
}
