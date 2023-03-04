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

package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 * @author  Tomas Zezula
 */
public class NBJRTURLStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new NBJRTConnection(u);
    }

    @ServiceProvider(service=URLStreamHandlerFactory.class)
    public static final class FactoryImpl implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (NBJRTUtil.PROTOCOL.equals(protocol)) { //NOI18N
                return new NBJRTURLStreamHandler();
            }
            return null;
        }

    }

    private static final class NBJRTConnection extends URLConnection {

        private volatile Path path;

        NBJRTConnection(@NonNull final URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            final Pair<URL,String> parsed = NBJRTUtil.parseURL(url);
            if (parsed == null) {
                throw new IOException(String.format(
                    "Invalid URL: %s",  //NOI18N
                    url));
            }
            final NBJRTFileSystem fs = NBJRTFileSystemProvider.getDefault().getFileSystem(parsed.first());
            if (fs == null) {
                throw new IOException(String.format(
                    "Cannot create file system for: %s",    //NOI18N
                    url));
            }
            final Path p = fs.getPath(parsed.second());
            if (p == null) {
                throw new IOException(String.format(
                    "Cannot resolve image path for: %s",    //NOI18N
                    url));
            }
            path = p;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            final Path _path = path;
            if (_path == null) {
                throw new IOException(String.format(
                    "Cannot resolve: %s",   //NOI18N
                    getURL()));
            }
            return Files.newInputStream(path);
        }

    }

}
