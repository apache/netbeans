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

package org.netbeans.modules.websvc.api.client;

/** Note: this class is not final, so as to allow implementors to derive from
 *  it to add additional implementation dependent properties such as wscompile
 *  features.
 *
 * @author Martin Grebac
 */
public class ClientStubDescriptor {

    /** Client types.  Not to be intermixed with service types.
     */
    /** Key to represent jsr-109 static stub clients
     *
     *  Note: This string may be embedded in build-impl.xsl for the projects
     *  that implement web service client support.  Change with care.
     */
    public static final String JSR109_CLIENT_STUB = "jsr-109_client";
    
    /** Key to represent jaxrpc static stub clients.
     *
     *  Note: This string may be embedded in build-impl.xsl for the projects
     *  that implement web service client support.  Change with care.
     */
    public static final String JAXRPC_CLIENT_STUB = "jaxrpc_static_client";
    
    // Private data
    private final String name;
    private final String displayName;

    public ClientStubDescriptor(final String name, final String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String toString() {
        return displayName;
    }
}
