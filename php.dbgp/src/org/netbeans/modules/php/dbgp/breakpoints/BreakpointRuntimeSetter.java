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
        performCommand((Breakpoint) source, Lazy.UPDATE_COMMAND);
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
