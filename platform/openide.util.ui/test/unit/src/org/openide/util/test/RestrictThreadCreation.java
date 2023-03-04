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

package org.openide.util.test;

import java.security.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Permits unit tests to limit creation of threads.
 * Unexpected thread creation can make tests fail randomly, which makes debugging difficult.
 * <p>Start off calling {@link #permitStandard} and {@link #forbidNewThreads}.
 * To determine which methods to permit, just try running the test;
 * if you see any {@link SecurityException}s which look like harmless thread creation
 * activities, just copy the appropriate part of the stack trace and pass to {@link #permit}.
 * <p>Use non-strict mode for {@link #forbidNewThreads} if you suspect some code
 * might be catching and not reporting {@link SecurityException}s; or you may prefer
 * to simply use non-strict mode while developing the test and then switch to strict
 * mode once it begins passing.
 * <p>Place calls to this class early in your test's initialization, e.g. in a static block.
 * (Not suitable for use from {@link junit.framework.TestCase#setUp}.)
 */
public class RestrictThreadCreation {

    private RestrictThreadCreation() {}

    private static Set<String> currentlyPermitted = new HashSet<String>();

    /**
     * Explicitly permits one or more thread creation idioms.
     * Each entry is of the form <samp>fully.qualified.Clazz.methodName</samp>
     * and if such a method can be found on the call stack the thread creation
     * is permitted.
     * @param permitted one or more fully qualified method names to accept
     */
    public static void permit(String... permitted) {
        currentlyPermitted.addAll(Arrays.asList(permitted));
    }

    /**
     * Permits a standard set of thread creation idioms which are normally harmless to unit tests.
     * Feel free to add to this list if it seems appropriate.
     */
    public static void permitStandard() {
        permit(// Found experimentally:
                "org.netbeans.junit.NbTestCase.runBare",
                "sun.java2d.Disposer.<clinit>",
                "java.awt.Toolkit.getDefaultToolkit",
                "java.util.logging.LogManager$Cleaner.<init>",
                "org.netbeans.ModuleManager$1.<init>",
                "org.netbeans.Stamps$Worker.<init>",
                "org.netbeans.core.startup.Splash$SplashComponent.setText",
                "org.netbeans.core.startup.preferences.NbPreferences.asyncInvocationOfFlushSpi",
                "org.openide.loaders.FolderInstance.waitFinished",
                "org.openide.loaders.FolderInstance.postCreationTask",
                "org.netbeans.modules.masterfs.filebasedfs.fileobjects.LockForFile.<clinit>",
                "org.netbeans.api.java.source.JavaSource.<clinit>",
                "org.netbeans.api.java.source.JavaSourceTaskFactory.fileObjectsChanged",
                "org.netbeans.modules.progress.spi.Controller.resetTimer",
                "org.netbeans.modules.project.ui.problems.BrokenProjectAnnotator.annotateIcon",
                "org.netbeans.modules.timers.InstanceWatcher$FinalizingToken.finalize",
                "org.openide.util.NbBundle.getBundleFast",
                "org.openide.util.RequestProcessor$TickTac.run",
                "org.openide.util.Utilities$ActiveQueue.ping",
                "javax.swing.JComponent.revalidate",
                "javax.swing.ImageIcon.<init>");
    }

    /**
     * Install a security manager which prevents new threads from being created
     * unless they were explicitly permitted.
     * @param strict if true, throw a security exception; if false, just print stack traces
     */
    public static void forbidNewThreads(final boolean strict) {
        System.setSecurityManager(new SecurityManager() {
            public @Override void checkAccess(ThreadGroup g) {
                boolean inThreadInit = false;
                for (StackTraceElement line : Thread.currentThread().getStackTrace()) {
                    String id = line.getClassName() + "." + line.getMethodName();
                    if (currentlyPermitted.contains(id)) {
                        return;
                    } else if (id.equals("java.lang.Thread.init") || id.equals("org.openide.util.RequestProcessor$Processor.checkAccess")) {
                        inThreadInit = true;
                    }
                }
                if (inThreadInit) {
                    SecurityException x = new SecurityException("Unauthorized thread creation");
                    if (strict) {
                        throw x;
                    } else {
                        x.printStackTrace();
                    }
                }
            }
            public @Override void checkPermission(Permission perm) {}
            public @Override void checkPermission(Permission perm, Object context) {}
        });
    }

}
