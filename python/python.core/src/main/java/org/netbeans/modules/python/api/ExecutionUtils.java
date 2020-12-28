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
package org.netbeans.modules.python.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Utility methods for external execution.
 * <p>
 * <i>Most of the methods here
 * used to be in <code>PythonExecution</code>.</i>
 *
 * @author Erno Mononen
 */
public final class ExecutionUtils {

    private static final Logger LOGGER = Logger.getLogger(ExecutionUtils.class.getName());


//    /** When not set (the default) do stdio syncing for native Python binaries */
//    private static final boolean SYNC_RUBY_STDIO = System.getProperty("ruby.no.sync-stdio") == null; // NOI18N
    /** When not set (the default) bypass the JPython launcher unix/ba-file scripts and launch VM directly */
//    private static final boolean LAUNCH_JRUBY_SCRIPT =
//        System.getProperty("ruby.use.jruby.script") != null; // NOI18N

    private ExecutionUtils() {
    }

//    /** When not set (the default) bypass the JPython launcher unix/ba-file scripts and launch VM directly */
//    public static boolean launchJPythonScript() {
//        return LAUNCH_JRUBY_SCRIPT;
//    }
//    /**
//     * Returns the basic Python interpreter command and associated flags (not
//     * application arguments)
//     */
//    public static List<? extends String> getPythonArgs(final PythonPlatform platform) {
//        PythonExecutionDescriptor desc = new PythonExecutionDescriptor(platform);
//        return getPythonArgs(platform.getHome().getAbsolutePath(),
//                platform.getInterpreterFile().getName(), desc, null);
//    }
//
//    private static List<? extends String> getPythonArgs(String rubyHome, String cmdName, PythonExecutionDescriptor descriptor, String charsetName) {
//        List<String> argvList = new ArrayList<String>();
//        // Decide whether I'm launching JPython, and if so, take a shortcut and launch
//        // the VM directly. This is important because killing JPython via the launcher script
//        // is not working right; now that JPython on Unix exec's the VM that part is okay but
//        // on Windows there are still problems.
//        if (!launchJPythonScript() && cmdName.startsWith("jruby")) { // NOI18N
//            String javaHome = getJavaHome();
//
//            argvList.add(javaHome + File.separator + "bin" + File.separator + // NOI18N
//                    "java"); // NOI18N
//            // XXX Do I need java.exe on Windows?
//
//            // Additional execution flags specified in the JPython startup script:
//            argvList.add("-Xverify:none"); // NOI18N
//            argvList.add("-da"); // NOI18N
//
//            String javaMemory = "-Xmx512m"; // NOI18N
//            String javaStack = "-Xss1024k"; // NOI18N
//
//            String[] jvmArgs = descriptor == null ? null : descriptor.getJVMArguments();
//            if (jvmArgs != null) {
//                for (String arg : jvmArgs) {
//                    if (arg.contains("-Xmx")) { // NOI18N
//                        javaMemory = null;
//                    }
//                    if (arg.contains("-Xss")) { // NOI18N
//                        javaStack = null;
//                    }
//                    argvList.add(arg);
//                }
//            }
//
//            if (javaMemory != null) {
//                argvList.add(javaMemory);
//            }
//            if (javaStack != null) {
//                argvList.add(javaStack);
//            }
//
//            // Classpath
//            argvList.add("-classpath"); // NOI18N
//
//            File rubyHomeDir = null;
//
//            try {
//                rubyHomeDir = new File(rubyHome);
//                rubyHomeDir = rubyHomeDir.getCanonicalFile();
//            } catch (IOException ioe) {
//                Exceptions.printStackTrace(ioe);
//            }
//
//            if (!rubyHomeDir.isDirectory()) {
//                throw new IllegalArgumentException(rubyHomeDir.getAbsolutePath() + " does not exist."); // NOI18N
//            }
//
//            File jrubyLib = new File(rubyHomeDir, "lib"); // NOI18N
//            if (!jrubyLib.isDirectory()) {
//                throw new AssertionError('"' + jrubyLib.getAbsolutePath() + "\" exists (\"" + cmdName + "\" is not valid JPython executable?)");
//            }
//
//            argvList.add(computeJPythonClassPath(
//                    descriptor == null ? null : descriptor.getClassPath(), jrubyLib));
//
//            argvList.add("-Djruby.base=" + rubyHomeDir); // NOI18N
//            argvList.add("-Djruby.home=" + rubyHomeDir); // NOI18N
//            argvList.add("-Djruby.lib=" + jrubyLib); // NOI18N
//
//            // TODO - turn off verifier?
//
//            if (Utilities.isWindows()) {
//                argvList.add("-Djruby.shell=\"cmd.exe\""); // NOI18N
//                argvList.add("-Djruby.script=jruby.bat"); // NOI18N
//            } else {
//                argvList.add("-Djruby.shell=/bin/sh"); // NOI18N
//                argvList.add("-Djruby.script=jruby"); // NOI18N
//            }
//
//            // Main class
//            argvList.add("org.jruby.Main"); // NOI18N
//
//        // TODO: JRUBYOPTS
//
//        // Application arguments follow
//        }
//
//        // Is this a native Python process? If so, do sync-io workaround.
//        if (SYNC_RUBY_STDIO && cmdName.startsWith("ruby")) { // NOI18N
//
//            int dot = cmdName.indexOf('.');
//
//            if ((dot == -1) || (dot == 4) || (dot == 5)) { // 5: rubyw
//
//                InstalledFileLocator locator = InstalledFileLocator.getDefault();
//                File f =
//                        locator.locate("modules/org-netbeans-modules-ruby-project.jar", // NOI18N
//                        null, false); // NOI18N
//
//                if (f == null) {
//                    throw new RuntimeException("Can't find cluster"); // NOI18N
//                }
//
//                f = new File(f.getParentFile().getParentFile().getAbsolutePath() + File.separator +
//                        "sync-stdio.rb"); // NOI18N
//
//                try {
//                    f = f.getCanonicalFile();
//                } catch (IOException ioe) {
//                    Exceptions.printStackTrace(ioe);
//                }
//
//                argvList.add("-r" + f.getAbsolutePath()); // NOI18N
//            }
//        }
//        return argvList;
//    }
//
//    /** Package-private for unit test. */
//    static String computeJPythonClassPath(String extraCp, final File jrubyLib) {
//        StringBuilder cp = new StringBuilder();
//        File[] libs = jrubyLib.listFiles();
//
//        for (File lib : libs) {
//            if (lib.getName().endsWith(".jar")) { // NOI18N
//
//                if (cp.length() > 0) {
//                    cp.append(File.pathSeparatorChar);
//                }
//
//                cp.append(lib.getAbsolutePath());
//            }
//        }
//
//        // Add in user-specified jars passed via JRUBY_EXTRA_CLASSPATH
//
//        if (extraCp != null && File.pathSeparatorChar != ':') {
//            // Ugly hack - getClassPath has mixed together path separator chars
//            // (:) and filesystem separators, e.g. I might have C:\foo:D:\bar but
//            // obviously only the path separator after "foo" should be changed to ;
//            StringBuilder p = new StringBuilder();
//            int pathOffset = 0;
//            for (int i = 0; i < extraCp.length(); i++) {
//                char c = extraCp.charAt(i);
//                if (c == ':' && pathOffset != 1) {
//                    p.append(File.pathSeparatorChar);
//                    pathOffset = 0;
//                    continue;
//                } else {
//                    pathOffset++;
//                }
//                p.append(c);
//            }
//            extraCp = p.toString();
//        }
//
//        if (extraCp == null) {
//            extraCp = System.getenv("JRUBY_EXTRA_CLASSPATH"); // NOI18N
//        }
//
//        if (extraCp != null) {
//            if (cp.length() > 0) {
//                cp.append(File.pathSeparatorChar);
//            }
//            //if (File.pathSeparatorChar != ':' && extraCp.indexOf(File.pathSeparatorChar) == -1 &&
//            //        extraCp.indexOf(':') != -1) {
//            //    extraCp = extraCp.replace(':', File.pathSeparatorChar);
//            //}
//            cp.append(extraCp);
//        }
//        return Utilities.isWindows() ? "\"" + cp.toString() + "\"" : cp.toString(); // NOI18N
//    }
//
//    public static void setupProcessEnvironment(Map<String, String> env, final String pwd, boolean appendJdkToPath) {
//        String path = pwd;
//        if (!Utilities.isWindows()) {
//            path = path.replace(" ", "\\ "); // NOI18N
//        }
//
//        // Find PATH environment variable - on Windows it can be some other
//        // case and we should use whatever it has.
//        String pathName = "PATH"; // NOI18N
//
//        if (Utilities.isWindows()) {
//            pathName = "Path"; // NOI18N
//
//            for (String key : env.keySet()) {
//                if ("PATH".equals(key.toUpperCase())) { // NOI18N
//                    pathName = key;
//
//                    break;
//                }
//            }
//        }
//
//        String currentPath = env.get(pathName);
//
//        if (currentPath == null) {
//            currentPath = "";
//        }
//
//        currentPath = path + File.pathSeparator + currentPath;
//
//        if (appendJdkToPath) {
//            // jruby.java.home always points to jdk(?)
//            String jdkHome = System.getProperty("jruby.java.home"); // NOI18N
//
//            if (jdkHome == null) {
//                // #115377 - add jdk bin to path
//                jdkHome = System.getProperty("jdk.home"); // NOI18N
//            }
//
//            String jdkBin = jdkHome + File.separator + "bin"; // NOI18N
//            if (!Utilities.isWindows()) {
//                jdkBin = jdkBin.replace(" ", "\\ "); // NOI18N
//            }
//            currentPath = currentPath + File.pathSeparator + jdkBin;
//        }
//
//        env.put(pathName, currentPath); // NOI18N
//    }
//
//    public static String getJavaHome() {
//        String javaHome = System.getProperty("jruby.java.home"); // NOI18N
//
//        if (javaHome == null) {
//            javaHome = System.getProperty("java.home"); // NOI18N
//        }
//
//        return javaHome;
//    }
//
//    /** Just helper method for logging. */
//    public static void logProcess(final ProcessBuilder pb) {
//        if (LOGGER.isLoggable(Level.FINE)) {
//            File dir = pb.directory();
//            String basedir = dir == null ? "" : "(basedir: " + dir.getAbsolutePath() + ") ";
//            LOGGER.fine("Running: " + basedir + '"' + getProcessAsString(pb.command()) + '"');
//            LOGGER.fine("Environment: " + pb.environment());
//        }
//    }
//
//    /** Just helper method for logging. */
//    private static String getProcessAsString(List<? extends String> process) {
//
//        StringBuilder sb = new StringBuilder();
//        for (String arg : process) {
//            sb.append(arg).append(' ');
//        }
//        return sb.toString().trim();
//    }
//

}
