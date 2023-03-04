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

package org.netbeans.modules.websvc.wsstack.spi;

import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;

/** Main SPI interface providing all necessary information about the server capability for particular WS Stack
 *
 * @author mkuchtiak, abadea
 */
public interface WSStackImplementation<T> {

    /** Provides an instance of type parameter (<T>).
     * This object can provide additional information about WS Stack.
     * It's responsibility of particular WS Stack support to specify this class
     * and what kind of information should be stored in it.
     * 
     * @return object of <T> class or null
     */
    T get();

    /** Provides WS Stack Version information.
     * 
     * @return WS Stack version
     */
    WSStackVersion getVersion();

    /** Provides {@link org.netbeans.modules.websvc.wsstack.api.WSTool} for particular WS Stack Tool (e.g. wsimport), based on toolId.
     * 
     * @param toolId WS tool identifier
     * @return WSTool API object
     */
    WSTool getWSTool(Tool toolId);
    
    /** Informs if WS feature is supported by particular WS stack.
     *  (e.g. JSR_109, WSIT, ... )
     * 
     * @param feature WS feature identifier
     * @return true if supported, false if not
     */
    boolean isFeatureSupported(Feature feature);
}
