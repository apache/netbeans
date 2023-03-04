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

package org.openide.filesystems;

import java.io.IOException;
import org.openide.util.Exceptions;

/**
 * I/O exception with localized message.
 */
final class FSException extends IOException {

    /**
     * @param message localized text
     */
    public FSException(String message) {
        super(message);
        Exceptions.attachLocalizedMessage(this, message);
    }

    public @Override String getMessage() {
        // should be distinct from localized message... XXX is this still necessary?
        return super.getMessage() + " "; // NOI18N
    }

    public @Override String getLocalizedMessage() {
        return super.getMessage();
    }

}
