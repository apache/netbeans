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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Color;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableHTMLModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=TableHTMLModelFilter.class,
                                 position=50),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=TableHTMLModelFilter.class,
                                 position=50),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=TableHTMLModelFilter.class,
                                 position=50),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types=TableHTMLModelFilter.class,
                                 position=50)
})
public class VariablesTableHTMLModel implements TableHTMLModelFilter, Constants {

    @Override
    public boolean hasHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        if (original.hasHTMLValueAt(node, columnID)) {
            return true;
        }
        if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
             WATCH_VALUE_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorValueMsg(((Variable) node));
                if (errorMsg != null) {
                    return true;
                }
            }
        }
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorToStringMsg(((Variable) node));
                if (errorMsg != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        if (original.hasHTMLValueAt(node, columnID)) {
            return original.getHTMLValueAt(node, columnID);
        }
        if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
             WATCH_VALUE_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorValueMsg(((Variable) node));
                if (errorMsg != null) {
                    return BoldVariablesTableModelFilter.toHTML(">" + errorMsg + "<", false, false, Color.RED);
                }
            }
        }
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            if (node instanceof Variable) {
                String errorMsg = VariablesTableModel.getErrorToStringMsg(((Variable) node));
                if (errorMsg != null) {
                    return BoldVariablesTableModelFilter.toHTML(">" + errorMsg + "<", false, false, Color.RED);
                }
            }
        }
        return original.getHTMLValueAt(node, columnID);
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
}
