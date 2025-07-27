/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.server;

import org.netbeans.modules.payara.tooling.PayaraIdeException;

/**
 * Payara IDE SDK Exception related to reading logs from server.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogException extends PayaraIdeException {

    // Constructors                                                           //
    /**
     * Constructs an instance of <code>FetchLogException</code> without
     * detail message.
     */
    public FetchLogException() {
        super();
    }

    /**
     * Constructs an instance of <code>FetchLogException</code> with the
     * specified detail message.
     * <p>
     * @param msg The detail message.
     */
    public FetchLogException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>FetchLogException</code> with the
     * specified detail message and cause. Exception is logged on WARN level.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this runtime exception's detail message.
     * <p>
     * @param msg   the detail message (which is saved for later retrieval
     *              by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <code>null</code> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public FetchLogException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
