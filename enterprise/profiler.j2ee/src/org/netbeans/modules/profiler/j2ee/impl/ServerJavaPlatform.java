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
package org.netbeans.modules.profiler.j2ee.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.openide.util.NbBundle;

/**
 * A simple registry-like access point for server-defined JavaPlatform
 * @author Jaroslav Bachorik
 */
public class ServerJavaPlatform {
    private static final String KEY_PLATFORM = "platform.ant.name"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(ServerJavaPlatform.class.getName());
    
    @NbBundle.Messages({
        "# {0} - the server instance id",
        "MSG_MissingServerJavaPlatform=Missing JavaPlatform for server {0}.\nUsing the IDE default platform instead."
    })
    /**
     * @since 1.20
     */
    public static @NonNull JavaPlatform getPlatform(String serverInstanceId) {
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceId);

        JavaPlatform newjp = null;
        try {
            if (si != null) {
                J2eePlatform jeep = si.getJ2eePlatform();
                if (jeep != null) {
                    org.netbeans.api.java.platform.JavaPlatform pjp = jeep.getJavaPlatform();
                    newjp = pjp != null ? JavaPlatform.getJavaPlatformById(pjp.getProperties().get(KEY_PLATFORM)) : null;
                }
            }
        } catch (InstanceRemovedException e) {
        }
        
        if (newjp == null) {
            LOG.log(Level.INFO, Bundle.MSG_MissingServerJavaPlatform(serverInstanceId));
            newjp = JavaPlatform.getDefaultPlatform();
        }
        
        return newjp != null ? newjp : JavaPlatform.getDefaultPlatform();
    }

}
