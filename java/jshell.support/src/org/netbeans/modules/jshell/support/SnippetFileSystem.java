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
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;

/**
 *
 * @author sdedic
 */
class SnippetFileSystem extends MultiFileSystem implements Callable<FileObject> {

    @Override
    protected FileSystem createWritableOn(String name) throws IOException {
        return super.createWritableOn(name); //To change body of generated methods, choose Tools | Templates.
    }
    private final FileObject  projectRoot;
    private final FileObject  configRoot;
    
    private final FileSystem  projectFileSystem;
    private final FileSystem  configFileSystem;
    /**
     * Path to the project root, will be prepended when using projectFS
     */
    private final String      projectRootPath;
    
    /**
     * Prefix within the project - snippet storage root
     */
    private final String      projectFSPrefix;
    
    private final String      configFSPathPrefix;

    public SnippetFileSystem(FileObject projectRoot, FileObject configRoot, String projectFSPathPrefix, String configFSPathPrefix) throws IOException {
        super(new FileSystem[] {
            projectRoot.getFileSystem(),
            configRoot.getFileSystem()
        });
        this.projectRoot = projectRoot;
        this.projectFileSystem = projectRoot.getFileSystem();
        this.configFileSystem = configRoot.getFileSystem();
        this.configRoot = configRoot;
        this.projectRootPath = projectRoot.getPath();
        this.projectFSPrefix = projectFSPathPrefix;
        this.configFSPathPrefix = configFSPathPrefix;
    }
    
    @Override
    protected FileObject findResourceOn(FileSystem fs, String res) {
        if (fs == projectFileSystem) {
            String append = projectRootPath + "/";
            if (!projectFSPrefix.isEmpty()) {
                append += projectFSPrefix + "/";
            }
            return projectFileSystem.findResource(append + res);
        } else {
            return configFileSystem.findResource(configFSPathPrefix + "/" + res);
        }
    }

    @Override
    public FileObject call() throws IOException {
        return FileUtil.createFolder(projectRoot, projectFSPrefix);
    }
    
    boolean isObsolete() {
        return !projectRoot.isValid();
    }
}
