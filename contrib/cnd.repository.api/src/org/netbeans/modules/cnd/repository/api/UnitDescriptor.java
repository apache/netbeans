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
package org.netbeans.modules.cnd.repository.api;

import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 *
 */
public final class UnitDescriptor {

    private final CharSequence name;
    private final FileSystem fileSystem;

    public UnitDescriptor(CharSequence unitName, FileSystem unitFileSystem) {
        this.name = CharSequences.create(unitName);
        this.fileSystem = unitFileSystem;
    }

    public CharSequence getName() {
        return name;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + (this.fileSystem != null ? this.fileSystem.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnitDescriptor other = (UnitDescriptor) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.fileSystem != other.fileSystem && (this.fileSystem == null || !this.fileSystem.equals(other.fileSystem))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return CndFileUtils.isLocalFileSystem(fileSystem) ? name.toString() :  (fileSystem.getDisplayName() + ":" + name); // NOI18N
    }
}
