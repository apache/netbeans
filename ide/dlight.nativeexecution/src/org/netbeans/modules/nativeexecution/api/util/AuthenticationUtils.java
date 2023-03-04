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

import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author in220748
 */
public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static void usePasswordAuthenticationFor(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);
        auth.setPassword();
        auth.apply();
    }

    public static void useSSHKeyAuthenticationFor(ExecutionEnvironment env, String sshKeyFile) {
        Authentication auth = Authentication.getFor(env);
        auth.setSSHKeyFile(sshKeyFile);
        auth.apply();
    }
    
    /**
     * Returns ssh key file name for the specified ExecutionEnvironment or null if it is not of SSH_KEY auth type
     */
    public static String getSSHKeyFileFor(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);
        if (auth.getType() == Authentication.Type.SSH_KEY) {
            String file = auth.getSSHKeyFile();
            if (file == null) {
                file = "";
            }
            return file;
        } else {
            return null;
        }
    }
    
    public static void changeAuth(ExecutionEnvironment env, Authentication auth) {
        ConnectionManagerAccessor.getDefault().changeAuth(env, auth);
    }
}
