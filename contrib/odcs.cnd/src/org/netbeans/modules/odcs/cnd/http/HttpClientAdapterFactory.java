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
package org.netbeans.modules.odcs.cnd.http;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.odcs.cnd.impl.ODCSAuthManager;

/**
 *
 */
public class HttpClientAdapterFactory {

    private static final Map<String, HttpClientAdapter> CLIENTS = new ConcurrentHashMap<>();

    // replaces old key
    public static HttpClientAdapter create(String base, PasswordAuthentication pa) {
        HttpClientAdapter adapter = HttpClientAdapter.create(base, pa);
        CLIENTS.put(base, adapter);

        return adapter;
    }

    public static HttpClientAdapter get(String base) {
        ODCSAuthManager.getInstance().onLogin(base, (PasswordAuthentication pa) -> {
            create(base, pa);
        });

        HttpClientAdapter client = CLIENTS.get(base);

        return client;
    }
}
