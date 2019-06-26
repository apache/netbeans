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
package org.netbeans.modules.payara.tooling.admin;

import org.netbeans.modules.payara.tooling.PayaraIdeException;

/**
 * Payara IDE SDK Exception related to server administration command package
 * problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Exception message for unsupported Payara version. */
    static final String UNSUPPORTED_VERSION = "Unsupported Payara version";

    /** Exception message for unknown Payara administration interface
     *  type. */
    static final String UNKNOWN_ADMIN_INTERFACE =
            "Unknown Payara administration interface type";

    /** Exception message for unknown Payara version. */
    static final String UNKNOWN_VERSION = "Unknown Payara version";

    /** Exception message for unsupported operation. */
    static final String UNSUPPORTED_OPERATION = "Operation is not supported";

    /** Exception message for IOException when reading HTTP response. */
    static final String HTTP_RESP_IO_EXCEPTION
            = "Can not read HTTP response, caught IOException";
    /**
     * Exception message for exceptions when initializing <code>Runner</code>
     * object.
     */
    static final String RUNNER_INIT = "Cannot initialize Runner class";

    /**
     * Exception message for exceptions when preparing headers for
     * HTTP connection.
     */
    static final String RUNNER_HTTP_HEADERS
            = "Cannos set headers for HTTP connection";

    /** Exception message for exceptions when building command URL. */
    static final String RUNNER_HTTP_URL = "Cannot build HTTP command URL";

    /** Exception message for illegal <code>Command</code> instance provided. */
    static final String ILLEGAL_COMAND_INSTANCE = "Illegal command instance provided";

    /** Exception message for illegal <code>null</code> value provided. */
    static final String ILLEGAL_NULL_VALUE
            = "Value shall not be null";

    /** Exception message for UnsupportedEncodingException when processing
     *  <code>Manifest</code> retrieved from server. */
    static final String HTTP_RESP_UNS_ENC_EXCEPTION
            = "Can not read HTTP response, caught UnsupportedEncodingException";

    /** Exception message for invalid server component (application) item. */
    public static final String MANIFEST_INVALID_COMPONENT_ITEM
            = "Invalid component item";

    /** Exception message for invalid constant representing <code>boolean</code>
     *  value. */
    public static final String INVALID_BOOLEAN_CONSTANT
            = "Invalid String representing boolean constant.";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>CommandException</code> without
     * detail message.
     */
    public CommandException() {
        super();
    }

    /**
     * Constructs an instance of <code>CommandException</code> with the
     * specified detail message.
     * <p>
     * @param msg The detail message.
     */
    public CommandException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>CommandException</code> with the
     * specified detail message and arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public CommandException(String msg, Object... arguments) {
        super(msg, arguments);
    }

    /**
     * Constructs an instance of <code>CommandException</code> with the
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
    public CommandException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
