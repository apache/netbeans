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
package org.netbeans.modules.glassfish.tooling;

import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Common GlassFish IDE SDK Exception.
 * <p>
 * Base exception for GlassFish IDE SDK Exception contains all common code.
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishIdeException extends RuntimeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(GlassFishIdeException.class);

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
     * Constructs an instance of <code>GlassFishIdeException</code> without
     * detail message.
     */
    public GlassFishIdeException() {
        super();
        final String METHOD = "init";
        // Log exception in WARNING level.
        LOGGER.exception(Level.WARNING, LOGGER.excMsg(METHOD, "empty"));
    }

    /**
     * Constructs an instance of <code>GlassFishIdeException</code> with the
     * specified detail message.
     * <p>
     * @param msg The detail message.
     */
    public GlassFishIdeException(String msg) {
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
     * Constructs an instance of <code>GlassFishIdeException</code> with the
     * specified detail message and arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public GlassFishIdeException(String msg, Object... arguments) {
        this(formatMessage(msg, arguments));
    }

    /**
     * Constructs an instance of <code>GlassFishIdeException</code> with the
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
    public GlassFishIdeException(String msg, Throwable cause) {
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
