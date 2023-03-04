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

package org.netbeans.modules.properties;

import org.openide.util.RequestProcessor;

/**
 *
 * Provides an {@link org.openide.util.RequestProcessor} instance for the Properties module.
 *
 * @author Tomas Stupka
 */
public class PropertiesRequestProcessor {

     /** the static {@link org.openide.util.RequestProcessor} instance */
    private static final RequestProcessor requestProcessor =
        new RequestProcessor ("org.netbeans.modules.properties.PropertiesRequestProcessor");

    /** Creates a new instance of PropertiesResourceProcessor */
    public PropertiesRequestProcessor() {        
    }
    
    /**
     * The getter for the shared instance of the {@link org.openide.util.RequestProcessor}
     *
     * @return a shared {@link org.openide.util.RequestProcessor} instance with throughput 1
     */
    public static RequestProcessor getInstance() {
        return requestProcessor;
    }
    
}
