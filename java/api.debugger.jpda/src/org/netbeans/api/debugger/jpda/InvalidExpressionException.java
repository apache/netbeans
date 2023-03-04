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

package org.netbeans.api.debugger.jpda;


/**
 * Notification about bad expression.
 *
 * @author   Jan Jancura
 */
public class InvalidExpressionException extends Exception {

    private final String message;
    private final boolean isFromApp;

    /**
     * Constructs a InvalidExpressionException with given message.
     *
     * @param message a exception message
     */
    public InvalidExpressionException (String message) {
        this(message, null);
    }

    /**
     * Constructs a InvalidExpressionException for a given target exception.
     *
     * @param t a target exception
     */
    public InvalidExpressionException (Throwable t) {
        this(null, t);
    }
    
    /**
     * Constructs a InvalidExpressionException for a given target exception.
     *
     * @param t a target exception
     * @param isFromApp <code>true</code> when the target exception is a mirror
     *                  of an application-level exception, <code>false</code>
     *                  otherwise.
     * @since 3.7
     */
    public InvalidExpressionException (Throwable t, boolean isFromApp) {
        this(null, t, isFromApp);
    }

    /**
     * Constructs a InvalidExpressionException with given message and target exception.
     *
     * @param message a exception message
     * @param t a target exception
     * @since 3.1
     */
    public InvalidExpressionException (String message, Throwable t) {
        this(message, t, false);
    }

    /**
     * Constructs a InvalidExpressionException with given message and target exception.
     *
     * @param message a exception message
     * @param t a target exception
     * @param isFromApp <code>true</code> when the target exception is a mirror
     *                  of an application-level exception, <code>false</code>
     *                  otherwise.
     * @since 3.7
     */
    public InvalidExpressionException (String message, Throwable t, boolean isFromApp) {
        super(message, t);
        // Assert that application-level exceptions have the appropriate mirror:
        assert isFromApp && t != null || !isFromApp;
        this.message = message;
        this.isFromApp = isFromApp;
    }

    @Override
    public String getMessage() {
        Throwable cause = getCause();
        if (cause != null &&
            (message == null || message.trim().isEmpty())) {
            
            return cause.getMessage();
        }
        return message;
    }
    
    
    
    /**
     * Get the thrown target exception.
     *
     * @return the thrown target exception
     */
    public Throwable getTargetException () {
        return getCause();
    }

    /**
     * Test whether the target exception is a mirror of an application-level
     * exception.
     * @return <code>true</code> when the target exception represents an
     *         exception in the application code, <code>false</code> otherwise.
     * @since 3.7
     */
    public final boolean hasApplicationTarget() {
        return isFromApp;
    }
}

