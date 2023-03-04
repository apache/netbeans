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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public final class NBJRTFileSystemProvider {
    private static final Logger LOG = Logger.getLogger(NBJRTFileSystemProvider.class.getName());
    private static final NBJRTFileSystemProvider INSTANCE = new NBJRTFileSystemProvider();

    //@GuardedBy("javaHome2JRTFS")
    private final Map<File, NBJRTFileSystem> javaHome2JRTFS = Collections.synchronizedMap(new HashMap<File,NBJRTFileSystem>());

    private NBJRTFileSystemProvider() {}

    @CheckForNull
    NBJRTFileSystem getFileSystem(@NonNull final URL jdkHome) {
        try {
            return getFileSystem(BaseUtilities.toFile(jdkHome.toURI()));
        } catch (URISyntaxException e) {
            LOG.log(
                    Level.WARNING,
                    "Invalid URI: {0}",     //NOI18N
                    jdkHome);
            return null;
        }
    }

    @CheckForNull
    NBJRTFileSystem getFileSystem(@NonNull final File jdkHome) {
        synchronized (javaHome2JRTFS) {
            NBJRTFileSystem fs = javaHome2JRTFS.get(jdkHome);
            if (fs == null && !javaHome2JRTFS.containsKey(jdkHome)) {
                fs = NBJRTFileSystem.create(jdkHome);
                javaHome2JRTFS.put(jdkHome, fs);
            }
            return fs;
        }
    }

    public static NBJRTFileSystemProvider getDefault() {
        return INSTANCE;
    }
}
