/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.debug;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
public class JPDAStart implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(JPDAStart.class);
    
    /**
     * @parameter expression="${jpda.transport}"
     */
    private String transport = "dt_socket"; //NOI18N
    
    private String name;
    
    private String stopClassName;
    
    private String stopMethod;
    
    private ClassPath additionalSourcePath;
    
    
    private final Object[] lock = new Object[2];
    
    private Project project;
    private final String actionName;
    private final InputOutput io;

    JPDAStart(InputOutput inputOutput, String actionName) {
        io = inputOutput;
        this.actionName = actionName;
    }
    
    /**
     * returns the port/address that the debugger listens to..
     */
    public String execute(Project project) throws Throwable {
        this.project = project;
        io.getOut().println("JPDA Listening Start..."); //NOI18N
//            getLog().debug("Entering synch lock"); //NOI18N
        synchronized (lock) {
//                getLog().debug("Entered synch lock"); //NOI18N
            RP.post(this);
//                    getLog().debug("Entering wait"); //NOI18N
            lock.wait();
//                    getLog().debug("Wait finished"); //NOI18N
            if (lock[1] != null) {
                throw ((Throwable) lock[1]); //NOI18N
            }
        }
        return (String)lock[0];
    }
    
    @Override
    public void run() {
        synchronized (lock) {
            
            try {
                
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
                    lock[0] = Integer.toString(port);
                } catch (NumberFormatException e) {
                    // this address format is not known, use default
//                    getProject ().setNewProperty (getAddressProperty (), address);
                    lock[0] = address;
                }
                io.getOut().println("JPDA Address: " + address); //NOI18N
                io.getOut().println("Port:" + lock[0]); //NOI18N
                
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
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            JPDADebugger.startListening(flc, args,
                                                        new Object[]{properties, project});
                        }
                        catch (DebuggerStartException ex) {
                            io.getErr().println("Debugger Start Error."); //NOI18N
                            Logger.getLogger(JPDAStart.class.getName()).log(Level.INFO, "Debugger Start Error.", ex);
                        }
                    }
                });
            } catch (java.io.IOException ioex) {
                io.getErr().println("IO Error:"); //NOI18N
//                org.openide.ErrorManager.getDefault().notify(ioex);
                lock[1] = ioex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                io.getErr().println("Illegal Connector"); //NOI18N
                lock[1] = icaex;
            } finally {
                lock.notify();
            }
        }
        
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
