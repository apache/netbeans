/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.freeform;

/**
 * Contains constants for various properties used in the web project
 *
 * @author Andrei Badea
 */
public class WebFreeformProperties {

    /**
     * JPDA debug session name
     */
    public final static String JPDA_SESSION_NAME = "jpda.session.name"; // NOI18N

    /**
     * JPDA transport type
     */
    public final static String JPDA_TRANSPORT = "jpda.transport"; // NOI18N

    /**
     * JPDA host to connect to
     */
    public final static String JPDA_HOST = "jpda.host"; // NOI18N
    
    public final static String JPDA_ADDRESS = "jpda.address"; // NOI18N
    
    /**
     * The full client URL (e.g., http://localhost:8084/AppName)
     */
    public final static String CLIENT_URL = "client.url"; // NOI18N
    
    public final static String SRC_FOLDERS = "src.folders"; // NOI18N
    
    public final static String WEB_DOCBASE_DIR = "web.docbase.dir"; // NOI18N
    
    public final static String DEBUG_SOURCEPATH = "debug.sourcepath"; // NOI18N
    
    private WebFreeformProperties() {
    }
    
}
