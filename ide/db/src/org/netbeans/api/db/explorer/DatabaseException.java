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

package org.netbeans.api.db.explorer;

/**
 * Generic database exception.
 *
 * @author Slavek Psenicka, Andrei Badea
 */
public final class DatabaseException extends Exception
{

    static final long serialVersionUID = 7114326612132815401L;

    /**
     * Constructs a new exception with a specified message.
     *
     * @param message the text describing the exception.
     */
    public DatabaseException(String message) {
        super (message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public DatabaseException(Throwable cause) {
        super (cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param message the text describing the exception.
     * @param cause the cause of the exception.
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
