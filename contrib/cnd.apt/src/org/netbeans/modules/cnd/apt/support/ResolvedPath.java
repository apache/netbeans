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

package org.netbeans.modules.cnd.apt.support;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.CharSequences;

/**
 *
 */
public final class ResolvedPath {
    private final CharSequence folder;
    private final FileSystem fileSystem;
    private final CharSequence path;
    private final boolean isDefaultSearchPath;
    private final int index;
    
    public ResolvedPath(FileSystem fileSystem, CharSequence folder, CharSequence path, boolean isDefaultSearchPath, int index) {
        assert CharSequences.isCompact(folder) : "forgot to FilePathCache.getManager().getString(folder)? " + folder;
        this.folder = folder;// should be already shared
        this.fileSystem = fileSystem;
        CndPathUtilities.assertNoUrl(path);
        this.path = FilePathCache.getManager().getString(path);
        this.isDefaultSearchPath = isDefaultSearchPath;
        this.index = index;
        boolean debug = false;
        assert debug = true;
        if (debug) {
            if (!CndFileUtils.isExistingFile(fileSystem, this.path.toString())) {
                APTUtils.LOG.log(Level.WARNING, "ResolvedPath: isExistingFile failed in {0} for {1}", new Object[]{fileSystem, path});
            }
            // there are situations when file is edited, but included file is 
            // removed/created by running undeground build infrastructure,
            // so resolved path can correspond to the file which is already not a file
            if (CndFileUtils.isLocalFileSystem(fileSystem)) {
                // check file existence using java.io.file as well
                if (!new File(this.path.toString()).isFile()) {
                    APTUtils.LOG.log(Level.WARNING, "ResolvedPath: isFile failed for {0}", path);
                }
            }
            if (CndFileUtils.toFileObject(fileSystem, path) == null) {
                APTUtils.LOG.log(Level.WARNING, "ResolvedPath: no FileObject in {0} for {1} FileUtil.toFileObject = {2} second check = {3}", 
                        new Object[]{
                            fileSystem, path, 
                            FileUtil.toFileObject(new File(FileUtil.normalizePath(path.toString()))), 
                            fileSystem.findResource(path.toString())});
            }
        }
        CndUtils.assertNormalized(fileSystem, folder);
        CndUtils.assertNormalized(fileSystem, path);
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public FileObject getFileObject() {
        // using fileSystem.findResource is not safe, see #196425 -  AssertionError: no FileObject 
        return CndFileUtils.toFileObject(fileSystem, path);
    }
    
    /**
     * Resolved file path (normalized version)
     */
    public CharSequence getPath(){
        return path;
    }

    /**
     * Include path used for resolving file path
     */
    public CharSequence getFolder(){
        return folder;
    }

    /**
     * Returns true if the header is resolved against owner file directory
     */
    public boolean isDefaultSearchPath(){
        return isDefaultSearchPath;
    }

    /**
     * Returns index of resolved path in user and system include paths
     */
    public int getIndex(){
        return index;
    }
    
    @Override
    public String toString(){
        return "ResPath{" + path + " in " + folder + (CndFileUtils.isLocalFileSystem(fileSystem) ? "" : fileSystem) + "}"; // NOI18N
    }
}
