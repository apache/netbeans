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
package org.netbeans.modules.web.project.spi;

/**
 * Allows removing broken library references from a project while the project
 * is being open.
 *
 * <p>Implementations of this interface are returned by {@link BrokenLibraryRefFilterProvider}.
 * When a web project is opened the {@link #removeLibraryReference} method is called
 * for all broken library references. If at least one implementation returns
 * <code>true</code>, the library reference is removed.</p>
 *
 * @author Andrei Badea
 */
public interface BrokenLibraryRefFilter {

    /**
     * Return <code>true</code> from this method to remove the
     * reference to the given library.
     * @param  libraryName the name of a library to which a broken
     *         reference exists; never null.
     * @return true to remove this reference from the project; false otherwise
     */
    boolean removeLibraryReference(String libraryName);
}
