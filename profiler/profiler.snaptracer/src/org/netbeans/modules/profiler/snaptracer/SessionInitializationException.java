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

package org.netbeans.modules.profiler.snaptracer;

/**
 * Signals that a TracerPackage/TracerProbe failed to initialize for a new
 * Tracer session. Preferably provides a message to notify the user about
 * the failure.
 *
 * @author Jiri Sedlacek
 */
public final class SessionInitializationException extends Exception {

    private final String userMessage;


    /**
     * Creates a new instance of SessionInitializationException with defined
     * log message and default user message.
     *
     * @param logMessage log message
     */
    public SessionInitializationException(String logMessage) {
        this(null, logMessage);
    }

    /**
     * Creates a new instance of SessionInitializationException with defined
     * log message and cause and default user message.
     *
     * @param logMessage log message
     * @param cause exception cause
     */
    public SessionInitializationException(String logMessage,
                                          Throwable cause) {
        this(null, logMessage, cause);
    }

    /**
     * Creates a new instance of SessionInitializationException with defined
     * user message and log message.
     *
     * @param userMessage user message
     * @param logMessage log message
     */
    public SessionInitializationException(String userMessage,
                                          String logMessage) {
        super(logMessage);
        this.userMessage = userMessage;
    }

    /**
     * Creates a new instance of SessionInitializationException with defined
     * user message, log message and cause.
     *
     * @param userMessage user message
     * @param logMessage log message
     * @param cause exception cause
     */
    public SessionInitializationException(String userMessage,
                                          String logMessage,
                                          Throwable cause) {
        super(logMessage, cause);
        this.userMessage = userMessage;
    }


    /**
     * Returns an user message to be displayed in Tracer UI. The message should
     * be short, for example "Probe XYZ failed to initialize" or "Probe XYZ
     * failed to connect to target application."
     *
     * @return user message to be displayed in Tracer UI
     */
    public String getUserMessage() {
        return userMessage;
    }

}
