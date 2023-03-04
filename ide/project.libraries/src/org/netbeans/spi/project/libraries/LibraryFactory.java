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

import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.project.libraries.LibraryAccessor;

/**
 * A factory class to create {@link Library} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link LibraryImplementation} and use this factory.
 * See also {@link org.netbeans.spi.project.libraries.support.LibrariesSupport}
 * for easier ways to create {@link LibraryImplementation}.
 * @since org.netbeans.modules.project.libraries/1 1.14
 * @author Tomas Zezula
 */
public class LibraryFactory {
    
    private LibraryFactory() {
    }
    
    /**
     * Creates Library for LibraryImplementation
     * @param libraryImplementation the library SPI object
     * @return Library API instance, for which the {@link Library#getManager} will be {@link LibraryManager#getDefault}
     */
    public static Library createLibrary (LibraryImplementation libraryImplementation) {
        assert libraryImplementation != null;
        return LibraryAccessor.getInstance().createLibrary(libraryImplementation);
    }
    
}
