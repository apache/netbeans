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
package org.netbeans.modules.odcs.cnd.impl;

import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSServer;

/**
 *
 */
public class ODCSAuthManager {

    private ODCSAuthManager() {
    }

    public static ODCSAuthManager getInstance() {
        return AuthManagerHolder.INSTANCE;
    }

    private static class AuthManagerHolder {

        private static final ODCSAuthManager INSTANCE = new ODCSAuthManager();
    }

    private final ODCSManager manager = ODCSManager.getDefault();
    private final Set<AuthCallback> subscriptions = new HashSet<>();

    public boolean onLogin(String serverUrl, AuthCallback authCallback) {
        ODCSServer server = manager.getServer(serverUrl);

        if (server == null) {
            return false;
        }

        // At first - check if we are already logged in:
        PasswordAuthentication existingPa = server.getPasswordAuthentication();
        boolean loggedInNow = (existingPa != null);

        if (loggedInNow) {
            authCallback.onLogin(existingPa);
        }

        synchronized (subscriptions) {
            if (subscriptions.contains(authCallback)) {
                return loggedInNow;
            }

            server.addPropertyChangeListener(ODCSServer.PROP_LOGIN, (evt) -> {
                Object newValue = evt.getNewValue();
                if (newValue instanceof PasswordAuthentication) {
                    PasswordAuthentication passwordAuthentication = (PasswordAuthentication) newValue;
                    authCallback.onLogin(passwordAuthentication);
                }
            });

            subscriptions.add(authCallback);
        }

        return loggedInNow;
    }

    public static interface AuthCallback {

        public void onLogin(PasswordAuthentication pa);
    }
}
