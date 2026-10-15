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
    private Boolean enable = null;
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
                new ModuleDeactivator(context).enableDisable(false);
            }

            // then disable
            if (isEnableScheduled()) {
                new ModuleDeactivator(context).enableDisable(true);
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
    
    private boolean isEnableScheduled () {
        if (enable == null) {
            exploreUpdateDir ();
        }
        return enable;
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
        enable = false;
        
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
                // enable
                if (enable == null || ! enable) {
                    enable = ModuleDeactivator.hasModulesForEnable (updateDir);
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
