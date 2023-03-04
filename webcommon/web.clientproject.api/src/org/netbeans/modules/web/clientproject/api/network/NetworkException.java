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
package org.netbeans.modules.web.clientproject.api.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openide.util.Parameters;

/**
 * This class represents a network issue (a file cannot be
 * downloaded and similar). Failed URIs (or names) can be handled
 * via {@link NetworkSupport#showNetworkErrorDialog(List)}.
 * @see NetworkSupport
 * @since 1.13
 */
public class NetworkException extends IOException {

    private static final long serialVersionUID = -3546842137531187L;

    private final List<String> failedRequests = new CopyOnWriteArrayList<String>();


    /**
     * Create new network exception.
     * @param failedRequest URI or name that failed to be downloaded
     * @param cause the cause, typically FileNotFoundException
     */
    public NetworkException(String failedRequest, Throwable cause) {
        this(Collections.singletonList(failedRequest), cause);
    }

    /**
     * Create new network exception.
     * @param failedRequests URIs or names that failed to be downloaded
     * @param cause the cause, typically FileNotFoundException
     */
    public NetworkException(List<String> failedRequests, Throwable cause) {
        super(cause);
        Parameters.notNull("failedRequests", failedRequests); // NOI18N
        if (failedRequests.isEmpty()) {
            throw new IllegalArgumentException("Failed requests must be provided.");
        }
        this.failedRequests.addAll(failedRequests);
    }

    /**
     * Get URIs or names that failed to be downloaded.
     * @return list of URIs or names that failed to be downloaded
     * @see NetworkSupport#showNetworkErrorDialog(List)
     */
    public List<String> getFailedRequests() {
        return new ArrayList<String>(failedRequests);
    }

}
