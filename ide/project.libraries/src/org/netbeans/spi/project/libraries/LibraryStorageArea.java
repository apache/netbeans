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

import java.net.URL;
import org.openide.util.NbBundle;

/**
 * Abstract location where zero or more libraries are defined.
 * {@link Object#equals} and {@link Object#hashCode} are expected to be defined
 * such that object identity (within the implementing class) are driven by {@link #getLocation}.
 * @see ArealLibraryProvider
 * @since org.netbeans.modules.project.libraries/1 1.15
 */
public interface LibraryStorageArea {

    /**
     * The {@link LibraryStorageArea} for global libraries.
     * @since 1.48
     */
    public static final LibraryStorageArea GLOBAL = new LibraryStorageArea() {
        @Override
        public URL getLocation() {
            return null;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(LibraryStorageArea.class, "LBL_global");
        }
    };

    /**
     * Gets an associated storage location.
     * The contents of the URL (if it is even accessible) are unspecified.
     * @return an associated URL uniquely identifying this location
     */
    URL getLocation();

    /**
     * Gets a human-readable display label for this area.
     * @return a localized display name
     */
    String getDisplayName();

}
