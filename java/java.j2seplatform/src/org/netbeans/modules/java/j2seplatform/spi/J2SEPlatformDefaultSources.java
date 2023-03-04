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

package org.netbeans.modules.java.j2seplatform.spi;

import java.net.URI;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 * Provides default location of sources.
 * @author Tomas Zezula
 * @since 1.36
 */
public interface J2SEPlatformDefaultSources {
    /**
     * Provides default location of sources.
     * @param platform to find the sources for
     * @return the sources
     */
    @NonNull
    List<URI> getDefaultSources(@NonNull JavaPlatform platform);
}
