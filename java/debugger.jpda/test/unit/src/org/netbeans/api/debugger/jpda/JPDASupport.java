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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Transport;

import java.beans.PropertyChangeEvent;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * Contains support functionality for unit tests.
 *
 * @author Maros Sandor
 */
public final class JPDASupport implements DebuggerManagerListener {

    private static final DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private JPDADebugger            jpdaDebugger;
    private DebuggerEngine          debuggerEngine;
    private final ProcessIO         processIO;
    
    private final Object            STATE_LOCK = new Object ();
    
    private JPDASupport (JPDADebugger jpdaDebugger, ProcessIO pio) {
        this.jpdaDebugger = jpdaDebugger;
        jpdaDebugger.addPropertyChangeListener (this);
        DebuggerEngine[] de = dm.getDebuggerEngines ();
        int i, k = de.length;
        for (i = 0; i < k; i++) {
            if (de [i].lookupFirst (null, JPDADebugger.class) == jpdaDebugger) {
                debuggerEngine = de [i];
                break;
            }
        }
        this.processIO = pio;
    }
    
    public static Test createTestSuite(Class<? extends TestCase> clazz) {
        Configuration suiteConfiguration = NbModuleSuite.createConfiguration(clazz);
        suiteConfiguration = suiteConfiguration.clusters(".*").enableModules(".*java.source.*").enableModules(".*libs.nbjavacapi.*").gui(false);
        if (!(ClassLoader.getSystemClassLoader() instanceof URLClassLoader)) {
            //when running on JDK 9+, to make the com.sun.jdi package dependency work, we need to make getPackage("com.sun.jdi") work
            //for system CL's parent (which otherwise happily loads the VirtualMachineManager class,
            //but won't return the package from getPackage due to JDK "specialty":
            suiteConfiguration = suiteConfiguration.parentClassLoader(new ClassLoader(ClassLoader.getSystemClassLoader().getParent()) {
                @Override
                protected Package getPackage(String pack) {
                    if ("com.sun.jdi".equals(pack)) {
                        try {
                            return loadClass("com.sun.jdi.VirtualMachineManager").getPackage();
                        } catch (ClassNotFoundException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                    return super.getPackage(pack);
                }
            });
        }
        //suiteConfiguration = suiteConfiguration.reuseUserDir(false);
        return NbModuleSuite.create(suiteConfiguration);
    }

    
    // starting methods ........................................................

//    public static JPDASupport listen (String mainClass) 
//    throws IOException, IllegalConnectorArgumentsException, 
//    DebuggerStartException {
//        VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
//        List lconnectors = vmm.listeningConnectors ();
//        ListeningConnector connector = null;
//        for (Iterator i = lconnectors.iterator (); i.hasNext ();) {
//            ListeningConnector lc = (ListeningConnector) i.next ();
//            Transport t = lc.transport ();
//            if (t != null && t.name ().equals ("dt_socket")) {
//                connector = lc;
//                break;
//            }
//        }
//        if (connector == null) 
//            throw new RuntimeException 
//                ("No listening socket connector available");
//
//        Map args = connector.defaultArguments ();
//        String address = connector.startListening (args);
//        String localhostAddres;
//        try
//        {
//            int port = Integer.parseInt 
//                (address.substring (address.indexOf (':') + 1));
//            localhostAddres = "localhost:" + port;
//            Connector.IntegerArgument portArg = 
//                (Connector.IntegerArgument) args.get("port");
//            portArg.setValue(port);
//        } catch (Exception e) {
//            // this address format is not known, use default
//            localhostAddres = address;
//        }
//
//        JPDADebugger jpdaDebugger = JPDADebugger.listen 
//            (connector, args, createServices ());
//        if (jpdaDebugger == null) 
//            throw new DebuggerStartException ("JPDA jpdaDebugger was not started");
//        Process process = launchVM (mainClass, localhostAddres, false);
//        ProcessIO pio = new ProcessIO (process);
//        pio.go ();
//        return new JPDASupport (jpdaDebugger);
//    }

    public static JPDASupport attach (String mainClass) throws IOException, 
    DebuggerStartException {
        return attach(mainClass, null);
    }
    
    public static JPDASupport attach (String mainClass, String[] args) throws IOException, 
    DebuggerStartException {
        return attach(mainClass, args, new File[0]);
    }
    public static JPDASupport attach (String mainClass, String[] args, File[] classPath) throws IOException,
    DebuggerStartException {
        return attach(new String[0], mainClass, args, classPath);
    }
    public static JPDASupport attach (String[] vmArgs, String mainClass, String[] args, File[] classPath) throws IOException,
    DebuggerStartException {
        String sourceRoot = System.getProperty ("test.dir.src");
        if (mainClass.endsWith(".java")) {
            sourceRoot = new File(mainClass).getParent();
        } else {
            sourceRoot = System.getProperty ("test.dir.src");
        }
        Process process = launchVM (vmArgs, mainClass, args, classPath, "", true);
        String line = readLine (process.getInputStream ());
        int port = Integer.parseInt (line.substring (line.lastIndexOf (':') + 1).trim ());
        ProcessIO pio = new ProcessIO (process);
        pio.go ();

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List aconnectors = vmm.attachingConnectors();
        AttachingConnector connector = null;
        for (Iterator i = aconnectors.iterator(); i.hasNext();) {
            AttachingConnector ac = (AttachingConnector) i.next();
            Transport t = ac.transport ();
            if (t != null && t.name().equals("dt_socket")) {
                connector = ac;
                break;
            }
        }
        if (connector == null) {
            throw new RuntimeException("No attaching socket connector available");
        }
        JPDADebugger jpdaDebugger = JPDADebugger.attach (
            "localhost", 
            port, 
            createServices (sourceRoot)
        );
        return new JPDASupport (jpdaDebugger, pio);
    }

    public static JPDASupport attachScript(String launcher, String path) throws IOException, DebuggerStartException {
        String [] cmdArray = new String [] {
            launcherPath(launcher),
            "--jvm",
            "--vm.agentlib:jdwp=transport=dt_socket,suspend=y,server=y",
            path
        };
        Process process = Runtime.getRuntime ().exec (cmdArray);
        String line = readLine (process.getInputStream ());
        int port = Integer.parseInt (line.substring (line.lastIndexOf (':') + 1).trim ());
        ProcessIO pio = new ProcessIO (process);
        pio.go();

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List aconnectors = vmm.attachingConnectors();
        AttachingConnector connector = null;
        for (Iterator i = aconnectors.iterator(); i.hasNext();) {
            AttachingConnector ac = (AttachingConnector) i.next();
            Transport t = ac.transport ();
            if (t != null && t.name().equals("dt_socket")) {
                connector = ac;
                break;
            }
        }
        if (connector == null) {
            throw new RuntimeException("No attaching socket connector available");
        }

        JPDADebugger jpdaDebugger = JPDADebugger.attach (
            "localhost",
            port,
            new Object[]{}
        );
        return new JPDASupport(jpdaDebugger, pio);
    }

    private static String launcherPath(String launcher) {
        return System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + launcher;
    }

    public static boolean isLauncherAvailable(String launcher) {
        return Files.exists(Paths.get(launcherPath(launcher)));
    }

    // public interface ........................................................
    
    public void doContinue () {
        if (jpdaDebugger.getState () != JPDADebugger.STATE_STOPPED) {
            throw new IllegalStateException ();
        }
        debuggerEngine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
    }

    public void stepOver () {
        step (ActionsManager.ACTION_STEP_OVER);
    }

    public void stepInto () {
        step (ActionsManager.ACTION_STEP_INTO);
    }

    public void stepOut () {
        step (ActionsManager.ACTION_STEP_OUT);
    }

    public void step (Object action) {
        if (jpdaDebugger.getState () != JPDADebugger.STATE_STOPPED) {
            throw new IllegalStateException ();
        }
        DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager().doAction(action);
        waitState (JPDADebugger.STATE_STOPPED);
    }

    public void stepAsynch (final Object actionAsynch, final ActionsManagerListener al) {
        if (jpdaDebugger.getState () != JPDADebugger.STATE_STOPPED)
            throw new IllegalStateException ();
        debuggerEngine.getActionsManager().addActionsManagerListener(
                new ActionsManagerListener() {
                    public void actionPerformed(Object action) {
                        if (action != actionAsynch) return ;
                        al.actionPerformed(action);
                        debuggerEngine.getActionsManager().removeActionsManagerListener(this);
                    }
                    public void actionStateChanged(Object action, boolean enabled) {
                    }
                }
        );
        debuggerEngine.getActionsManager ().postAction (actionAsynch);
    }

    public void doFinish () {
        if (jpdaDebugger == null) return;
        debuggerEngine.getActionsManager ().
            doAction (ActionsManager.ACTION_KILL);
        waitState (JPDADebugger.STATE_DISCONNECTED);
        try {
            processIO.join();
        } catch (InterruptedException ex) {
            // Interrupted
        }
    }

    public void waitState (int state) {
        synchronized (STATE_LOCK) {
            while ( jpdaDebugger.getState () != state &&
                    jpdaDebugger.getState () != JPDADebugger.STATE_DISCONNECTED
            ) {
                try {
                    STATE_LOCK.wait ();
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            }
        }
    }

    /*public void waitState (int state) {
        synchronized (STATE_LOCK) {
            int ds = jpdaDebugger.getState ();
            System.err.println("JPDASupport.waitState("+state+"): ds = "+ds+", jpdaDebugger = "+jpdaDebugger);
            while ( ds != state &&
                    ds != JPDADebugger.STATE_DISCONNECTED
            ) {
                try {
                    STATE_LOCK.wait ();
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
                ds = jpdaDebugger.getState ();
                System.err.println("JPDASupport.waitState("+state+"): new ds = "+ds+", jpdaDebugger = "+jpdaDebugger);
            }
            System.err.println("JPDASupport.waitState("+state+"): state reached.");
        }
    }*/

    public JPDADebugger getDebugger() {
        return jpdaDebugger;
    }
    
    /**
     * Remove all non-hidden breakpoints.
     */
    public static void removeAllBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
            if (!(bs[i] instanceof JPDABreakpoint && ((JPDABreakpoint) bs[i]).isHidden())) {
                DebuggerManager.getDebuggerManager ().removeBreakpoint (bs [i]);
            }
        }
    }
    
    
    // other methods ...........................................................
    
    private static Object[] createServices (String sourceRoot) {
        try {
            Map map = new HashMap ();
            URL sourceUrl = new File(sourceRoot).toURI().toURL();
            String sourceUrlStr = sourceUrl.toString() + "/";
            sourceUrl = new URL(sourceUrlStr);
            ClassPath cp = ClassPathSupport.createClassPath (new URL[] {
                sourceUrl
            });
            map.put ("sourcepath", cp);
            map.put ("baseDir", new File(sourceRoot).getParentFile());
            return new Object[] { map };
        } catch (MalformedURLException ex) {
            //System.err.println("MalformedURLException: sourceRoot = '"+sourceRoot+"'.");
            ex.printStackTrace();
            return new Object[] {};
        }
    }

    private static String readLine (InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            int c = in.read();
            if (c == -1) throw new EOFException();
            if (c == 0x0D) {
                c = in.read();
                if (c != 0x0A) sb.append((char)0x0D);
            }
            if (c == 0x0A) return sb.toString();
            sb.append((char)c);
        }
    }
    
    private static Process launchVM (
        String[] vmArgs,
        String mainClass,
        String[] args,
        File[] extraCP,
        String connectorAddress, 
        boolean server
    ) throws IOException {

        String cp = getClassPath(extraCP);
        //System.err.println("CP = "+cp);

        List<String> cmdArgs = new ArrayList<>();

        cmdArgs.add(launcherPath("java"));
        cmdArgs.add("-agentlib:jdwp=transport=" + "dt_socket" + ",address=" +
                connectorAddress + ",suspend=y,server=" + 
                (server ? "y" : "n"));
        cmdArgs.add("-classpath");
        cmdArgs.add(cp.substring(0, cp.length() -1));
        cmdArgs.addAll(Arrays.asList(vmArgs));
        cmdArgs.add(mainClass);
        if (args != null && args.length > 0) {
            cmdArgs.addAll(Arrays.asList(args));
        }

        ProcessBuilder pb = new ProcessBuilder().command(cmdArgs);
        String classesDir = System.getProperty("test.dir.classes");
        if (classesDir != null) {
            pb.directory(new File(classesDir));
        }
        return pb.start();
    }
    
    private static String getClassPath(File[] extraCP) {
        StringBuilder cp = new StringBuilder (200);
        ClassLoader cl = JPDASupport.class.getClassLoader ();
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader) cl;
            URL [] urls = ucl.getURLs ();
            
            for (int i = 0; i < urls.length; i++) {
                URL url = urls [i];
                cp.append (url.getPath ());
                cp.append (File.pathSeparatorChar);
            }
        } else if (cl.getClass().getName().indexOf("org.netbeans.ModuleManager$SystemClassLoader") >= 0) {
            Class jarClassLoaderClass = cl.getClass().getSuperclass();
            try {
                java.lang.reflect.Field sourcesField = jarClassLoaderClass.getDeclaredField("sources");
                sourcesField.setAccessible(true);
                Object[] sources = (Object[]) sourcesField.get(cl);
                for (int i = 0; i < sources.length; i++) {
                    Method getURL = sources[i].getClass().getMethod("getURL");
                    getURL.setAccessible(true);
                    URL url = (URL) getURL.invoke(sources[i]);
                    cp.append (url.getPath ());
                    cp.append (File.pathSeparatorChar);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Problem retrieving class path from class loader: "+cl, ex);
            }
        } else {
            throw new RuntimeException("Unsupported class loader: "+cl);
        }
        for (File f : extraCP) {
            cp.append(f.getPath());
            cp.append(File.pathSeparatorChar);
        }
        
        return cp.toString();
    }
    
    public String toString () {
        switch (jpdaDebugger.getState ()) {
            case JPDADebugger.STATE_DISCONNECTED:
                return "Debugger finished.";
            case JPDADebugger.STATE_RUNNING:
                return "Debugger running.";
            case JPDADebugger.STATE_STARTING:
                return "Debugger starting.";
            case JPDADebugger.STATE_STOPPED:
                CallStackFrame f = jpdaDebugger.getCurrentCallStackFrame ();
                return "Debugger stopped: " +
                    f.getClassName () + "." + 
                    f.getMethodName () + ":" + 
                    f.getLineNumber (null);
        }
        return super.toString ();
    }
    
    // DebuggerListener ........................................................

    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }

    public void breakpointAdded(Breakpoint breakpoint) {
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
    }

    public void initWatches() {
    }

    public void watchAdded(Watch watch) {
    }

    public void watchRemoved(Watch watch) {
    }

    public void sessionAdded(Session session) {
    }

    public void sessionRemoved(Session session) {
    }

    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof JPDADebugger) {
            JPDADebugger dbg = (JPDADebugger) evt.getSource();

            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                synchronized (STATE_LOCK) {
                    STATE_LOCK.notifyAll ();
                }
                if (jpdaDebugger.getState () == JPDADebugger.STATE_DISCONNECTED)
                    jpdaDebugger.removePropertyChangeListener (this);
            }
        }
    }

    // TODO: Include check of these call in the test suite
    public void engineAdded (DebuggerEngine debuggerEngine) {
    }

    // TODO: Include check of these call in the test suite
    public void engineRemoved (DebuggerEngine debuggerEngine) {
    }

    
    // innerclasses ............................................................
    
    private static class ProcessIO {

        private final Process p;
        private Thread threadOut;
        private Thread threadErr;

        public ProcessIO(Process p) {
            this.p = p;
        }

        public void go() {
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();

            (threadOut = new SimplePipe(System.out, out)).start();
            (threadErr = new SimplePipe(System.out, err)).start();
        }

        private void join() throws InterruptedException {
            threadOut.join();
            threadErr.join();
            assertEquals(0, p.waitFor());
        }
    }

    private static class SimplePipe extends Thread {
        private OutputStream out;
        private InputStream in;

        public SimplePipe(OutputStream out, InputStream in) {
            this.out = out;
            this.in = in;
            setDaemon(true);
        }

        public void run() {
            byte [] buffer = new byte[1024];
            int n;
            try {
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            } catch (IOException e) {
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                }
            }
            System.out.println("PIO QUIT");
        }
    }
}
