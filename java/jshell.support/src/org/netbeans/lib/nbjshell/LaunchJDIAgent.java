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
package org.netbeans.lib.nbjshell;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.execution.JdiExecutionControl;
import jdk.jshell.execution.JdiInitiator;
import jdk.jshell.execution.Util;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 * Launches a JShell VM using standard JDI agent, but incorporates
 * a customized agent class.
 *
 * @author sdedic
 */
// PENDING: JDIExecutionControl does not bring that much - copy over and derive
// from NbExecutionControlBase to provide an uniform API
public class LaunchJDIAgent extends JdiExecutionControl
    implements ExecutionControl, RemoteJShellService, NbExecutionControl{

    private static final Logger LOG = Logger.getLogger(LaunchJDIAgent.class.getName());

    private static final String REMOTE_AGENT =  "org.netbeans.lib.jshell.agent.AgentWorker"; // NOI18N

    protected final ObjectInput in;
    protected final ObjectOutput out;

    public LaunchJDIAgent(ObjectOutput out, ObjectInput in, VirtualMachine vm) {
        super(out, in);
        this.in = in;
        this.out = out;
        this.vm = vm;
    }

    /**
     * Create an instance.
     *
     * @param cmdout the output for commands
     * @param cmdin the input for responses
     */
    private LaunchJDIAgent(ObjectOutput cmdout, ObjectInput cmdin,
            VirtualMachine vm, Process process, List<Consumer<String>> deathListeners) {
        this(cmdout, cmdin, vm);
        this.process = process;
        deathListeners.add(s -> disposeVM());
    }

    protected VirtualMachine vm;
    private Process process;

    private final Object STOP_LOCK = new Object();
    private boolean userCodeRunning = false;
    private boolean closed = false;

    @Override
    public void closeStreams() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ex) {
            // ignore
        }
    }
    
    protected void notifyClosed() {
        closeStreams();
    }

    public Map<String, String> commandVersionInfo() {
        Map<String, String> result = new HashMap<>();
        try {
            Object o = extensionCommand("nb_vmInfo", null);
            if (!(o instanceof Map)) {
                return Collections.emptyMap();
            }
            result = (Map<String, String>)o;
        } catch (RunException | InternalException ex) {
            LOG.log(Level.INFO, "Error invoking JShell agent", ex.toString());
        } catch (EngineTerminationException ex) {
            notifyClosed();
        }
        return result;
    }

    /**
     * Returns the agent's object reference obtained from the debugger.
     * May return null, so the {@link #sendStopUserCode()} will stop the first
     * running agent it finds.
     * 
     * @return the target agent's reference
     */
    protected ObjectReference  getAgentObjectReference() {
        return null;
    }
    
    @Override
    public boolean requestShutdown() {
        disposeVM();
        return true;
    }
    
    public boolean isClosed() {
        return vm == null;
    }

    @Override
    public String getTargetSpec() {
        return null;
    }

    /**
     * Creates an ExecutionControl instance based on a JDI
     * {@code LaunchingConnector}.
     *
     * @return the generator
     */
    public static ExecutionControlProvider launch(JavaPlatform platform) {
        return new ExecutionControlProvider() {
            @Override
            public String name() {
                return getClass().getName();
            }

            @Override
            public ExecutionControl generate(ExecutionEnv ee, Map<String, String> map) throws Throwable {
                return create(platform, ee, true, map);
            }
        };
    }

    /**
     * Creates an ExecutionControl instance based on a JDI
     * {@code ListeningConnector} or {@code LaunchingConnector}.
     *
     * Initialize JDI and use it to launch the remote JVM. Set-up a socket for
     * commands and results. This socket also transports the user
     * input/output/error.
     *
     * @param env the context passed by
     * {@link jdk.jshell.spi.ExecutionControl#start(jdk.jshell.spi.ExecutionEnv) }
     * @return the channel
     * @throws IOException if there are errors in set-up
     */
    private static JdiExecutionControl create(JavaPlatform platform, ExecutionEnv env, boolean isLaunch, Map<String, String> customArgs) throws IOException {
        try (final ServerSocket listener = new ServerSocket(0)) {
            // timeout after 60 seconds
            listener.setSoTimeout(60000);
            int port = listener.getLocalPort();

            Map<String, String> customArguments = new HashMap<>();
            if (customArgs != null) {
                customArguments.putAll(customArgs);
            }
            
            if (platform != null) {
                String jHome = platform.getSystemProperties().get("java.home");
                // TODO: if jHome is null for some reason, the connector fails.
                customArguments.put("home", jHome);
            }
            String loopback = InetAddress.getLoopbackAddress().getHostAddress();
            // Set-up the JDI connection
            JdiInitiator jdii = new JdiInitiator(port,
                    env.extraRemoteVMOptions(), REMOTE_AGENT, isLaunch, loopback, 
                    5000, customArguments);
            VirtualMachine vm = jdii.vm();
            Process process = jdii.process();
            
            List<Consumer<String>> deathListeners = new ArrayList<>();
            deathListeners.add(s -> env.closeDown());
            
            vm.resume();

            // Set-up the commands/reslts on the socket.  Piggy-back snippet
            // output.
            Socket socket = listener.accept();
            // out before in -- match remote creation so we don't hang
            Map<String, OutputStream> io = new HashMap<>();
            CloseFilter outFilter = new CloseFilter(env.userOut());
            io.put("out", outFilter);
            io.put("err", env.userErr());

            /*
            class L implements BiFunction<ObjectInput, ObjectOutput, ExecutionControl> {
                LaunchJDIAgent  agent;
                
                ExecutionControl forward() throws IOException {
                    return Util.remoteInputOutput(
                        socket.getInputStream(), 
                        socket.getOutputStream(),
                        io,
                        null, this);
                }

                @Override
                public ExecutionControl apply(ObjectInput cmdIn, ObjectOutput cmdOut) {
                    agent = new LaunchJDIAgent(cmdout, cmdIn, vm, process, deathListeners);
                    return agent;
                }
                
            }
            */

            LaunchJDIAgent agent = (LaunchJDIAgent)
                    Util.remoteInputOutput(
                        socket.getInputStream(), 
                        socket.getOutputStream(),
                        io,
                        Collections.emptyMap(), 
                        (ObjectInput cmdIn, ObjectOutput cmdOut) ->
                                new LaunchJDIAgent(cmdOut, cmdIn, vm, process, deathListeners)
                    );
            Util.detectJdiExitEvent(vm, s -> {
                for (Consumer<String> h : deathListeners) {
                    h.accept(s);
                }
                agent.disposeVM();
            });
            outFilter.agent = agent;
            return agent;
        }
    }
    
    static class CloseFilter extends FilterOutputStream {
        volatile LaunchJDIAgent agent;
        
        public CloseFilter(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (agent != null) {
                agent.notifyClosed();
            }
        }
    }

    @Override
    public void addToClasspath(String path) throws EngineTerminationException, InternalException {
        if (!suppressClasspath) {
            super.addToClasspath(path);
        }
    }
    
    

    @Override
    public String invoke(String classname, String methodname)
            throws ExecutionControl.RunException,
            ExecutionControl.EngineTerminationException, ExecutionControl.InternalException {
        String res;
        synchronized (STOP_LOCK) {
            userCodeRunning = true;
        }
        try {
            res = super.invoke(classname, methodname);
        } finally {
            synchronized (STOP_LOCK) {
                userCodeRunning = false;
            }
        }
        return res;
    }
    
    protected boolean isUserCodeRunning() {
        return userCodeRunning;
    }
    
    protected Object getLock() {
        return STOP_LOCK;
    }

    /**
     * Interrupts a running remote invoke by manipulating remote variables
     * and sending a stop via JDI.
     *
     * @throws EngineTerminationException the execution engine has terminated
     * @throws InternalException an internal problem occurred
     */
    @Override
    public void stop() throws ExecutionControl.EngineTerminationException, ExecutionControl.InternalException {
        synchronized (STOP_LOCK) {
            if (!userCodeRunning) {
                return;
            }

            vm().suspend();
            try {
                OUTER:
                for (ThreadReference thread : vm().allThreads()) {
                    // could also tag the thread (e.g. using name), to find it easier
                    for (StackFrame frame : thread.frames()) {
                        if (REMOTE_AGENT.equals(frame.location().declaringType().name()) &&
                                (    "invoke".equals(frame.location().method().name())
                                || "varValue".equals(frame.location().method().name()))) {
                            ObjectReference thiz = frame.thisObject();
                            com.sun.jdi.Field inClientCode = thiz.referenceType().fieldByName("inClientCode");
                            com.sun.jdi.Field expectingStop = thiz.referenceType().fieldByName("expectingStop");
                            com.sun.jdi.Field stopException = thiz.referenceType().fieldByName("stopException");
                            if (((BooleanValue) thiz.getValue(inClientCode)).value()) {
                                thiz.setValue(expectingStop, vm().mirrorOf(true));
                                ObjectReference stopInstance = (ObjectReference) thiz.getValue(stopException);

                                vm().resume();
                                debug("Attempting to stop the client code...\n");
                                thread.stop(stopInstance);
                                thiz.setValue(expectingStop, vm().mirrorOf(false));
                            }

                            break OUTER;
                        }
                    }
                }
            } catch (ClassNotLoadedException | IncompatibleThreadStateException | InvalidTypeException ex) {
                throw new ExecutionControl.InternalException("Exception on remote stop: " + ex);
            } finally {
                vm().resume();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        disposeVM();
    }

    private synchronized void disposeVM() {
        if (process != null) {
            try {
                if (vm != null) {
                    vm.dispose(); // This could NPE, so it is caught below
                    vm = null;
                }
            } catch (VMDisconnectedException ex) {
                // Ignore if already closed
            } catch (Throwable ex) {
                debug(ex, "disposeVM");
            } finally {
                if (process != null) {
                    process.destroy();
                    process = null;
                }
            }
        } else {
            vm = null;
        }
    }

    @Override
    protected synchronized VirtualMachine vm() throws ExecutionControl.EngineTerminationException {
        if (vm == null) {
            throw new ExecutionControl.EngineTerminationException("VM closed");
        } else {
            return vm;
        }
    }

    /**
     * Log debugging information. Arguments as for {@code printf}.
     *
     * @param format a format string as described in Format string syntax
     * @param args arguments referenced by the format specifiers in the format
     * string.
     */
    private static void debug(String format, Object... args) {
        // Reserved for future logging
    }

    /**
     * Log a serious unexpected internal exception.
     *
     * @param ex the exception
     * @param where a description of the context of the exception
     */
    private static void debug(Throwable ex, String where) {
        // Reserved for future logging
    }
    
    private boolean suppressClasspath;

    @Override
    public void suppressClasspathChanges(boolean b) {
        this.suppressClasspath = b;
    }

    @Override
    public ExecutionControlException getBrokenException() {
        return null;
    }

}
