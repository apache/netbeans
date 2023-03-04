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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;

/**
 * Allows an implementer to provide a pre created index
 * for given source root.
 * The instances of {@link IndexDownloader} should be
 * registered in the global {@link Lookup}.
 * @author Tomas Zezula
 * @since 1.45
 */
public interface IndexDownloader {
    /**
     * Provides an {@link URL} for given source root.
     * @param root the source root for which the index should be provided.
     * @return an {@link URL} of a pre index bundle or null if the {@link IndexDownloader}
     * does not provide pre index data for given root.
     * The pre created index has to be stored in zip file, the zip file is unpacked into
     * the index folder for given root.
     */
    @CheckForNull
    URL getIndexURL (@NonNull final URL root);
}
