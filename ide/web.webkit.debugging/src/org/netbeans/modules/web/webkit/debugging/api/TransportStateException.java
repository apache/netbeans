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
package org.netbeans.modules.web.webkit.debugging.api;

/**
 * An exception thrown when the underlying transport is in a state
 * that doesn't allow execution of the requested command.
 *
 * @author Jan Stola
 */
public class TransportStateException extends Exception {

    /**
     * Creates a new {@code TransportStateException} without a message.
     */
    public TransportStateException() {
    }

    /**
     * Creates a new {@code TransportStateException} with the specified message.
     * 
     * @param message message with the exception details.
     */
    public TransportStateException(String message) {
        super(message);
    }

}
