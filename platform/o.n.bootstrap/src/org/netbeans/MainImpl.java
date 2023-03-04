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

package org.netbeans;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Bootstrap main class.
 * @author Jaroslav Tulach, Jesse Glick
 */
final class MainImpl extends Object {

    /** Starts the IDE.
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        AtomicReference<Method> m = new AtomicReference<Method>();
        int res = execute (args, System.in, System.out, System.err, m);
        if (res == -1) {
            // Connected to another running NB instance and succeeded in making a call.
            System.exit(CLIHandler.Status.CONNECTED);
        } else if (res != 0) {
            // Some CLIHandler refused the invocation
            if (res == Integer.MIN_VALUE) {
                res = 0;
            }
            System.exit(res);
        }

        m.get().invoke(null, new Object[] {args});
    }

    /** Constructs the correct ClassLoader, finds main method to execute
     * and invokes all registered CLIHandlers.
     *
     * @param args the arguments to pass to the handlers
     * @param reader the input stream reader for the handlers
     * @param writer the output stream for the handlers
     * @param methodToCall null, or cell that will be set to
     *   a method that shall be executed as the main application
     */
    static int execute (
        String[] args,
        java.io.InputStream reader,
        java.io.OutputStream writer,
        java.io.OutputStream error,
        AtomicReference<Method> methodToCall
    ) throws Exception {
        // #42431: turn off jar: caches, they are evil
        // Note that setDefaultUseCaches changes a static field
        // yet for some reason it is an instance method!
        new URLConnection(MainImpl.class.getResource("Main.class")) { // NOI18N
            @Override
            public void connect() throws IOException {}
        }.setDefaultUseCaches(false);

        ArrayList<File> list = new ArrayList<File>();

        HashSet<File> processedDirs = new HashSet<File> ();
        HashSet<String> processedPaths = new HashSet<String> ();
        List<String> argsL = Arrays.asList (args);
        // only nbexec.exe puts userdir into netbeans.user
        String user = System.getProperty ("netbeans.user"); // NOI18N
        if (user == null) {
            // read userdir from args (for unix nbexec)
            int idx = argsL.indexOf ("--userdir"); // NOI18N
            if (idx != -1 && argsL.size () > idx + 1) {
                user = argsL.get (idx + 1);
            }
        }
        if (user != null) {
            build_cp (new File (user), list, processedDirs, processedPaths);
        }
        String home = System.getProperty ("netbeans.home"); // NOI18N
        if (home != null) {
            build_cp (new File (home), list, processedDirs, processedPaths);
        }
        // #34069: need to do the same for nbdirs.
        String nbdirs = System.getProperty("netbeans.dirs"); // NOI18N
        String extNbDirs = System.getProperty("netbeans.extra.dirs"); // NOI18N
        if (extNbDirs != null) {
            // support for potential spaces in the cluster path:
            if (extNbDirs.startsWith("\"")) {
                extNbDirs = extNbDirs.substring(1, extNbDirs.lastIndexOf('"'));
            }
            if (nbdirs == null) {
                nbdirs = extNbDirs;
            } else {
                nbdirs = nbdirs + File.pathSeparator + extNbDirs;
            }
            System.setProperty("netbeans.dirs", nbdirs); // NOI18N
        }
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                // passing false as last argument as we need to initialize openfile-cli.jar
                build_cp(new File(tok.nextToken()).getAbsoluteFile(), list, processedDirs, processedPaths);
            }
        }

        //
        // prepend classpath
        //
        String prepend = System.getProperty("netbeans.classpath"); // NOI18N
        if (prepend != null) {
            StringTokenizer tok = new StringTokenizer (prepend, File.pathSeparator);
            while (tok.hasMoreElements()) {
                File f = new File(tok.nextToken());
                list.add(0, f);
            }
        }

        // Compute effective dynamic classpath (mostly lib/*.jar) for TopLogging, NbInstaller:
        StringBuilder buf = new StringBuilder(1000);
        for (File o : list) {
	    String f = o.getAbsolutePath();
            if (buf.length() > 0) {
                buf.append(File.pathSeparatorChar);
            }
            buf.append(f);
        }
        System.setProperty("netbeans.dynamic.classpath", buf.toString());

        BootClassLoader loader = new BootClassLoader(list, new ClassLoader[] {
            MainImpl.class.getClassLoader()
        });

        // Needed for Lookup.getDefault to find MainLookup.
        // Note that ModuleManager.updateContextClassLoaders will later change
        // the loader on this and other threads to be MM.SystemClassLoader anyway.
        Thread.currentThread().setContextClassLoader (loader);


        //
        // Evaluate command line interfaces and lock the user directory
        //

        CLIHandler.Status result;
        result = CLIHandler.initialize(args, reader, writer, error, loader, true, false, loader);
        if (result.getExitCode () == CLIHandler.Status.CANNOT_CONNECT) {
            int value = JOptionPane.CLOSED_OPTION;
            
            if (!GraphicsEnvironment.isHeadless()) {
                value = JOptionPane.showConfirmDialog (
                    null,
                    ResourceBundle.getBundle("org/netbeans/Bundle").getString("MSG_AlreadyRunning"),
                    ResourceBundle.getBundle("org/netbeans/Bundle").getString("MSG_AlreadyRunningTitle"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
            }
            if (value == JOptionPane.OK_OPTION) {
                result = CLIHandler.initialize(args, reader, writer, error, loader, true, true, loader);
            } else {
                return result.getExitCode();
            }

        }
        if (result.getExitCode () == CLIHandler.Status.CANNOT_WRITE) {
            int value = JOptionPane.CLOSED_OPTION;
            
            if (!GraphicsEnvironment.isHeadless()) {
                value = JOptionPane.showConfirmDialog (
                    null,
                    MessageFormat.format(ResourceBundle.getBundle("org/netbeans/Bundle").getString("MSG_CannotWrite"), user),
                    ResourceBundle.getBundle("org/netbeans/Bundle").getString("MSG_CannotWriteTitle"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
            }
            if (value == JOptionPane.OK_OPTION) {
                result = CLIHandler.initialize(args, reader, writer, error, loader, true, true, loader);
            } else {
                return result.getExitCode();
            }
        }
        if (result.getExitCode () == CLIHandler.Status.ALREADY_RUNNING) {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null,
                    MessageFormat.format(ResourceBundle.getBundle("org/netbeans/Bundle").getString("MSG_AlreadyRunning"), user),
                    ResourceBundle.getBundle("org/netbeans/Bundle").getString("MSG_AlreadyRunningTitle"),
                    JOptionPane.OK_OPTION
                );
            }
            return result.getExitCode();
        }

        if (methodToCall != null) {
            String className = System.getProperty("netbeans.mainclass", "org.netbeans.core.startup.Main"); // NOI18N
            Class<?> c = loader.loadClass(className);
            methodToCall.set(c.getMethod("main", String[].class)); // NOI18N
        }

        return result.getExitCode ();
    }

    /**
     * Call when the system is up and running, to complete handling of
     * delayed command-line options like -open FILE.
     */
    public static void finishInitialization() {
        int r = CLIHandler.finishInitialization (false);
        if (r != 0) {
            if (r == Integer.MIN_VALUE) {
                r = 0;
            }
            TopSecurityManager.exit(r);
        }
    }

    static final class BootClassLoader extends JarClassLoader
    implements Runnable {
        private Lookup metaInf;

        private List<CLIHandler> handlers;

        public BootClassLoader(List<File> cp, ClassLoader[] parents) {
            super(cp, parents);

            metaInf = Lookups.metaInfServices(this);

            String value = null;
            try {
                if (cp.isEmpty ()) {
                    value = searchBuildNumber(this.getResources("META-INF/MANIFEST.MF"));
                } else {
                    value = searchBuildNumber(this.findResources("META-INF/MANIFEST.MF"));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (value == null) {
                System.err.println("Cannot set netbeans.buildnumber property no OpenIDE-Module-Build-Version found"); // NOI18N
            } else {
                System.setProperty ("netbeans.buildnumber", value); // NOI18N
            }
        }

        @Override // #154417: work around JAXP #6723276, at least within tests for now
        public InputStream getResourceAsStream(String name) {
            if (name.equals("META-INF/services/javax.xml.stream.XMLInputFactory")) { // NOI18N
                return super.getResourceAsStream(name);
            } else if (Boolean.getBoolean("org.netbeans.MainImpl.154417") && name.startsWith("META-INF/services/javax.xml.")) { // NOI18N
                return new ByteArrayInputStream(new byte[0]);
            } else {
                return super.getResourceAsStream(name);
            }
        }

        /** @param en enumeration of URLs */
        private static String searchBuildNumber(Enumeration<URL> en) {
            String value = null;
            try {
                java.util.jar.Manifest mf;
                URL u = null;
                while(en.hasMoreElements()) {
                    u = en.nextElement();
                    InputStream is = u.openStream();
                    mf = new java.util.jar.Manifest(is);
                    is.close();
                    // #251035: core-base now allows impl dependencies, with manually added impl version. Prefer Build-Version.
                    value = mf.getMainAttributes().getValue("OpenIDE-Module-Build-Version"); // NOI18N
                    if (value == null) {
                        value = mf.getMainAttributes().getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
                    }
                    if (value != null) {
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return value;
        }

        private boolean onlyRunRunOnce;
        /** Checks for new JARs in netbeans.user */
        @Override
        public void run () {
            // do not call this method twice
            if (onlyRunRunOnce) return;
            onlyRunRunOnce = true;

            ArrayList<File> toAdd = new ArrayList<File> ();
            String user = System.getProperty ("netbeans.user"); // NOI18N
            try {
                if (user != null) {
                    JarClassLoader.initializeCache();
                    
                    build_cp (new File (user), toAdd, new HashSet<File> (), new HashSet<String> ());
        
                }

                if (!toAdd.isEmpty ()) {
                    // source were already added in MainImpl.execute() method while processing userdir
                    metaInf = Lookups.metaInfServices(this);
                    if (handlers != null) {
                        handlers.clear();
                        handlers.addAll(metaInf.lookupAll(CLIHandler.class));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        /** #27226: startup optimization. */
        @Override
        protected PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermission();
        }
        private static PermissionCollection modulePermissions;
        private static synchronized PermissionCollection getAllPermission() {
            if (modulePermissions == null) {
                modulePermissions = new Permissions();
                modulePermissions.add(new AllPermission());
                modulePermissions.setReadOnly();
            }
            return modulePermissions;
        }

        /** For a given classloader finds all registered CLIHandlers.
         */
        public final Collection<? extends CLIHandler> allCLIs () {
            if (handlers == null) {
                handlers = new ArrayList<CLIHandler>(metaInf.lookupAll(CLIHandler.class));
            }
            return handlers;
        }
    } // end of BootClassLoader

    private static void append_jars_to_cp (File base, String pathToDir, Collection<File> toAdd, Set<String> processedPaths) throws IOException {
        File dir = new File (base, pathToDir);
        if (!dir.isDirectory()) return;

        File[] arr = dir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            String n = arr[i].getName ();
            /*
            if (n.equals("updater.jar") || // NOI18N
                (dir.getName().equals("locale") && n.startsWith("updater_") && n.endsWith(".jar"))) { // NOI18N
                // Used by launcher, not by us.
                continue;
            }
            */
            if (n.endsWith("jar") || n.endsWith ("zip")) { // NOI18N
                if (processedPaths.add (pathToDir + '/' + n)) { // NOI18N
                    toAdd.add(arr[i]);
                }
            }
        }
    }


    private static void build_cp(File base, Collection<File> toAdd, Set<File> processedDirs, Set<String> processedPaths)
    throws java.io.IOException {
        if (!processedDirs.add (base)) {
            // already processed
            return;
        }

        append_jars_to_cp(base, "core/patches", toAdd, processedPaths); // NOI18N
        append_jars_to_cp(base, "core", toAdd, processedPaths); // NOI18N
        // XXX a minor optimization: exclude any unused locale JARs
        // For example, lib/locale/ might contain:
        // core_ja.jar
        // core_f4j.jar
        // core_f4j_ja.jar
        // core_f4j_ce.jar
        // core_f4j_ce_ja.jar
        // core_ru.jar
        // core_fr.jar
        // [etc.]
        // Only some of these will apply to the current session, based on the
        // current values of Locale.default and NbBundle.branding.
        append_jars_to_cp(base, "core/locale", toAdd, processedPaths); // NOI18N
    }
}
