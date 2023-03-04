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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.spi.ArchiveRootProvider;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=ArchiveRootProvider.class, position = 1000)
public class NBJRTArchiveRootProvider implements ArchiveRootProvider {

    private static final Logger LOG = Logger.getLogger(NBJRTArchiveRootProvider.class.getName());

    @Override
    public boolean isArchiveFile(@NonNull final URL url, final boolean strict) {
        final FileObject fo = URLMapper.findFileObject(url);
        return fo == null ? false : isArchiveFile(fo, strict);
    }

    @Override
    public boolean isArchiveFile(FileObject fo, boolean strict) {
        if (!fo.isFolder()) {
            return false;
        }
        final File file = FileUtil.toFile(fo);
        return file == null ? false : NBJRTUtil.getNIOProvider(file) != null;
    }

    @Override
    public boolean isArchiveArtifact(@NonNull final URL url) {
        return NBJRTUtil.PROTOCOL.equals(url.getProtocol());
    }

    @Override
    @CheckForNull
    public URL getArchiveFile(@NonNull final URL url) {
        final Pair<URL,String> p = NBJRTUtil.parseURL(url);
        return p != null ? p.first() : null;
    }

    @Override
    @NonNull
    public URL getArchiveRoot(@NonNull final URL url) {
        try {
            return new URL(String.format("%s:%s!/", //NOI18N
                    NBJRTUtil.PROTOCOL,
                    url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
