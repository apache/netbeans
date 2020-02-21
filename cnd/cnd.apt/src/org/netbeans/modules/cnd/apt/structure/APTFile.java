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

package org.netbeans.modules.cnd.apt.structure;

import org.openide.filesystems.FileSystem;

/**
 * APT root element to present whole file
 * it doesn't have any siblings, only children
 *
 */
public interface APTFile extends APT {
    enum Kind {
        C_CPP,
        FORTRAN_FREE,
        FORTRAN_FIXED
    }
    
    public Kind getKind();
    
    /**
     * Gets file system
     * @return 
     */
    public FileSystem getFileSystem();
    
    /**
     * returns the full path of file
     */
    public CharSequence getPath();
    
    /** check the existence of tokens **/
    public boolean isTokenized();

    /** guard detected for this file or empty string if no guard in file */
    public CharSequence getGuardMacro();
}
