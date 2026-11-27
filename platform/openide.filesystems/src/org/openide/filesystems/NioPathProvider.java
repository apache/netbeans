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
package org.openide.filesystems;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

final class NioPathProvider extends FileSystemProvider {
    private final NioFsForFileSystem fileSystem;

    NioPathProvider(NioFsForFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public String getScheme() {
        return fileSystem.getFileSystem().getRoot().toURI().getScheme();
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return fileSystem;
    }

    @Override
    public Path getPath(URI uri) {
        String path = uri.getPath();
        return fileSystem.getPath(path);
    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options,
            FileAttribute<?>... attrs) throws IOException {
        return NioFileObjectChannel.open(fromPath(path), options, attrs);
    }

    @Override
    public SeekableByteChannel newByteChannel(
            Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return NioFileObjectChannel.open(fromPath(path), options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        NioPathForFileObject nio = fromPath(dir);
        Objects.requireNonNull(filter, "filter");
        FileObject folder = nio.getFileObject();
        if (folder == null || !folder.isValid()) {
            throw new NoSuchFileException(dir.toString());
        }
        if (!folder.isFolder()) {
            throw new NotDirectoryException(dir.toString());
        }
        return new FileObjectDirectoryStream(dir, folder.getChildren(), filter);
    }

    /**
     * Iterates over a snapshot of the directory's children. Unlike the
     * {@link DirectoryStream} contract, multiple independent iterators are
     * allowed, and {@link #close()} does nothing: existing and new iterators
     * remain usable afterward. No directory resource is held open.
     */
    private static final class FileObjectDirectoryStream implements DirectoryStream<Path> {
        private final Path directory;
        private final FileObject[] children;
        private final Filter<? super Path> filter;

        FileObjectDirectoryStream(Path directory, FileObject[] children, Filter<? super Path> filter) {
            this.directory = directory;
            this.children = children;
            this.filter = filter;
        }

        @Override
        public Iterator<Path> iterator() {
            return new FileObjectDirectoryIterator();
        }

        private final class FileObjectDirectoryIterator implements Iterator<Path> {
            private record State(int index, Path next) {}
            private final AtomicReference<State> state = new AtomicReference<>(new State(0, null));

            @Override
            public boolean hasNext() {
                while (true) {
                    State previous = state.get();
                    if (previous.next() != null) {
                        return true;
                    }
                    if (previous.index() == children.length) {
                        return false;
                    }
                    // Filtering is outside the state update and may be retried after a concurrent change.
                    Path candidate = directory.resolve(children[previous.index()].getNameExt());
                    boolean accepted;
                    try {
                        accepted = filter.accept(candidate);
                    } catch (IOException ex) {
                        throw new DirectoryIteratorException(ex);
                    }
                    State next = new State(previous.index() + 1, accepted ? candidate : null);
                    if (state.compareAndSet(previous, next) && accepted) {
                        return true;
                    }
                }
            }

            @Override
            public Path next() {
                while (true) {
                    State previous = state.get();
                    if (previous.next() == null) {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        continue;
                    }
                    State next = new State(previous.index(), null);
                    if (state.compareAndSet(previous, next)) {
                        return previous.next();
                    }
                }
            }
        }

        @Override
        public void close() {
        }
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        NioPathForFileObject nio = fromPath(dir);
        FileObject existing = nio.getFileObject();
        if (existing != null && existing.isValid()) {
            throw new java.nio.file.FileAlreadyExistsException(dir.toString());
        }
        Path parent = dir.getParent();
        NioPathForFileObject nioParent = parent instanceof NioPathForFileObject ? fromPath(parent) : null;
        FileObject folder = nioParent == null ? null : nioParent.getFileObject();
        if (folder == null || !folder.isValid()) {
            throw new NoSuchFileException(dir.toString());
        }
        folder.createFolder(dir.getFileName().toString());
    }

    @Override
    public void delete(Path path) throws IOException {
        FileObject fo = fromPath(path).getFileObject();
        if (fo == null || !fo.isValid()) {
            throw new NoSuchFileException(path.toString());
        }
        fo.delete();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        boolean replaceExisting = false;
        boolean copyAttributes = false;
        for (CopyOption option : options) {
            Objects.requireNonNull(option, "option");
            if (option == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
            } else if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                copyAttributes = true;
            } else if (option != LinkOption.NOFOLLOW_LINKS) {
                throw new UnsupportedOperationException("Unsupported copy option: " + option);
            }
        }

        NioPathForFileObject sourcePath = fromPath(source);
        NioPathForFileObject targetPath = fromPath(target);
        if (sourcePath.getFileSystem() != fileSystem || targetPath.getFileSystem() != fileSystem) {
            throw new ProviderMismatchException();
        }
        FileObject sourceFile = sourcePath.getFileObject();
        if (sourceFile == null || !sourceFile.isValid()) {
            throw new NoSuchFileException(source.toString());
        }
        FileObject targetFile = targetPath.getFileObject();
        if (targetFile != null && targetFile.isValid()) {
            if (sourceFile.equals(targetFile)) {
                return;
            }
            if (!replaceExisting) {
                throw new FileAlreadyExistsException(target.toString());
            }
            if (targetFile.isFolder() && targetFile.getChildren().length != 0) {
                throw new DirectoryNotEmptyException(target.toString());
            }
        }

        Path targetParentPath = target.getParent();
        if (targetParentPath == null) {
            throw new NoSuchFileException(target.toString());
        }
        FileObject targetParent = fromPath(targetParentPath).getFileObject();
        if (targetParent == null || !targetParent.isValid()) {
            throw new NoSuchFileException(targetParentPath.toString());
        }
        if (!targetParent.isFolder()) {
            throw new NotDirectoryException(targetParentPath.toString());
        }
        if (targetFile != null && targetFile.isValid()) {
            targetFile.delete();
        }
        String targetName = target.getFileName().toString();
        FileObject copied;
        if (sourceFile.isFolder()) {
            copied = targetParent.createFolder(targetName);
        } else {
            copied = targetParent.createData(targetName);
            try (InputStream input = sourceFile.getInputStream();
                    OutputStream output = copied.getOutputStream()) {
                input.transferTo(output);
            }
        }
        if (copyAttributes) {
            FileUtil.copyAttributes(sourceFile, copied);
        }
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        FileObject fo = fromPath(source).getFileObject();
        FileObject targetParent = fromPath(target.getParent()).getFileObject();
        if (fo == null || targetParent == null) {
            throw new NoSuchFileException(source.toString());
        }
        Path targetName = target.getFileName();
        if (targetName == null) {
            throw new NoSuchFileException(target.toString());
        }
        String targetFileName = targetName.toString();
        int dot = targetFileName.lastIndexOf('.');
        String name = dot == -1 ? targetFileName : targetFileName.substring(0, dot);
        String ext = dot == -1 ? null : targetFileName.substring(dot + 1);
        FileLock lock = fo.lock();
        try {
            fo.move(lock, targetParent, name, ext);
        } finally {
            lock.releaseLock();
        }
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        NioPathForFileObject first = fromPath(path);
        NioPathForFileObject second = fromPath(path2);
        if (first.getFileObject() != null && second.getFileObject() != null) {
            return first.getFileObject().equals(second.getFileObject());
        }
        return path.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) {
        FileObject fo = fromPath(path).getFileObject();
        return fo != null && fo.getNameExt().startsWith(".");
    }

    @Override
    public FileStore getFileStore(Path path) {
        throw new UnsupportedOperationException("FileStore is not supported");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        FileObject fo = fromPath(path).getFileObject();
        if (fo == null || !fo.isValid()) {
            throw new NoSuchFileException(path.toString());
        }
        for (AccessMode mode : modes) {
            if (mode == AccessMode.WRITE && fo.isReadOnly()) {
                throw new AccessDeniedException(path.toString());
            }
            if (mode == AccessMode.EXECUTE) {
                throw new AccessDeniedException(path.toString());
            }
        }
    }

    @Override
    public <V extends java.nio.file.attribute.FileAttributeView> V getFileAttributeView(
            Path path, Class<V> type, LinkOption... options) {
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(
            Path path, Class<A> type, LinkOption... options) throws IOException {
        if (!type.equals(BasicFileAttributes.class)) {
            throw new UnsupportedOperationException("Unsupported attributes type");
        }
        final FileObject fo = fromPath(path).getFileObject();
        if (fo == null || !fo.isValid()) {
            throw new NoSuchFileException(path.toString());
        }
        final FileTime modified = FileTime.fromMillis(fo.lastModified().getTime());
        final boolean regularFile = fo.isData();
        final boolean directory = fo.isFolder();
        final long size = fo.getSize();
        final BasicFileAttributes attrs = new BasicFileAttributes() {
            @Override
            public FileTime lastModifiedTime() {
                return modified;
            }

            @Override
            public FileTime lastAccessTime() {
                return modified;
            }

            @Override
            public FileTime creationTime() {
                return modified;
            }

            @Override
            public boolean isRegularFile() {
                return regularFile;
            }

            @Override
            public boolean isDirectory() {
                return directory;
            }

            @Override
            public boolean isSymbolicLink() {
                return false;
            }

            @Override
            public boolean isOther() {
                return !regularFile && !directory;
            }

            @Override
            public long size() {
                return size;
            }

            @Override
            public Object fileKey() {
                return fo;
            }
        };
        return type.cast(attrs);
    }

    @Override
    public Map<String, Object> readAttributes(
            Path path, String attributes, LinkOption... options) throws IOException {
        int separator = attributes.indexOf(':');
        if (separator >= 0 && !attributes.substring(0, separator).equals("basic")) {
            throw new UnsupportedOperationException("Unsupported attribute view: " + attributes.substring(0, separator));
        }
        String selection = attributes.substring(separator + 1);
        if (selection.isEmpty()) {
            throw new IllegalArgumentException("No attributes specified");
        }
        BasicFileAttributes attrs = readAttributes(path, BasicFileAttributes.class, options);
        Map<String, Object> all = new HashMap<>();
        all.put("lastModifiedTime", attrs.lastModifiedTime());
        all.put("lastAccessTime", attrs.lastAccessTime());
        all.put("creationTime", attrs.creationTime());
        all.put("size", attrs.size());
        all.put("isRegularFile", attrs.isRegularFile());
        all.put("isDirectory", attrs.isDirectory());
        all.put("isSymbolicLink", attrs.isSymbolicLink());
        all.put("isOther", attrs.isOther());
        all.put("fileKey", attrs.fileKey());
        Map<String, Object> result = new HashMap<>();
        for (String name : selection.split(",", -1)) {
            if (name.equals("*")) {
                result.putAll(all);
            } else if (all.containsKey(name)) {
                result.put(name, all.get(name));
            } else {
                throw new IllegalArgumentException("Unrecognized attribute: " + name);
            }
        }
        return result;
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
            throws IOException {
        FileObject fo = fromPath(path).getFileObject();
        if (fo == null || !fo.isValid()) {
            throw new NoSuchFileException(path.toString());
        }
        throw new UnsupportedOperationException("Unsupported attribute: " + attribute);
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        FileObject fo = fromPath(path).getFileObject();
        if (fo == null || !fo.isValid()) {
            throw new NoSuchFileException(path.toString());
        }
        return fo.getInputStream();
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        Set<OpenOption> selected = new HashSet<>();
        for (OpenOption option : options) {
            Objects.requireNonNull(option, "option");
            if (option == StandardOpenOption.READ) {
                throw new IllegalArgumentException("READ is not supported for output streams");
            }
            selected.add(option);
        }
        if (selected.isEmpty()) {
            selected.add(StandardOpenOption.CREATE);
            selected.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        selected.add(StandardOpenOption.WRITE);
        if (canUseFileObjectOutputStream(selected)) {
            return NioFileObjectChannel.findFile(fromPath(path), selected, true).getOutputStream();
        } else {
            FileChannel channel = newFileChannel(path, selected);
            OutputStream output = Channels.newOutputStream(channel);
            return new FilterOutputStream(output) {
                @Override
                public void flush() throws IOException {
                    channel.force(false);
                    super.flush();
                }
            };
        }
    }

    private static boolean canUseFileObjectOutputStream(Set<OpenOption> options) {
        return overwritesFile(options) && hasOnlyOverwriteOptions(options);
    }

    private static boolean overwritesFile(Set<OpenOption> options) {
        return options.contains(StandardOpenOption.TRUNCATE_EXISTING)
                || options.contains(StandardOpenOption.CREATE_NEW);
    }

    private static boolean hasOnlyOverwriteOptions(Set<OpenOption> options) {
        return Set.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING).containsAll(options);
    }

    private NioPathForFileObject fromPath(Path path) {
        if (!(path instanceof NioPathForFileObject nio)) {
            throw new IllegalArgumentException("Not a NetBeans FileObject path");
        }
        return nio;
    }

}
