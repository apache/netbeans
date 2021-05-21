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
package org.netbeans.modules.payara.tooling.data;

import org.netbeans.modules.payara.tooling.PayaraIdeException;

/**
 * Payara IDE SDK Exception related to server administration command package
 * problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class DataException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Exception message for invalid Payara administration interface
     *  type. */
    static final String INVALID_ADMIN_INTERFACE =
            "Invalid Payara administration interface type";

    /** Exception message for invalid Payara version. */
    static final String INVALID_CONTAINER = "Invalid Payara container";

    /** Exception message for invalid Payara URL.
     *  Used in IDE URL entity class. */
    public static final String INVALID_URL = "Invalid Payara URL";

    /** Exception for Payara installation root directory null value. */
    static final String SERVER_ROOT_NULL
            = "Payara installation root directory is null";

    /** Exception for Payara home directory null value. */
    static final String SERVER_HOME_NULL
            = "Payara home directory is null";

    /** Exception for non existent Payara installation root directory.
        Requires 1 directory argument.*/
    static final String SERVER_ROOT_NONEXISTENT
            = "Payara installation root directory {0} does not exist";

    /** Exception for non existent Payara home directory.
        Requires 1 directory argument.*/
    static final String SERVER_HOME_NONEXISTENT
            = "Payara home directory {0} does not exist";

    /** Exception for unknown Payara version in Payara home directory.
     */
    static final String SERVER_HOME_NO_VERSION
            = "Unknown Payara version in home directory {0}";

    /**  Exception for Payara URL null value. */
    static final String SERVER_URL_NULL = "Payara URL is null";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>DataException</code> without
     * detail message.
     */
    public DataException() {
        super();
    }

    /**
     * Constructs an instance of <code>DataException</code> with the
     * specified detail message.
     * <p>
     * @param msg The detail message.
     */
    public DataException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>DataException</code> with the
     * specified detail message and arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public DataException(final String msg, final Object... arguments) {
        super(msg, arguments);
    }

    /**
     * Constructs an instance of <code>DataException</code> with the
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
    public DataException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
