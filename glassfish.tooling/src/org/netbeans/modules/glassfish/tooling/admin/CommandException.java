/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;

/**
 * GlassFish IDE SDK Exception related to server administration command package
 * problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandException extends GlassFishIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Exception message for unsupported GlassFish version. */
    static final String UNSUPPORTED_VERSION = "Unsupported GlassFish version";

    /** Exception message for unknown GlassFish administration interface
     *  type. */
    static final String UNKNOWN_ADMIN_INTERFACE =
            "Unknown GlassFish administration interface type";

    /** Exception message for unknown GlassFish version. */
    static final String UNKNOWN_VERSION = "Unknown GlassFish version";

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
