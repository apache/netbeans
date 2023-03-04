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

import java.io.File;
import org.netbeans.modules.db.mysql.impl.Installation;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Standalone version on Windows, not installed as a server
 *
 * @author David Van Couvering
 */
public class WindowsStandaloneInstallation implements Installation {
    static final File DEFAULT_BASE_PATH = FileUtil.normalizeFile(new File("C:/Program Files/MySQL/"));  //NOI18N
    static final String FOLDER_NAME_PREFIX = "MySQL Server ";           //NOI18N
    
    private final File basePath;

    protected WindowsStandaloneInstallation(String folderName) {
        this.basePath = new File(DEFAULT_BASE_PATH, folderName);
    }
    
    public String[] getStartCommand() {
        return new String[] { new File(basePath, "/bin/mysqld.exe").getAbsolutePath(), "--console"}; // NOI18N
    }

    public String[] getStopCommand() {
        return new String[] { new File(basePath, "/bin/mysqladmin.exe").getAbsolutePath(), "-u root shutdown"}; // NOI18N
    }
    
    public boolean isInstalled() {
        return Utilities.isWindows() && 
                Utils.isValidExecutable(getStartCommand()[0]);
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

    public Installation getInstallation(String command, Command cmdType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return "Windows Installation - " + basePath;                    //NOI18N
    }
}
