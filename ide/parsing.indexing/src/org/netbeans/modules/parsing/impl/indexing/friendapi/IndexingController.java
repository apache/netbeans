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

package org.netbeans.modules.parsing.impl.indexing.friendapi;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;

/**
 *
 * @author vita
 */
public abstract class IndexingController {

    public static synchronized IndexingController getDefault() {
        return RepositoryUpdater.getDefault().getController();
    }

    public abstract void enterProtectedMode();

    public abstract void exitProtectedMode(Runnable followUpTask);

    public abstract boolean isInProtectedMode();

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    public abstract Map<URL, List<URL>> getRootDependencies();

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    public abstract Map<URL, List<URL>> getBinaryRootDependencies();

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    public abstract Map<URL, List<URL>> getRootPeers();

    public abstract int getFileLocksDelay();

    protected IndexingController() {
    }

}
