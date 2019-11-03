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
package org.netbeans.modules.payara.tooling;

import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;

/**
 * Common Payara IDE SDK Exception.
 * <p>
 * Base exception for Payara IDE SDK Exception contains all common code.
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class PayaraIdeException extends RuntimeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(PayaraIdeException.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create exception message from message pattern and arguments using
     * {@link java.text.MessageFormat}.
     * <p/>
     * @param msg The detail message pattern.
     * @param arguments Arguments to be inserted into message pattern.
     */
    private static String formatMessage(String msg, Object... arguments) {
        if (arguments != null && arguments.length > 0) {
            return MessageFormat.format(msg, arguments);
        } else {
            return msg;
        }
    }
 
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>PayaraIdeException</code> without
     * detail message.
     */
    public PayaraIdeException() {
        super();
        final String METHOD = "init";
        // Log exception in WARNING level.
        LOGGER.exception(Level.WARNING, LOGGER.excMsg(METHOD, "empty"));
    }

    /**
     * Constructs an instance of <code>PayaraIdeException</code> with the
     * specified detail message.
     * <p>
     * @param msg The detail message.
     */
    public PayaraIdeException(String msg) {
        super(msg);
        final String METHOD = "init";
        // Log exception in WARNING level.
        if (LOGGER.isLoggable(Level.WARNING)) {
            String hdr = LOGGER.excMsg(METHOD, "msg");
            String sep = msg != null ? ": " : ".";
            StringBuilder sb = new StringBuilder(hdr.length() + sep.length()
                    + (msg != null ? msg.length() : 0));
            sb.append(hdr);
            sb.append(sep);
            if (msg != null) {
                sb.append(msg);
            }
            LOGGER.exception(Level.WARNING, sb.toString());
        }
    }

    /**
     * Constructs an instance of <code>PayaraIdeException</code> with the
     * specified detail message and arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public PayaraIdeException(String msg, Object... arguments) {
        this(formatMessage(msg, arguments));
    }

    /**
     * Constructs an instance of <code>PayaraIdeException</code> with the
     * specified detail message and cause. Exception is logged on WARN level.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated int his runtime exception's detail message.
     * <p>
     * @param msg   the detail message (which is saved for later retrieval
     *              by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <code>null</code> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public PayaraIdeException(String msg, Throwable cause) {
        super(msg, cause);
        final String METHOD = "init";
        // Log exception in WARNING level.
        if (LOGGER.isLoggable(Level.WARNING)) {
            String hdr = LOGGER.excMsg(METHOD, "msg");
            String sep = msg != null ? ": " : ".";
            StringBuilder sb = new StringBuilder(hdr.length() + sep.length()
                    + (msg != null ? msg.length() : 0));
            sb.append(hdr);
            sb.append(sep);
            if (msg != null) {
                sb.append(msg);
            }
            LOGGER.exception(Level.WARNING, sb.toString());
            // Log cause exception in WARNING level.
            if (cause != null) {
                String className = cause.getClass().getName();
                msg = cause.getMessage();
                sep = msg != null ? ": " : ".";
                hdr = LOGGER.excMsg(METHOD, "cause");
                sb = new StringBuilder(hdr.length() + className.length()
                        + sep.length() + (msg != null ? msg.length() : 0));
                sb.append(hdr);
                sb.append(className);
                sb.append(sep);
                if (msg != null) {
                    sb.append(msg);
                }
                LOGGER.exception(Level.WARNING, sb.toString());
            }
        }
    }

}
