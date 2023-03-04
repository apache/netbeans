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

package org.netbeans.modules.db.metadata.model.api;

/**
 * An unchecked exception thrown by the metadata classes ({@link Metadata} and the
 * classes it aggregates.
 *
 * <p>{@code MetadataException}s thrown inside a metadata model
 * read action need not be handled by clients. Such unhandled exceptions
 * will be rethrown from {@link MetadataModel#runReadAction} as
 * {@link MetadataModelException}s.
 *
 * @author Andrei Badea
 */
public class MetadataException extends RuntimeException {

    public MetadataException() {
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(Throwable cause) {
        super(cause);
    }
}
