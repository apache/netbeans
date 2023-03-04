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

package org.netbeans.modules.websvc.saas.util;

import java.io.File;
import java.util.ArrayList;
import org.openide.modules.InstalledFileLocator;

/**
 *  InstalledFileLocator implementation that searches the NB install directory
 * (uses java.endorsed.dirs value from nbproject/project.properties)
 * @author quynguyen
 */
public class InstalledFileLocatorImpl extends InstalledFileLocator {

    private ArrayList<File> baseDirs;
    private File userDirConfigRoot;
    
    public InstalledFileLocatorImpl() {
        super();
        File endorsedDir = new File(System.getProperty("java.endorsed.dirs"));
        for (int i = 0; i < 5; i++) {
            endorsedDir = endorsedDir.getParentFile();
        }

        File installRoot = endorsedDir;
        File[] subdirs = installRoot.listFiles();
        baseDirs = new ArrayList<File>();

        for (int i = 0; subdirs != null && i < subdirs.length; i++) {
            if (subdirs[i].isDirectory()) {
                baseDirs.add(subdirs[i]);
            }
        }
    }

    @Override
    public File locate(String relativePath, String codeNameBase, boolean localized) {
        for (File baseDir : baseDirs) {
            File f = new File(baseDir, relativePath);
            if (f.exists()) {
                return f;
            }
        }

        return null;
    }
    
    public void setUserConfigRoot(File baseDir) {
        if (userDirConfigRoot != null) {
            baseDirs.remove(userDirConfigRoot);
            userDirConfigRoot = null;
        }
        
        if (baseDir != null) {
            baseDirs.add(baseDir);
            userDirConfigRoot = baseDir;
        }
    }
}
