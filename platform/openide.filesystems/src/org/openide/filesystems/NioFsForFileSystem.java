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
 * KIND, ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.filesystems;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

final class NioFsForFileSystem extends FileSystem {
    private final org.openide.filesystems.FileSystem fileSystem;
    private final NioPathProvider provider;

    NioFsForFileSystem(org.openide.filesystems.FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.provider = new NioPathProvider(this);
    }

    org.openide.filesystems.FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return fileSystem.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singletonList(NioPathForFileObject.create(fileSystem.getRoot(), this));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return Collections.emptyList();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Collections.singleton("basic");
    }

    @Override
    public Path getPath(String first, String... more) {
        StringBuilder value = new StringBuilder(first);
        for (String part : more) {
            if (!value.isEmpty() && value.charAt(value.length() - 1) != '/') {
                value.append('/');
            }
            value.append(part);
        }
        String normalized = normalizedPath(value.toString());
        FileObject fo = fileSystem.findResource(normalized);
        String name = normalizeName(value.toString());
        if (!value.toString().startsWith("/")) {
            return NioPathForFileObject.relative(fo, this, name);
        }
        return NioPathForFileObject.create(fo, this, name);
    }

    private String normalizedPath(String value) {
        if (value.isEmpty()) {
            return "";
        }
        String result = value.replace('\\', '/');
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    private String normalizeName(String value) {
        String result = value.replace('\\', '/');
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("PathMatcher is not supported");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("User lookup is not supported");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return new NioWatchService();
    }
}
