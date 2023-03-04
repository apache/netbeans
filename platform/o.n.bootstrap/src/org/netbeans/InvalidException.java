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

package org.netbeans;

import java.io.IOException;
import java.util.jar.Manifest;

/** Exception thrown indicating that a module's contents are ill-formed.
 * This could be a parse error in the manifest, or an inability to load
 * certain resources from the classloader.
 * ErrorManager should be used where needed to attach related exceptions
 * or user-friendly annotations.
 * @author Jesse Glick
 */
public final class InvalidException extends IOException {

    private final Module m;
    private final Manifest man;
    private String localizedMessage;

    public InvalidException(String detailMessage) {
        super(detailMessage);
        m = null;
        man = null;
    }
    
    public InvalidException(Module m, String detailMessage) {
        super(m + ": " + detailMessage); // NOI18N
        this.m = m;
        this.man = null;
    }

    InvalidException(String msg, Manifest manifest) {
        super(msg);
        this.m = null;
        this.man = manifest;
    }

    public InvalidException(Module m, String detailMessage, String localizedMessage) {
        this(m, detailMessage);
        this.localizedMessage = localizedMessage;
    }

    /** Affected module. May be null if this is hard to determine
     * (for example a problem which would make the module ill-formed,
     * during creation or reloading).
     */
    public Module getModule() {
        return m;
    }

    /** The manifest that caused this exception. Can be null, if the
     * manifest cannot be obtained.
     * @return manifest that contains error 
     * @since 2.20
     */
    public Manifest getManifest() {
        if (man != null) {
            return man;
        }
        if (m != null) {
            return m.getManifest();
        }
        return null;
    }

    @Override
    public String getLocalizedMessage() {
        if (localizedMessage != null) {
            return localizedMessage;
        } else {
            return super.getLocalizedMessage();
        }
    }
}
