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

package org.netbeans.modules.java.j2seproject.api;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 * Allows J2SE project extensions to supply additional build properties.
 * The instances are registered in project {@link Lookup}.
 * @see ProjectServiceProvider
 * @author Tomas Zezula
 * @since 1.71
 */
public interface J2SEBuildPropertiesProvider {
    /**
     * Returns the additional build properties.
     * @param command the command to be invoked
     * @param context the invocation context
     * @return the {@link Map} of additional properties.
     */
    @NonNull
    Map<String,String> createAdditionalProperties(@NonNull String command, @NonNull Lookup context);

    /**
     * Returns the names of concealed properties.
     * @param command the command to be invoked
     * @param context the invocation context
     * @return the {@link Set} of concealed properties names.
     */
    @NonNull
    Set<String> createConcealedProperties(@NonNull String command, @NonNull Lookup context);
}
