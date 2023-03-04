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

package org.netbeans.modules.hudson.spi;

import java.net.URL;
import java.net.URLConnection;

/**
 * Service which is able to add authentication to a Hudson HTTP connection.
 */
public interface ConnectionAuthenticator {

    /**
     * Prepare a request with authentication that might be needed.
     * {@link URLConnection#connect} has not yet been called.
     * @param conn a pending connection
     * @param home the Hudson root URL
     */
    void prepareRequest(URLConnection conn, URL home);

    /**
     * Called in response to a failed attempt to access a resource.
     * Can try to authenticate and restart the request.
     * @param conn a connection which has received a 403 (Forbidden) response
     * @param home the Hudson root URL
     * @return a fresh connection (do not call {@link URLConnection#connect}),
     *         or null if this authenticator is unable to log in
     */
    URLConnection forbidden(URLConnection conn, URL home);

}
