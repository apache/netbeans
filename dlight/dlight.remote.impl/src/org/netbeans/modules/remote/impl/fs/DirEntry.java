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

package org.netbeans.modules.remote.impl.fs;

import java.util.Date;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;

/**
 *
 */
public abstract class DirEntry {

    private String cache;

    public DirEntry(String cache) {
        this.cache = cache;
    }
    
    public abstract String getName();

    public abstract long getSize();
    
    public abstract boolean canExecute();
    public abstract boolean canRead();
    public abstract boolean canWrite();

    /** Device no (stat.st_dev field). Zero value means that it is unknown */
    public abstract long getDevice();
    
    /** Inode (stat.st_ino field). Zero value means that it is unknown */
    public abstract long getINode();

    public abstract Date getLastModified();

    public abstract boolean isLink();
    public abstract boolean isDirectory();
    public abstract boolean isPlainFile();

    public abstract FileType getFileType();
    
    public boolean isSameLastModified(DirEntry other) {
        return getLastModified().equals(other.getLastModified());
    }
    
    public boolean hasINode() {
        return getINode() != 0;
    }

    public boolean isSameINode(DirEntry other) {
        return other.getDevice() == getDevice() && other.getINode() == this.getINode();
    }

    public boolean isSameType(DirEntry other) {
        return isLink() == other.isLink() && isDirectory() == other.isDirectory() && isPlainFile() == other.isPlainFile();
    }

    public boolean isSameAccess(DirEntry other) {
        if (other == null) {
            return false;
        } else {
            return this.canRead() == other.canRead()
                    && this.canWrite() == other.canWrite()
                    && this.canExecute() == other.canExecute();
        }
    }    
    
    public abstract String getLinkTarget();

    public final String getCache() {
        return cache;
    }
    
    public final void setCache(String cache) {
        this.cache = cache;
    }

    public abstract String toExternalForm();
    
    public abstract boolean isValid();    
}
