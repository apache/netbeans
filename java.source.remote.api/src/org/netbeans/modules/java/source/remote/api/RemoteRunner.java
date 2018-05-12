/**
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
package org.netbeans.modules.java.source.remote.api;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.java.source.remote.api.Parser.Config;
import org.netbeans.modules.java.source.remoteapi.RemoteProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class RemoteRunner {
    
    public static @CheckForNull RemoteRunner create(FileObject source) {
        URI base = RemoteProvider.getRemoteURL(source);
        
        return base != null ? new RemoteRunner(base) : null;
    }

    private static final Gson gson = new Gson();

    private final URI base;

    private RemoteRunner(URI base) {
        this.base = base;
    }
    
    public <T> @CheckForNull T readAndDecode(Config conf, String path, Class<T> decodeType, String... extraParams) throws IOException {
        StringBuilder targetPath = new StringBuilder();
        
        targetPath.append(path);
        targetPath.append("?parser-config=");
        targetPath.append(URLEncoder.encode(gson.toJson(conf), "UTF-8"));
        
        for (String p : extraParams) {
            targetPath.append('&');
            int eq = p.indexOf('=');
            if (eq != (-1)) {
                p = p.substring(0, eq + 1) + URLEncoder.encode(p.substring(eq + 1), "UTF-8");
            }
            targetPath.append(p);
        }

        URI query = base.resolve(targetPath.toString());

        HttpURLConnection conn = (HttpURLConnection) query.toURL().openConnection();
        
        int code = conn.getResponseCode();

        try (InputStream in = conn.getInputStream();
             Reader r = new InputStreamReader(in, "UTF-8")) {
            return gson.fromJson(r, decodeType);
        }
    }
}
