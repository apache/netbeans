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

package org.netbeans.modules.autoupdate.ui;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;

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
            return container;
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
            return container;
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
            return container;
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
            return container;
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
            return container;
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
            return container;
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
            return container;
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
            return container;
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
            return container;
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
            return container;
        }
    }
}
