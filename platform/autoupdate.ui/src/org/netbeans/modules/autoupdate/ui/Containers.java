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

package org.netbeans.modules.autoupdate.ui;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.openide.util.NbPreferences;

/**
 *
 * @author Radek Matous
 */
public class Containers {
    private static Reference<OperationContainer<InstallSupport>> INSTALL;
    private static Reference<OperationContainer<InstallSupport>> INTERNAL_UPDATE;
    private static Reference<OperationContainer<InstallSupport>> UPDATE;
    private static Reference<OperationContainer<InstallSupport>> INSTALL_FOR_NBMS;
    private static Reference<OperationContainer<InstallSupport>> UPDATE_FOR_NBMS;
    private static Reference<OperationContainer<OperationSupport>> UNINSTALL;
    private static Reference<OperationContainer<OperationSupport>> ENABLE;
    private static Reference<OperationContainer<OperationSupport>> DISABLE;
    private static Reference<OperationContainer<OperationSupport>> CUSTOM_INSTALL;
    private static Reference<OperationContainer<OperationSupport>> CUSTOM_UNINSTALL;

    private Containers(){}
    public static void initNotify() {
        try {
            forAvailableNbms().removeAll();
            forUpdateNbms().removeAll();
            forAvailable().removeAll();
            forUninstall().removeAll();
            forUpdate().removeAll();
            forEnable().removeAll();
            forDisable().removeAll();
            forCustomInstall().removeAll();
            forCustomUninstall().removeAll();
            forInternalUpdate().removeAll();
        } catch (NullPointerException npe) {
            // doesn't matter, can ignore that
        }
    }

    public static OperationContainer<InstallSupport> forAvailableNbms() {
        synchronized(Containers.class) {
            OperationContainer<InstallSupport> container = null;
            if (INSTALL_FOR_NBMS != null) {
                container = INSTALL_FOR_NBMS.get();
            }
            if (container==null) {
                container = OperationContainer.createForInstall();
                INSTALL_FOR_NBMS = new WeakReference<OperationContainer<InstallSupport>>(container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<InstallSupport> forUpdateNbms() {
        synchronized(Containers.class) {
            OperationContainer<InstallSupport> container = null;
            if (UPDATE_FOR_NBMS != null) {
                container = UPDATE_FOR_NBMS.get();
            }
            if (container==null) {
                container = OperationContainer.createForUpdate();
                UPDATE_FOR_NBMS = new WeakReference<OperationContainer<InstallSupport>>(container);
            }
            return useUnpack200(container);
        }
    }

    public static OperationContainer<InstallSupport> forAvailable() {
        synchronized(Containers.class) {
            OperationContainer<InstallSupport> container = null;
            if (INSTALL != null) {
                container = INSTALL.get();
            }
            if (container == null) {
                container  = OperationContainer.createForInstall();
                INSTALL = new WeakReference<OperationContainer<InstallSupport>>(container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<InstallSupport> forUpdate() {
        synchronized(Containers.class) {
            OperationContainer<InstallSupport> container = null;
            if (UPDATE != null) {
                container = UPDATE.get();
            }
            if (container == null) {
                container = OperationContainer.createForUpdate();
                UPDATE = new WeakReference<OperationContainer<InstallSupport>>(container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<OperationSupport> forUninstall() {
        synchronized(Containers.class) {
            OperationContainer<OperationSupport> container = null;
            if (UNINSTALL != null) {
                container = UNINSTALL.get();
            }
            if (container == null) {
                container = OperationContainer.createForUninstall();
                UNINSTALL = new WeakReference<OperationContainer<OperationSupport>>(container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<OperationSupport> forEnable() {
        synchronized(Containers.class) {
            OperationContainer<OperationSupport> container = null;
            if (ENABLE != null) {
                container = ENABLE.get();
            }
            if(container == null) {
                container = OperationContainer.createForEnable();
                ENABLE = new WeakReference<OperationContainer<OperationSupport>>(container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<OperationSupport> forDisable() {
        synchronized(Containers.class) {
            OperationContainer<OperationSupport> container = null;
            if (DISABLE != null) {
                container = DISABLE.get();
            }
            if(container == null) {
                container = OperationContainer.createForDisable();
                DISABLE = new WeakReference<OperationContainer<OperationSupport>>(container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<OperationSupport> forCustomInstall () {
        synchronized (Containers.class) {
            OperationContainer<OperationSupport> container = null;
            if (CUSTOM_INSTALL != null) {
                container = CUSTOM_INSTALL.get ();
            }
            if(container == null) {
                container = OperationContainer.createForCustomInstallComponent ();
                CUSTOM_INSTALL = new WeakReference<OperationContainer<OperationSupport>> (container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<OperationSupport> forCustomUninstall () {
        synchronized (Containers.class) {
            OperationContainer<OperationSupport> container = null;
            if (CUSTOM_UNINSTALL != null) {
                container = CUSTOM_UNINSTALL.get ();
            }
            if(container == null) {
                container = OperationContainer.createForCustomUninstallComponent ();
                CUSTOM_UNINSTALL = new WeakReference<OperationContainer<OperationSupport>> (container);
            }
            return useUnpack200(container);
        }
    }
    public static OperationContainer<InstallSupport> forInternalUpdate () {
        synchronized (Containers.class) {
            OperationContainer<InstallSupport> container = null;
            if (INTERNAL_UPDATE != null) {
                container = INTERNAL_UPDATE.get ();
            }
            if(container == null) {
                container = OperationContainer.createForInternalUpdate();
                INTERNAL_UPDATE = new WeakReference<OperationContainer<InstallSupport>> (container);
            }
            return useUnpack200(container);
        }
    }

    public static void defineUnpack200(File executable) {
        NbPreferences.forModule(OperationContainer.class).put("unpack200", executable.getPath()); // NOI18N
        INSTALL.clear();
        INTERNAL_UPDATE.clear();
        UPDATE.clear();
        INSTALL_FOR_NBMS.clear();
        UPDATE_FOR_NBMS.clear();
        UNINSTALL.clear();
        ENABLE.clear();
        DISABLE.clear();
        CUSTOM_INSTALL.clear();
        CUSTOM_UNINSTALL.clear();
    }

    private static <T> OperationContainer<T> useUnpack200(OperationContainer<T> container) {
        String pack200 = NbPreferences.forModule(OperationContainer.class).get("unpack200", null); // NOI18N
        if (pack200 != null) {
            final File file = new File(pack200);
            if (file.canExecute()) {
                container.setUnpack200(file);
            }
        }
        return container;
    }
}
