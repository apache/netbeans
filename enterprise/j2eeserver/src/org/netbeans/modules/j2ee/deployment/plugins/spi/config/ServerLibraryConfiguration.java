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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;

/**
 * The interface defining the methods that can handle the server libraries in
 * scope of enterprise module.
 * <p>
 * This interface is typically looked up in {@link ModuleConfiguration}'s
 * lookup.
 *
 * @since 1.68
 * @author Petr Hejl
 */
public interface ServerLibraryConfiguration {

    /**
     * Configure the library (dependency) the enterprise module needs in order
     * to work properly.
     * <p>
     * Once library is configured it should be present in the result
     * of the {@link #getLibraries()} call.
     *
     * @param library the library the enterprise module needs in order to work
     *             properly
     * @throws ConfigurationException if there was a problem writing
     *             configuration
     */
    void configureLibrary(@NonNull ServerLibraryDependency library) throws ConfigurationException;

    /**
     * Returns the server library dependencies the enterprise module needs
     * to work properly.
     *
     * @return the server library dependencies
     * @throws ConfigurationException if there was a problem reading
     *             configuration
     */
    @NonNull
    Set<ServerLibraryDependency> getLibraries() throws ConfigurationException;

    void addLibraryChangeListener(@NonNull ChangeListener listener);

    void removeLibraryChangeListener(@NonNull ChangeListener listener);

}
