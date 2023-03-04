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
package org.netbeans.modules.git.client;

import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author Ondra
 */
public final class ProgressDelegate {
    
    private final GitProgressSupport supp;
    
    ProgressDelegate (GitProgressSupport supp) {
        this.supp = supp;
    }
    
    public void setDisplayName (String displayName) {
        supp.setDisplayName(displayName);
    }
    
    public void setProgress (String progressMessage) {
        supp.setProgress(progressMessage);
    }
    
    public ProgressMonitor getProgressMonitor () {
        return supp.getProgressMonitor();
    }

    public boolean isCanceled () {
        return supp.isCanceled();
    }

    public boolean cancel () {
        return supp.cancel();
    }

    public void setError (boolean errFlag) {
        supp.setError(errFlag);
    }
    
}
