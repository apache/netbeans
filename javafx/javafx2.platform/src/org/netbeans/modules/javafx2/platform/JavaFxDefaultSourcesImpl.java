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

package org.netbeans.modules.javafx2.platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.spi.J2SEPlatformDefaultSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(
        service =J2SEPlatformDefaultSources.class,
        position = 200,
        path = "org-netbeans-api-java/platform/j2seplatform/defaultSourcesProviders")
public class JavaFxDefaultSourcesImpl implements J2SEPlatformDefaultSources {

    private static final String FX_SOURCES = "javafx-src.zip";  //NOI18N

    @Override
    @NonNull
    public List<URI> getDefaultSources(@NonNull final JavaPlatform platform) {
        final Collection<? extends FileObject> fos = platform.getInstallFolders();
        if (fos.isEmpty()) {
            return Collections.emptyList();
        }
        final File javaHome = FileUtil.toFile(fos.iterator().next());
        if (javaHome == null) {
            return Collections.emptyList();
        }
        return getFxSources(javaHome);
    }

    private static List<URI> getFxSources(@NonNull final File javaHome) {
        try {
            final File f = new File (javaHome, FX_SOURCES);
            if (f.exists() && f.canRead()) {
                final URL url = FileUtil.getArchiveRoot(Utilities.toURI(f).toURL());
                final URI uri = url.toURI();
                return Collections.singletonList (uri);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        return Collections.emptyList();
    }

}
