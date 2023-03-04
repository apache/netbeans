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

package org.netbeans.modules.php.project.classpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for sharing the same code between {@link org.netbeans.modules.php.project.api.PhpSourcePath}
 * and {@link ClassPathProviderImpl}.
 * @author Tomas Mysik
 */
public final class CommonPhpSourcePath {

    private static final Logger LOGGER = Logger.getLogger(CommonPhpSourcePath.class.getName());

    // GuardedBy(CommonPhpSourcePath.class)
    private static List<FileObject> internalFolders = null;

    private CommonPhpSourcePath() {
    }

    public static synchronized List<FileObject> getInternalPath() {
        if (internalFolders == null) {
            internalFolders = getInternalFolders();
        }
        return internalFolders;
    }

    private static List<FileObject> getInternalFolders() {
        assert Thread.holdsLock(CommonPhpSourcePath.class);

        List<FileObject> preindexedFolders = PhpSourcePath.getPreindexedFolders();
        // XXX disabled, unit tests failures
        //assert !preindexedFolders.contains(null) : "Preindexed folders contains null";
        FileObject sfsFolder = FileUtil.getConfigFile("PHP/RuntimeLibraries"); // NOI18N
        List<FileObject> folders = new ArrayList<>(preindexedFolders.size() + 1);
        // #210578
        if (sfsFolder != null) {
            folders.add(sfsFolder);
        } else {
            LOGGER.info("SFS folder PHP/RuntimeLibraries not found");
        }
        folders.addAll(preindexedFolders);
        return folders;
    }
}
