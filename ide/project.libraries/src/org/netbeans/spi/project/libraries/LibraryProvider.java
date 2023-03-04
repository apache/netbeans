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

package org.netbeans.spi.project.libraries;

import java.beans.PropertyChangeListener;

/**
 * Provider interface for implementing the read only library storage.
 * Library storage is a source of libraries used by LibraryManager.
 * LibraryManager allows existence of multiple LibraryProviders registered in
 * the default lookup.
 * @param L the type of implementation which will be produced by this provider
 */
public interface LibraryProvider<L extends LibraryImplementation> {

    /**
     * Name of libraries property
     */
    String PROP_LIBRARIES = "libraries"; // NOI18N

    /**
     * Returns libraries provided by the implemented provider.
     * @return (possibly empty but not null) list of libraries
     */
    L[] getLibraries();

    /**
     * Adds property change listener, the listener is notified when the libraries changed
     * @param listener
     */
    void addPropertyChangeListener (PropertyChangeListener listener);

    /**
     * Removes property change listener
     * @param listener
     */
    void removePropertyChangeListener (PropertyChangeListener listener);

}
