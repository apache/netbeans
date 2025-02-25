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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.CLIHandler;
import org.netbeans.NbExit;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.LifecycleManager;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rudimentary manager useful for non-GUI platform applications.
 * Superseded by NbLifecycleManager.
 * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=158525">158525</a>
 */
@ServiceProvider(service=LifecycleManager.class)
public class ModuleLifecycleManager extends LifecycleManager {
    public ModuleLifecycleManager() {
    }

    public void saveAll() {
        // XXX #77210 would make it possible for some objects to be saved here
    }

    private final AtomicBoolean exiting = new AtomicBoolean(false);
    public void exit() {
        exit(0);
    }
    
    public void exit(int status) {
        if (exiting.getAndSet(true)) {
            return;
        }
        // Simplified version of NbLifecycleManager.doExit.
        if (Main.getModuleSystem().shutDown(new Runnable() {
            public void run() {
                try {
                    CLIHandler.stopServer();
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        })) {
            try {
                SessionManager.getDefault().close();
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
            if (System.getProperty("netbeans.close.no.exit") == null) {
                NbExit.exit(status);
            }
        }
    }

    public @Override void markForRestart() throws UnsupportedOperationException {
        markReadyForRestart();
    }

    /** Creates files that instruct the native launcher to perform restart as
     * soon as the Java process finishes. 
     * 
     * @since 1.45
     * @throws UnsupportedOperationException some environments (like WebStart)
     *   do not support restart and may throw an exception to indicate that
     */
    static void markReadyForRestart() throws UnsupportedOperationException {
        String classLoaderName = NbExit.class.getClassLoader().getClass().getName();
        if (!classLoaderName.endsWith(".Launcher$AppClassLoader") && !classLoaderName.endsWith(".ClassLoaders$AppClassLoader")) {   // NOI18N
            throw new UnsupportedOperationException("not running in regular module system, cannot restart"); // NOI18N
        }
        File userdir = Places.getUserDirectory();
        if (userdir == null) {
            throw new UnsupportedOperationException("no userdir"); // NOI18N
        }
        File restartFile = new File(userdir, "var/restart"); // NOI18N
        if (!restartFile.exists()) {
            try {
                restartFile.createNewFile();
            } catch (IOException x) {
                throw new UnsupportedOperationException(x);
            }
        }
    }

}
