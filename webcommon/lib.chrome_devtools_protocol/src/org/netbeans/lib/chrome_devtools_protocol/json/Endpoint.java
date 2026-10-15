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
package org.netbeans.lib.chrome_devtools_protocol.json;

import java.net.URI;
import java.util.Objects;

public class Endpoint {
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_IFRAME = "iframe";
    public static final String TYPE_NODE = "node";

    private String id;
    private String title;
    private String description;
    private String type;
    private URI faviconUrl;
    private URI url;
    private URI webSocketDebuggerUrl;

    public Endpoint() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URI getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(URI faviconUrl) {
        this.faviconUrl = faviconUrl;
    }


    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public URI getWebSocketDebuggerUrl() {
        return webSocketDebuggerUrl;
    }

    public void setWebSocketDebuggerUrl(URI webSocketDebuggerUrl) {
        this.webSocketDebuggerUrl = webSocketDebuggerUrl;
    }

    @Override
    public String toString() {
        return "Endpoint{\n  " + "id=" + id + ",\n  title=" + title + ",\n  description=" + description + ",\n  type=" + type + ",\n  url=" + url + ",\n  webSocketDebuggerUrl=" + webSocketDebuggerUrl + "\n}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.webSocketDebuggerUrl);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Endpoint other = (Endpoint) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.webSocketDebuggerUrl, other.webSocketDebuggerUrl);
    }


}
