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
package org.netbeans.modules.acp.api;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class AcpWorkspaceFiles {

    private AcpWorkspaceFiles() {
    }

    static boolean isSupported() {
        Path root = Path.of("").toAbsolutePath().getRoot();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            return stream instanceof SecureDirectoryStream<?>;
        } catch (IOException ex) {
            return false;
        }
    }

    static Path resolve(List<Path> roots, Path requested, boolean allowNewFile) throws IOException {
        if (!requested.isAbsolute() || requested.getParent() == null) {
            throw new AccessDeniedException(requested.toString(), null, "An absolute file path is required");
        }
        Path resolved;
        try {
            resolved = requested.toRealPath();
        } catch (NoSuchFileException ex) {
            if (!allowNewFile || requested.getFileName().toString().equals("..")
                    || requested.getFileName().toString().equals(".")) {
                throw ex;
            }
            resolved = requested.getParent().toRealPath().resolve(requested.getFileName());
        }
        for (Path root : roots) {
            if (resolved.startsWith(root) && !resolved.equals(root)) {
                return resolved;
            }
        }
        throw new AccessDeniedException(requested.toString(), null, "Path is outside the session directories");
    }

    static String read(Path resolved, int start, Integer limit) throws IOException {
        return withParent(resolved, (directory, name) -> {
            requireRegularFile(directory, name);
            return read(directory, name, start, limit);
        });
    }

    private static String read(SecureDirectoryStream<Path> directory, Path name, int start,
            Integer limit) throws IOException {
        try (SeekableByteChannel channel = directory.newByteChannel(name,
                Set.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS));
                Reader reader = Channels.newReader(channel, StandardCharsets.UTF_8)) {
            if (start == 1 && limit == null) {
                StringWriter content = new StringWriter();
                reader.transferTo(content);
                return content.toString();
            }
            StringBuilder content = new StringBuilder();
            int line = 1;
            int ch;
            while ((ch = reader.read()) != -1) {
                if (line >= start) {
                    content.append((char) ch);
                }
                if (ch == '\n') {
                    line++;
                    if (limit != null && (long) line - start >= limit) {
                        break;
                    }
                }
            }
            return content.toString();
        }
    }

    static String write(Path resolved, String content) throws IOException {
        ByteBuffer bytes = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(content));
        return withParent(resolved, (directory, name) -> {
            boolean exists;
            try {
                requireRegularFile(directory, name);
                exists = true;
            } catch (NoSuchFileException ex) {
                exists = false;
            }
            String previous = null;
            if (exists) {
                try {
                    previous = read(directory, name, 1, null);
                } catch (AccessDeniedException | CharacterCodingException ex) {
                    // A write-only or non-UTF-8 file has no text to show in the preview.
                }
            }
            Set<OpenOption> options = exists
                    ? Set.of(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)
                    : Set.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, LinkOption.NOFOLLOW_LINKS);
            try (SeekableByteChannel channel = directory.newByteChannel(name, options)) {
                while (bytes.hasRemaining()) {
                    channel.write(bytes);
                }
            }
            return previous;
        });
    }

    private static void requireRegularFile(SecureDirectoryStream<Path> directory, Path name) throws IOException {
        BasicFileAttributes attributes = directory.getFileAttributeView(name,
                BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).readAttributes();
        if (!attributes.isRegularFile()) {
            throw new AccessDeniedException(name.toString(), null, "A regular file is required");
        }
    }

    private static <T> T withParent(Path file, FileAction<T> action) throws IOException {
        // Walk from the filesystem root with directory handles so no component can be
        // redirected through a symbolic link after the path has been authorized.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.getRoot())) {
            if (!(stream instanceof SecureDirectoryStream<Path> directory)) {
                throw new AccessDeniedException(file.toString(), null,
                        "The filesystem provider does not support secure directory access");
            }
            return withParent(directory, file.iterator(), action);
        }
    }

    private static <T> T withParent(SecureDirectoryStream<Path> directory,
            Iterator<Path> components, FileAction<T> action) throws IOException {
        Path name = components.next();
        if (!components.hasNext()) {
            return action.run(directory, name);
        }
        try (SecureDirectoryStream<Path> child = directory.newDirectoryStream(name, LinkOption.NOFOLLOW_LINKS)) {
            return withParent(child, components, action);
        }
    }

    @FunctionalInterface
    private interface FileAction<T> {

        T run(SecureDirectoryStream<Path> directory, Path name) throws IOException;
    }
}
