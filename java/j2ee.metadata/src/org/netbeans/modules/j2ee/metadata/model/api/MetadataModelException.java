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

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;

/**
 * Signals that an exception has occured while working with
 * the metadata model.
 *
 * @author Andrei Badea
 */
public final class MetadataModelException extends IOException {

    /**
     * Constructs an {@code MetadataModelException} with {@code null}
     * as its error detail message.
     */
    public MetadataModelException() {
        super();
    }

    /**
     * Constructs an {@code MetadataModelException} with the specified detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method)
     */
    public MetadataModelException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code MetadataModelException} with the specified cause and a
     * detail message equal to the localized message (if present) or message of <code>cause</code>.
     * This constructor is useful for IO exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A null value is permitted,
     *         and indicates that the cause is nonexistent or unknown.)
     */
    public MetadataModelException(Throwable cause) {
        super(cause == null ? null : getMessage(cause));
        initCause(cause);
    }

    private static String getMessage(Throwable t) {
        String message = t.getLocalizedMessage();
        if (message == null) {
            message = t.getMessage();
        }
        return message;
    }
}
