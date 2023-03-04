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

package org.netbeans.modules.db.mysql.installations;

import org.netbeans.modules.db.mysql.impl.Installation;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.util.Utilities;

/**
 * Standalone version on Linux.  Assuming installed as a service, and that
 * the standard approach is to manage the service, not another instance.
 * 
 * @author David Van Couvering
 */
public class LinuxStandaloneInstallation implements Installation {
    
    private static final LinuxStandaloneInstallation DEFAULT = new
            LinuxStandaloneInstallation();
    
    private static final String SVC_EXE = "/etc/init.d/mysql"; // NOI18N
    private static final String GKSU = "/usr/bin/gksu"; // NOI18N
        
    public static LinuxStandaloneInstallation getDefault() {
        return DEFAULT;
    }

    protected LinuxStandaloneInstallation() {
    }
    
    public String[] getStartCommand() {
        return new String[] { GKSU, SVC_EXE + " start"};
    }

    public String[] getStopCommand() {
        return new String[] { GKSU, SVC_EXE + " stop"};
    }
    
    public boolean isInstalled() {
        return Utilities.isUnix() && Utils.isValidExecutable(SVC_EXE) &&
                Utils.isValidExecutable(GKSU);
    }

    public boolean isStackInstall() {
        return false;
    }

    public String[] getAdminCommand() {
        return new String[] { "", ""};
    }

    public String getDefaultPort() {
        return "3306"; // NOI18N
    }

    @Override
    public String toString() {
        return "Linux Standalone Installation - " + SVC_EXE;            //NOI18N
    }
}
