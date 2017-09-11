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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
