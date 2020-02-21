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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public class RemoteFileUrlMapper extends URLMapper {

    @Override
    public FileObject[] getFileObjects(URL url) {
        if (url.getProtocol().equals(RemoteFileURLStreamHandler.PROTOCOL)) {
            ExecutionEnvironment env;
            String user = unescapeUser(url.getUserInfo());
            String host = unescapeHost(url.getHost());
            if (user != null) {
                env = ExecutionEnvironmentFactory.createNew(user, host, url.getPort());
            } else {
                RemoteLogger.assertTrue(false, "Trying to access remote file system without user name: {0}", url);
                env = RemoteFileSystemUtils.getExecutionEnvironment(host, url.getPort());
                if (env == null) {
                    user = System.getProperty("user.name");
                    if (user != null) {
                        env = ExecutionEnvironmentFactory.createNew(user, host, url.getPort());
                    }
                }
            }
            if (env != null) {
                RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                FileObject fo = fs.findResource(unescapePath(url));
                return new FileObject[] { fo };
            }
        }
        return null;
    }

    @Override
    public URL getURL(FileObject fo, int type) {
        if (fo instanceof RemoteFileObject) {
            RemoteFileObject rfo = (RemoteFileObject) fo;
            try {
                ExecutionEnvironment env = rfo.getExecutionEnvironment();
                return toURL(env, rfo.getPath(), rfo.isFolder());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public static URI toURI(ExecutionEnvironment env, String path, boolean folder) throws URISyntaxException {
        return new URI(toURLString(env, path, folder));
    }
    
    public static URL toURL(ExecutionEnvironment env, String path, boolean folder) throws MalformedURLException {
        return new URL(toURLString(env, path, folder));
    }

    private static String toURLString(ExecutionEnvironment env, String path, boolean folder) {
        /*
         * Prepare URL here as a string to be used in the URL(String spec)
         * constructor as it works with userinfo as expected (ipv6 address case).
         * URL(String protocol, String host, int port, String file) cannot be
         * used here, as 'host' should contain only host information, without 
         * username/password etc... 
         */
        StringBuilder sb = new StringBuilder(RemoteFileURLStreamHandler.PROTOCOL);
        sb.append("://"); // NOI18N
        sb.append(escapeUserIfNeed(env.getUser())).append('@').append(escapeHostIfNeed(env.getHost()));
        sb.append(':').append(env.getSSHPort()).append(PathUtilities.escapePathForUseInURL(path));
        if (folder && !(path.endsWith("/"))) { // NOI18N
            sb.append('/'); // NOI18N
        }
        return sb.toString();
    }

    private static String unescapePath(URL url) {
        String path = url.getFile();
        if (path.contains("%")) { //NOI18N
            try {
                return url.toURI().getPath();
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return path;
    }
    
    public static String escapeUserIfNeed(String user) {
        return PathUtilities.escapeHostOrUserForUseInURL(user);
    }

    public static String escapeHostIfNeed(String host) {
        return PathUtilities.escapeHostOrUserForUseInURL(host);
    }

    public static String unescapeUser(String user) {
        return PathUtilities.unescapePath(user);
    }

    public static String unescapeHost(String host) {
        return PathUtilities.unescapePath(host);
    }
}
