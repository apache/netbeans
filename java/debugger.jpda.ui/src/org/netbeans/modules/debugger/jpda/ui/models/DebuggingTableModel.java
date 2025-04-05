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

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=TableModel.class,
                             position=500)
public class DebuggingTableModel implements TableModel {

    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (columnID.equals("suspend")) {
            if (node instanceof JPDADVThread) {
                JPDADVThread dvt = (JPDADVThread) node;
                if (JPDAThread.STATE_ZOMBIE == dvt.getKey().getState()) {
                    return null;
                } else {
                    return dvt.isSuspended();
                }
            }
            return null;
        }
        throw new UnknownTypeException(node.toString());
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

}
