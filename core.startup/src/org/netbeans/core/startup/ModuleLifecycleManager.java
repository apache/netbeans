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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.CLIHandler;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.LifecycleManager;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rudimentary manager useful for non-GUI platform applications.
 * Superseded by NbLifecycleManager.
 * @see #158525
 */
@ServiceProvider(service=LifecycleManager.class)
public class ModuleLifecycleManager extends LifecycleManager {
    public ModuleLifecycleManager() {
        Runtime.getRuntime().addShutdownHook(new Thread("close modules") { // NOI18N
            public @Override void run() {
                if (System.getSecurityManager() instanceof TopSecurityManager) {
                    LifecycleManager.getDefault().exit();
                }
            }
        });
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
                TopSecurityManager.exit(status);
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
        if (!TopSecurityManager.class.getClassLoader().getClass().getName().endsWith(".Launcher$AppClassLoader")) {
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
