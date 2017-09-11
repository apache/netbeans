/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.launch;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.netbeans.lib.nbjshell.LaunchJDIAgent;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import org.netbeans.lib.nbjshell.RemoteJShellService;

/**
 *
 * @author sdedic
 */
public class DebugExecutionEnvironment extends LaunchJDIAgent implements RemoteJShellService, NbExecutionControl, ShellLaunchListener {
    private static final Logger LOG = Logger.getLogger(DebugExecutionEnvironment.class.getName());
    private static final String AGENT_VARVALUE_METHOD = "varValue"; // NOI18N
    private static final String AGENT_INVOKE_METHOD = "invoke"; // NOI18N
    private static final String REMOTE_AGENT_CLASS = "jdk.jshell.execution.RemoteExecutionControl"; // NOI18N

    private boolean added;
    private volatile JShellConnection shellConnection;
    private boolean closed;
    final ShellAgent agent;

    public DebugExecutionEnvironment(ShellAgent agent, ObjectOutput out, ObjectInput in, VirtualMachine vm, JShellConnection c) {
        super(out, in, vm);
        this.agent = agent;
        ShellLaunchManager.getInstance().addLaunchListener(this);
        this.shellConnection = c;
    }

    @Override
    public String getTargetSpec() {
        return null;
    }

    public JShellConnection getOpenedConnection() {
        synchronized (this) {
            return shellConnection;
        }
    }

    public ShellAgent getAgent() {
        return agent;
    }

    public boolean isClosed() {
        return closed;
    }

    public ObjectOutput getOut() {
        return out;
    }

    protected ObjectInput  getIn() {
        return in;
    }

    protected void shutdown() {
        agent.closeConnection(shellConnection);
        if (in == null) {
            return;
        }
        ShellLaunchManager.getInstance().removeLaunchListener(this);
        closeStreams();
    }

    @Override
    public void stop() {
        synchronized (getLock()) {
            if (isUserCodeRunning()) {
                sendStopUserCode();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        closeStreams();
    }

    public boolean sendStopUserCode() throws IllegalStateException {
        if (closed) {
            return false;
        }
        vm.suspend();
        try {
            ObjectReference myRef = getAgentObjectReference();

            OUTER:
            for (ThreadReference thread : vm.allThreads()) {
                // could also tag the thread (e.g. using name), to find it easier
                AGENT: for (StackFrame frame : thread.frames()) {
                    if (REMOTE_AGENT_CLASS.equals(frame.location().declaringType().name())) {
                        String n = frame.location().method().name();
                        if (AGENT_INVOKE_METHOD.equals(n) || AGENT_VARVALUE_METHOD.equals(n)) {
                            ObjectReference thiz = frame.thisObject();
                            if (myRef != null && myRef != thiz) {
                                break AGENT;
                            }
                            if (((BooleanValue) thiz.getValue(thiz.referenceType().fieldByName("inClientCode"))).value()) {
                                thiz.setValue(thiz.referenceType().fieldByName("expectingStop"), vm.mirrorOf(true));
                                ObjectReference stopInstance = (ObjectReference) thiz.getValue(thiz.referenceType().fieldByName("stopException"));
                                vm.resume();
                                thread.stop(stopInstance);
                                thiz.setValue(thiz.referenceType().fieldByName("expectingStop"), vm.mirrorOf(false));
                            }
                            return true;
                        }
                    }
                }
            }
        } catch (ClassNotLoadedException | IncompatibleThreadStateException | InvalidTypeException ex) {
            throw new IllegalStateException(ex);
        } finally {
            vm.resume();
        }
        return false;
    }

    public synchronized void closeStreams() {
        if (shellConnection == null) {
            return;
        }
        super.closeStreams();
        try {
            OutputStream os = shellConnection.getAgentInput();
            os.close();
        } catch (IOException ex) {
        }
        try {
            InputStream is = shellConnection.getAgentOutput();
            is.close();
        } catch (IOException ex) {
        }
        requestShutdown();
    }

    protected ObjectReference getAgentObjectReference() {
        return shellConnection.getAgentHandle();
    }

    @Override
    public boolean requestShutdown() {
        agent.closeConnection(shellConnection);
        return false;
    }

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) {
    }

    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) {
    }

    @Override
    public void connectionClosed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getConnection() != this.shellConnection || closed) {
                return;
            }
            closed = true;
        }
        shutdown();
    }

    @Override
    public void agentDestroyed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getAgent() != agent || closed) {
                return;
            }
            closed = true;
        }
        shutdown();
//        shellEnv.reportClosedBridge(reportSession, false);
    }
}
