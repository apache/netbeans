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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.BaseUtilities;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public final class NBJRTUtil {

    public static final String PROTOCOL = "nbjrt"; //NOI18N
    private static final String NIO_PROVIDER = "jrt-fs.jar";    //NOI18N
    private static final Logger LOG = Logger.getLogger(NBJRTUtil.class.getName());

    private NBJRTUtil() {
        throw new IllegalStateException("No instance allowed");
    }

    @CheckForNull
    public static URI getImageURI(@NonNull final File jdkHome) {
        final File jrtFsJar = getNIOProvider(jdkHome);
        try {
            return jrtFsJar == null ?
                null :
                createURI(jdkHome, "");  //NOI18N
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    @NonNull
    public static URI createURI(
        @NonNull final File jdkHome,
        @NonNull final String pathInImageFile) throws URISyntaxException {
         return new URI(String.format(
            "%s:%s!/%s",  //NOI18N
            PROTOCOL,
            BaseUtilities.toURI(jdkHome).toString(),
            pathInImageFile));
    }

    @CheckForNull
    public static Pair<URL,String> parseURI(@NonNull final URI uri) {
        try {
            return parseURL(uri.toURL());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @CheckForNull
    static Pair<URL,String> parseURL(@NonNull final URL url) {
        if (PROTOCOL.equals(url.getProtocol())) {
            final String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N
            if (index >= 0) {
                if (!path.startsWith("file:")) {    //NOI18N
                    throw new IllegalArgumentException(String.format(
                        "Invalid %s URI: %s",   //NOI18N
                        PROTOCOL,
                        url.toExternalForm()
                    ));
                }
                String jdkPath = null;
                try {
                    jdkPath = path.substring(0, index);
                    if (jdkPath.indexOf("file://") > -1 && jdkPath.indexOf("file:////") == -1) {  //NOI18N
                        /* Replace because JDK application classloader wrongly recognizes UNC paths. */
                        jdkPath = jdkPath.replaceFirst("file://", "file:////");  //NOI18N
                    }
                    final URL archiveFile = new URL(jdkPath);
                    final String pathInArchive = path.substring(index + 2);
                    return Pair.of(archiveFile, pathInArchive);
                } catch (MalformedURLException mue) {
                    LOG.log(
                        Level.WARNING,
                        "Invalid URL ({0}): {1}, jdkHome: {2}", //NOI18N
                        new Object[] {
                            mue.getMessage(),
                            url.toExternalForm(),
                            jdkPath
                        });
                }
            } else {
                LOG.log(
                    Level.WARNING,
                    "Invalid {0} URI: {1}",   //NOI18N
                    new Object[] {
                    PROTOCOL,
                    url});
            }
        }
        return null;
    }

    @CheckForNull
    static File getNIOProvider(@NonNull final File jdkHome) {
        File jrtFsJar = new File(jdkHome, String.format(
                "lib%s%s",   //NOI18N
                File.separator,
                NIO_PROVIDER));
        if (jrtFsJar.exists()) {
            return jrtFsJar;
        }
        jrtFsJar = new File(jdkHome, NIO_PROVIDER);
        return jrtFsJar.exists() ?
            jrtFsJar :
            null;
    }
}
