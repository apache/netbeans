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

package org.netbeans.modules.ide.ergonomics.fod;

import org.netbeans.api.progress.ProgressHandle;

/**
 * Progress monitor for downloading/validating/installing/enabling modules.
 * For empty implementation one can use {@link #DEV_NULL_PROGRESS_MONITOR}.
 * @author Tomas Mysik
 */
public interface ProgressMonitor {
    static final ProgressMonitor DEV_NULL_PROGRESS_MONITOR = new DevNullProgressMonitor();

    void onDownload(ProgressHandle progressHandle);

    void onValidate(ProgressHandle progressHandle);

    void onInstall(ProgressHandle progressHandle);

    void onEnable(ProgressHandle progressHandle);

    void onError(String message);
    
    static final class DevNullProgressMonitor implements ProgressMonitor {
        public void onDownload(ProgressHandle progressHandle) {
        }

        public void onValidate(ProgressHandle progressHandle) {
        }

        public void onInstall(ProgressHandle progressHandle) {
        }

        public void onEnable(ProgressHandle progressHandle) {
        }

        public void onError(String message) {
        }
    }
}
