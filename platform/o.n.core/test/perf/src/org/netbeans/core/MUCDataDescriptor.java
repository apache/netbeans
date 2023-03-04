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
package org.netbeans.core;

import java.io.File;

import org.netbeans.performance.DataDescriptor;
import org.openide.filesystems.multifs.MultiXMLFSTest.FileWrapper;

/**
 * Describes data
 */
final class MUCDataDescriptor extends DataDescriptor {

    private int classCount;
    private int fsCount;

    private DataDescriptor dd;

    /** New MFSDataDescriptor */
    public MUCDataDescriptor(int classCount, int fsCount) {
        this.classCount = classCount;
        this.fsCount = fsCount;
    }

    /** Getter for classCount */
    public int getClassCount() {
        return classCount;
    }
    
    /** Getter for xmlfsCount */
    public int getFsCount() {
        return fsCount;
    }
    
    /** Getter for dd */
    public DataDescriptor getDD() {
        return dd;
    }
    
    /** Setter for dd */
    public void setDD(DataDescriptor dd) {
        this.dd = dd;
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
        
        if (obj instanceof MUCDataDescriptor) {
            MUCDataDescriptor dd = (MUCDataDescriptor) obj;
            
            boolean flag = this.dd == dd.dd || this.dd == null || this.dd.equals(dd.dd);
            
            return flag && getClassName().equals(dd.getClassName()) && classCount == dd.classCount && fsCount == dd.fsCount;
        }
        
        return false;
    }
}
