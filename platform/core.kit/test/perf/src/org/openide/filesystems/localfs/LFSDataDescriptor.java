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
package org.openide.filesystems.localfs;

import org.netbeans.performance.DataDescriptor;

import java.io.File;

/**
 * Describes data
 */
final class LFSDataDescriptor extends DataDescriptor {

    // non persistent data!
    private transient File rootDir;
    private int foCount;

    /** New LFSDataDescriptor */
    public LFSDataDescriptor(int count) {
        this.foCount = count;
    }

    /** Sette for rootDir */
    final void setFile(File root) {
        this.rootDir = root;
    }

    /** @return foCount */
    final int getFileNo() {
        return foCount;
    }
    
    /** @return rootDir */
    final File getRootDir() {
        return rootDir;
    }
    
    /** @return hashCode */
    public int hashCode() {
        return getClassName().hashCode();
    }
    
    /** @return boolean iff obj equals this */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof LFSDataDescriptor) {
            LFSDataDescriptor dd = (LFSDataDescriptor) obj;
            return getClassName().equals(dd.getClassName()) && foCount == dd.foCount;
        }
        
        return false;
    }
    
    public String toString() {
        return super.toString() + " root: " + rootDir + " foCount: " + foCount + " " + System.identityHashCode(this);
    }
}
