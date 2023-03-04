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

package org.netbeans.modules.web.api.webmodule;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation;
import org.openide.util.Parameters;

/**
 * This query serves for executing single file in the server.
 * It returns the request parameters for a given file.
 *
 * @see org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation
 *
 * @author Pavel Buzek
 */
public final class RequestParametersQuery {

    private static final Lookup.Result<RequestParametersQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(RequestParametersQueryImplementation.class);

    /**
     * Returns the part of URL for access the file. It can include the query string.
     * @param  file the file to find the request parameters for.
     * @return path from the context; can be null.
     * @throws NullPointerException if the <code>file</code> parameter is null.
     */
    public static String getFileAndParameters(FileObject file) {
        Parameters.notNull("file", file); // NOI18N
        for (RequestParametersQueryImplementation impl : implementations.allInstances()) {
            String params = impl.getFileAndParameters(file);
            if (params != null) {
                return params;
            }
        }
        return null;
    }
}
