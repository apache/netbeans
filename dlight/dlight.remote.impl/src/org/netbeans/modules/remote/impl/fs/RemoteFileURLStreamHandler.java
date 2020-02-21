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

package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.openide.util.URLStreamHandlerRegistration;

/**
 *
 */
@URLStreamHandlerRegistration(protocol="rfs")
public class RemoteFileURLStreamHandler extends URLStreamHandler {

    public static final String PROTOCOL = "rfs"; //NOI18N
    public static final String PROTOCOL_PREFIX = "rfs:"; //NOI18N
    
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new RemoteFileURLConnection(url);
    }

    @Override
    protected int getDefaultPort() {
        return 22;
    }

    @Override
    protected boolean hostsEqual(URL u1, URL u2) {
        // string based implementation to prevent use of expensive InetAddress
        if (u1.getHost() != null && u2.getHost() != null) {
            return u1.getHost().equalsIgnoreCase(u2.getHost());
        } else {
            return u1.getHost() == null && u2.getHost() == null;
        }
    }
}
