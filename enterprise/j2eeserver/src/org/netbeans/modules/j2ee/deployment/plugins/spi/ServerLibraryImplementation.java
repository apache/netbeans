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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.deployment.common.api.Version;

/**
 * The representation of the server library. This means the library server
 * manages not the jars deployed along with the application.
 *
 * @since 1.68
 * @author Petr Hejl
 */
public interface ServerLibraryImplementation {

    /**
     * Returns the specification title of the library. May return
     * <code>null</code>.
     * <p>
     * <div class="nonnormative">
     * For example specification title for JSF would be JavaServer Faces.
     * </div>
     *
     * @return the specification title of the library; may return <code>null</code>
     */
    @CheckForNull
    String getSpecificationTitle();

    /**
     * Returns the implementation title of the library. May return
     * <code>null</code>.
     * <p>
     * <div class="nonnormative">
     * For example specification title for MyFaces implementation of JSF
     * this would be something like MyFaces.
     * </div>
     *
     * @return the implementation title of the library; may return <code>null</code>
     */
    @CheckForNull
    String getImplementationTitle();

    /**
     * Returns the specification version of the library. May return
     * <code>null</code>.
     * 
     * @return the specification version of the library; may return <code>null</code>
     */
    @CheckForNull
    Version getSpecificationVersion();

    /**
     * Returns the implementation version of the library. May return
     * <code>null</code>.
     *
     * @return the implementation version of the library; may return <code>null</code>
     */
    @CheckForNull
    Version getImplementationVersion();

    /**
     * Returns the library name. May return <code>null</code>.
     * 
     * @return the library name; may return <code>null</code>
     */
    @CheckForNull
    String getName();
}
