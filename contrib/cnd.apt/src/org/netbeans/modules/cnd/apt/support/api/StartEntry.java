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

package org.netbeans.modules.cnd.apt.support.api;

import java.io.IOException;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;
import org.openide.util.Parameters;

/**
 *
 */
public final class StartEntry implements SelfPersistent {
    private final CharSequence startFile;
    //private boolean isCPP; // TODO: flag to be used for understanding C/C++ lang
    private final Key startFileProject;
    transient private final FileSystem fileSystem;
    
    public StartEntry(FileSystem fs, String startFile, Key startFileProject) {
        Parameters.notNull("startFileProject", startFileProject); //NOI18N
        this.fileSystem = fs;
        if (CndUtils.isDebugMode()) {
            CndUtils.assertTrue(CndFileSystemProvider.isAbsolute(fs, startFile),
                    "Start entry path should be absolute! FS=" + fileSystem + " Path=" + startFile); //NOI18N
        }
        this.startFile = FilePathCache.getManager().getString(startFile);
        this.startFileProject = startFileProject;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public CharSequence getStartFile() {
        return startFile;
    }

    public APTFileSearch getFileSearch(){
        return APTFileSearch.get(startFileProject);
    }

    public Key getStartFileProject(){
        return startFileProject;
    }
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        assert output != null;
        output.writeFileSystem(fileSystem);
        output.writeFilePathForFileSystem(fileSystem, startFile);
        KeyFactory.getDefaultFactory().writeKey(startFileProject, output);
    }
    
    public StartEntry(RepositoryDataInput input) throws IOException {
        assert input != null;
        fileSystem = input.readFileSystem();
        startFile = input.readFilePathForFileSystem(fileSystem);
        startFileProject = KeyFactory.getDefaultFactory().readKey(input);
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StartEntry other = (StartEntry) obj;
        if (this.startFile != other.startFile && (this.startFile == null || !this.startFile.equals(other.startFile))) {
            return false;
        }
        if (this.startFileProject != other.startFileProject && (this.startFileProject == null || !this.startFileProject.equals(other.startFileProject))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
        hash = 89 * hash + (this.startFileProject != null ? this.startFileProject.hashCode() : 0);
        return hash;
    }

    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Start Entry: from file=").append(startFile).append("\nof project=").append(startFileProject); //NOI18N
        return out.toString();
    }
}
