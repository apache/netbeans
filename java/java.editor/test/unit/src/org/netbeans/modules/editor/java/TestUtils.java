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

package org.netbeans.modules.editor.java;

import java.io.File;
import java.net.URI;
import org.netbeans.editor.ext.DataAccessor;
//import org.netbeans.editor.ext.java.DAFileProvider;
//import org.netbeans.editor.ext.java.JCFileProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 * Utilities for accessing input data for tests. It mounts
 * test/unit/src/org/netbeans/modules/editor/java/data folder
 * as local FS and make its subfolders accessible for you in tests.
 * Call setupData() in test's setUp() and cleanupData() in test's tearDown().
 * Then you can you rest of the utility methods.
 *
 * @author David Konecny
 */
public final class TestUtils {
    
    private static LocalFileSystem lfs;

    /** Returns FO for test/unit/src/org/netbeans/modules/editor/java/data */
    public static synchronized FileObject getDataFolder() {
        if (lfs == null) {
            return null;
        }
        return lfs.getRoot();
    }
    
    /** Returns FS with root test/unit/src/org/netbeans/modules/editor/java/data */
    public static synchronized FileSystem getDataFilesystem() {
        return lfs;
    }
    
    
}
