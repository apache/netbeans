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
package org.netbeans.modules.web.webkit.debugging.spi;

import java.net.URL;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;

/**
 * Transport of {@link Command}s or {@link Response}s between IDE and WebKit browser.
 */
public interface TransportImplementation {

    // requestChildNodes was introduced in 
    // http://trac.webkit.org/changeset/93396/trunk/Source/WebCore/inspector/Inspector.json
    public static final String VERSION_UNKNOWN_BEFORE_requestChildNodes = "version without requestChildNodes";
    public static final String VERSION_1 = "version 1.0";
    
    /**
     * Activate transport.
     */
    boolean attach();
    
    /**
     * Deactivate transport.
     */
    boolean detach();

    /**
     * Send command to WebKit.
     * 
     * @throws TransportStateException when the transport is not in a state
     * that allows execution of the given command.
     */
    void sendCommand(Command command) throws TransportStateException;
    
    /**
     * Register callback for receiving responses from WebKit.
     */
    void registerResponseCallback(ResponseCallback callback);

    /**
     * Descriptive name of the established transport. For example URL being debugged.
     */
    String getConnectionName();
    
    /**
     * URL being debugged.
     */
    URL getConnectionURL();
    
    /**
     * Returns version of the protocol supported on browser side. See constants
     * above.
     */
    String getVersion();
    
}
