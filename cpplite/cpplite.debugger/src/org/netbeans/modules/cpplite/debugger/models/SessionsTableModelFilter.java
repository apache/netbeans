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
package org.netbeans.modules.cpplite.debugger.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="SessionsView", types=TableModelFilter.class)
public class SessionsTableModelFilter implements TableModelFilter, Constants {

    private final List<Session> sessionListeners = new ArrayList<>();
    private final List<ModelListener> modelListeners = new CopyOnWriteArrayList<>();

    public SessionsTableModelFilter() {}

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Session && isCPPSession((Session) node)) {
            if (SESSION_STATE_COLUMN_ID.equals (columnID)) {
                return getSessionState ((Session) node);
            } else
            if (SESSION_LANGUAGE_COLUMN_ID.equals (columnID)) {
                return node;
            } else
            if (SESSION_HOST_NAME_COLUMN_ID.equals (columnID)) {
                return ((Session) node).getLocationName ();
            } else {
                throw new UnknownTypeException (node);
            }
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Session && isCPPSession((Session) node)) {
            if (SESSION_STATE_COLUMN_ID.equals (columnID)) {
                return true;
            } else
            if (SESSION_LANGUAGE_COLUMN_ID.equals (columnID)) {
                return true;
            } else
            if (SESSION_HOST_NAME_COLUMN_ID.equals (columnID)) {
                return true;
            } else {
                throw new UnknownTypeException (node);
            }
        }
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    private static boolean isCPPSession(Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null) {
            return false;
        }
        CPPLiteDebugger d = e.lookupFirst(null, CPPLiteDebugger.class);
        return d != null;
    }

    @NbBundle.Messages({"MSG_Session_State_Starting=Starting",
                        "MSG_Session_State_Finished=Finished",
                        "MSG_Session_State_Running=Running",
                        "MSG_Session_State_Stopped=Stopped"})
    private String getSessionState (Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null) {
            return Bundle.MSG_Session_State_Starting();
        }
        CPPLiteDebugger d = e.lookupFirst(null, CPPLiteDebugger.class);
        if (d.isFinished()) {
            return Bundle.MSG_Session_State_Finished();
        }
        synchronized (sessionListeners) {
            if (!sessionListeners.contains(s)) {
                CPPLiteDebugger.StateListener asl = new SessionStateListener(s, d);
                d.addStateListener(asl);
                sessionListeners.add(s);
            }
        }
        if (d.isSuspended()) {
            return Bundle.MSG_Session_State_Stopped();
        } else {
            return Bundle.MSG_Session_State_Running();
        }
    }

    private void fireModelChanged(Object node) {
        ModelEvent me = new ModelEvent.TableValueChanged(this, node, SESSION_STATE_COLUMN_ID);
        for (ModelListener ml : modelListeners) {
            ml.modelChanged(me);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        modelListeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        modelListeners.remove(l);
    }

    private class SessionStateListener implements CPPLiteDebugger.StateListener {

        private final Session s;
        private final CPPLiteDebugger d;

        SessionStateListener(Session s, CPPLiteDebugger d) {
            this.s = s;
            this.d = d;
        }

        @Override
        public void suspended(boolean suspended) {
            fireModelChanged(s);
        }

        @Override
        public void finished() {
            fireModelChanged(s);
            d.removeStateListener(this);
            synchronized (sessionListeners) {
                sessionListeners.remove(s);
            }
        }

        @Override
        public void currentThread(CPPThread thread) {
        }

        @Override
        public void currentFrame(CPPFrame frame) {
        }

    }

}
