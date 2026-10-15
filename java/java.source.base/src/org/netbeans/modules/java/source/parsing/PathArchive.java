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
package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;

/**
 *
 * @author Tomas Zezula
 */
public final class PathArchive extends AbstractPathArchive {

    

    PathArchive(
            @NonNull final Path root,
            @NullAllowed final URI rootURI) {
        super(root, rootURI);
    }

    @Override
    @NonNull
    public Iterable<JavaFileObject> getFiles(
            @NonNull String folderName,
            @NullAllowed final ClassPath.Entry entry,
            @NullAllowed final Set<JavaFileObject.Kind> kinds,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean recursive) throws IOException {
        if (separator != FileObjects.NBFS_SEPARATOR_CHAR) {
            folderName = folderName.replace(FileObjects.NBFS_SEPARATOR_CHAR, separator);
        }
        final Path target = root.resolve(folderName);
        final List<JavaFileObject> res = new ArrayList<>();
        try (final Stream<Path> s = recursive ? Files.walk(target, FileVisitOption.FOLLOW_LINKS) : Files.list(target)) {
            s.filter((p)->{
                return (kinds == null || kinds.contains(FileObjects.getKind(FileObjects.getExtension(p.getFileName().toString()))))
                    && Files.isRegularFile(p);
            })
            .forEach((p)->{res.add(FileObjects.pathFileObject(p, root, rootURI, null));});
        }
        return Collections.unmodifiableCollection(res);
    }

    @Override
    @CheckForNull
    public JavaFileObject getFile(@NonNull String name) throws IOException {
        if (separator != FileObjects.NBFS_SEPARATOR_CHAR) {
            name = name.replace(FileObjects.NBFS_SEPARATOR_CHAR, separator);
        }
        final Path target = root.resolve(name);
        return Files.exists(target) ?
                FileObjects.pathFileObject(target, root, rootURI, null) :
                null;
    }

    @Override
    public URI getDirectory(String dirName) throws IOException {
        if (separator != FileObjects.NBFS_SEPARATOR_CHAR) {
            dirName = dirName.replace(FileObjects.NBFS_SEPARATOR_CHAR, separator);
        }
        final Path target = root.resolve(dirName);
        return Files.isDirectory(target) ?
                target.toUri() :
                null;
    }

    @Override
    public JavaFileObject create(String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Write not supported");   //NOI18N
    }
}
