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
package org.netbeans.lib.jshell.agent;

/**
 * The exception thrown on the remote side upon executing a
 * {@link jdk.jshell.Snippet.Status#RECOVERABLE_DEFINED RECOVERABLE_DEFINED}
 * user method. This exception is not seen by the end user nor through the API.
 * @author Robert Field
 */
@SuppressWarnings("serial")             // serialVersionUID intentionally omitted
public class RemoteResolutionException extends RuntimeException {

    final int id;

    /**
     * The throw of this exception is generated into the body of a
     * {@link jdk.jshell.Snippet.Status#RECOVERABLE_DEFINED RECOVERABLE_DEFINED}
     * method.
     * @param id An internal identifier of the specific method
     */
    public RemoteResolutionException(int id) {
        super("RemoteResolutionException");
        this.id = id;
    }
}
