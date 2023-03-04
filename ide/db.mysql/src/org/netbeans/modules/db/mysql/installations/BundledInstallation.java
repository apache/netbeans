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
 * Supports the MySQL/NetBeans bundled installation.  The bundled
 * installation sets a system variable to tell us what the start
 * and stop commands and arguments are.
 * 
 * @author David Van Couvering
 */
public class BundledInstallation implements Installation {
    private String startExe;
    private String startArgs;
    private String stopExe;
    private String stopArgs;
    private String port;
    
    private static final BundledInstallation DEFAULT = 
            new BundledInstallation();
    
    public static final BundledInstallation getDefault() {
        return DEFAULT;
    }
    
    private BundledInstallation() {
        startExe = System.getProperty("com.sun.mysql.startcommand");
        startArgs = System.getProperty("com.sun.mysql.startargs");
        stopExe = System.getProperty("com.sun.mysql.stopcommand");
        stopArgs = System.getProperty("com.sun.mysql.stopargs");
        port = System.getProperty("com.sun.mysql.port", "3306");
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isInstalled() {
        return startExe != null && startExe.length() > 0;
    }

    public String[] getAdminCommand() {
        return new String[] {
            "",
            ""
        };
    }

    public String[] getStartCommand() {
        return new String[] { startExe, startArgs };
    }

    public String[] getStopCommand() {
        return new String[] { stopExe, stopArgs };
    }
    
    public String getDefaultPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Bundled Installation";                                  //NOI18N
    }
}
