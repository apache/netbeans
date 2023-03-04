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
 * Defines the AMP stack distribution called "XAMPP" for Linux
 * See <a href="http://www.apachefriends.org/en/xampp-linux.html">
 * http://www.apachefriends.org/en/xampp-linux.html</a>
 * 
 * @author David Van Couvering
 */
public class LinuxXAMPPInstallation implements Installation {
    private static final String LAMPP = "/opt/lampp/lampp"; // NOI18N
    private static final String ADMIN_URL = "http://localhost/phpmyadmin"; // NOI18N
    private static final String DEFAULT_PORT = "3306"; // NOI18N
    private static final String GKSU = "/usr/bin/gksu"; // NOI18N
        
    private static final LinuxXAMPPInstallation DEFAULT = 
            new LinuxXAMPPInstallation();
    
    public static final LinuxXAMPPInstallation getDefault() {
        return DEFAULT;
    }
    
    protected LinuxXAMPPInstallation() {
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isInstalled() {
        return Utilities.isUnix() && Utils.isValidExecutable(LAMPP)
                && Utils.isValidExecutable(GKSU);
    }

    public String[] getAdminCommand() {
        return new String[] { ADMIN_URL, "" };
    }

    public String[] getStartCommand() {
        return new String[] { GKSU, LAMPP + " startmysql" };
    }

    public String[] getStopCommand() {
        return new String[] { GKSU, LAMPP + " stopmysql" };
    }
    
    public String getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public String toString() {
        return "Linux XAMPP Installation - " + LAMPP;                   //NOI18N
    }
}
