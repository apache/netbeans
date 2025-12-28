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
package org.netbeans.lib.chrome_devtools_protocol;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class CDTUtil {

    private CDTUtil() {
    }

    /**
     * Java created file URIs in the style "file:/PATH" by default, but Node
     * only accepts the form "file:///PATH";
     *
     * @param uri
     * @return
     */
    public static URI toNodeUrl(URI uri) {
        if (uri == null) {
            return null;
        } else if ("file".equals(uri.getScheme())) {
            try {
                return new URI(uri.getScheme(), "", uri.getPath(), null);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
        } else {
            return uri;
        }
    }

    /**
     * Java created file URIs in the style "file:/PATH" by default, but Node
     * only accepts the form "file:///PATH";
     *
     * @param uri
     * @return
     */
    public static String toNodeUrl(String uri) {
        try {
            URI parsedUri = new URI(uri);
            if("file".equals(parsedUri.getScheme())) {
                return toNodeUrl(parsedUri).toString();
            } else {
                return uri;
            }
        } catch (URISyntaxException ex) {
            return uri;
        }
    }
}
