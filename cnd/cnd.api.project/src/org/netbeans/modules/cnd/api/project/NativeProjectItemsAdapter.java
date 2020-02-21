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
package org.netbeans.modules.cnd.api.project;

import java.util.List;

/**
 *
 */
public class NativeProjectItemsAdapter implements NativeProjectItemsListener {

    @Override
    public void filesAdded(List<NativeFileItem> fileItems) {
    }

    @Override
    public void filesRemoved(List<NativeFileItem> fileItems) {
    }

    @Override
    public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
    }

    @Override
    public void filesPropertiesChanged(NativeProject nativeProject) {
    }

    @Override
    public void fileRenamed(String oldPath, NativeFileItem newFileIetm) {
    }

    @Override
    public void projectDeleted(NativeProject nativeProject) {
    }

    @Override
    public void fileOperationsStarted(NativeProject nativeProject) {
    }

    @Override
    public void fileOperationsFinished(NativeProject nativeProject) {
    }
}
