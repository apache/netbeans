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
package org.openide.filesystems.multifs;

import java.io.File;

import org.netbeans.performance.DataDescriptor;
import org.openide.filesystems.multifs.MultiXMLFSTest.FileWrapper;

/**
 * Describes data
 */
final class MFSDataDescriptor extends DataDescriptor {

    private int foCount;
    private int fsCount;

    private FileWrapper[] wrappers;

    /** New MFSDataDescriptor */
    public MFSDataDescriptor(int foCount, int fsCount) {
        this.foCount = foCount;
        this.fsCount = fsCount;
    }

    /** Setter for wrappers */
    void setFileWrappers(FileWrapper[] wrappers) {
        this.wrappers = wrappers;
    }
    
    /** Getter for wrappers */
    public FileWrapper[] getFileWrappers() {
        return wrappers;
    }
    
    /** Getter for foCount */
    public int getFoCount() {
        return foCount;
    }
    
    /** Getter for xmlfsCount */
    public int getFsCount() {
        return fsCount;
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
        
        if (obj instanceof MFSDataDescriptor) {
            MFSDataDescriptor dd = (MFSDataDescriptor) obj;
            return getClassName().equals(dd.getClassName()) && foCount == dd.foCount && fsCount == dd.fsCount;
        }
        
        return false;
    }
}
