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

package org.netbeans.modules.debugger.jpda.console;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.connect.Connector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextChild;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LaunchingDICookie;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.modules.debugger.jpda.DebuggerConsoleIO;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.util.NbBundle;


/**
 * Listens on
 * {@link org.netbeans.api.debugger.ActionsManagerListener#PROP_ACTION_PERFORMED} and
 * {@link org.netbeans.api.debugger.jpda.JPDADebugger#PROP_STATE}
 * properties and writes some messages to Debugger Console.
 *
 * @author   Jan Jancura
 */
public class DebuggerOutput implements PropertyChangeListener {


    // set of all IOManagers
    private static Set<IOManager> managers = new HashSet<IOManager>();
    
    private JPDADebugger        debugger;
    private IOManager           ioManager;
    private ContextProvider     contextProvider;
    
    private static final String PROP_OPERATIONS_UPDATE = "operationsUpdate"; // NOI18N
    private static final String PROP_OPERATIONS_SET = "operationsSet"; // NOI18N
    


    public DebuggerOutput (JPDADebuggerImpl debugger, ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = debugger;
        
        // close old tabs
        if (DebuggerManager.getDebuggerManager ().getSessions ().length == 1) {
            Iterator<IOManager> i = managers.iterator ();
            while (i.hasNext ()) {
                i.next().close();
            }
            managers = new HashSet<IOManager>();
        }
        
        // open new tab
        String title = contextProvider.lookupFirst(null, String.class);
        if (title == null) {
            title = NbBundle.getMessage 
                (IOManager.class, "CTL_DebuggerConsole_Title");
        }
        ioManager = new IOManager (debugger, title);
        managers.add (ioManager);
        
        debugger.addPropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
    }

    //@Override
    private synchronized void destroy () {
        debugger.removePropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
        debugger = null;
        //ioManager = null; - leave it for late writes, it closes with a delay.
    }

    /*
    @Override
    public String[] getProperties () {
        return new String[] {ActionsManagerListener.PROP_ACTION_PERFORMED};
    }
    */

    @Override
    public void propertyChange (java.beans.PropertyChangeEvent evt) {
        final JPDAThread t;
        int debuggerState;
        IOManager ioManager;
        synchronized (this) {
            if (debugger == null) {
                return ;
            }
            t = debugger.getCurrentThread ();
            debuggerState = debugger.getState();
            ioManager = this.ioManager;
        }
        if (debuggerState == JPDADebugger.STATE_STARTING) {
            AbstractDICookie cookie = contextProvider.lookupFirst(null, AbstractDICookie.class);
            if (cookie instanceof AttachingDICookie) {
                AttachingDICookie c = (AttachingDICookie) cookie;
                if (c.getHostName () != null) {
                    print (
                        "CTL_Attaching_socket",
//                        where,
                        new String[] {
                            c.getHostName (),
                            String.valueOf(c.getPortNumber ())
                        },
                        null
                    );
                } else if (c.getSharedMemoryName() != null) {
                    print (
                        "CTL_Attaching_shmem",
//                        where,
                        new String[] {
                            c.getSharedMemoryName ()
                        },
                        null
                    );
                } else if (c.getArgs().get("pid") != null) {
                    print (
                        "CTL_Attaching_pid",
//                        where,
                        new String[] {
                            c.getArgs().get("pid").toString()
                        },
                        null
                    );
                } else {
                    print (
                        "CTL_Attaching",
                        null,
                        null
                    );
                }
            } else
            if (cookie instanceof ListeningDICookie) {
                ListeningDICookie c = (ListeningDICookie) cookie;
                if (c.getSharedMemoryName () != null) {
                    print (
                        "CTL_Listening_shmem",
//                        where,
                        new String[] {
                            c.getSharedMemoryName ()
                        },
                        null
                    );
                } else {
                    String port = String.valueOf(c.getPortNumber());
                    Connector.StringArgument localAddress = (Connector.StringArgument) c.getArgs().get("localAddress"); // NOI18N
                    String address;
                    if (localAddress == null) {
                        address = port;
                    } else {
                        String host = localAddress.value();
                        if (host.isEmpty() || "localhost".equals(host) || "127.0.0.1".equals(host)) {   // NOI18N
                            address = port;
                        } else {
                            address = host+":"+port;
                        }
                    }
                    print (
                        "CTL_Listening_socket",
//                        where,
                        new String[] {
                            address
                        },
                        null
                    );
                }
            } else
            if (cookie instanceof LaunchingDICookie) {
                LaunchingDICookie c = (LaunchingDICookie) cookie;
                    print (
                        "CTL_Launching",
//                        where,
                        new String[] {
                            c.getCommandLine ()
                        },
                        null
                    );
            }
        } else
        if (debuggerState == JPDADebugger.STATE_RUNNING) {
            print (
                "CTL_Debugger_running",
//                where,
                new String[] {
                },
                null
            );
        } else
        if (debuggerState == JPDADebugger.STATE_DISCONNECTED) {
            Throwable e = null;
            try {
                synchronized (this) {
                    if (debugger != null) {
                        debugger.waitRunning ();
                    }
                }
            } catch (DebuggerStartException ex) {
                e = ex.getTargetException ();
            }
            if (e == null) {
                print ("CTL_Debugger_finished", null, null);
            } else {
                String message = e.getMessage ();
                if (e instanceof ConnectException) {
                    message = NbBundle.getMessage
                            (DebuggerOutput.class, "CTL_Connection_refused");
                }
                if (e instanceof UnknownHostException) {
                    message = NbBundle.getMessage
                            (DebuggerOutput.class, "CTL_Unknown_host");
                }
                if (message != null) {
                    ioManager.println (
                        message,
                        null
                    );
                } else {
                    ioManager.println (
                        e.toString (),
                        null
                    );
                }
                //e.printStackTrace ();
            }
            ioManager.closeStream ();
            destroy();
        } else
        if (debuggerState == JPDADebugger.STATE_STOPPED) {
            //DebuggerEngine engine = debugger.getEngine ();
            //S ystem.out.println("State Stopped " + debugger.getLastAction ());
            if (t == null) {
                print ("CTL_Debugger_stopped", null, null);
                return;
            }
            Session session = null;
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (int i = 0; i < sessions.length; i++) {
                if (sessions[i].lookupFirst(null, JPDADebugger.class) == debugger) {
                    session = sessions[i];
                    break;
                }
            }
            final String language = (session != null) ? session.getCurrentLanguage() : null;
            String threadName = t.getName ();
            final String methodName = t.getMethodName ();
            String className = t.getClassName ();
            final int lineNumber = t.getLineNumber (language);
            Operation op = t.getCurrentOperation();
            try {
                String url = null;
                SourcePath sourcePath = contextProvider.lookupFirst(null, SourcePath.class);
                if (sourcePath != null) {
                    try {
                        url = sourcePath.getURL(t, language);
                    } catch (InternalExceptionWrapper | InvalidStackFrameExceptionWrapper |
                             ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {}
                }
                String urlName = null;
                if (url != null) {
                    int index = url.lastIndexOf('/');
                    if (index > 0) {
                        urlName = url.substring(index + 1);
                    }
                }
                final String sourceName = (urlName != null) ? urlName : t.getSourceName(language);
//                String relativePath = EditorContextBridge.getRelativePath 
//                    (t, language);
//                synchronized (this) {
//                    if (relativePath != null && engineContext != null) {
//                        url = engineContext.getURL(relativePath, true);
//                    }
//                }
                final DebuggerConsoleIO.Line line;
                if (lineNumber > 0 && url != null) {
                    line = new DebuggerConsoleIO.Line (
                        url, 
                        lineNumber,
                        debugger
                    );
                } else {
                    line = null;
                }

                boolean important = url == null; // Bring attention to the console output if there's no URL to display.
                // Make an exception for a service class used by visual debugger
                if (important && "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService".equals(className)) {
                    important = false;
                }
                final boolean importantf = important;
                PropertyChangeListener operationsUpdateListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        String name = evt.getPropertyName();
                        if (PROP_OPERATIONS_UPDATE.equals(name)) {
                            Operation op = t.getCurrentOperation();
                            printOperation(t, op, sourceName, methodName, lineNumber, line, importantf);
                        }
                        if (PROP_OPERATIONS_SET.equals(name)) {
                            ((BeanContextChild) t).removePropertyChangeListener(PROP_OPERATIONS_UPDATE, this);
                            ((BeanContextChild) t).removePropertyChangeListener(PROP_OPERATIONS_SET, this);
                        }
                    }
                };
                ((BeanContextChild) t).addPropertyChangeListener(PROP_OPERATIONS_UPDATE, operationsUpdateListener);
                ((BeanContextChild) t).addPropertyChangeListener(PROP_OPERATIONS_SET, operationsUpdateListener);
                if (op != null) {
                    printOperation(t, op, sourceName, methodName, lineNumber, line, important);
                } else if (lineNumber > 0) {
                    print (
                        "CTL_Thread_stopped",
                      //  IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            sourceName,
                            methodName,
                            String.valueOf(lineNumber)
                        },
                        line,
                        important
                    );
                } else if (sourceName.length() > 0 && methodName.length() > 0) {
                    print (
                        "CTL_Thread_stopped_no_line",
                    //    IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            sourceName,
                            methodName
                        },
                        line,
                        important
                    );
                } else {
                    print (
                        "CTL_Thread_stopped_no_line_no_source",
                        new String[] { threadName },
                        line,
                        important
                    );
                }
            } catch (AbsentInformationException ex) {
                if (lineNumber > 0) {
                    print (
                        "CTL_Thread_stopped_no_info",
                     //   IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            className,
                            methodName,
                            lineNumber > 0 ? String.valueOf(lineNumber) : ""
                        },
                        null,
                        true
                    );
                } else {
                    print (
                        "CTL_Thread_stopped_no_info_no_line",
                        //IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            className,
                            methodName
                        },
                        null,
                        true
                    );
                }
            }
        }
    }
    
    public void printOperation(JPDAThread t, Operation op, String sourceName,
                               String methodName, int lineNumber,
                               DebuggerConsoleIO.Line line, boolean important) {
        String threadName = t.getName();
        List<Operation> lastOperations = t.getLastOperations();
        Operation lastOperation = (lastOperations != null && lastOperations.size() > 0) ?
                                  lastOperations.get(lastOperations.size() - 1) : null;
        boolean done = op == lastOperation;
        if (!done) {
            print("CTL_Thread_stopped_before_op",
                new String[] {
                    threadName,
                    sourceName,
                    methodName,
                    String.valueOf(lineNumber),
                    op.getMethodName()
                },
                line,
                important
            );
        } else {
            print("CTL_Thread_stopped_after_op",
                new String[] {
                    threadName,
                    sourceName,
                    methodName,
                    String.valueOf(lineNumber),
                    lastOperation.getMethodName()
                },
                line,
                important
            );
        }
    }

    public void actionPerformed (Object action, boolean success) {
        if (!success) {
            return;
        }
        //print ("CTL_Debugger_running", where, null, null);
        if (action == ActionsManager.ACTION_CONTINUE) {
            print ("CTL_Continue", null, null);
        } else
        if (action == ActionsManager.ACTION_STEP_INTO) {
            print ("CTL_Step_Into", null, null);
        } else
        if (action == ActionsManager.ACTION_STEP_OUT) {
            print ("CTL_Step_Out", null, null);
        } else
        if (action == ActionsManager.ACTION_STEP_OVER) {
            print ("CTL_Step_Over", null, null);
        }
    }

    public IOManager getIOManager() {
        return ioManager;
    }

    // helper methods ..........................................................

    private void print (
        String message,
//        int where,
        String[] args,
        DebuggerConsoleIO.Line line
    ) {
        print(message, args, line, false);
    }

    private void print (
        String message,
//        int where,
        String[] args,
        DebuggerConsoleIO.Line line,
        boolean important
    ) {
        String text = (args == null) ?
            NbBundle.getMessage (
                DebuggerOutput.class,
                message
            ) :
            new MessageFormat (NbBundle.getMessage (
                DebuggerOutput.class,
                message
            )).format (args);

        IOManager ioManager;
        synchronized (this) {
            ioManager = this.ioManager;
            if (ioManager == null) {
                return ;
            }
        }
        ioManager.println (
            text,
//            where,
            line,
            important
        );
    }
}
