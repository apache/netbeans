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

package org.netbeans.modules.websvc.wsstack.api;

import java.net.URL;
import org.netbeans.modules.websvc.wsstack.WSToolAccessor;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;

/** WS Tool API object, provides libraries (jar files) required by the tool.
 *  As an example of the tool is JAX-WS:wsimport tool.
 *
 * @author mkuchtiak
 */
public final class WSTool {

    static  {
        WSToolAccessor.DEFAULT = new WSToolAccessor() {

            @Override
            public WSTool createWSTool(WSToolImplementation spi) {
                return new WSTool(spi);
            }
        };
    }

    WSToolImplementation spi;
    private WSTool(WSToolImplementation spi) {
        this.spi = spi;
    }

    /** Returns WS tool name
     *
     * @return WS tool name
     */
    public String getName() {
        return spi.getName();
    }

    /** Returns array of jar files, located on server, required by the tool
     *
     * @return array of jar files
     */
    public URL[] getLibraries() {
        return spi.getLibraries();
    }
}
