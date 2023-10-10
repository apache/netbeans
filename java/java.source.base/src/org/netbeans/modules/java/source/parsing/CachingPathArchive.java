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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import static org.netbeans.modules.java.source.parsing.FileObjects.convertPackage2Folder;

/**
 *
 * @author Tomas Zezula
 */
public class CachingPathArchive extends AbstractPathArchive {
    private static final int[] EMPTY_FOLDER = new int[0];
    private static final byte[] EMPTY_NAMES = new byte[0];

    //@GuardedBy("this")
    private Map<String,int[]> data;
    //@GuardedBy("this")
    private byte[] packedNames;
    //@GuardedBy("this")
    private int nameIndex;

    CachingPathArchive(
            @NonNull final Path root,
            @NullAllowed final URI rootURI) {
        super(root, rootURI);
        this.packedNames = EMPTY_NAMES;
    }

    @Override
    @NonNull
    public synchronized Iterable<JavaFileObject> getFiles(
            @NonNull String folderName,
            @NullAllowed final ClassPath.Entry entry,
            @NullAllowed Set<JavaFileObject.Kind> kinds,
            @NullAllowed JavaFileFilterImplementation filter,
            final boolean recursive) throws IOException {
        init();
        if (recursive) {
            final List<JavaFileObject> collector = new ArrayList<>();
            data.entrySet().stream()
                    .filter((e) -> {
                        if (folderName.isEmpty()) {
                            return true;
                        }
                        final String fld = e.getKey();
                        return fld.startsWith(folderName) &&
                            (fld.length() == folderName.length() || fld.charAt(folderName.length()) == FileObjects.NBFS_SEPARATOR_CHAR);
                    })
                    .forEach((e) -> {
                        listFolder(e.getValue(), e.getKey(), kinds, collector);
                    });
            return collector;
        } else {
            final int[] pkgContent = data.get(folderName);
            return pkgContent == null || pkgContent == EMPTY_FOLDER ?
                Collections.emptyList() :
                listFolder(pkgContent, folderName, kinds, null);
        }
    }

    @Override
    @CheckForNull
    public synchronized JavaFileObject getFile(@NonNull final String name) throws IOException {
        init();
        final String[] fnPair = FileObjects.getFolderAndBaseName(name, FileObjects.NBFS_SEPARATOR_CHAR);
        final int[] pkgContent = data.get(fnPair[0]);
        if (pkgContent != null) {
            for (int i = 0; i < pkgContent.length; i+=2) {
                final String baseName = getName(pkgContent[i], pkgContent[i+1]);
                if (fnPair[1].equals(baseName)) {
                    return FileObjects.pathFileObject(
                            fnPair[0],
                            fnPair[1],
                            root,
                            rootURI,
                            null);
                }
            }
        }
        return null;
    }

    @Override
    @CheckForNull
    public synchronized URI getDirectory(String dirName) throws IOException {
        init();

        if (!data.containsKey(dirName)) {
            return null;
        }

        final char sep = root.getFileSystem().getSeparator().charAt(0);
        Path resolved = root.resolve(convertPackage2Folder(dirName, sep));

        return resolved.toUri();
    }

    @Override
    public synchronized void clear() {
        super.clear();
        data = null;
        packedNames = EMPTY_NAMES;
        nameIndex = 0;
    }

    @Override
    public JavaFileObject create(String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Write not supported");   //NOI18N
    }

    private int putName(@NonNull final byte[] name) {
        if (packedNames.length < nameIndex + name.length) {
            packedNames = Arrays.copyOfRange(packedNames, 0, name.length + packedNames.length<<1);
        }
        final int start = nameIndex;
        System.arraycopy(name, 0, packedNames, start, name.length);
        nameIndex+=name.length;
        return start;
    }

    private String getName(final int start, final int len) {
        return new String(packedNames, start, len, StandardCharsets.UTF_8);
    }

    @NonNull
    private String getResourceName(@NonNull final Path path) {
        return root.relativize(path).toString().replace(separator, FileObjects.NBFS_SEPARATOR_CHAR);
    }

    private void init () throws IOException {
        assert Thread.holdsLock(this);
        if (data == null) {
            data = new HashMap<>();
            Files.walkFileTree(root, new FileVisitor<Path>() {
                private final Deque<State> states = new ArrayDeque<>();

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    states.offer(new State());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final State state = states.getLast();
                    int[] cf = state.currentFolder;
                    int co = state.currentOffset;
                    if (cf.length < co+2) {
                        cf = state.currentFolder = Arrays.copyOfRange(cf, 0, 2 + cf.length<<1);
                    }
                    final byte[] name = file.getFileName().toString().getBytes(StandardCharsets.UTF_8);
                    cf[co] = putName(name);
                    cf[co+1] = name.length;
                    state.currentOffset+=2;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    final State state = states.removeLast();

                    data.put(
                        getResourceName(dir),
                        Arrays.copyOfRange(state.currentFolder, 0, state.currentOffset));

                    return FileVisitResult.CONTINUE;
                }
            });
            packedNames = Arrays.copyOfRange(packedNames, 0, nameIndex);
        }
    }

    private List<JavaFileObject> listFolder(
        @NonNull final int[] pkgContent,
        @NonNull final String folderName,
        @NullAllowed final Set<JavaFileObject.Kind> kinds,
        @NullAllowed List<JavaFileObject> collector) {
        if (collector == null) {
            collector = new ArrayList<>(pkgContent.length>>>1);
        }
        for (int i = 0; i < pkgContent.length; i+=2) {
            final String name = getName(pkgContent[i], pkgContent[i+1]);
            if (kinds == null || kinds.contains(FileObjects.getKind(FileObjects.getExtension(name)))) {
                collector.add(FileObjects.pathFileObject(
                        folderName,
                        name,
                        root,
                        rootURI,
                        null));
            }
        }
        return collector;
    }

    private static class State {
        int[] currentFolder;
        int currentOffset;
        State() {
            currentFolder = EMPTY_FOLDER;
        }
    }

}
