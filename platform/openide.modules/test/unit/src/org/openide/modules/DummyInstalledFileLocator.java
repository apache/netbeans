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
package org.openide.modules;

import java.io.File;

/**
 *
 * @author Jaroslav Tulach
 */
public class DummyInstalledFileLocator extends org.openide.modules.InstalledFileLocator {
    private static File installDir;


    /**
     * Creates a new instance of InstalledFileLocatorImpl
     */
    public DummyInstalledFileLocator() {
    }


    public File locate(String relativePath, String codeNameBase, boolean localized) {
        String user = System.getProperty("netbeans.user");
        File f = new File(user + File.separator + relativePath);
        if (f.exists()) {
            return f;
        }

        File root = installDir;
        if (root == null) {
            return null;
        }

        File[] arr = installDir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            f = new File(arr[i], relativePath);
            if (f.exists()) {
                return f;
            }
        }

        return null;
    }

    public static void registerDestDir(File file) {
        installDir = file;
    }

}
