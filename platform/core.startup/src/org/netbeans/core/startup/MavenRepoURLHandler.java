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
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.openide.util.URLStreamHandlerRegistration;

@URLStreamHandlerRegistration(position = 99999, protocol="m2")
public final class MavenRepoURLHandler extends URLStreamHandler {
    private static final URI CENTRAL_REPO_URI;
    static {
        try {
            CENTRAL_REPO_URI = new URI("https://repo.maven.apache.org/maven2/");
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        String path = u.getPath();
        if (!path.startsWith("/")) {
            throw new IOException(path);
        }
        String stuff = path.substring(1);
        File m2Repo = new File(new File(new File(System.getProperty("user.home")), ".m2"), "repository");
        String[] pieces = stuff.split(":");
        URI uri = null;
        if (pieces.length >= 4) {
            uri = relativeMavenURI(m2Repo.toURI(), pieces);
        } else {
            throw new IOException(stuff);
        }
        File f = new File(uri);
        if (uri == null || !f.isFile()) {
            uri = relativeMavenURI(CENTRAL_REPO_URI, pieces);
        }
        return uri.toURL().openConnection();

    }

    private URI relativeMavenURI(URI m2Repo, String[] pieces) {
        URI groupId = m2Repo.resolve(pieces[0].replace('.', '/') + '/');
        URI dir = groupId.resolve(pieces[1] + '/').resolve(pieces[2] + '/');
        String fileName = pieces[1] + '-' + pieces[2];
        if (pieces.length == 5) {
            fileName += "-" + pieces[4];
        }
        fileName += "." + pieces[3];
        URI uri = dir.resolve(fileName);
        return uri;
    }
}
