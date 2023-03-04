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
package org.netbeans.modules.nativeexecution.api.util;

import java.security.SignatureException;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.sps.impl.SPSLocalImpl;
import org.netbeans.modules.nativeexecution.sps.impl.SPSRemoteImpl;
import org.netbeans.modules.nativeexecution.support.Logger;

public final class SolarisPrivilegesSupportProvider {

    private static final ConcurrentHashMap<ExecutionEnvironment, SolarisPrivilegesSupport> instances =
            new ConcurrentHashMap<>();

    private SolarisPrivilegesSupportProvider() {
    }

    public static SolarisPrivilegesSupport getSupportFor(ExecutionEnvironment execEnv) {
        SolarisPrivilegesSupport result = instances.get(execEnv);

        if (result == null) {
            if (execEnv.isLocal()) {
                try {
                    result = SPSLocalImpl.getNewInstance(execEnv);
                } catch (SignatureException ex) {
                    Logger.getInstance().log(Level.SEVERE, "Resource signature is wrong: {0}", ex.getMessage()); // NOI18N
                } catch (MissingResourceException ex) {
                    Logger.getInstance().log(Level.SEVERE, "Resource not found: {0}", ex.getMessage()); // NOI18N
                }
            } else {
                result = SPSRemoteImpl.getNewInstance(execEnv);
            }

            if (result != null) {
                SolarisPrivilegesSupport oldRef = instances.putIfAbsent(execEnv, result);

                if (oldRef != null) {
                    result = oldRef;
                }
            }
        }

        return result;
    }
}
