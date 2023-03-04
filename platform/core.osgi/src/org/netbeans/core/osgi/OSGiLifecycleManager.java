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

package org.netbeans.core.osgi;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

class OSGiLifecycleManager extends LifecycleManager {

    private static final Logger LOG = Logger.getLogger(OSGiLifecycleManager.class.getName());

    private final BundleContext context;
    private final AtomicBoolean exited = new AtomicBoolean();

    OSGiLifecycleManager(BundleContext context) {
        this.context = context;
    }

    public @Override void saveAll() {
        for (LifecycleManager mgr : Lookup.getDefault().lookupAll(LifecycleManager.class)) {
            if (!(mgr instanceof OSGiLifecycleManager)) { // NbLifecycleManager, perhaps
                mgr.saveAll();
            }
        }
    }

    public @Override void exit() {
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            if (!((Boolean) loader.loadClass("org.netbeans.core.ExitDialog").getMethod("showDialog").invoke(null))) {
                return;
            }
        } catch (ClassNotFoundException x) {
            // Fine, core not available
        } catch (Exception x) {
            LOG.log(Level.WARNING, "Could not prompt to save open files", x);
            saveAll(); // backup
        }
        for (Map.Entry<Bundle,ModuleInstall> entry : Activator.installers.entrySet()) {
            String name = entry.getKey().getSymbolicName();
            LOG.log(Level.FINE, "closing: {0}", name);
            if (!entry.getValue().closing()) {
                LOG.log(Level.FINE, "Will not close by request of {0}", name);
                return;
            }
        }
        if (exited.getAndSet(true)) {
            return; // cannot exit twice
        }
        try {
            final Class<?> windowSystemClazz = loader.loadClass("org.netbeans.core.WindowSystem");
            final Object windowSystem = Lookup.getDefault().lookup(windowSystemClazz);
            if (windowSystem != null) {
                Mutex.EVENT.readAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        windowSystemClazz.getMethod("hide").invoke(windowSystem);
                        windowSystemClazz.getMethod("save").invoke(windowSystem);
                        return null;
                    }
                });
            }
        } catch (ClassNotFoundException x) {
            // Fine, just not using window system.
        } catch (Exception x) {
            LOG.log(Level.WARNING, "Could not shut down window system", x);
        }
        for (Map.Entry<Bundle,ModuleInstall> entry : Activator.installers.entrySet()) {
            String name = entry.getKey().getSymbolicName();
            LOG.log(Level.FINE, "close: {0}", name);
            entry.getValue().close();
        }
        SessionManager.getDefault().close();
        Bundle system = context.getBundle(0);
        if (system instanceof Framework) {
            try {
                ((Framework) system).stop();
            } catch (BundleException x) {
                LOG.log(Level.WARNING, "Could not stop OSGi framework", x);
            }
        }
    }

}
