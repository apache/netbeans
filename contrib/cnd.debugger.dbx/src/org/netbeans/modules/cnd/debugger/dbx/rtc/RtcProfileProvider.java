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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.NativeDebuggerAuxObjectFactory;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObjectProvider;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service=NativeDebuggerAuxObjectFactory.class, position=300),
    @ServiceProvider(service=ConfigurationAuxObjectProvider.class, position=300)
})
public class RtcProfileProvider implements NativeDebuggerAuxObjectFactory {

    /**
     * public constructor as contract for service providers
     */
    public RtcProfileProvider() {
    }

    /**
     * Creates an instance of the auxiliary information object
     */

    public ConfigurationAuxObject factoryCreate(String baseDir, PropertyChangeSupport pcs, Configuration configuration) {
	return new RtcProfile(baseDir, pcs);
    }

    public String getAuxObjectID() {
        return RtcProfile.ID;
    }

}
