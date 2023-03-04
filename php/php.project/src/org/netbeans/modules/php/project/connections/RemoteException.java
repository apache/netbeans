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

package org.netbeans.modules.php.project.connections;

/**
 * @author Tomas Mysik
 */
public class RemoteException extends Exception {
    private static final long serialVersionUID = 314926954722925034L;
    private final String remoteServerAnswer;

    /**
     * @see Exception#Exception(java.lang.String)
     */
    public RemoteException(String message) {
        this(message, null, null);
    }

    /**
     * @see Exception#Exception(java.lang.String)
     * @see #RemoteException(String, Throwable, String)
     */
    public RemoteException(String message, String remoteServerAnswer) {
        this(message, null, remoteServerAnswer);
    }

    /**
     * @see Exception#Exception(java.lang.String, java.lang.Throwable)
     */
    public RemoteException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * The same as {@link #RemoteException(String, Throwable)} but remote server answer can be provided.
     * It is usually some detailed information about the failure, probably never localized (it is taken directly
     * from a remote server).
     * @see Exception#Exception(java.lang.String, java.lang.Throwable)
     */
    public RemoteException(String message, Throwable cause, String remoteServerAnswer) {
        super(message, cause);
        this.remoteServerAnswer = remoteServerAnswer;
    }

    /**
     * Get non-localized remote server answer or <code>null</code>.
     * @return non-localized remote server answer or <code>null</code>.
     */
    public String getRemoteServerAnswer() {
        return remoteServerAnswer;
    }
}
