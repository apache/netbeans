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

package org.netbeans.lib.v8debug.connection;

/**
 * Properties contained in the communication header.
 * The static fields provide names of well-known properties that are in the
 * debugger protocol header.
 * 
 * @author Martin Entlicher
 */
public final class HeaderProperties {
    
    /**
     * A type header property. The typical value is 'connect'.
     */
    public static final String TYPE = "Type";                                   // NOI18N
    /**
     * A V8 version property. The typical value are four dot-separated numbers,
     * like '4.0.1.2'.
     */
    public static final String V8_VERSION = "V8-Version";                       // NOI18N
    /**
     * A protocol version property. The typical value is '1'.
     */
    public static final String PROTOCOL_VERSION = "Protocol-Version";           // NOI18N
    /**
     * An embedding host property. For e.g. node.js the typical value is 'node v1.5.1'.
     */
    public static final String EMBEDDING_HOST = "Embedding-Host";               // NOI18N
    
    private HeaderProperties() {}
    
}
