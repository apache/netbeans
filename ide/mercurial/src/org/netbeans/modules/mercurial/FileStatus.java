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
package org.netbeans.modules.mercurial;

import java.io.File;

/**
 * Holds detailed information about status of a mercurial-managed file, ie repository URL, remote path, branch, etc.
 * 
 * @author Maros Sandor
 */
public class FileStatus {
    private File mFile;
    private boolean mbCopied;
    private final File originalFile;
    
    public FileStatus(File file, File original){
        mFile = file;
        this.originalFile = original;
        mbCopied = original != null;
    }
    public File getFile(){
        return mFile;
    }
    public File getOriginalFile(){
        return originalFile;
    }
    public boolean isCopied(){
        return mbCopied;
    }
}
