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
package org.netbeans.modules.web.jsfapi.api;

import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

public interface Library extends LibraryInfo {

    /**
     * Gets default namespace of the library.
     * @return default namespace
     */
    @NonNull
    public String getDefaultNamespace();

    /**
     * Gets type of the library.
     * @return type of the library (class/component one)
     * @deprecated Not used for the detection of the library type any more. Can be removed in next releases.
     */
    @Deprecated
    @NonNull
    public LibraryType getType();

    /**
     * Returns collections of all available component of this library.
     * @return all components
     */
    @NonNull
    public Collection<? extends LibraryComponent> getComponents();

    /**
     * Gets component for given name.
     * @param componentName name of the component to seek
     * @return found component, or {@code null} if no such component exist in the library
     */
    @CheckForNull
    public LibraryComponent getComponent(String componentName);

}
