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

package org.netbeans.modules.debugger.jpda.ant;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.beans.PropertyChangeEvent;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.Connector;
import java.beans.PropertyChangeListener;
import java.io.FileFilter;
import java.io.IOException;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.DebuggerStartException;

import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Ant task to start the NetBeans JPDA debugger in listening mode.
 *
 * @author Jesse Glick, David Konecny
 */
public class JPDAStart extends Task implements Runnable {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N

    private static final String URL_EMBEDDING = String.format("!%c",File.separatorChar);   //NOI18N
    private static final String SOCKET_TRANSPORT = "dt_socket"; // NOI18N
    private static final String SHMEM_TRANSPORT = "dt_shmem"; // NOI18N
    private static final String SOCKET_CONNECTOR = "com.sun.jdi.SocketListen"; // NOI18N
    private static final String SHMEM_CONNECTOR = "com.sun.jdi.SharedMemoryListen"; // NOI18N
    private static final String MODULE_INFO_CLZ = "module-info.class";  //NOI18N
    private static final Set<? extends String> MODULE_FILES;
    static {
        final Set<String> exts = new HashSet<String>();
        exts.add("jar");    //NOI18N
        exts.add("jmod");   //NOI18N
        MODULE_FILES = Collections.unmodifiableSet(exts);
    }

    /** Name of the property to which the JPDA address will be set.
     * Target VM should use this address and connect to it
     */
    private String                  addressProperty;
    /** Default transport is socket*/
    private String                  transport = SOCKET_TRANSPORT;
    /** Preferred connector name. May be null. */
    private String                  connector;
    /** Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String                  name;
    /** Explicit sourcepath of the debugged process. */
    private Sourcepath              sourcepath = null;
    private Path                    plainSourcepath = null;
    private boolean                 isSourcePathExclusive;
    /** Explicit classpath of the debugged process. */
    private Path                    classpath = null;
    /** Explicit bootclasspath of the debugged process. */
    private Path                    bootclasspath = null;
    /** Explicit modulepath of the debugged process. */
    private Path                    modulepath = null;
    private final Object []         lock = new Object[2];
    /** The class debugger should stop in, or null. */
    private String                  stopClassName = null;
    private String                  listeningCP = null;
    private boolean                 useLoopBackAddress = true;
    private RequestProcessor        rp = new RequestProcessor("JPDAStart", 5);

    
    // properties ..............................................................

    public void setAddressProperty (String propertyName) {
        this.addressProperty = propertyName;
    }

    private String getAddressProperty () {
        return addressProperty;
    }

    public void setTransport (String transport) {
        logger.log(Level.FINE, "Set transport: ''{0}''", transport);
        this.transport = transport;
    }

    private String getTransport () {
        return transport;
    }
    
    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getConnector() {
        return connector;
    }

    public void setName (String name) {
        this.name = name;
    }

    private String getName () {
        return name;
    }

    public void setStopClassName (String stopClassName) {
        this.stopClassName = stopClassName;
    }

    private String getStopClassName () {
        return stopClassName;
    }

    public void setListeningcp(String listeningCP) {
        this.listeningCP = listeningCP;
    }
    
    public void setUseLoopBackAddress(String useLoopBackAddress) { // useLoopBackAddress
        this.useLoopBackAddress = Boolean.parseBoolean(useLoopBackAddress);
        logger.log(Level.FINE, "setUseLoopBackAddress = ''{0}'' => {1}", new Object[] { useLoopBackAddress, this.useLoopBackAddress });
    }

    public void addClasspath (Path path) {
        logger.log(Level.FINE, "addClasspath({0})", path);
        if (classpath != null)
            throw new BuildException ("Only one classpath subelement is supported");
        classpath = path;
    }

    public void addBootclasspath (Path path) {
        logger.log(Level.FINE, "addBootclasspath({0})", path);
        if (bootclasspath != null)
            throw new BuildException ("Only one bootclasspath subelement is supported");
        bootclasspath = path;
    }

    public void addModulepath (Path path) {
        logger.log(Level.FINE, "addModlepath({0})", path);
        if (modulepath != null)
            throw new BuildException ("Only one modulepath subelement is supported");
        modulepath = path;
    }

    public void addSourcepath (Sourcepath path) {
        logger.log(Level.FINE, "addSourcepath({0})", path);
        if (sourcepath != null)
            throw new BuildException ("Only one sourcepath subelement is supported");
        sourcepath = path;
    }

    static void verifyPaths(Project project, Path path) {
        if (path == null) return ;
        String[] paths = path.list();
        for (int i = 0; i < paths.length; i++) {
            String pathName = project.replaceProperties(paths[i]);
            File file = FileUtil.normalizeFile
                (project.resolveFile (pathName));
            if (!file.exists()) {
                project.log("Non-existing path \""+pathName+"\" provided.", Project.MSG_WARN);
                //throw new BuildException("Non-existing path \""+paths[i]+"\" provided.");
            }
        }
    }

    /** Searching for a connector in given collection.
     * @param name - name of the connector
     * @param connectors
     * @return the connector or null
     */
    private static ListeningConnector findConnector(String name, final Collection<ListeningConnector> connectors) {
        assert name != null;
        for (ListeningConnector c : connectors) {
            if (name.equals(c.name())) {
                return c;
            }
        }
        return null;
    }
    
    // main methods ............................................................

    @Override
    public void execute () throws BuildException {
        verifyPaths(getProject(), classpath);
        verifyPaths(getProject(), modulepath);
        //verifyPaths(getProject(), bootclasspath); Do not check the paths on bootclasspath (see issue #70930).
        if (sourcepath != null) {
            isSourcePathExclusive = sourcepath.isExclusive();
            plainSourcepath = sourcepath.getPlainPath();
        }
        verifyPaths(getProject(), plainSourcepath);
        try {
            logger.fine("JPDAStart.execute()"); // NOI18N
            debug ("Execute started"); // NOI18N
            if (name == null)
                throw new BuildException ("name attribute must specify name of this debugging session", getLocation ());
            if (addressProperty == null)
                throw new BuildException ("addressproperty attribute must specify name of property to which address will be set", getLocation ());
            if (transport == null)
                transport = SOCKET_TRANSPORT;
            debug ("Entering synch lock"); // NOI18N
            lock[0] = lock[1] = null;
            synchronized (lock) {
                debug ("Entered synch lock"); // NOI18N
                rp.post (this);
                try {
                    debug ("Entering wait"); // NOI18N
                    lock.wait ();
                    debug ("Wait finished"); // NOI18N
                    if (lock [1] != null) {
                        if (lock[1] instanceof DebuggerStartException) {
                            //getProject().log(((DebuggerStartException) lock[1]).getLocalizedMessage(), Project.MSG_ERR);
                            throw new BuildException(((DebuggerStartException) lock[1]).getLocalizedMessage());
                        } else if (lock[1] instanceof ThreadDeath) {
                            throw (ThreadDeath) lock[1];
                        } else {
                            throw new BuildException ((Throwable) lock [1]);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new BuildException (e);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace ();
            throw new BuildException (t);
        }
    }

    @Override
    public void run () {
        logger.fine("JPDAStart.run()"); // NOI18N
        debug ("Entering synch lock"); // NOI18N
        synchronized (lock) {
            debug("Entered synch lock"); // NOI18N
            try {
                ListeningConnector lc = null;
                final Set<ListeningConnector> connectors = new HashSet<ListeningConnector>();
                // search for connectors registered by NetBeans modules
                // In JavaFX listening connectors are registered as Connector.class.
                final Lookup.Result<Connector> r = Lookup.getDefault().lookupResult(Connector.class);
                for(Connector c: r.allInstances()) {
                    if (c instanceof ListeningConnector) connectors.add((ListeningConnector) c);
                }
                // use JDI default as well
                connectors.addAll(Bootstrap.virtualMachineManager().listeningConnectors());

                // if name of the connector has been specified, try to use it
                if (connector != null) {
                    logger.log(Level.FINE, "Looking for connector {0}", connector);
                    lc = findConnector(connector, connectors);
                }
                if (lc == null) {
                    // if dt_socket then use default socket as specified by JDI
                    if (transport.equals(SOCKET_TRANSPORT)) {
                        logger.log(Level.FINE, "Looking for default connector {0}", SOCKET_CONNECTOR);
                        lc = findConnector(SOCKET_CONNECTOR, connectors);
                    // if dt_shmem then use the default socket as specified by JDI
                    } else if (transport.equals(SHMEM_TRANSPORT)) {
                        logger.log(Level.FINE, "Looking for default connector {0}", SHMEM_CONNECTOR);
                        lc = findConnector(SHMEM_CONNECTOR, connectors);
                    }
                }
                // fallback to the original, i.e. find first connector whose transport
                // name matches given transport
                if (lc == null) {
                    logger.log(Level.FINE, "Fall back, looking for a connector with transport {0}", transport);
                    for (ListeningConnector c: connectors) {
                        Transport t = c.transport ();
                        if (t != null && t.name ().equals (transport)) {
                            lc = c;
                            break;
                        }
                    }
                }
                if (lc == null) {
                    throw new BuildException
                        ("No transports named " + transport + " found!");
                }
                logger.log(Level.FINE, "Listening using connector {0}, transport {1}", new Object[] {lc.name(), lc.transport().name()});

                final Map args = lc.defaultArguments ();
                Connector.StringArgument localAddress = (Connector.StringArgument) args.get("localAddress"); // NOI18N
                if (localAddress != null) {
                    String host = null;
                    if (!useLoopBackAddress) {
                        try {
                            host = InetAddress.getLocalHost().getCanonicalHostName();
                        } catch (UnknownHostException uhex) {}
                    }
                    if (host == null) {
                        host = "127.0.0.1"; // NOI18N
                    }
                    localAddress.setValue(host);
                }
                logger.log(Level.FINE, "Assigned host = ''{0}''", localAddress);
                String address = null;
                try {
                    address = lc.startListening (args);
                } catch (java.io.IOException ioex) {
                    boolean passed = false;
                    // workaround for issue 148490
                    if (SHMEM_TRANSPORT.equals(transport)) {
                        Connector.StringArgument argName = (Connector.StringArgument) args.get("name"); // NOI18N
                        for (int x = 0; x < 5; x++) {
                            String tryAddress = "javadebug" + Math.round(Math.random() * 10000); // NOI18N
                            try {
                                argName.setValue (tryAddress);
                                address = lc.startListening (args);
                                passed = true;
                                break;
                            } catch (Exception e) {
                                // ignore
                            }
                        } // for
                    }
                    if (!passed) {
                        getProject().log("Listening failed with arguments: "+args);
                        throw ioex;
                    }
                } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException iaex) {
                    getProject().log("Listening failed with arguments: "+args);
                    throw iaex;
                }
                /* A fix to bug http://developer.java.sun.com/developer/bugParade/bugs/4932074.html has been integrated into JDK 1.5
                // Uncomment if the fix is not complete in all cases
                // This code parses the address string "HOST:PORT" to extract PORT and then point debugee to localhost:PORT
                // This is NOT a clean solution to the problem but it SHOULD work in 99% cases
                if (SOCKET_TRANSPORT.equals(transport)) {
                    int port = -1;
                    try {
                        port = Integer.parseInt (address.substring (address.indexOf (':') + 1));
                        Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); // NOI18N
                        portArg.setValue (port);
                        address = "localhost:" + port; // NOI18N
                    } catch (Exception e) {
                        // this address format is not known, use default
                    }
                }*/
                if (SOCKET_TRANSPORT.equals(transport)) {
                    try {
                        int port = Integer.parseInt (address.substring (address.indexOf (':') + 1));
                        Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); // NOI18N
                        portArg.setValue (port);
                        // Since some users have badly configured host addresses,
                        // perform a check for the address and use "localhost"
                        // if the address can not be resolved: (see http://www.netbeans.org/issues/show_bug.cgi?id=154974)
                        String host = address.substring(0, address.indexOf (':'));
                        logger.log(Level.FINE, "  socket listening at {0}, host = {1}, port = {2}",  // NOI18N
                                   new Object[]{address, host, port});
                        try {
                            InetAddress.getByName(host);
                        } catch (UnknownHostException uhex) {
                            logger.log(  Level.FINE, "unknown host ''{0}''", host);
                            address = "localhost:" + port; // NOI18N
                        } catch (SecurityException  se) {}
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (SHMEM_TRANSPORT.equals(transport)) {
                    try {
                        Connector.StringArgument name = (Connector.StringArgument) args.get("name"); // NOI18N
                        name.setValue (address);
                    } catch (Exception e) {
                        // ignore
                    }
                }
                getProject ().setNewProperty (getAddressProperty (), address);

                debug ("Creating source path"); // NOI18N
                ClassPath sourcePath = createSourcePath (
                    getProject (),
                    modulepath,
                    classpath,
                    plainSourcepath,
                    isSourcePathExclusive
                );
                ClassPath jdkSourcePath = createJDKSourcePath (
                    getProject (),
                    bootclasspath
                );
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Create sourcepath:"); // NOI18N
                    logger.log(Level.FINE, "    modulepath : {0}", modulepath); // NOI18N
                    logger.log(Level.FINE, "    classpath : {0}", classpath); // NOI18N
                    logger.log(Level.FINE, "    sourcepath : {0}", plainSourcepath); // NOI18N
                    logger.log(Level.FINE, "    bootclasspath : {0}", bootclasspath); // NOI18N
                    logger.log(Level.FINE, "    >> sourcePath : {0}", sourcePath); // NOI18N
                    logger.log(Level.FINE, "    >> jdkSourcePath : {0}", jdkSourcePath); // NOI18N
                }

                Breakpoint first = null;

                if (stopClassName != null && stopClassName.length() > 0) {
                    logger.log(Level.FINE, "create method breakpoint, class name = {0}", stopClassName);    // NOI18N
                    first = createBreakpoint (stopClassName);
                }

                debug ("Debugger started"); // NOI18N
                logger.log(Level.FINE, "start listening at {0}", address); // NOI18N

                final Map properties = new HashMap ();
                // uncomment to implement smart stepping with step-outs
                // rather than step-ins (for J2ME)
                // props.put("SS_ACTION_STEPOUT", Boolean.TRUE);
                properties.put ("sourcepath", sourcePath); // NOI18N
                properties.put ("name", getName ()); // NOI18N
                properties.put ("jdksources", jdkSourcePath); // NOI18N
                properties.put ("listeningCP", listeningCP); // NOI18N
                String workDir = getProject().getProperty("work.dir");
                final File baseDir;
                if (workDir != null) {
                    baseDir = new File(workDir);
                } else {
                    baseDir = getProject().getBaseDir();
                }
                properties.put ("baseDir", baseDir); // NOI18N

                logger.log(Level.FINE, "JPDAStart: properties = {0}", properties);

                final ListeningConnector flc = lc;
                final WeakReference<Session> startedSessionRef[] = new WeakReference[] { new WeakReference<Session>(null) };

                Map<URL, ArtifactsUpdated> listeners = new HashMap<URL, ArtifactsUpdated>();
                List<Breakpoint> artificialBreakpoints = new LinkedList<Breakpoint>();
                if (listeningCP != null) {
                    ExceptionBreakpoint b = createCompilationErrorBreakpoint();
                    DebuggerManager.getDebuggerManager ().addBreakpoint (b);
                    artificialBreakpoints.add(b);
                }

                DebuggerManager.getDebuggerManager().addDebuggerListener(
                        DebuggerManager.PROP_DEBUGGER_ENGINES,
                        new Listener(first, artificialBreakpoints, listeners, startedSessionRef, rp));

                // Let it start asynchronously so that the script can go on and start the debuggee
                final Thread[] listeningThreadPtr = new Thread[] { null };
                final boolean[] listeningStarted = new boolean[] { false };
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (listeningStarted) {
                            listeningThreadPtr[0] = Thread.currentThread();
                            listeningStarted[0] = true;
                            listeningStarted.notifyAll();
                        }
                        Object[] services = null;
                        try {
                            FileObject prjRoot = FileUtil.toFileObject(FileUtil.normalizeFile(baseDir));
                            if (prjRoot != null) {
                                org.netbeans.api.project.Project prj = ProjectManager.getDefault().findProject(prjRoot);
                                if (prj != null) {
                                    services = new Object[] { properties, prj };
                                }
                            }
                        } catch (IOException ioex) {}
                        if (services == null) {
                            services = new Object[] { properties };
                        }
                        try {
                            DebuggerEngine[] engines = JPDADebugger.startListeningAndGetEngines (
                                flc,
                                args,
                                services
                            );
                            startedSessionRef[0] = new WeakReference(engines[0].lookupFirst(null, Session.class));
                        } catch (DebuggerStartException dsex) {
                            // Was not able to start up
                        } finally {
                            synchronized (listeningStarted) {
                                listeningThreadPtr[0] = null;
                                listeningStarted.notifyAll();
                            }
                        }
                    }
                });

                logger.log(Level.FINE, "adding a BuildListener to project {0} in {1}", new Object[] {getProject().getName(), getProject().getBaseDir()});
                getProject().addBuildListener(new BuildListener() {

                    @Override public void messageLogged(BuildEvent event) {}
                    @Override public void taskStarted(BuildEvent event) { }
                    @Override public void taskFinished(BuildEvent event) {}
                    @Override public void targetStarted(BuildEvent event) {}
                    @Override public void targetFinished(BuildEvent event) {}
                    @Override public void buildStarted(BuildEvent event) {}
                    @Override public void buildFinished(BuildEvent event) {
                        // First wait until listening actually starts:
                        logger.fine("buildFinished: waiting for listening start...");
                        synchronized (listeningStarted) {
                            if (!listeningStarted[0]) {
                                try {
                                    listeningStarted.wait();
                                } catch (InterruptedException ex) {}
                            }
                        }
                        logger.fine("buildFinished: stopping listening...");
                        // Then stop it:
                        try {
                            flc.stopListening(args);
                        } catch (java.io.IOException ioex) {
                        } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException iaex) {
                        }
                        logger.fine("buildFinished: interrupting listening thread...");
                        // If the listening is still running, interrupt it:
                        for (int i = 0; i < 10; i++) {
                            synchronized (listeningStarted) {
                                logger.log(Level.FINE, "buildFinished: listening thread = {0}", listeningThreadPtr[0]);
                                if (listeningThreadPtr[0] != null) {
                                    listeningThreadPtr[0].interrupt();
                                    try {
                                        listeningStarted.wait(500);
                                    } catch (InterruptedException ex) {}
                                } else {
                                    break;
                                }
                            }
                        }
                        // Finally, kill the started session:
                        Session s = startedSessionRef[0].get();
                        logger.log(Level.FINE, "buildFinished: killing session {0}", s);
                        if (s != null) {
                            s.kill();
                        }
                    }

                });
            } catch (java.io.IOException ioex) {
                lock[1] = ioex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                lock[1] = icaex;
            } catch (ThreadDeath td) {
                // Session was canceled - see issue #148483
                lock[1] = td;
            } finally {
                debug ("Notifying"); // NOI18N
                lock.notify ();
            }
        }
    } // run ()


    // support methods .........................................................

    private MethodBreakpoint createBreakpoint (String stopClassName) {
        MethodBreakpoint breakpoint = MethodBreakpoint.create (
            stopClassName,
            "*"
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        return breakpoint;
    }

    private ExceptionBreakpoint createCompilationErrorBreakpoint() {
        ExceptionBreakpoint b = ExceptionBreakpoint.create("java.lang.RuntimeException", ExceptionBreakpoint.TYPE_EXCEPTION_UNCAUGHT);
        b.setHidden (true);
        b.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                boolean suspend = false;
                try {
                    if (event.getVariable() instanceof ObjectVariable) {
                        ObjectVariable ov = (ObjectVariable) event.getVariable();
                        JPDAClassType ct = ov.getClassType();
                        if (ct != null) {
                            suspend = "java.lang.RuntimeException".equals(ct.getName());
                            if (suspend) {
                                java.lang.reflect.Method invokeMethodMethod = ov.getClass().getMethod("invokeMethod", JPDAThread.class, String.class, String.class, Variable[].class);
                                invokeMethodMethod.setAccessible(true);
                                Variable message = (Variable) invokeMethodMethod.invoke(ov, event.getThread(), "getMessage", "()Ljava/lang/String;", new Variable[0]);
                                if (message != null) {
                                    suspend = message.getValue().startsWith("\"Uncompilable source code");
                                }
                                //suspend = suspend && ov.invokeMethod("getMessage", "()Ljava/lang/String;", new Variable[0]).getValue().startsWith("\"Uncompilable source code");
                            }
                        }
                    }
                } catch (IllegalAccessException iaex) {
                    logger.log(Level.FINE, null, iaex);
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    logger.log(Level.FINE, null, itex);
                } catch (NoSuchMethodException ex) {
                    logger.log(Level.FINE, null, ex);
                //} catch (InvalidExpressionException ex) {
                //    logger.log(Level.FINE, null, ex);
                }

                if (!suspend) {
                    event.resume();
                }
            }
        });
        b.setPrintText(NbBundle.getBundle("org/netbeans/modules/debugger/jpda/ant/Bundle").getString("MSG_StoppedOnCompileError"));
        return b;
    }

    private static void debug (String msg) {
        if (!logger.isLoggable(Level.FINER)) return;
        logger.log(Level.FINER, "{0} [{1}] - {2}",
                   new Object[]{ new Date(), Thread.currentThread().getName(), msg });
    }

    static ClassPath createSourcePath (
        Project project,
        Path modulepath,
        Path classpath,
        Path sourcepath,
        boolean isSourcePathExclusive
    ) {
        if (sourcepath != null && isSourcePathExclusive) {
            return convertToClassPath (project, sourcepath);
        }
        ClassPath cp = convertToSourcePath (project, classpath, true);
        ClassPath modulesSources = convertToSourcePath(project, modules(project, modulepath), true);
        ClassPath sp = convertToClassPath (project, sourcepath);

        ClassPath sourcePath = ClassPathSupport.createProxyClassPath (
            new ClassPath[] {cp, modulesSources, sp}
        );
        return sourcePath;
    }

    private static Path modules(
            Project project,
            Path modulepath) {
        if (modulepath == null) {
            return null;
        }
        final Path modules = new Path(project);
        for (String pathElement : modulepath.list()) {
            final String pathName = project.replaceProperties(pathElement);
            if (pathName.lastIndexOf(URL_EMBEDDING) >=0) {
                modules.append(new Path(project, pathElement));
                continue;
            }
            final File file = FileUtil.normalizeFile(project.resolveFile (pathName));
            if (file.isDirectory() && !new File(file,MODULE_INFO_CLZ).exists()) {
                //Folder of modules add them
                for (File module : file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory() ||
                            MODULE_FILES.contains(getExt(pathname));
                    }
                   })) {
                    modules.append(new Path(project, String.format("%s%s%s",   //NOI18N
                           pathElement,
                           File.separatorChar,
                           module.getName())));
               }
            } else {
                modules.append(new Path(project, pathElement));
            }

        }
        return modules;
    }

    private static String getExt(final File file) {
        final String name = file.getName();
        final int dot = name.indexOf('.');  //NOI18N
        return dot <= 0 ?
                "" :
                name.substring(dot+1).toLowerCase();
    }

    static ClassPath createJDKSourcePath (
        Project project,
        Path bootclasspath
    ) {
        if (bootclasspath == null) {
            // if current platform is default one, bootclasspath is set to null
            JavaPlatform jp = JavaPlatform.getDefault();
            if (jp != null) {
                return jp.getSourceFolders ();
            } else {
                return ClassPathSupport.createClassPath(java.util.Collections.EMPTY_LIST);
            }
        } else {
            return convertToSourcePath (project, bootclasspath, false);
        }
    }

    private static ClassPath convertToClassPath (Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list ();
        List l = new ArrayList ();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            String pathName = project.replaceProperties(paths[i]);
            File f = FileUtil.normalizeFile (project.resolveFile (pathName));
            if (!isValid (f, project)) continue;
            URL url = fileToURL (f, project, true, false);
            if (url == null) continue;
            l.add (url);
        }
        URL[] urls = (URL[]) l.toArray (new URL [0]);
        return ClassPathSupport.createClassPath (urls);
    }

    /**
     * This method uses SourceForBinaryQuery to find sources for each
     * path item and returns them as ClassPath instance. All path items for which
     * the sources were not found are omitted.
     *
     */
    private static ClassPath convertToSourcePath (Project project, Path path, boolean reportNonExistingFiles) {
        String[] paths = path == null ? new String [0] : path.list ();
        List l = new ArrayList ();
        Set exist = new HashSet ();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            String pathName = project.replaceProperties(paths[i]);
            final String pathInArchive;
            final int index = pathName.lastIndexOf(URL_EMBEDDING);
            if (index >= 0) {
                pathInArchive = pathName.substring(index+URL_EMBEDDING.length()).replace(File.separatorChar, '/');  //NOI18N
                pathName = pathName.substring(0, index);
            } else {
                pathInArchive = ""; //NOI18N
            }
            File file = FileUtil.normalizeFile
                (project.resolveFile (pathName));
            if (!isValid (file, project)) continue;
            URL url = fileToURL (file, project, reportNonExistingFiles, true);
            if (url == null) continue;
            if (!pathInArchive.isEmpty()) {
                url = appendPathInArchive(url, pathInArchive, project);
            }
            logger.log(Level.FINE, "convertToSourcePath - class: {0}", url); // NOI18N
            try {
                SourceForBinaryQuery.Result srcRootsResult = SourceForBinaryQuery.findSourceRoots(url);
                FileObject fos[] = srcRootsResult.getRoots();
                int j, jj = fos.length;
                logger.log(Level.FINE, "  source roots = {0}; jj = {1}", new Object[]{java.util.Arrays.asList(fos), jj});
                /* ?? (#60640)
                if (jj == 0) { // no sourcepath defined
                    // Take all registered source roots
                    Set allSourceRoots = GlobalPathRegistry.getDefault().getSourceRoots();
                    fos = (FileObject[]) allSourceRoots.toArray(new FileObject[0]);
                    jj = fos.length;
                }
                 */
                for (j = 0; j < jj; j++) {
                    FileObject fo = fos[j];
                    logger.log(Level.FINE, "convertToSourcePath - source : {0}", fo); // NOI18N
                    if (FileUtil.isArchiveFile (fo)) {
                        fo = FileUtil.getArchiveRoot (fo);
                        if (fo == null) { // can occur if we fail to find the actual archive
                            fo = fos[j];
                        }
                    }
                    url = fo.toURL ();
                    if (url == null) continue;
                    if (!exist.contains (url)) {
                        l.add (ClassPathSupport.createResource (url));
                        exist.add (url);
                    }
                } // for
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
                logger.log(Level.FINE, "Have illegal url! {0}", ex.getLocalizedMessage()); // NOI18N
            }
        }
        return ClassPathSupport.createClassPath (l);
    }

    private static URL appendPathInArchive(URL rootURL, String pathInArchive, Project prj) {
        String embeddedURL = rootURL.toExternalForm() + pathInArchive;
        if (embeddedURL.charAt(embeddedURL.length()-1) != '/') {    //NOI18N
            embeddedURL = embeddedURL + '/';    //NOI18N
        }
        try {
            return new URL(embeddedURL);
        } catch (MalformedURLException e) {
            prj.log("Invalid embedded URL: \""+embeddedURL+"\".", Project.MSG_WARN);   //NOI18N
            return rootURL;
        }
    }


    private static URL fileToURL (File file, Project project, boolean reportNonExistingFiles, boolean withSlash) {
        FileObject fileObject = FileUtil.toFileObject (file);
        if (fileObject == null) {
            if (reportNonExistingFiles) {
                String path = file.getAbsolutePath();
                project.log("Have no file for "+path, Project.MSG_WARN);
            }
            return null;
        }
        if (FileUtil.isArchiveFile (fileObject)) {
            fileObject = FileUtil.getArchiveRoot (fileObject);
            if (fileObject == null) {
                project.log("Bad archive "+file.getAbsolutePath(), Project.MSG_WARN);
                /*
                ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(
                        new NullPointerException("Bad archive "+file.toString()),
                        NbBundle.getMessage(JPDAStart.class, "MSG_WrongArchive", file.getAbsolutePath())));
                 */
                return null;
            }
        }
        if (withSlash) {
            return FileUtil.urlForArchiveOrDir(file);
        } else {
            return fileObject.toURL ();
        }
    }

    private static boolean isValid (File f, Project project) {
        if (f.getPath ().indexOf ("${") != -1 && !f.exists ()) { // NOI18N
            project.log (
                "Classpath item " + f + " will be ignored.",  // NOI18N
                Project.MSG_VERBOSE
            );
            return false;
        }
        return true;
    }


    // innerclasses ............................................................

    public static class Sourcepath extends Path {

        private boolean isExclusive;
        private String path = null;
        private Path plainPath;

        public Sourcepath(Project p) {
            super(p);
            logger.log(Level.FINE, "new Sourcepath({0})", p);
        }

        public Sourcepath(Project p, String path) {
            super(p, path);
            this.path = path;
            logger.log(Level.FINE, "new Sourcepath({0}, {1})", new Object[]{p, path});
        }

        public void setExclusive(String exclusive) {
            isExclusive = "true".equalsIgnoreCase(exclusive);
        }

        boolean isExclusive() {
            return isExclusive;
        }

        public Path getPlainPath() {
            if (plainPath == null) {
                if (getRefid() != null) {
                    Path pp;
                    if (path != null) {
                        pp = new Path(getProject(), path);
                    } else {
                        pp = new Path(getProject());
                    }
                    pp.setLocation(getLocation());
                    pp.setDescription(getDescription());
                    pp.setRefid(getRefid());
                    //pp.setChecked(isChecked());
                    //pp.union = union == null ? union : (Union) union.clone();
                    plainPath = pp;
                } else {
                    plainPath = this;
                }
            }
            return plainPath;
        }

    }

    private static class Listener extends DebuggerManagerAdapter {

        private final PropertyChangeListener pcl = WeakListeners.propertyChange(this, null);

        private Set<DebuggerEngine> engines = new HashSet<DebuggerEngine>();

        private Breakpoint first;
        private final List<Breakpoint> artificalBreakpoints;
        private final Map<URL, ArtifactsUpdated> listeners;
        private final WeakReference<Session> startedSessionRef[];
        private boolean enginesCheckDone = false;
        private final RequestProcessor rp;

        private Listener(Breakpoint first,
                         List<Breakpoint> artificalBreakpoints,
                         Map<URL, ArtifactsUpdated> listeners,
                         WeakReference<Session> startedSessionRef[],
                         RequestProcessor rp) {
            this.first = first;
            this.artificalBreakpoints = artificalBreakpoints;
            this.listeners = listeners;
            this.startedSessionRef = startedSessionRef;
            this.rp = rp;
        }

        @Override
        public void propertyChange (final PropertyChangeEvent e) {
            if (JPDADebugger.PROP_STATE.equals(e.getPropertyName ())) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if (state == JPDADebugger.STATE_STOPPED || state == JPDADebugger.STATE_DISCONNECTED) {
                    rp.post(new Runnable() {
                        @Override
                        public void run() {
                            if (first != null) {
                                DebuggerManager.getDebuggerManager().removeBreakpoint(first);
                                first = null;
                                ((JPDADebugger) e.getSource()).removePropertyChangeListener(JPDADebugger.PROP_STATE, pcl);
                            }
                        }
                    });
                }
            }
        }

        private void dispose() {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_DEBUGGER_ENGINES,
                this
            );
            rp.post (new Runnable () {
                @Override
                public void run () {
                    if (artificalBreakpoints != null) {
                        for (Breakpoint b : artificalBreakpoints) {
                            DebuggerManager.getDebuggerManager().removeBreakpoint(b);
                        }
                    }
                    if (first != null) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(first);
                    }
                    if (listeners != null) {
                        for (Entry<URL, ArtifactsUpdated> e : listeners.entrySet()) {
                            BuildArtifactMapper.removeArtifactsUpdatedListener(e.getKey(), e.getValue());
                        }
                    }
                }
            });
        }

        @Override
        public void engineAdded (DebuggerEngine engine) {
            // Consider only engines from the started session.
            Session session;
            synchronized (startedSessionRef) {
                session = startedSessionRef[0].get();
            }
            if (session != null) {
                // perform check
                boolean hasEngine = false;
                for (String l : session.getSupportedLanguages()) {
                    if (engine.equals(session.getEngineForLanguage(l))) {
                        hasEngine = true;
                        break;
                    }
                }
                if (!hasEngine) {
                    return;
                }
            }
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                pcl
            );
            engines.add(engine);
        }

        @Override
        public void engineRemoved (DebuggerEngine engine) {
            Session session;
            synchronized (startedSessionRef) {
                session = startedSessionRef[0].get();
            }
            if (session != null && !enginesCheckDone) {
                // check each registered engine if it belong to the session
                enginesCheckDone = true;
                List<DebuggerEngine> list = new ArrayList<DebuggerEngine>(engines);
                for (DebuggerEngine eng : list) {
                    boolean hasEngine = false;
                    for (String l : session.getSupportedLanguages()) {
                        if (engine.equals(session.getEngineForLanguage(l))) {
                            hasEngine = true;
                            break;
                        }
                    }
                    if (!hasEngine) {
                        engines.remove(eng);
                    }
                }
            }
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) return;
            if (engines.remove(engine)) {
                debugger.removePropertyChangeListener (
                    JPDADebugger.PROP_STATE,
                    pcl
                );
            }
            if (engines.isEmpty()) {
                dispose();
            }
        }
    }

}
