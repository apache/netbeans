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
package org.netbeans.modules.maven.debug;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;


/**
 * Start the JPDA debugger
 * @author Milos Kleint
 */
public final class MavenJPDAStart {

    private static final RequestProcessor RP = new RequestProcessor(MavenJPDAStart.class.getName(), 1, true);
    
    /**
     * @parameter expression="${jpda.transport}"
     */
    private String transport = "dt_socket"; //NOI18N
    
    private String name;
    
    private String stopClassName;
    
    private String stopMethod;
    
    private ClassPath additionalSourcePath;
    
    
    private final Project project;
    private volatile Future<?> lastFuture;

    private MavenJPDAStart(Project p) {
        this.project = p;
    }

    /** Create and place into Project's Lookup.
     * @param p the project to associate this start with
     * @return new instance of the start infrastructure
     */
    public static MavenJPDAStart create(Project p) {
        return new MavenJPDAStart(p);
    }
    
    /**
     * returns the port/address that the debugger listens to..
     */
    public String execute(InputOutput io) throws Throwable {
        Future<?> prev = lastFuture;
        if (prev != null && !prev.isDone()) {
            io.getOut().print("Cancelling previous JPDA listening..."); //NOI18N
            if (prev.cancel(true)) {
                io.getOut().println("done"); //NOI18N
            } else {
                io.getOut().println("failed"); //NOI18N
            }
        }
        io.getOut().println("JPDA Listening Start..."); //NOI18N
        Future<String> future = RP.submit(() -> {
            return startDebugger(io);
        });
        lastFuture = future;
        return future.get();
    }
    
    private String startDebugger(InputOutput io) throws IOException, IllegalConnectorArgumentsException {
        String portOrAddress;
        ListeningConnector lc = null;
        Iterator i = Bootstrap.virtualMachineManager().
                listeningConnectors().iterator();
        for (; i.hasNext();) {
            lc = (ListeningConnector) i.next();
            Transport t = lc.transport();
            if (t != null && t.name().equals(getTransport())) {
                break;
            }
        }
        if (lc == null) {
            throw new RuntimeException
                    ("No trasports named " + getTransport() + " found!"); //NOI18N
        }
        // TODO: revisit later when http://developer.java.sun.com/developer/bugParade/bugs/4932074.html gets integrated into JDK
        // This code parses the address string "HOST:PORT" to extract PORT and then point debugee to localhost:PORT
        // This is NOT a clean solution to the problem but it SHOULD work in 99% cases
        final Map args = lc.defaultArguments();
        String address = lc.startListening(args);
        try {
            int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
//                    getProject ().setNewProperty (getAddressProperty (), "localhost:" + port);
            Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); //NOI18N
            portArg.setValue(port);
            portOrAddress = Integer.toString(port);
        } catch (NumberFormatException e) {
            // this address format is not known, use default
//                    getProject ().setNewProperty (getAddressProperty (), address);
            portOrAddress = address;
        }
        io.getOut().println("JPDA Address: " + address); //NOI18N
        io.getOut().println("Port:" + portOrAddress); //NOI18N

        ClassPath sourcePath = Utils.createSourcePath(project);
        if (getAdditionalSourcePath() != null) {
            sourcePath = ClassPathSupport.createProxyClassPath(sourcePath, getAdditionalSourcePath());
        }
        ClassPath jdkSourcePath = Utils.createJDKSourcePath(project);

        if (getStopClassName() != null && getStopClassName().length() > 0) {
            final MethodBreakpoint b = getStopMethod() != null ? Utils.createBreakpoint(getStopClassName(), getStopMethod()) : Utils.createBreakpoint(getStopClassName());
            final Listener list = new Listener(b);
            b.addPropertyChangeListener(MethodBreakpoint.PROP_VALIDITY, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    if (Breakpoint.VALIDITY.INVALID.equals(b.getValidity()) && getStopMethod() != null) {
                        //when the original method with method is not available (maybe defined in parent class?), replace it with a class breakpoint
                        DebuggerManager.getDebuggerManager().removeBreakpoint(b);
                        MethodBreakpoint b2 = Utils.createBreakpoint(getStopClassName());
                        list.replaceBreakpoint(b2);
                    }
                }
            });
            DebuggerManager.getDebuggerManager().addDebuggerListener(
                    DebuggerManager.PROP_DEBUGGER_ENGINES,
                    list);
        }

        final Map properties = new HashMap();
        properties.put("sourcepath", sourcePath); //NOI18N
        properties.put("name", getName()); //NOI18N
        properties.put("jdksources", jdkSourcePath); //NOI18N
        properties.put("baseDir", FileUtil.toFile(project.getProjectDirectory())); // NOI18N
        if (RunUtils.isCompileOnSaveEnabled(project)) {
            properties.put ("listeningCP", "sourcepath"); // NOI18N
        }

        final ListeningConnector flc = lc;
        
        lastFuture = RP.submit(() -> {
            try {
                JPDADebugger.startListening(flc, args,
                                            new Object[]{properties, project});
            } catch (DebuggerStartException ex) {
                io.getErr().println("Debugger Start Error."); //NOI18N
                Logger.getLogger(MavenJPDAStart.class.getName()).log(Level.INFO, "Debugger Start Error.", ex);
            }
        });

        return portOrAddress;
    }

    private static class Listener extends DebuggerManagerAdapter {
        
        private MethodBreakpoint breakpoint;
        private final Set debuggers = new HashSet();
        
        
        Listener(MethodBreakpoint breakpoint) {
            this.breakpoint = breakpoint;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (JPDADebugger.PROP_STATE.equals(e.getPropertyName())) {
                int state = ((Integer) e.getNewValue()).intValue();
                if ( (state == JPDADebugger.STATE_DISCONNECTED) ||
                        (state == JPDADebugger.STATE_STOPPED)
                        ) {
//                    RequestProcessor.getDefault ().post (new Runnable () {
//                        public void run () {
                    if (breakpoint != null) {
                        DebuggerManager.getDebuggerManager().
                                removeBreakpoint(breakpoint);
                        breakpoint = null;
                    }
//                        }
//                    });
                    dispose();
                }
            }
        }
        
        private void dispose() {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(
                    DebuggerManager.PROP_DEBUGGER_ENGINES,
                    this
                    );
            Iterator it = debuggers.iterator();
            while (it.hasNext()) {
                JPDADebugger d = (JPDADebugger) it.next();
                d.removePropertyChangeListener(
                        JPDADebugger.PROP_STATE,
                        this
                        );
            }
        }
        
        @Override
        public void engineAdded(DebuggerEngine engine) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) {
                return;
            }
            debugger.addPropertyChangeListener(
                    JPDADebugger.PROP_STATE,
                    this
                    );
            debuggers.add(debugger);
        }
        
        @Override
        public void engineRemoved(DebuggerEngine engine) {
            JPDADebugger debugger = engine.lookupFirst
                    (null, JPDADebugger.class);
            if (debugger == null) {
                return;
            }
            debugger.removePropertyChangeListener(
                    JPDADebugger.PROP_STATE,
                    this
                    );
            debuggers.remove(debugger);
        }

        private void replaceBreakpoint(MethodBreakpoint b2) {
            breakpoint = b2;
        }
        
        
    }
    
    public String getTransport() {
        return transport;
    }
    
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStopClassName() {
        return stopClassName;
    }
    
    public void setStopClassName(String stopClassName) {
        this.stopClassName = stopClassName;
    }

    public String getStopMethod() {
        return stopMethod;
    }

    public void setStopMethod(String stopMethod) {
        this.stopMethod = stopMethod;
    }

    public ClassPath getAdditionalSourcePath() {
        return additionalSourcePath;
    }

    public void setAdditionalSourcePath(ClassPath additionalSourcePath) {
        this.additionalSourcePath = additionalSourcePath;
    }
    
    
    
    
}
