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
