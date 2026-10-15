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
package org.netbeans.lib.chrome_devtools_protocol.runtime;

import java.util.Objects;

/**
 * <p><strong>Experimental</strong></p>
 */
public final class CustomPreview {

    private String header;
    private String bodyGetterId;

    /**
     * The JSON-stringified result of formatter.header(object, config) call. It
     * contains json ML array that represents RemoteObject.
     */
    public String getHeader() {
        return header;
    }

    /**
     * The JSON-stringified result of formatter.header(object, config) call. It
     * contains json ML array that represents RemoteObject.
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * If formatter returns true as a result of formatter.hasBody call then
     * bodyGetterId will contain RemoteObjectId for the function that returns
     * result of formatter.body(object, config) call. The result value is json
     * ML array.
     */
    public String getBodyGetterId() {
        return bodyGetterId;
    }

    /**
     * If formatter returns true as a result of formatter.hasBody call then
     * bodyGetterId will contain RemoteObjectId for the function that returns
     * result of formatter.body(object, config) call. The result value is json
     * ML array.
     */
    public void setBodyGetterId(String bodyGetterId) {
        this.bodyGetterId = bodyGetterId;
    }

    @Override
    public String toString() {
        return "CustomPreview{" + "header=" + header + ", bodyGetterId=" + bodyGetterId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.header);
        hash = 23 * hash + Objects.hashCode(this.bodyGetterId);
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
        final CustomPreview other = (CustomPreview) obj;
        if (!Objects.equals(this.header, other.header)) {
            return false;
        }
        return Objects.equals(this.bodyGetterId, other.bodyGetterId);
    }
}
