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

package org.netbeans.core.startup;

import org.netbeans.core.startup.logging.PrintStreamLogger;
import org.netbeans.core.startup.logging.NbFormatter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.logging.NbLogging;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 * Class that sets the java.util.logging.LogManager configuration to log into
 * the right file and put there the right content. Does nothing if
 * either <code>java.util.logging.config.file</code> or
 * <code>java.util.logging.config.class</code> is specified.
 */
public final class TopLogging {
    private static boolean disabledConsole = ! Boolean.getBoolean("netbeans.logger.console"); // NOI18N
    /** reference to the old error stream */
    private static final PrintStream OLD_ERR = System.err;

    /** Initializes the logging configuration. Invoked by <code>LogManager.readConfiguration</code> method.
     */
    public TopLogging() {
        AWTHandler.install();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Properties properties = System.getProperties();
        configureFromProperties(os, properties);
        try {
            StartLog.unregister();
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(os.toByteArray()));
        } catch (IOException ex) {
            ex.printStackTrace(OLD_ERR);
        } finally {
            StartLog.register();
        }


        Logger logger = Logger.getLogger (""); // NOI18N

        Handler[] old = logger.getHandlers();
        for (int i = 0; i < old.length; i++) {
            logger.removeHandler(old[i]);
        }
        logger.addHandler(defaultHandler ());
        if (!disabledConsole) { // NOI18N
            logger.addHandler (streamHandler ());
        }
        logger.addHandler(new LookupDel());
    }

    private Collection<Logger> configureFromProperties(ByteArrayOutputStream os, Properties properties) {
        try (PrintStream ps = new PrintStream(os)) {
            Collection<Logger> keep = new LinkedList<>();
            for (String key : properties.stringPropertyNames()) {
                if ("sun.os.patch.level".equals(key)) { // NOI18N
                    // skip this property as it does not mean level of logging
                    continue;
                }
                String v = properties.getProperty(key);
                if (v == null) {
                    continue;
                }
                if (key.endsWith(".level")) {
                    ps.print(key);
                    ps.print('=');
                    ps.println(v);
                    keep.add(Logger.getLogger(key.substring(0, key.length() - 6)));
                }
            }
            return keep;
        }
    }

    /**
     * For use from NbErrorManagerTest.
     */
    public static void initializeQuietly() {
        initialize(false);
    }
    
    public static void printStackTrace(Throwable t, PrintWriter pw) {
        NbFormatter.printStackTrace(t, pw);
    }
    
    private static File previousUser;
    static final void initialize() {
        initialize(true);
    }
    private static void initialize(boolean verbose) {
        AWTHandler.install();
        
        if (previousUser == null || previousUser.equals(Places.getUserDirectory())) {
            // useful from tests
            streamHandler = null;
            defaultHandler = null;
        }

        if (System.getProperty("java.util.logging.config.file") != null) { // NOI18N
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return;
        }
        String v = System.getProperty("java.util.logging.config.class"); // NOI18N
        String p = TopLogging.class.getName();
        if (v != null && !v.equals(p)) {
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return;
        }

        // initializes the properties
        TopLogging logging = new TopLogging();
        // next time invoke the constructor of TopLogging itself please
        System.setProperty("java.util.logging.config.class", p);

        if (verbose) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(os)) {
                logging.printSystemInfo(ps);
            }
            try {
                Logger logger = Logger.getLogger(TopLogging.class.getName()); // NOI18N
                logger.log(Level.INFO, os.toString("utf-8"));
            } catch (UnsupportedEncodingException ex) {
                assert false;
            }
        }
        if (!Boolean.getBoolean("netbeans.logger.noSystem")) {
            if (!PrintStreamLogger.isLogger(System.err)) {
                System.setErr(PrintStreamLogger.create("stderr")); // NOI18N
                if (NbLogging.DEBUG != null) NbLogging.DEBUG.println("initializing stderr"); // NOI18N
            }
            if (!PrintStreamLogger.isLogger(System.out)) {
                System.setErr(PrintStreamLogger.create("stderr")); // NOI18N
                if (NbLogging.DEBUG != null) NbLogging.DEBUG.println("initializing stdout"); // NOI18N
            }
        }
    }


    private void printSystemInfo(PrintStream ps) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
        Date date = new Date();

        ps.println("-------------------------------------------------------------------------------"); // NOI18N
        ps.println(">Log Session: "+df.format (date)); // NOI18N
        ps.println(">System Info: "); // NOI18N

        List<File> clusters = new ArrayList<>();
        String nbdirs = System.getProperty("netbeans.dirs");
        if (nbdirs != null) { // noted in #67862: should show all clusters here.
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                File dir = FileUtil.normalizeFile(new File(tok.nextToken()));
                if (dir.isDirectory()) {
                    clusters.add(dir);
                }
            }
        }

        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        String currentVersion = NbBundle.getMessage(TopLogging.class, "currentVersion", buildNumber );
        System.setProperty("netbeans.productversion", currentVersion); // NOI18N
        ps.print("  Product Version         = " + currentVersion); // NOI18N
        for (File cluster : clusters) { // also print Hg ID if available; more precise
            File buildInfo = new File(cluster, "build_info"); // NOI18N
            if (buildInfo.isFile()) {
                try {
                    try (Reader r = new FileReader(buildInfo)) {
                        BufferedReader b = new BufferedReader(r);
                        Pattern p = Pattern.compile("Hg ID:    ([0-9a-f]{12})"); // NOI18N
                        for (;;) {
                            String line = b.readLine();
                            if (line == null) {
                                break;
                            }
                            Matcher m = p.matcher(line);
                            if (m.matches()) {
                                ps.print(" (#" + m.group(1) + ")"); // NOI18N
                                break;
                            }
                        }
                    }
                } catch (IOException x) {
                    x.printStackTrace(ps);
                }
                break;
            }
        }
        ps.println();
        ps.println("  Operating System        = " + System.getProperty("os.name", "unknown")
                   + " version " + System.getProperty("os.version", "unknown")
                   + " running on " +  System.getProperty("os.arch", "unknown"));
        ps.println("  Java; VM; Vendor        = "
                + System.getProperty("java.version", "unknown") + "; "
                + System.getProperty("java.vm.name", "unknown") + " "
                + System.getProperty("java.vm.version", "") + "; "
                + System.getProperty("java.vendor", "unknown"));
        ps.println("  Runtime                 = "
                + System.getProperty("java.runtime.name", "unknown") + " "
                + System.getProperty("java.runtime.version", ""));
        ps.println("  Java Home               = " + System.getProperty("java.home", "unknown"));
        ps.print(  "  System Locale; Encoding = " + Locale.getDefault()); // NOI18N
        String branding = NbBundle.getBranding ();
        if (branding != null) {
            ps.print(" (" + branding + ")"); // NOI18N
        }
        ps.println("; " + System.getProperty("file.encoding", "unknown")); // NOI18N
        ps.println("  Home Directory          = " + System.getProperty("user.home", "unknown"));
        ps.println("  Current Directory       = " + System.getProperty("user.dir", "unknown"));
        ps.print(  "  User Directory          = "); // NOI18N
        ps.println(CLIOptions.getUserDir()); // NOI18N
        ps.println("  Cache Directory         = " + Places.getCacheDirectory()); // NOI18N
        ps.print(  "  Installation            = "); // NOI18N
        for (File cluster : clusters) {
            ps.print(cluster + "\n                            "); // NOI18N
        }
        ps.println(CLIOptions.getHomeDir()); // platform cluster is separate
        ps.println("  Boot & Ext. Classpath   = " + createBootClassPath()); // NOI18N
        String cp;
        ClassLoader l = Lookup.class.getClassLoader();
        if (l == ClassLoader.getSystemClassLoader()) {
            cp = System.getProperty("java.class.path", "unknown"); // NOI18N
        } else {
            StringBuilder sb = new StringBuilder("loaded by "); // NOI18N
            if (l instanceof URLClassLoader) {
                sb.append("URLClassLoader"); // NOI18N
                for (URL u : ((URLClassLoader)l).getURLs()) {
                    sb.append(' ').append(u);
                }
            } else {
                sb.append(l);
            }
            cp = sb.toString();
        }
        ps.println("  Application Classpath   = " + cp); // NOI18N
        ps.println("  Startup Classpath       = " + System.getProperty("netbeans.dynamic.classpath", "unknown")); // NOI18N
        ps.println("-------------------------------------------------------------------------------"); // NOI18N
    }

    // Copied from NbClassPath:
    private static String createBootClassPath() {
        // boot
        String boot = System.getProperty("sun.boot.class.path"); // NOI18N
        StringBuffer sb = (boot != null ? new StringBuffer(boot) : new StringBuffer());

        // std extensions
        findBootJars(System.getProperty("java.ext.dirs"), sb);
        findBootJars(System.getProperty("java.endorsed.dirs"), sb);
        return sb.toString();
    }

    /** Scans path list for something that can be added to classpath.
     * @param extensions null or path list
     * @param sb buffer to put results to
     */
    private static void findBootJars(final String extensions, final StringBuffer sb) {
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                File dir = new File(st.nextToken());
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        String name = entries[i].getName().toLowerCase(Locale.US);
                        if (name.endsWith(".zip") || name.endsWith(".jar")) { // NOI18N
                            if (sb.length() > 0) {
                                sb.append(File.pathSeparatorChar);
                            }
                            sb.append(entries[i].getPath());
                        }
                    }
                }
            }
        }
    }

    /** Logger for test purposes.
     */
    static Handler createStreamHandler (PrintStream pw) {
        StreamHandler s = new StreamHandler (
            pw, NbFormatter.FORMATTER
        );
        return NbLogging.createDispatchHandler(s, 50);
    }

    private static Handler streamHandler;
    private static synchronized Handler streamHandler() {
        if (streamHandler == null) {
            StreamHandler sth = new StreamHandler (OLD_ERR, NbFormatter.FORMATTER);
            sth.setLevel(Level.ALL);
            streamHandler = NbLogging.createDispatchHandler(sth, 500);
        }
        return streamHandler;
    }

    private static Handler defaultHandler;
    private static synchronized Handler defaultHandler() {
        if (defaultHandler != null) return defaultHandler;

        File home = Places.getUserDirectory();
        if (home != null && !CLIOptions.noLogging) {
            File dir = new File(new File(home, "var"), "log");
            dir.mkdirs ();

            Handler h = NbLogging.createMessagesHandler(dir);
            defaultHandler = NbLogging.createDispatchHandler(h, 5000);
        }

        if (defaultHandler == null) {
            defaultHandler = streamHandler();
            disabledConsole = true;
        }
        return defaultHandler;
    }

    /** Allows tests to flush all standard handlers */
    static void flush(boolean clear) {
        System.err.flush();


        Handler s = streamHandler;
        if (s != null) {
            s.flush();
        }

        Handler d = defaultHandler;
        if (d != null) {
            d.flush();
        }

        if (clear) {
            streamHandler = null;
            defaultHandler = null;
        }
    }
    static void close() {
        NbLogging.close(streamHandler);
        NbLogging.close(defaultHandler);
    }
    static void exit(int exit) {
        flush(false);
        TopSecurityManager.exit(exit);
    }

    static void exit(int exit, Throwable t) {
        t.printStackTrace(OLD_ERR);
        exit(exit);
    }

    private static final class LookupDel extends Handler {
        private final Lookup.Result<Handler> handlers;
        private Collection<? extends Handler> instances;


        LookupDel() {
            handlers = Lookup.getDefault().lookupResult(Handler.class);
            LookupListener onChange = (__) -> {
                instances = new ArrayList<>(handlers.allInstances());
            };
            onChange.resultChanged(null);
            assert instances != null;
            handlers.addLookupListener(onChange);
        }


        @Override
        public void publish(LogRecord record) {
            for (Handler h : instances) {
                h.publish(record);
            }
        }

        @Override
        public void flush() {
            for (Handler h : instances) {
                h.flush();
            }
        }

        @Override
        public void close() throws SecurityException {
            for (Handler h : instances) {
                h.close();
            }
        }
    } // end of LookupDel

    private static final class AWTHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler delegate;
        private final Logger g;

        private AWTHandler(UncaughtExceptionHandler delegate) {
            this.delegate = delegate;
            this.g = Logger.getLogger("global"); // NOI18N
        }
        
        static void install() {
            if (Thread.getDefaultUncaughtExceptionHandler() instanceof AWTHandler) {
                return;
            }
            Thread.setDefaultUncaughtExceptionHandler(new AWTHandler(Thread.getDefaultUncaughtExceptionHandler()));
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (delegate != null) {
                delegate.uncaughtException(t, e);
            }
            
            // Either org.netbeans or org.netbeans.core.execution pkgs:
            if (e.getClass().getName().endsWith(".ExitSecurityException")) { // NOI18N
                return;
            }
            if (e instanceof ThreadDeath) {
                return;
            }
            g.log(Level.SEVERE, null, e);
        }
    } // end of AWTHandler

}
