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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class NioPathForFileObject implements Path {
    private final FileObject fileObject;
    private final NioFsForFileSystem fileSystem;
    private final String name;
    private final boolean absolute;

    private NioPathForFileObject(FileObject fileObject, NioFsForFileSystem fileSystem, String name) {
        this(fileObject, fileSystem, name, true);
    }

    private NioPathForFileObject(FileObject fileObject, NioFsForFileSystem fileSystem, String name, boolean absolute) {
        this.fileObject = fileObject;
        this.fileSystem = fileSystem;
        this.name = normalize(name);
        this.absolute = absolute;
    }

    static NioPathForFileObject create(FileObject fileObject, NioFsForFileSystem fileSystem) {
        return new NioPathForFileObject(fileObject, fileSystem, fullName(fileObject));
    }

    static NioPathForFileObject create(FileObject fileObject, NioFsForFileSystem fileSystem, String name) {
        return new NioPathForFileObject(fileObject, fileSystem, name);
    }

    static NioPathForFileObject relative(FileObject fileObject, NioFsForFileSystem fileSystem, String name) {
        return new NioPathForFileObject(fileObject, fileSystem, name, false);
    }

    FileObject getFileObject() {
        if (!name.isEmpty()) {
            FileObject resolved = fileSystem.getFileSystem().findResource(name);
            if (resolved != null) {
                return resolved;
            }
        }
        return fileObject;
    }

    @Override
    public java.nio.file.FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public Path getRoot() {
        return absolute ? create(fileSystem.getFileSystem().getRoot(), fileSystem) : null;
    }

    @Override
    public Path getFileName() {
        if (name.isEmpty()) {
            return absolute ? null : this;
        }
        int separator = name.lastIndexOf('/');
        String fileName = separator == -1 ? name : name.substring(separator + 1);
        return fileName.isEmpty() ? null : relative(fileObject, fileSystem, fileName);
    }

    @Override
    public Path getParent() {
        if (name.isEmpty()) {
            return null;
        }
        int separator = name.lastIndexOf('/');
        if (separator == -1) {
            return absolute ? getRoot() : null;
        }
        String parentName = name.substring(0, separator);
        FileObject parent = fileSystem.getFileSystem().findResource(parentName);
        if (parent == null && fileObject != null) {
            parent = fileObject.getParent();
        }
        return new NioPathForFileObject(parent, fileSystem, parentName, absolute);
    }

    @Override
    public int getNameCount() {
        if (name.isEmpty()) {
            return absolute ? 0 : 1;
        }
        int count = 0;
        for (String part : name.split("/")) {
            if (!part.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Path getName(int index) {
        if (index < 0 || index >= getNameCount()) {
            throw new IllegalArgumentException("Illegal name index: " + index);
        }
        if (name.isEmpty()) {
            return this;
        }
        List<String> names = names();
        return relative(null, fileSystem, names.get(index));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > getNameCount() || beginIndex >= endIndex) {
            throw new IllegalArgumentException();
        }
        if (name.isEmpty()) {
            return this;
        }
        List<String> names = names();
        return relative(null, fileSystem, String.join("/", names.subList(beginIndex, endIndex)));
    }

    @Override
    public boolean startsWith(Path other) {
        if (!(other instanceof NioPathForFileObject nio) || nio.fileSystem != fileSystem) {
            return false;
        }
        if (absolute != nio.absolute) {
            return false;
        }
        if (nio.name.isEmpty()) {
            return absolute || name.isEmpty();
        }
        return name.equals(nio.name) || name.startsWith(nio.name + "/");
    }

    @Override
    public boolean startsWith(String other) {
        return startsWith(fileSystem.getPath(other));
    }

    @Override
    public boolean endsWith(Path other) {
        if (!(other instanceof NioPathForFileObject nio) || nio.fileSystem != fileSystem) {
            return false;
        }
        if (nio.absolute || nio.name.isEmpty()) {
            return absolute == nio.absolute && name.equals(nio.name);
        }
        return name.equals(nio.name) || name.endsWith("/" + nio.name);
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(fileSystem.getPath(other));
    }

    @Override
    public Path normalize() {
        List<String> normalized = new ArrayList<>();
        for (String part : names()) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            }
            if (part.equals("..")) {
                if (!normalized.isEmpty() && !normalized.get(normalized.size() - 1).equals("..")) {
                    normalized.remove(normalized.size() - 1);
                } else if (!absolute) {
                    normalized.add(part);
                }
            } else {
                normalized.add(part);
            }
        }
        String normalizedName = String.join("/", normalized);
        FileObject fo = fileSystem.getFileSystem().findResource(normalizedName);
        return new NioPathForFileObject(fo, fileSystem, normalizedName, absolute);
    }

    @Override
    public Path resolve(Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        if (!(other instanceof NioPathForFileObject nio)) {
            return resolve(other.toString());
        }
        return resolve(nio.name);
    }

    @Override
    public Path resolve(String other) {
        if (other.isEmpty()) {
            return this;
        }
        Path otherPath = fileSystem.getPath(other);
        if (otherPath.isAbsolute()) {
            return otherPath;
        }
        String result = name.isEmpty() ? normalize(other) : normalize(name + "/" + other);
        FileObject fo = fileSystem.getFileSystem().findResource(result);
        return new NioPathForFileObject(fo, fileSystem, result, absolute);
    }

    @Override
    public Path resolveSibling(Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        Path parent = getParent();
        return parent == null ? other : parent.resolve(other);
    }

    @Override
    public Path resolveSibling(String other) {
        return resolveSibling(fileSystem.getPath(other));
    }

    @Override
    public Path relativize(Path other) {
        if (!(other instanceof NioPathForFileObject nio) || nio.fileSystem != fileSystem) {
            throw new IllegalArgumentException("Cannot relativize different file systems");
        }
        if (absolute != nio.absolute) {
            throw new IllegalArgumentException("Cannot relativize absolute and relative paths");
        }
        List<String> base = names();
        List<String> target = nio.names();
        int common = 0;
        while (common < base.size() && common < target.size() && base.get(common).equals(target.get(common))) {
            common++;
        }
        List<String> result = new ArrayList<>();
        for (int i = common; i < base.size(); i++) {
            result.add("..");
        }
        result.addAll(target.subList(common, target.size()));
        String relative = String.join("/", result);
        return relative(null, fileSystem, relative.isEmpty() ? "" : relative);
    }

    @Override
    public URI toUri() {
        FileObject root = fileSystem.getFileSystem().getRoot();
        FileObject resolved = name.isEmpty() ? root : fileSystem.getFileSystem().findResource(name);
        if (resolved != null) {
            return resolved.toURI();
        }
        String rootUri = root.toURI().toString();
        try {
            String encodedPath = new URI(null, null, "/" + name, null).getRawPath().substring(1);
            return URI.create(rootUri + (rootUri.endsWith("/") ? "" : "/") + encodedPath);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Cannot represent path as URI: " + name, ex);
        }
    }

    @Override
    public Path toAbsolutePath() {
        if (absolute) {
            return this;
        }
        FileObject fo = name.isEmpty() ? fileSystem.getFileSystem().getRoot()
                : fileSystem.getFileSystem().findResource(name);
        return create(fo, fileSystem, name);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        NioPathForFileObject normalized = (NioPathForFileObject) toAbsolutePath().normalize();
        FileObject resolved = normalized.getFileObject();
        if (resolved == null || !resolved.isValid()) {
            throw new NoSuchFileException(toString());
        }
        return normalized;
    }

    @Override
    public File toFile() {
        if (fileObject != null) {
            File disk = FileUtil.toFile(fileObject);
            if (disk != null) {
                return disk;
            }
        }
        throw new UnsupportedOperationException("Virtual FileObject has no java.io.File");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        if (!(watcher instanceof NioWatchService nioWatcher)) {
            throw new IllegalArgumentException("Unsupported WatchService");
        }
        return nioWatcher.register(this, events);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return register(watcher, events, new WatchEvent.Modifier[0]);
    }

    @Override
    public Iterator<Path> iterator() {
        List<Path> paths = new ArrayList<>();
        if (name.isEmpty() && !absolute) {
            paths.add(this);
        }
        for (String part : names()) {
            paths.add(relative(null, fileSystem, part));
        }
        return paths.iterator();
    }

    @Override
    public int compareTo(Path other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return absolute ? (name.isEmpty() ? "/" : "/" + name) : name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NioPathForFileObject other)) {
            return false;
        }
        return fileSystem == other.fileSystem && absolute == other.absolute && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, name, absolute);
    }

    private List<String> names() {
        List<String> names = new ArrayList<>();
        if (!name.isEmpty()) {
            for (String part : name.split("/")) {
                if (!part.isEmpty()) {
                    names.add(part);
                }
            }
        }
        return names;
    }

    private static String normalize(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '\\') {
                c = '/';
            }
            if (c != '/' || (!result.isEmpty() && result.charAt(result.length() - 1) != '/')) {
                result.append(c);
            }
        }
        if (!result.isEmpty() && result.charAt(result.length() - 1) == '/') {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    private static String fullName(FileObject fileObject) {
        if (fileObject == null) {
            return "";
        }
        List<String> names = new ArrayList<>();
        FileObject current = fileObject;
        while (current != null) {
            String name = current.getNameExt();
            if (!name.isEmpty()) {
                names.add(0, name);
            }
            current = current.getParent();
        }
        return String.join("/", names);
    }

    static NioPathForFileObject fromInvalidPath(String name, NioFsForFileSystem fileSystem) {
        return new NioPathForFileObject(null, fileSystem, name);
    }
}
