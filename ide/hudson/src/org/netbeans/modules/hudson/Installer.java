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

package org.netbeans.modules.hudson;

import java.util.prefs.BackingStoreException;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;

@OnShowing
public class Installer implements Runnable {
    
    @Override public void run() {
        if (active()) {
            doRun();
        }
    }

    /** split into different method to make sure JVM does not try to load HudsonManagerImpl if inactive */
    private void doRun() {
        HudsonManagerImpl.getDefault().getInstances();
    }

    @OnStop // XXX really needed?
    public static class Uninstaller implements Runnable {

        @Override public void run() {
            if (active()) {
                doRun();
            }
        }

        private void doRun() {
            HudsonManagerImpl.getDefault().terminate();
        }

    }

    /** #159810: avoid loading anything further unless this module is known to be in use */
    public static boolean active() {
        try {
            return NbPreferences.forModule(Installer.class).nodeExists("instances"); // NOI18N
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

}
