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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.packets.BrkpntRemoveCommand;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.State;
import org.netbeans.modules.php.dbgp.packets.BrkpntUpdateCommand;

/**
 * Responsible for setting breakpoints while debugging. ( Otherwise breakpoints
 * are used that was set before debugger start ).
 *
 * @author ads
 *
 */
public class BreakpointRuntimeSetter extends DebuggerManagerAdapter {

    @Override
    public String[] getProperties() {
        return new String[]{DebuggerManager.PROP_BREAKPOINTS};
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        breakpoint.addPropertyChangeListener(Breakpoint.PROP_ENABLED, this);
        performCommand(breakpoint, Lazy.SET_COMMAND);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        breakpoint.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
        performCommand(breakpoint, Lazy.REMOVE_COMMAND);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!Breakpoint.PROP_ENABLED.equals(event.getPropertyName())) {
            return;
        }
        Object source = event.getSource();

        if (((Breakpoint) source).isEnabled()) {
            performCommand((Breakpoint) source, Lazy.SET_COMMAND);
        } else {
            performCommand((Breakpoint) source, Lazy.REMOVE_COMMAND);
        }
    }

    private void performCommand(Breakpoint breakpoint, Command command) {
        if (!(breakpoint instanceof AbstractBreakpoint)) {
            return;
        }
        AbstractBreakpoint bpoint = (AbstractBreakpoint) breakpoint;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session sess : sessions) {
            SessionId id = (SessionId) sess.lookupFirst(null, SessionId.class);
            if (id == null) {
                continue;
            }
            SessionManager sessionManager = SessionManager.getInstance();
            Collection<DebugSession> collection = sessionManager.findSessionsById(id);
            for (DebugSession debugSession : collection) {
                command.perform(bpoint, id, debugSession);
            }
        }
    }

    private interface Command {

        void perform(AbstractBreakpoint breakpoint, SessionId id, DebugSession session);

    }

    private static class SetBreakpointCommand implements Command {
        @Override
        public void perform(AbstractBreakpoint breakpoint, SessionId id, DebugSession session) {
            if (session != null) {
                BrkpntSetCommand command = Utils.getCommand(session, id, breakpoint);
                if (command != null) {
                    session.sendCommandLater(command);
                }
            }
        }

    }

    private static class RemoveBreakpointCommand implements Command {

        @Override
        public void perform(AbstractBreakpoint breakpoint, SessionId id, DebugSession session) {
            if (!breakpoint.isSessionRelated(session)) {
                return;
            }
            BrkpntRemoveCommand command = new BrkpntRemoveCommand(session.getTransactionId(), breakpoint.getBreakpointId());
            session.sendCommandLater(command);
        }

    }

    private static class UpdateBreakpointCommand implements Command {
        private static final Logger LOGGER = Logger.getLogger(UpdateBreakpointCommand.class.getName());

        @Override
        public void perform(AbstractBreakpoint breakpoint, SessionId id,
                DebugSession session) {
            if (session != null && breakpoint != null) {
                BrkpntUpdateCommand command = new BrkpntUpdateCommand(session.getTransactionId(), breakpoint.getBreakpointId());
                State state = breakpoint.isEnabled() ? State.ENABLED : State.DISABLED;
                command.setState(state);
                session.sendCommandLater(command);
            } else {
                LOGGER.log(Level.FINE, "Session and Breakpoint can't be null! Session: {0} || Breakpoint: {1}", new Object[]{session, breakpoint});
            }
        }

    }

    private static class Lazy {

        static final Command SET_COMMAND = new SetBreakpointCommand();
        static final Command REMOVE_COMMAND = new RemoveBreakpointCommand();
        static final Command UPDATE_COMMAND = new UpdateBreakpointCommand();

    }

}
