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
package org.netbeans.modules.java.source.parsing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author Tomas Zezula
 */
public abstract class AbstractPathArchive implements Archive {
    private static final Logger LOG = Logger.getLogger(AbstractPathArchive.class.getName());
    protected final Path root;
    protected final String rootURI;
    protected final char separator;
    private volatile Boolean multiRelease;

    protected AbstractPathArchive(
            @NonNull final Path root,
            @NullAllowed final URI rootURI) {
        assert root != null;
        this.root = root;
        this.rootURI = rootURI == null ? null : rootURI.toString();
        final String separator = root.getFileSystem().getSeparator();
        if (separator.length() != 1) {
            throw new IllegalArgumentException("Multi character separators are unsupported");
        }
        this.separator = separator.charAt(0);
    }

    @Override
    public void clear() {
        multiRelease = null;
    }

    @Override
    public boolean isMultiRelease() {
        Boolean res = multiRelease;
        if (res == null) {
            res = Boolean.FALSE;
            if (FileObjects.JAR.equals(root.getFileSystem().provider().getScheme())) {
                try {
                    final JavaFileObject jfo = getFile("META-INF/MANIFEST.MF"); //NOI18N
                    if (jfo != null) {
                        try(final InputStream in = new BufferedInputStream(jfo.openInputStream())) {
                            res = FileObjects.isMultiVersionArchive(in);
                        }
                    }
                } catch (IOException ioe) {
                    LOG.log(
                            Level.WARNING,
                            "Cannot read: {0} manifest",    //NOI18N
                            rootURI.toString());
                }
            }
            multiRelease = res;
        }
        return res;
    }
}
