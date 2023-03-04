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

package org.netbeans.modules.web.javascript.debugger.sessions;

import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import static org.netbeans.spi.debugger.ui.Constants.*;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "Session_running=Running",
    "Session_paused=Paused",
    "Session_none="
})
@DebuggerServiceRegistration(path="SessionsView", types=TableModelFilter.class)
public final class SessionsModel extends ViewModelSupport implements TableModelFilter, Debugger.Listener  {

    private Debugger d;
    
    public SessionsModel() {
    }

    @Override
    public Object getValueAt(TableModel original, Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Session && isDebuggerSession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals(columnID)) {
                return getSessionState((Session) row);
            }
        }
        return original.getValueAt(row, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Session && isDebuggerSession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals(columnID)) {
                return true;
            }
        }
        return original.isReadOnly(row, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object row, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(row, columnID, value);
    }

    private static boolean isDebuggerSession(final Session s) {
        DebuggerEngine e = s.getCurrentEngine();
        if (e == null) {
            return false;
        }
        return e.lookupFirst(null, Debugger.class) != null;
    }
    
    private Object getSessionState(final Session s) {
        Debugger debugger = s.getCurrentEngine().lookupFirst(null, Debugger.class);
        if (debugger == null) {
            return "";
        }
        if (d == null) {
            d = debugger;
            d.addListener(this);
        }
        if (debugger.isEnabled()) {
            if (debugger.isSuspended()) {
                return Bundle.Session_paused();
            } else {
                return Bundle.Session_running();
            }
        }
        return Bundle.Session_none();
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        refresh();
    }

    @Override
    public void resumed() {
        refresh();
    }

    @Override
    public void reset() {
    }

    @Override
    public void enabled(boolean enabled) {
    }
    
}
