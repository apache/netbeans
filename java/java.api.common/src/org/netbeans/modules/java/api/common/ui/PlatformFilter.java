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
package org.netbeans.modules.java.api.common.ui;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 * Platform filter is to be used in platform lists (e.g., in combo boxes).
 * Projects should be able to register implementation in lookup, to restrict
 * lists of platforms to only platforms fulfilling project's needs. This mechanism
 * is useful in projects that extend the SE Project and need to hook into the
 * underlying SE UI infrastructure.
 *
 * @author Petr Somol
 */
@FunctionalInterface
public interface PlatformFilter {
    /**
     * Returns true if platform fulfills whatever condition is implemented
     * @since 1.49
     */
    boolean accept(@NonNull JavaPlatform platform);
}
