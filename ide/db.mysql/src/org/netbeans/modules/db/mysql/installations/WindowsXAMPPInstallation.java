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
public class WindowsXAMPPInstallation implements Installation {
    private static final String DEFAULT_BASE_PATH = "C:/xampp"; // NOI18N
    private static final String START_PATH="/mysql_start.bat";
    private static final String STOP_PATH="/mysql_stop.bat";
    private static final String ADMIN_URL = "http://localhost/phpmyadmin";
    private static final String DEFAULT_PORT = "3306";
    
    private String basePath = DEFAULT_BASE_PATH;
    
    private static final WindowsXAMPPInstallation DEFAULT = 
            new WindowsXAMPPInstallation(DEFAULT_BASE_PATH);
    
    public static final WindowsXAMPPInstallation getDefault() {
        return DEFAULT;
    }
    
    private WindowsXAMPPInstallation(String basePath) {
        this.basePath = basePath;
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isInstalled() {
        return Utilities.isWindows() &&
                Utils.isValidExecutable(getStartCommand()[0]);
    }

    public String[] getAdminCommand() {
        return new String[] { ADMIN_URL, "" };
    }

    public String[] getStartCommand() {
        String command = basePath + START_PATH; // NOI18N
        return new String[] { command, "" };
    }

    public String[] getStopCommand() {
        String command = basePath + STOP_PATH; // NOI18N
        return new String[] { command, "" };
    }
    
    public String getDefaultPort() {
        return DEFAULT_PORT;
    }
    
    @Override
    public String toString() {
        return "XAMPP Installation - " + basePath; //NOI18N
    }
}
