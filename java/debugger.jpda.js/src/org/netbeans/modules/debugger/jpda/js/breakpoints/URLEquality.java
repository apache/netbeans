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

package org.netbeans.modules.debugger.jpda.js.breakpoints;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/** Equality for URLs that can handle canonical paths on local file URLs.
 *
 */
final class URLEquality {
    private final String protocol;
    private final String host;
    private final int port;
    private final String path;
    private final int hash;

    public URLEquality(URL url) {
        protocol = url.getProtocol().toLowerCase();
        String h = url.getHost();
        if (h != null) {
            h = h.toLowerCase();
        }
        host = h;
        port = url.getPort();
        path = url.getPath();
        int last = url.getPath().lastIndexOf("/");
        hash = protocol.hashCode() + host.hashCode() + port + url.getPath().substring(last + 1).hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof URLEquality)) {
            return false;
        }
        URLEquality ue = (URLEquality) obj;
        if (ue.hash != hash) {
            return false;
        }
        if (
            protocol.equals(ue.protocol) && 
            Objects.equals(host, ue.host) &&
            port == ue.port
        ) {
            if (Objects.equals(path, ue.path)) {
                return true;
            }
            if ("file".equals(protocol) && path != null && ue.path != null) { // NOI18N
                try {
                    File fThis = new File(path);
                    File fObj = new File(ue.path);
                    if (fThis.getCanonicalPath().equals(fObj.getCanonicalPath())) {
                        return true;
                    }
                } catch (IOException ex) {
                    // go on
                }
            }
        }
        return false;
    }
    
}
