/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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
        try {
            return new String(packedNames, start, len, "UTF-8");    //NOI18N
        } catch (UnsupportedEncodingException ue) {
            throw new IllegalStateException(ue);
        }
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
                    final byte[] name = file.getFileName().toString().getBytes("UTF-8");
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
                    if (state.currentFolder != EMPTY_FOLDER) {
                        data.put(
                            getResourceName(dir),
                            Arrays.copyOfRange(state.currentFolder, 0, state.currentOffset));
                    }
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
