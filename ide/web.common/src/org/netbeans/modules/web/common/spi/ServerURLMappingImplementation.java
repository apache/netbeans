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

package org.netbeans.modules.web.common.spi;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;

/**
 * Provides mapping between project's source file and its location on server
 * and vice versa. To be registered in project's lookup. See the API counterpart
 * class for more details.
 */
public interface ServerURLMappingImplementation {

    /**
     * Convert given project's file into server URL.
     * @return could return null if file is not deployed to server and therefore
     *   not accessible
     */
    @CheckForNull URL toServer(int projectContext, FileObject projectFile);
    
    /**
     * Convert given server URL into project's file.
     * @return returns null if nothing is known about this server URL
     */
    @CheckForNull FileObject fromServer(int projectContext, URL serverURL);
}
