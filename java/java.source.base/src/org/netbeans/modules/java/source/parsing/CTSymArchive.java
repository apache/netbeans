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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Specialized {@link CachingArchive} for ctsym file.
 * It provides API classes from ct.sym and non API classes from original archive file.
 * @author Tomas Zezula
 */
public class CTSymArchive extends CachingArchive {

    private static final Logger LOG = Logger.getLogger(CTSymArchive.class.getName());

    private final File ctSym;
    private final String pathToRootInCtSym;
    private ZipFile zipFile;
    private Map<String,Set<String>> pkgs;

    CTSymArchive(
        @NonNull final File archive,
        @NullAllowed final String pathToRootInArchive,
        @NonNull final File ctSym,
        @NullAllowed final String pathToRootInCtSym) {
        super(archive, pathToRootInArchive, true);
        this.ctSym = ctSym;
        this.pathToRootInCtSym = pathToRootInCtSym;
    }

    @Override
    protected void beforeInit() throws IOException {
        zipFile = new ZipFile(ctSym);
        pkgs = new HashMap<>();
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            final String name = entry.getName();
            String dirname;
            String basename;
            if (pathToRootInCtSym != null) {
                if (!name.startsWith(pathToRootInCtSym)) {
                    continue;
                }
                final int i = name.lastIndexOf(FileObjects.NBFS_SEPARATOR_CHAR);
                dirname = i < pathToRootInCtSym.length() ?
                    "" :    //NOI18N
                    name.substring(pathToRootInCtSym.length(), i);
                basename = name.substring(i+1);
            } else {
                final int i = name.lastIndexOf(FileObjects.NBFS_SEPARATOR_CHAR);
                dirname = i == -1 ? "" : name.substring(0, i);  //NOI18N
                basename = name.substring(i+1);
            }
            Set<String> content = pkgs.get(dirname);
            if (content == null) {
                pkgs.put(dirname, content = new HashSet<>());
            }
            content.add(basename);
        }
    }

    @Override
    protected short getFlags(@NonNull final String dirname) throws IOException {
        boolean isPublic = pkgs.containsKey(dirname);
        LOG.log(
            Level.FINE,
            "Package: {0} is public: {1}", //NOI18N
            new Object[]{
                dirname,
                isPublic
            });
        return (short) (isPublic ? 0 : 1);
    }

    @Override
    protected boolean includes(int flags, String folder, String name) {
        if (flags == 0) {
            final Set<String> content = pkgs.get(folder);
            return content == null ?
                    false :
                    content.contains(name);
        } else {
            return super.includes(flags, folder, name);
        }
    }

    @Override
    protected void afterInit(final boolean success) throws IOException {
        pkgs = null;
    }

    @Override
    protected ZipFile getArchive(final short flags) {
        return flags == 0 ?
            zipFile :
            super.getArchive(flags);
    }

    @Override
    protected String getPathToRoot(final short flags) {
        return flags == 0 ?
            pathToRootInCtSym :
            super.getPathToRoot(flags);
    }
}
