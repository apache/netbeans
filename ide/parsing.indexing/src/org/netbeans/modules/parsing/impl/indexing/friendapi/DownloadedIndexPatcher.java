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
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;

/**
 * Provides an ability to update data in downloaded index.
 * The instances of {@link DownloadedIndexPatcher} should be
 * registered in the global {@link Lookup}.
 * @see IndexDownloader
 * @author Tomas Zezula
 * @since 1.45
 */
public interface DownloadedIndexPatcher {
    /**
     * Called by the infrastructure when the index is downloaded and unpacked
     * to allow the implementer to update the downloaded index if needed.
     * @param sourceRoot for which the index was downloaded.
     * @param indexFolder the cache folder in which the index was unpacked.
     * @return true if an update was successful, false if the update failed and
     * downloaded index should not be used.
     */
    boolean updateIndex (@NonNull final URL sourceRoot, @NonNull final URL indexFolder);
}
