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
package org.netbeans.modules.cnd.model.jclank.bridge.trace;

import org.clang.tools.services.ClankProgressHandler;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 */
public class JClankProgressHandler implements ClankProgressHandler {

    private final ProgressHandle delegate;

    public JClankProgressHandler(ProgressHandle handle) {
        this.delegate = handle;
    }

    @Override
    public void setDisplayName(String newDisplayName) {
        delegate.setDisplayName(newDisplayName);
    }

    @Override
    public void start(int workunits, long estimate) {
        delegate.start(workunits, estimate);
    }

    @Override
    public void switchToIndeterminate() {
        delegate.switchToIndeterminate();
    }

    @Override
    public void switchToDeterminate(int workunits, long estimate) {
        delegate.switchToDeterminate(workunits, estimate);
    }

    @Override
    public void progress(String message, int workunit) {
        delegate.progress(message, workunit);
    }

}
