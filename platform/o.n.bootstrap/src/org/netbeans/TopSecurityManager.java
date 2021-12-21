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

package org.netbeans;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.agent.hooks.TrackingHooks;
import org.openide.util.WeakSet;

/** NetBeans security manager implementation.
* @author Ales Novak, Jesse Glick
*/
public class TopSecurityManager extends TrackingHooks {
    private static final boolean check = !Boolean.getBoolean("netbeans.security.nocheck"); // NOI18N
    private static final Logger LOG = Logger.getLogger(TopSecurityManager.class.getName());

    public static void install() {
        try {
            Class<?> agent = Class.forName("org.netbeans.agent.TrackingAgent", false, ClassLoader.getSystemClassLoader());
            agent.getDeclaredMethod("install").invoke(null);
        } catch (ReflectiveOperationException ex) {
            LOG.log(Level.WARNING, "Cannot associate tracking hooks, the application will be unstable"); // NOI18N
            LOG.log(Level.INFO, "Cannot associate tracking hooks, the application will be unstable", ex); // NOI18N
        }
        TrackingHooks.register(new TopSecurityManager(), 1000, TrackingHooks.HOOK_EXIT, TrackingHooks.HOOK_PROPERTY, TrackingHooks.HOOK_SECURITY_MANAGER, TrackingHooks.HOOK_ACCESSIBLE);
    }

    static boolean officialExit = false;

    @Override
    protected void checkExit(int i) {
        if (!officialExit) {
            throw new ExitSecurityException("Illegal attempt to exit early"); // NOI18N
        }
    }

    /** Can be called from core classes to exit the system.
     * Direct calls to System.exit will not be honored, for safety.
     * @param status the status code to exit with
     * @see "#20751"
     */
    public static void exit(int status) {
        if (officialExit) {
            return; // already inside a shutdown hook
        }
        officialExit = true;
        System.exit(status);
    }

    @Override
    protected  final void checkSystemProperty(String property) {
        if ("netbeans.debug.exceptions".equals(property)) { // NOI18N
            // Get rid of this old system property.
            for (Class<?> c : getStack()) {
                if (c != TopSecurityManager.class &&
                        c != System.class &&
                        c != Boolean.class) {
                    String n = c.getName();
                    synchronized (warnedClassesNDE) {
                        if (warnedClassesNDE.add(n)) {
                            LOG.log(Level.WARNING, "use of system property netbeans.debug.exceptions has been obsoleted in favor of java.util.logging.Logger at {0}", findCallStackLine(n));
                        }
                    }
                    break;
                }
            }
        }
        if ("netbeans.home".equals(property) || "netbeans.user".equals(property)) { // NOI18N
            // Control access to this system property.
            for (Class<?> c : getStack()) {
                if (c != TopSecurityManager.class &&
                        c != System.class &&
                        c != Boolean.class) {
                    String n = c.getName();
                    boolean log;
                    synchronized (warnedClassesNH) {
                        log = warnedClassesNH.add(n);
                    }
                    if (log) {
                        LOG.log(Level.WARNING, "use of system property {0} has been obsoleted in favor of InstalledFileLocator/Places at {1}", new Object[] {property, findCallStackLine(n)});
                    }
                    break;
                }
            }
        }
    }
    private static String findCallStackLine(String callerClazz) {
        for (StackTraceElement line : Thread.currentThread().getStackTrace()) {
            if (line.getClassName().equals(callerClazz)) {
                return line.toString();
            }
        }
        return callerClazz;
    }
    private final Set<String> warnedClassesNDE = new HashSet<String>(25);
    private static final Set<String> warnedClassesNH = new HashSet<String>(25);
    static {
        // XXX cleaner would be to use @SuppressWarnings, but that has Retention(SOURCE), and not all these can use org.netbeans.api.annotations.common
        warnedClassesNH.add("org.openide.modules.Places");
        warnedClassesNH.add("org.netbeans.MainImpl"); // NOI18N
        warnedClassesNH.add("org.netbeans.MainImpl$BootClassLoader");
        warnedClassesNH.add("org.netbeans.CLIHandler");
        warnedClassesNH.add("org.netbeans.Stamps"); // NOI18N
        warnedClassesNH.add("org.netbeans.Clusters"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.startup.InstalledFileLocatorImpl"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.startup.CLIOptions");
        warnedClassesNH.add("org.netbeans.core.startup.preferences.RelPaths");
        warnedClassesNH.add("org.netbeans.core.startup.layers.BinaryFS");
        warnedClassesNH.add("org.netbeans.modules.netbinox.NetbinoxFactory");
        warnedClassesNH.add("org.netbeans.updater.UpdateTracking"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.ui.ProductInformationPanel"); // #47429; NOI18N
        warnedClassesNH.add("org.netbeans.lib.uihandler.LogFormatter");
        warnedClassesNH.add("org.netbeans.modules.project.libraries.LibrariesStorage");
        warnedClassesNH.add("org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties"); // AS bundle is not in any cluster
        warnedClassesNH.add("org.netbeans.modules.apisupport.project.universe.NbPlatform"); // defaultPlatformLocation
    }


    static Class<?>[] getStack() {
        StackSecurityManager t = new StackSecurityManager();
        return t.getClassContext();
    }
    private static final class StackSecurityManager extends SecurityManager {
        @Override
        protected Class[] getClassContext() {
            return super.getClassContext();
        }
    }


    private final Set<Class<?>> warnedSunMisc = new WeakSet<>();
    private final Set<String> callerWhiteList = createCallerWhiteList();

    @Override
    @SuppressWarnings("deprecation")
    protected void checkSetAccessible(AccessibleObject what) {
        Class<?> clazz;
        if (what instanceof Executable) {
            clazz = ((Executable) what).getDeclaringClass();
        } else if (what instanceof Field) {
            clazz = ((Field) what).getDeclaringClass();
        } else {
            return ;
        }
        final String n = clazz.getName();
        if (n.startsWith("sun.misc")) { // NOI18N
            Class<?> caller = null;
            Class<?>[] arr = getStack();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == TopSecurityManager.class) {
                    continue;
                }
                if (arr[i] != Class.class) {
                    caller = arr[i];
                    break;
                }
            }
            StringBuilder msg = new StringBuilder();
            msg.append("Dangerous reflection access to ").append(n).append(" by ").append(caller).append(" detected!");
            if (caller != null && caller.getProtectionDomain() != null) {
                CodeSource cs = caller.getProtectionDomain().getCodeSource();
                msg.append("\ncode location: ").append(cs == null ? "null" : cs.getLocation());
            }
            if (caller != null && isDangerous(caller.getName(), n)) {
                throw new SecurityException(msg.toString());
            }
            Level l;
            if (caller != null && callerWhiteList.contains(caller.getName())) {
                l = Level.FINEST;
            } else {
                l = Level.FINE;
                assert (l = Level.INFO) != null;
            }
            if (!warnedSunMisc.add(caller)) {
                LOG.log(l, msg.toString());
                return;
            }
            Exception ex = new Exception(msg.toString()); // NOI18N
            LOG.log(l, null, ex);
        }
    }

    /**
     * Create list of safe callers for {@link #checkMemberAccess(Class, int)}.
     */
    private static Set<String> createCallerWhiteList() {
        Set<String> wl = new HashSet<String>();
        wl.add("org.netbeans.core.output2.FileMapStorage");             //NOI18N
        wl.add("com.sun.tools.javac.util.CloseableURLClassLoader");     //NOI18N
        wl.add("java.lang.Thread$1");                                   //NOI18N
        wl.add("org.clank.support.NativeMemory");                       //NOI18N
        wl.add("org.apache.lucene.store.MMapDirectory$1");              //NOI18N
        wl.add("org.apache.lucene.util.Constants"); //#217037
        wl.add("org.apache.lucene.util.RamUsageEstimator");//#217037
        wl.add("com.google.gson.internal.UnsafeAllocator"); //#219464   //NOI18N
        wl.add("org.netbeans.modules.web.jspparser_ext.WebAppParseSupport$ParserClassLoader"); //#218690 // NOI18N
        return wl;
    }
    private static boolean isDangerous(String caller, String accessTo) {
        if ("com.sun.istack.tools.ProtectedTask".equals(caller)) { // NOI18N
            if ("sun.misc.ClassLoaderUtil".equals(accessTo)) { // NOI18N
                // calling ClassLoaderUtil is allowed
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void checkSetSecurityManager(Object what) {
        if (!check) {
            return;
        }
        Class<?>[] arr = getStack();
        boolean seenJava = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getName().equals("org.netbeans.TopSecurityManager")) { // NOI18N
                if (seenJava) {
                    // if the change of security manager is called from my own
                    // class or the class loaded by other classloader, then it is likely ok
                    return;
                } else {
                    continue;
                }
            }
            if (arr[i] != System.class) {
                // if there is a non-java class on stack, skip and throw exception
                break;
            }
            seenJava = true;
        }
        throw new SecurityException();
    }
}
