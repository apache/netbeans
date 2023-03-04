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
package org.netbeans.modules.glassfish.tooling.data;

import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;

/**
 * GlassFish IDE SDK Exception related to server administration command package
 * problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class DataException extends GlassFishIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Exception message for invalid GlassFish administration interface
     *  type. */
    static final String INVALID_ADMIN_INTERFACE =
            "Invalid GlassFish administration interface type";

    /** Exception message for invalid GlassFish version. */
    static final String INVALID_CONTAINER = "Invalid GlassFish container";

    /** Exception message for invalid GlassFish URL.
     *  Used in IDE URL entity class. */
    public static final String INVALID_URL = "Invalid GlassFish URL";

    /** Exception for GlassFish installation root directory null value. */
    static final String SERVER_ROOT_NULL
            = "GlassFish installation root directory is null";

    /** Exception for GlassFish home directory null value. */
    static final String SERVER_HOME_NULL
            = "GlassFish home directory is null";

    /** Exception for non existent GlassFish installation root directory.
        Requires 1 directory argument.*/
    static final String SERVER_ROOT_NONEXISTENT
            = "GlassFish installation root directory {0} does not exist";

    /** Exception for non existent GlassFish home directory.
        Requires 1 directory argument.*/
    static final String SERVER_HOME_NONEXISTENT
            = "GlassFish home directory {0} does not exist";

    /** Exception for unknown GlassFish version in GlassFish home directory.
     */
    static final String SERVER_HOME_NO_VERSION
            = "Unknown GlassFish version in home directory {0}";

    /**  Exception for GlassFish URL null value. */
    static final String SERVER_URL_NULL = "GlassFish URL is null";

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
