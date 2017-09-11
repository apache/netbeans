/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.updater;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * @author  Jiri Rechtacek
 */
public final class UpdaterDispatcher implements Runnable {
    private Boolean disable = null;
    private Boolean install = null;
    private Boolean uninstall = null;
    
    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String DEACTIVATE_DIR = "deactivate"; // NOI18N
    public static final String NEW_UPDATER_DIR = "new_updater"; // NOI18N
    
    public static final String DEACTIVATE_LATER = "deactivate_later.txt"; // NOI18N
    
    public static final String LAST_MODIFIED = ".lastModified"; // NOI18N
    private final UpdatingContext context;

    UpdaterDispatcher (UpdatingContext context) {
        this.context = context;
    }
    
    
    /** Explore <cluster>/update directory and schedules actions handler for
     * Install/Update, Uninstall or Disable modules
     * 
     */
    private void dispatch () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ";
        try {
            // uninstall first
            if (isUninstallScheduled ()) {
                new ModuleDeactivator(context).delete();
            }

            // then disable
            if (isDisableScheduled ()) {
                new ModuleDeactivator(context).disable();
            }

            // finally install/update
            if (isInstallScheduled ()) {
                try {
                    ModuleUpdater mu = new ModuleUpdater (context);
                    mu.start ();
                    mu.join ();
                } catch (InterruptedException ex) {
                    XMLUtil.LOG.log(Level.SEVERE, "Error", ex);
                }
            }
        } catch (Exception x) {
            XMLUtil.LOG.log (Level.WARNING, "Handling delete throws", x);
        } finally {
            context.unpackingFinished ();
        }
    }
    
    private boolean isDisableScheduled () {
        if (disable == null) {
            exploreUpdateDir ();
        }
        return disable;
    }
    
    private boolean isUninstallScheduled () {
        if (uninstall == null) {
            exploreUpdateDir ();
        }
        return uninstall;
    }
    
    private boolean isInstallScheduled () {
        if (install == null) {
            exploreUpdateDir ();
        }
        return install;
    }
    
    private void exploreUpdateDir () {
        // initialize to false
        install = false;
        uninstall = false;
        disable = false;
        
        // go over all clusters
        for (File cluster : UpdateTracking.clusters (true)) {
            File updateDir = new File (cluster, UPDATE_DIR);
            if (updateDir.exists () && updateDir.isDirectory ()) {
                // install/update
                if (install == null || ! install) {
                    install = ! ModuleUpdater.getModulesToInstall (cluster).isEmpty ();
                }
                // uninstall
                if (uninstall == null || ! uninstall) {
                    uninstall = ModuleDeactivator.hasModulesForDelete (updateDir);
                }
                // disable
                if (disable == null || ! disable) {
                    disable = ModuleDeactivator.hasModulesForDisable (updateDir);
                }
            }
        }
    }

    @Override
    public void run () {
        dispatch ();
        context.disposeSplash();
    }
    
    public static void touchLastModified (File cluster) {
        if(!cluster.exists()) {
            return;
        }
        try {
            File stamp = new File (cluster, LAST_MODIFIED);
            if(!stamp.exists() && !stamp.createNewFile ()) {
                throw new IOException("Can`t create stamp file " + stamp);
            }
            if(!stamp.setLastModified (System.currentTimeMillis ())) {
                stamp.delete ();
                stamp.createNewFile ();
                stamp.setLastModified (System.currentTimeMillis ());
            }
        } catch (IOException ex) {
            XMLUtil.LOG.log(Level.WARNING, null, ex);
        }
    }
    
}
