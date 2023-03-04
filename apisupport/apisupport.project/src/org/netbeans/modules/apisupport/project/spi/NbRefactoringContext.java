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

package org.netbeans.modules.apisupport.project.spi;

import org.openide.filesystems.FileObject;

/**
 *
 * @author mkozeny
 */
public final class NbRefactoringContext {
    
    private FileObject fileToRefactored;
    
    private String oldPackagePath;
    
    private String newPackagePath;

    public NbRefactoringContext(FileObject fileToRefactored, String newPackagePath, String oldPackagePath) {
        this.fileToRefactored = fileToRefactored;
        this.newPackagePath = newPackagePath;
        this.oldPackagePath = oldPackagePath;
    }

    public FileObject getFileToRefactored() {
        return fileToRefactored;
    }
    
    public String getOldPackagePath() {
        return oldPackagePath;
    }

    public String getNewPackagePath() {
        return newPackagePath;
    }
    
    
}
