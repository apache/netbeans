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

package org.netbeans.modules.websvc.rest.codegen;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Peter Liu
 */
public abstract class AbstractGenerator {
    
    private ProgressHandle pHandle;
    private int totalWorkUnits;
    private int workUnits;
    
    public AbstractGenerator() {
    }
    
    public abstract Set<FileObject> generate(ProgressHandle pHandle) throws IOException;
    
    protected void initProgressReporting(ProgressHandle pHandle) {
        initProgressReporting(pHandle, true);
    }
    
    protected void initProgressReporting(ProgressHandle pHandle, boolean start) {
        this.pHandle = pHandle;
        this.totalWorkUnits = getTotalWorkUnits();
        this.workUnits = 0;
        
        if (start) {
            if (totalWorkUnits > 0) {
                pHandle.start(totalWorkUnits);
            } else {
                pHandle.start();
            }
        }
    }
    
    protected void reportProgress(String message) {     
        if (pHandle != null) {
            if (totalWorkUnits > 0) {
                pHandle.progress(message, ++workUnits);
            } else {
                pHandle.progress(message);
            }
        }
    }
    
    protected void finishProgressReporting() {
        if (pHandle != null) {
            pHandle.finish();
        }
    }
    
    protected int getTotalWorkUnits() {
        return 0;
    }
    
    protected ProgressHandle getProgressHandle() {
        return pHandle;
    }
}
