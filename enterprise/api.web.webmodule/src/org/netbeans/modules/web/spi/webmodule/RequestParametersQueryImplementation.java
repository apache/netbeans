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

package org.netbeans.modules.web.spi.webmodule;

import org.openide.filesystems.FileObject;

/**
 * This is the SPI counterpart of {@link org.netbeans.modules.web.api.webmodule.RequestParametersQuery}.
 * Register an instance of this provider in the default lookup to provide
 * access to the file part of URL and request parameters.
 *
 * @author Pavel Buzek
 */
public interface RequestParametersQueryImplementation {

    /**
     * Return the part of URL for access the file. It can include the query string.
     *
     * @param  file the file for find the request parameters for; never null.
     * @return path fom the context; can be null.
     */
    String getFileAndParameters (FileObject file);
}
