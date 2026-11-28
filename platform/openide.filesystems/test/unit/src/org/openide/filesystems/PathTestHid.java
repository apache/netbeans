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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathTestHid extends TestBaseHid {

    private FileObject root;

    public PathTestHid(String name) {
        super(name);
    }

    @Override
    protected String[] getResources(String testName) {
        return new String[] {
            "pathtest/dir/file.txt",
            "pathtest/dir/sibling.txt"
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        super.setUp();
        Repository.getDefault().addFileSystem(testedFS);
        root = testedFS.findResource(getResourcePrefix());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(testedFS);
        super.tearDown();
    }

    public void testPathAccessorsAndNames() throws Exception {
        Path path = getPath();

        assertNotNull(path.getFileSystem());
        assertTrue(path.isAbsolute());
        assertNotNull(path.getRoot());
        assertEquals("file.txt", path.getFileName().toString());
        assertNotNull(path.getParent());
        assertTrue(path.getNameCount() > 0);
        assertEquals(path.getFileName(), path.getName(path.getNameCount() - 1));
        assertEquals(path.getNameCount(), path.subpath(0, path.getNameCount()).getNameCount());
        assertTrue(path.startsWith(path.getRoot()));
        assertTrue(path.startsWith(path.getParent()));
        assertTrue(path.startsWith(path.getParent().toString()));
        assertTrue(path.endsWith(path.getFileName()));
        assertTrue(path.endsWith(path.getFileName().toString()));
        assertEquals(path, path.normalize());

        Iterator<Path> names = path.iterator();
        Path lastName = null;
        while (names.hasNext()) {
            lastName = names.next();
        }
        assertEquals(path.getFileName(), lastName);
    }

    public void testListChildrenThroughPath() throws Exception {
        FileObject folder = getFileObject().getParent();
        FileObject[] children = folder.getChildren();
        Set<String> fileObjectNames = new HashSet<>();
        for (FileObject child : children) {
            fileObjectNames.add(child.getNameExt());
        }

        Set<String> pathNames;
        try (Stream<Path> entries = Files.list(FileUtil.toPath(folder))) {
            pathNames = entries
                    .map(entry -> entry.getFileName().toString())
                    .collect(Collectors.toSet());
        }

        assertEquals(fileObjectNames, pathNames);
        assertEquals(2, children.length);
    }

    public void testDirectoryStreamFilter() throws Exception {
        Path folder = getPath().getParent();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder,
                entry -> entry.getFileName().toString().equals("file.txt"))) {
            Iterator<Path> entries = stream.iterator();
            assertTrue(entries.hasNext());
            assertTrue("Repeated hasNext must not consume the entry", entries.hasNext());
            assertEquals(getPath(), entries.next());
            assertFalse(entries.hasNext());
            try {
                entries.next();
                fail("An exhausted iterator must throw");
            } catch (NoSuchElementException expected) {
            }
            try {
                entries.remove();
                fail("A directory iterator must not support remove");
            } catch (UnsupportedOperationException expected) {
            }
        }
    }

    public void testDirectoryStreamCloseAndReadAhead() throws Exception {
        Path directory = getPath().getParent();
        boolean virtual = directory.getFileSystem() instanceof NioFsForFileSystem;
        DirectoryStream<Path> stream = Files.newDirectoryStream(directory);
        try {
            Iterator<Path> entries = stream.iterator();
            assertTrue(entries.hasNext());
            stream.close();
            assertNotNull("A read-ahead entry must remain available after close", entries.next());
            int remaining = 0;
            while (entries.hasNext()) {
                entries.next();
                assertTrue("A closed stream must eventually be exhausted", ++remaining <= 2);
            }
            try {
                entries.next();
                fail("An exhausted closed stream must throw");
            } catch (NoSuchElementException expected) {
            }
            if (!virtual) {
                try {
                    stream.iterator();
                    fail("A closed directory stream must not provide an iterator");
                } catch (IllegalStateException expected) {
                }
            }
        } finally {
            stream.close();
        }

        if (!virtual) {
            DirectoryStream<Path> closedBeforeIterator = Files.newDirectoryStream(directory);
            closedBeforeIterator.close();
            try {
                closedBeforeIterator.iterator();
                fail("A closed directory stream must not provide its first iterator");
            } catch (IllegalStateException expected) {
            }
        }
    }

    public void testDirectoryStreamFilterIOException() throws Exception {
        IOException failure = new IOException("filter failed");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(getPath().getParent(), entry -> {
            throw failure;
        })) {
            try {
                stream.iterator().hasNext();
                fail("Filter failure must be reported during iteration");
            } catch (DirectoryIteratorException ex) {
                assertSame(failure, ex.getCause());
            }
        }
    }

    public void testDirectoryStreamInvalidTargets() throws Exception {
        for (Path invalid : Arrays.asList(getPath(), getPath().resolveSibling("missing-directory"))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(invalid)) {
                fail("Opening a non-directory must fail: " + invalid + ", got " + stream);
            } catch (IOException expected) {
            }
        }
    }

    public void testURLsAreTheSame() throws Exception {
        FileObject folder = getFileObject().getParent();
        FileObject[] children = folder.getChildren();
        Set<URI> foUris = new HashSet<>();
        for (FileObject child : children) {
            foUris.add(child.toURI());
        }

        Set<URI> pathUris;
        try (Stream<Path> entries = Files.list(FileUtil.toPath(folder))) {
            pathUris = entries
                    .map(entry -> entry.toUri())
                    .collect(Collectors.toSet());
        }

        assertEquals(foUris, pathUris);
        assertEquals(2, children.length);

        for (URI u : foUris) {
            assertEquals("Scheme is correct", u.getScheme(), FileUtil.toPath(folder).getFileSystem().provider().getScheme());
        }
    }

    public void testPathResolutionAndRelativization() throws Exception {
        Path path = getPath();

        Path parent = path.getParent();
        Path fileName = path.getFileName();
        assertEquals(path, parent.resolve(fileName));
        assertEquals(path, parent.resolve(fileName.toString()));
        assertEquals(path, path.resolveSibling(path.getFileName()));
        assertEquals(path, path.resolveSibling(path.getFileName().toString()));
        assertEquals("sibling.txt", path.resolveSibling("sibling.txt").getFileName().toString());

        Path relative = parent.relativize(path);
        assertEquals(fileName, relative);
        assertEquals(path, parent.resolve(relative));
        assertEquals(path.getFileSystem().getPath(""), path.relativize(path));
    }

    public void testRelativePathNormalizationAndResolution() throws Exception {
        java.nio.file.FileSystem fs = getPath().getFileSystem();
        Path base = fs.getPath("alpha", "beta");
        Path relative = fs.getPath("gamma", "..", "delta");

        assertNull(base.getRoot());
        assertEquals(fs.getPath("alpha"), base.getParent());
        assertNull(fs.getPath("alpha").getParent());
        assertEquals(fs.getPath("alpha", "beta", "gamma", "..", "delta"), base.resolve(relative));
        assertEquals(fs.getPath("alpha", "beta", "delta"), base.resolve(relative).normalize());
        assertEquals(fs.getPath("..", "..", "alpha"), fs.getPath("..", "alpha", "..", "..", "alpha").normalize());
        assertEquals(fs.getPath(""), fs.getPath("alpha", "..").normalize());
        assertEquals(fs.getPath("alpha", "other"), base.resolveSibling("other"));
        assertEquals(fs.getPath("other"), fs.getPath("alpha").resolveSibling("other"));
        Path simple = fs.getPath("gamma", "delta");
        assertEquals(simple, base.relativize(base.resolve(simple)));
    }

    public void testEmptyPathNamesAndResolution() throws Exception {
        java.nio.file.FileSystem fs = getPath().getFileSystem();
        Path empty = fs.getPath("");
        Path name = fs.getPath("alpha");
        assertNull(empty.getRoot());
        assertNull(empty.getParent());
        assertEquals(empty, empty.getFileName());
        assertEquals(1, empty.getNameCount());
        assertEquals(empty, empty.getName(0));
        assertEquals(empty, empty.subpath(0, 1));
        Iterator<Path> names = empty.iterator();
        assertTrue(names.hasNext());
        assertEquals(empty, names.next());
        assertFalse(names.hasNext());
        assertEquals(name, empty.resolve(name));
        assertEquals(name, name.resolve(empty));
        assertEquals(empty, name.resolveSibling(empty));
        assertEquals(name, empty.relativize(name));
        assertEquals(fs.getPath(".."), name.relativize(empty));
    }

    public void testRootPathNamesAndResolution() throws Exception {
        Path rootPath = getPath().getRoot();
        Path name = rootPath.getFileSystem().getPath("alpha");
        assertNull(rootPath.getParent());
        assertNull(rootPath.getFileName());
        assertEquals(0, rootPath.getNameCount());
        assertFalse(rootPath.iterator().hasNext());
        try {
            rootPath.getName(0);
            fail("A root has no name elements");
        } catch (IllegalArgumentException expected) {
        }
        assertEquals(name, rootPath.resolveSibling(name));
        assertEquals(name, rootPath.relativize(rootPath.resolve(name)));
        assertEquals(rootPath, rootPath.resolve("../..").normalize());
    }

    public void testPathRedundantSeparators() throws Exception {
        java.nio.file.FileSystem fs = getPath().getFileSystem();
        String separator = fs.getSeparator();
        Path path = fs.getPath("alpha" + separator + separator + "beta" + separator);
        assertEquals(fs.getPath("alpha", "beta"), path);
        assertEquals(fs.getPath("alpha"), path.getParent());
        assertEquals(fs.getPath("beta"), path.getFileName());
        Path rootPath = getPath().getRoot();
        assertEquals(rootPath.resolve(fs.getPath("alpha", "beta")), rootPath.resolve(path));
    }

    public void testResolveRelativizeRoundTrips() throws Exception {
        java.nio.file.FileSystem fs = getPath().getFileSystem();
        Path rootPath = getPath().getRoot();
        List<Path> paths = Arrays.asList(fs.getPath(""), fs.getPath("alpha"),
                fs.getPath("alpha", "beta"), fs.getPath("other", "leaf"));
        for (Path base : paths) {
            for (Path target : paths) {
                assertEquals(target, base.resolve(base.relativize(target)).normalize());
                Path absoluteBase = rootPath.resolve(base);
                Path absoluteTarget = rootPath.resolve(target);
                assertEquals(absoluteTarget, absoluteBase.resolve(absoluteBase.relativize(absoluteTarget)).normalize());
            }
        }
    }

    public void testAbsoluteAndRelativePathsAreDistinct() throws Exception {
        java.nio.file.FileSystem fs = getPath().getFileSystem();
        Path relative = fs.getPath("alpha", "beta");
        Path absolute = getPath().getRoot().resolve(relative);

        assertFalse(relative.isAbsolute());
        assertTrue(absolute.isAbsolute());
        assertEquals(absolute, relative.resolve(absolute));
        assertEquals(absolute, relative.resolve(absolute.toString()));
        assertEquals(getPath().getRoot(), getPath().getRoot().resolve("..").normalize());
        assertFalse(relative.startsWith(absolute));
        assertFalse(absolute.startsWith(relative));
        try {
            absolute.relativize(relative);
            fail("Cannot relativize an absolute path against a relative path");
        } catch (IllegalArgumentException expected) {
        }
        try {
            relative.relativize(absolute);
            fail("Cannot relativize a relative path against an absolute path");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testEndsWithWholePathElements() throws Exception {
        java.nio.file.FileSystem fs = getPath().getFileSystem();
        Path path = getPath();

        assertTrue(path.endsWith("file.txt"));
        assertTrue(path.endsWith(fs.getPath("dir/file.txt")));
        assertTrue(path.endsWith(path));
        assertFalse(path.endsWith("le.txt"));
        assertFalse(path.endsWith(fs.getPath("ir/file.txt")));
        assertFalse(path.endsWith(path.getRoot().resolve("file.txt")));
        assertFalse(fs.getPath("dir/file.txt").endsWith(path));
        assertFalse(path.endsWith(""));
        assertTrue(fs.getPath("").endsWith(""));
        assertTrue(path.getRoot().endsWith(path.getRoot()));
        assertFalse(path.getRoot().endsWith(""));
    }

    public void testPathConversionAndComparison() throws Exception {
        FileObject fileObject = getFileObject();
        Path path = getPath();

        Path samePath = path.getFileSystem().getPath(path.toString());
        assertEquals(path, samePath);
        assertEquals(path.hashCode(), samePath.hashCode());
        assertEquals(0, path.compareTo(samePath));
        assertEquals(fileObject, FileUtil.toFileObject(path));
        assertTrue(path.toUri().isAbsolute());
        assertEquals(path.toAbsolutePath().toUri(), path.toUri());
        assertFalse(path.toString().isEmpty());

        if (testedFS.isReadOnly()) {
            try {
                path.toFile();
                fail("Read-only virtual Path unexpectedly has a java.io.File");
            } catch (UnsupportedOperationException ex) {
                // Expected for virtual filesystems.
            }
        } else {
            File file = FileUtil.toFile(fileObject);
            if (file != null) {
                if (isFileSystemBasedPath(path)) {
                    assertEquals(file, path.toFile());
                    assertEquals(file.getCanonicalFile().toPath(), path.toRealPath());
                }
            } else {
                try {
                    path.toFile();
                    fail("Virtual FileObject unexpectedly has a java.io.File");
                } catch (UnsupportedOperationException ex) {
                    // Expected for virtual filesystems.
                }
            }
        }
    }

    public void testVirtualPathUriForMissingAndRelativePaths() throws Exception {
        Path root = getPath().getRoot();
        if (!(root instanceof NioPathForFileObject)) {
            return;
        }
        Path missing = getPath().resolveSibling("missing space #%25.txt");
        URI before = missing.toUri();
        assertTrue(before.isAbsolute());
        assertTrue(before.toASCIIString().endsWith("/missing%20space%20%23%2525.txt"));
        assertEquals(before, missing.toAbsolutePath().toUri());

        Path relative = root.relativize(missing);
        assertTrue(relative.toAbsolutePath().isAbsolute());
        assertEquals(missing, relative.toAbsolutePath());
        assertEquals(before, relative.toUri());
        assertEquals(root.toUri(), root.relativize(root).toUri());
        assertTrue(missing.resolve("child").toUri().toASCIIString().endsWith(
                "/missing%20space%20%23%2525.txt/child"));

        if (!testedFS.isReadOnly()) {
            Path created = getPath().resolveSibling("uri-created.txt");
            URI createdUri = created.toUri();
            Files.createFile(created);
            assertEquals("The URI must identify the same location after creation", createdUri, created.toUri());
        }
    }

    public void testPathForNonExistingFileObjects() throws Exception {
        FileObject folder = getFileObject().getParent();
        FileObject missing = folder.getFileObject("missing-object.txt", false);
        FileObject nested = folder.getFileObject("missing-parent/missing-child.txt", false);
        assertNotNull(missing);
        assertNotNull(nested);
        assertFalse(missing.isValid());
        assertFalse(nested.isValid());

        Path path = FileUtil.toPath(missing);
        Path nestedPath = FileUtil.toPath(nested);
        Path folderPath = path instanceof NioPathForFileObject
                ? testedFS.toVirtualPath(folder) : FileUtil.toPath(folder);
        assertEquals(folderPath.resolve("missing-object.txt"), path);
        assertEquals(folderPath.resolve("missing-parent/missing-child.txt"), nestedPath);
        assertFalse(Files.exists(path));
        assertFalse(Files.exists(nestedPath));
        assertEquals(missing.toURI(), path.toUri());
        assertTrue(nestedPath.toUri().toASCIIString().endsWith("/missing-parent/missing-child.txt"));
        try {
            path.toRealPath();
            fail("A path for a missing FileObject has no real path");
        } catch (IOException expected) {
        }

        if (!testedFS.isReadOnly()) {
            URI before = path.toUri();
            Files.writeString(path, "created");
            assertEquals("created", Files.readString(path));
            assertEquals(before, path.toUri());
            assertEquals(path, path.toRealPath());

            FileObject missingFolder = folder.getFileObject("missing-folder", false);
            Path folderToCreate = FileUtil.toPath(missingFolder);
            Files.createDirectory(folderToCreate);
            assertTrue(Files.isDirectory(folderToCreate));

            URI nestedUri = nestedPath.toUri();
            Files.createDirectory(nestedPath.getParent());
            Files.writeString(nestedPath, "nested");
            assertEquals("nested", Files.readString(nestedPath));
            assertEquals(nestedUri, nestedPath.toUri());
            assertEquals(nestedPath, nestedPath.toRealPath());
        }
    }

    public void testIndependentPathConversions() throws Exception {
        FileObject fileObject = getFileObject();
        Path first = FileUtil.toPath(fileObject);
        Path second = FileUtil.toPath(fileObject);
        Path parent = FileUtil.toPath(fileObject.getParent());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(0, first.compareTo(second));
        assertTrue(first.startsWith(parent));
        assertTrue(first.endsWith(second.getFileName()));
        assertEquals(first.getFileName(), parent.relativize(first));
        assertEquals(first, parent.resolve(parent.relativize(second)));
    }

    public void testFileSystemConversion() throws Exception {
        FileObject fileObject = getFileObject();
        Path originalPath = getPath();
        java.nio.file.FileSystem nioFileSystem = originalPath.getFileSystem();
        Path path = nioFileSystem.getPath(originalPath.toString());

        FileObject converted = FileUtil.toFileObject(path);
        assertEquals(fileObject, converted);
        assertSame(testedFS, converted.getFileSystem());
    }

    public void testCreateFileAndFolderThroughPath() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject parent = getFileObject().getParent();
        Path folderPath = getPath().getParent().resolve("nio-folder");
        Path filePath = folderPath.resolve("nio-file.txt");

        Files.createDirectory(folderPath);
        Files.createFile(filePath);
        Files.writeString(filePath, "created through NIO");

        parent.refresh();
        FileObject folder = parent.getFileObject("nio-folder");
        assertNotNull(folder);
        FileObject file = folder.getFileObject("nio-file.txt");
        assertNotNull(file);
        assertTrue(folder.isFolder());
        assertTrue(file.isData());
        assertEquals(folder, file.getParent());
        assertEquals("created through NIO", file.asText());
    }

    public void testCopyFileThroughPath() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject source = getFileObject();
        FileObject folder = source.getParent();
        Path sourcePath = FileUtil.toPath(source);
        Path targetPath = sourcePath.resolveSibling("nio-copy.txt");
        Files.writeString(sourcePath, "original content");

        assertEquals(targetPath, Files.copy(sourcePath, targetPath));
        folder.refresh();
        assertTrue("Copy must not invalidate the source", source.isValid());
        assertEquals(source, folder.getFileObject("file.txt"));
        assertEquals("original content", source.asText("UTF-8"));
        FileObject target = folder.getFileObject("nio-copy.txt");
        assertNotNull(target);
        assertEquals("original content", target.asText("UTF-8"));

        Files.writeString(targetPath, "different content");
        try {
            Files.copy(sourcePath, targetPath);
            fail("Copying over an existing file must fail");
        } catch (FileAlreadyExistsException expected) {
        }
        assertEquals("different content", Files.readString(targetPath));
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        assertEquals("original content", Files.readString(targetPath));
        assertEquals("original content", Files.readString(sourcePath));
        Files.copy(sourcePath, sourcePath);
        Files.copy(sourcePath, sourcePath, StandardCopyOption.REPLACE_EXISTING);
        assertEquals("original content", Files.readString(sourcePath));
        try {
            Files.copy(sourcePath, sourcePath.resolveSibling("missing-folder").resolve("copy.txt"));
            fail("Copying to a missing parent directory must fail");
        } catch (NoSuchFileException expected) {
        }
        try {
            Files.copy(sourcePath, sourcePath.resolveSibling("unsupported.txt"), StandardCopyOption.ATOMIC_MOVE);
            fail("An unsupported copy option must fail");
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testCopyDirectoryThroughPath() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path source = getPath().getParent();
        Path target = source.resolveSibling("nio-copy-folder");

        Files.copy(source, target);
        assertTrue(Files.isDirectory(target));
        try (Stream<Path> entries = Files.list(target)) {
            assertEquals("Directory copy must not copy children", 0, entries.count());
        }
        assertTrue(Files.exists(source.resolve("file.txt")));
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        Files.createFile(target.resolve("child.txt"));
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            fail("Replacing a nonempty directory must fail");
        } catch (IOException expected) {
        }
        assertTrue(Files.exists(target.resolve("child.txt")));
        try {
            Files.copy(source.resolve("missing.txt"), target.resolve("missing.txt"));
            fail("Copying a missing source must fail");
        } catch (NoSuchFileException expected) {
        }
    }

    public void testCopyFileToLocalFilesystem() throws Exception {
        FileObject source = getFileObject();
        Path sourcePath = FileUtil.toPath(source);
        Path targetPath = getWorkDir().toPath().resolve("nio-external-copy.txt");
        byte[] content = Files.readAllBytes(sourcePath);

        Files.copy(sourcePath, targetPath);
        assertTrue(Arrays.equals(content, Files.readAllBytes(targetPath)));
        assertTrue(source.isValid());
        assertEquals(source, source.getParent().getFileObject(source.getNameExt()));
    }

    public void testCopyFileFromLocalFilesystem() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path source = getWorkDir().toPath().resolve("nio-external-source.txt");
        Path target = getPath().resolveSibling("nio-imported.txt");
        Files.writeString(source, "external content");

        Files.copy(source, target);
        assertEquals("external content", Files.readString(source));
        assertEquals("external content", Files.readString(target));
    }

    public void testCopyToReadOnlyFilesystemFails() throws Exception {
        if (!testedFS.isReadOnly()) {
            return;
        }
        FileObject source = getFileObject();
        Path sourcePath = FileUtil.toPath(source);
        Path targetPath = sourcePath.resolveSibling("nio-copy.txt");
        try {
            Files.copy(sourcePath, targetPath);
            fail("Copying to a read-only filesystem must fail");
        } catch (IOException expected) {
        }
        assertTrue(source.isValid());
        assertEquals(source, source.getParent().getFileObject(source.getNameExt()));
        assertNull(source.getParent().getFileObject("nio-copy.txt"));
    }

    public void testReadAndWriteThroughPathStreams() throws Exception {
        FileObject file = getFileObject();
        Path path = getPath();
        byte[] original = Files.readAllBytes(path);
        assertEquals(Files.size(path), file.getSize());
        try (InputStream input = Files.newInputStream(path)) {
            assertTrue(Arrays.equals(original, input.readAllBytes()));
        }

        if (testedFS.isReadOnly()) {
            return;
        }
        String content = "Written through a Path output stream";
        while (content.getBytes(StandardCharsets.UTF_8).length == original.length) {
            content += "!";
        }
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        if (isFileSystemBasedPath(path)) {
            Files.setLastModifiedTime(path, FileTime.fromMillis(
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)));
            file.refresh();
        }
        CountDownLatch changed = new CountDownLatch(1);
        FileChangeListener listener = new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent event) {
                changed.countDown();
            }
        };
        file.addFileChangeListener(listener);
        try (WatchService watcher = path.getFileSystem().newWatchService()) {
            path.getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            try (OutputStream output = Files.newOutputStream(path)) {
                output.write(contentBytes);
            }

            assertWatchEventPresent(
                    watcher, new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_MODIFY, path.getFileName()));
            file.refresh();
            if (FileUtil.toFile(file) != null) {
                assertTrue("FileChangeListener was not notified", changed.await(5, TimeUnit.SECONDS));
            }
            assertEquals(content, file.asText("UTF-8"));
            assertEquals(contentBytes.length, Files.size(path));
            assertEquals(Files.size(path), file.getSize());
            try (InputStream input = Files.newInputStream(path)) {
                assertEquals(content, new String(input.readAllBytes(), StandardCharsets.UTF_8));
            }
        } finally {
            file.removeFileChangeListener(listener);
        }
    }

    public void testFileObjectAndPathOutputStreams() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject file = getFileObject();
        Path path = FileUtil.toPath(file);
        byte[][] results = new byte[2][];
        for (int i = 0; i < results.length; i++) {
            Files.writeString(path, "old content");
            try (OutputStream output = i == 0 ? file.getOutputStream() : Files.newOutputStream(path)) {
                output.write('A');
                output.write(new byte[] {'x', 'B', 'C', 'x'}, 1, 2);
                output.flush();
                output.write('D');
            }
            results[i] = Files.readAllBytes(path);
        }
        assertTrue("FileObject and Path output streams produce the same contents",
                Arrays.equals(results[0], results[1]));
        assertEquals("ABCD", new String(results[0], StandardCharsets.UTF_8));
    }

    public void testOutputStreamAppendWriteAndTruncate() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath();
        Files.writeString(path, "abcdef");
        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.APPEND)) {
            output.write('X');
            output.write('Y');
            output.flush();
            assertEquals("abcdefXY", Files.readString(path));
        }
        assertEquals("abcdefXY", Files.readString(path));
        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.WRITE)) {
            output.write("12".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals("12cdefXY", Files.readString(path));
        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING)) {
            assertEquals("Truncation happens when the stream opens", 0L, Files.size(path));
            output.write('Z');
        }
        assertEquals("Z", Files.readString(path));
        try (OutputStream output = Files.newOutputStream(path)) {
            output.flush();
            assertEquals("Default options truncate an existing file on open", 0L, Files.size(path));
        }
        assertEquals(0L, Files.size(path));
    }

    public void testOutputStreamCreateOptions() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath().resolveSibling("stream-created.txt");
        for (OpenOption option : Arrays.asList(StandardOpenOption.WRITE, StandardOpenOption.APPEND,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            try (OutputStream output = Files.newOutputStream(path, option)) {
                fail("Opening a missing file without CREATE must fail: " + output);
            } catch (NoSuchFileException expected) {
            }
        }
        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            output.write('A');
        }
        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            fail("CREATE_NEW must reject an existing file: " + output);
        } catch (FileAlreadyExistsException expected) {
        }
        assertEquals("A", Files.readString(path));

        Path appended = path.resolveSibling("stream-append-created.txt");
        try (OutputStream output = Files.newOutputStream(appended,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            output.write('B');
        }
        assertEquals("B", Files.readString(appended));

        Path defaultCreated = path.resolveSibling("stream-default-created.txt");
        try (OutputStream output = Files.newOutputStream(defaultCreated)) {
            output.write('C');
        }
        assertEquals("C", Files.readString(defaultCreated));
    }

    public void testOutputStreamInvalidOptions() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath();
        byte[] before = Files.readAllBytes(path);
        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.READ)) {
            fail("An output stream cannot be opened for reading: " + output);
        } catch (IllegalArgumentException expected) {
        }
        try (OutputStream output = Files.newOutputStream(path,
                StandardOpenOption.APPEND, StandardOpenOption.TRUNCATE_EXISTING)) {
            fail("APPEND and TRUNCATE_EXISTING cannot be combined: " + output);
        } catch (IllegalArgumentException expected) {
        }
        OpenOption unsupported = new OpenOption() {};
        try (OutputStream output = Files.newOutputStream(path, unsupported)) {
            fail("An unknown open option must fail: " + output);
        } catch (UnsupportedOperationException expected) {
        }
        assertTrue("Invalid options must not modify the file", Arrays.equals(before, Files.readAllBytes(path)));
    }

    public void testPathSizeAndBasicAttributes() throws Exception {
        FileObject file = getFileObject();
        Path path = getPath();
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        assertEquals(attributes.size(), Files.size(path));
        assertEquals(attributes.size(), file.getSize());
        assertEquals(attributes.lastModifiedTime().toMillis(), file.lastModified().getTime());
    }

    public void testBasicAttributesForFileAndDirectory() throws Exception {
        BasicFileAttributes file = Files.readAttributes(getPath(), BasicFileAttributes.class);
        assertTrue(file.isRegularFile());
        assertFalse(file.isDirectory());
        assertFalse(file.isSymbolicLink());
        assertFalse(file.isOther());

        BasicFileAttributes directory = Files.readAttributes(getPath().getParent(), BasicFileAttributes.class);
        assertTrue(directory.isDirectory());
        assertFalse(directory.isRegularFile());
        assertFalse(directory.isSymbolicLink());
        assertFalse(directory.isOther());
    }

    public void testBasicAttributeSelection() throws Exception {
        Path path = getPath();
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        Map<String, Object> selected = Files.readAttributes(path, "basic:size,lastModifiedTime,isDirectory");
        assertEquals(Set.of("size", "lastModifiedTime", "isDirectory"), selected.keySet());
        assertEquals(attributes.size(), selected.get("size"));
        assertEquals(attributes.lastModifiedTime(), selected.get("lastModifiedTime"));
        assertEquals(attributes.isDirectory(), selected.get("isDirectory"));
        assertEquals(Map.of("size", attributes.size()), Files.readAttributes(path, "size"));

        Map<String, Object> all = Files.readAttributes(path, "basic:*");
        assertEquals(Set.of("size", "lastModifiedTime", "lastAccessTime", "creationTime",
                "isRegularFile", "isDirectory", "isSymbolicLink", "isOther", "fileKey"), all.keySet());
        assertEquals(all, Files.readAttributes(path, "size,*"));
        Map<String, Object> fileKey = Files.readAttributes(path, "fileKey");
        assertEquals(Set.of("fileKey"), fileKey.keySet());
        assertEquals(all.get("fileKey"), fileKey.get("fileKey"));
        assertEquals(Map.of("isRegularFile", false, "isDirectory", true),
                Files.readAttributes(path.getParent(), "isRegularFile,isDirectory"));
    }

    public void testBasicAttributesOfMissingFile() throws Exception {
        Path missing = getPath().resolveSibling("missing-attributes.txt");
        try {
            Files.readAttributes(missing, BasicFileAttributes.class);
            fail("Reading attributes of a missing file must fail");
        } catch (NoSuchFileException expected) {
        }
        try {
            Files.readAttributes(missing, "basic:size");
            fail("Reading selected attributes of a missing file must fail");
        } catch (NoSuchFileException expected) {
        }
    }

    public void testInvalidBasicAttributeSelection() throws Exception {
        for (String selection : Arrays.asList("", "basic:", "size,notAnAttribute")) {
            try {
                Files.readAttributes(getPath(), selection);
                fail("Invalid selection must fail: " + selection);
            } catch (IllegalArgumentException expected) {
            }
        }
        try {
            Files.readAttributes(getPath(), "unsupportedView:*");
            fail("An unsupported attribute view must fail");
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testBasicAttributesAfterFileChanges() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath();
        Files.writeString(path, "abc");
        BasicFileAttributes before = Files.readAttributes(path, BasicFileAttributes.class);
        Files.writeString(path, "abcdefgh");
        BasicFileAttributes after = Files.readAttributes(path, BasicFileAttributes.class);
        assertEquals(3L, before.size());
        assertEquals(8L, after.size());
        assertTrue(before.isRegularFile());
        assertTrue(after.isRegularFile());
        Files.delete(path);
        assertTrue("The earlier attributes describe the file before deletion", before.isRegularFile());
        assertEquals(8L, after.size());
    }

    public void testPathFileChannelLock() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject file = getFileObject();
        Path path = getPath();
        assertFalse(file.isLocked());

        if (FileUtil.toFile(file) != null && isFileSystemBasedPath(path)) {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
                java.nio.channels.FileLock nioLock = channel.lock();
                try {
                    assertTrue(nioLock.isValid());
                    assertFalse(file.isLocked());
                } finally {
                    nioLock.release();
                }
                assertFalse(nioLock.isValid());
            }
        }

        FileLock fileObjectLock = file.lock();
        try {
            assertTrue(file.isLocked());
        } finally {
            fileObjectLock.releaseLock();
        }
        assertFalse(file.isLocked());
    }

    private static boolean isFileSystemBasedPath(Path path) {
        return "file".equals(path.getFileSystem().provider().getScheme());
    }

    public void testPathFileChannelOverlappingLocks() throws Exception {
        Path path = getPath();
        if (testedFS.isReadOnly() || FileUtil.toFile(getFileObject()) == null || !isFileSystemBasedPath(path)) {
            return;
        }
        try (FileChannel first = FileChannel.open(path, StandardOpenOption.WRITE);
                FileChannel second = FileChannel.open(path, StandardOpenOption.WRITE)) {
            java.nio.channels.FileLock held = first.lock(0, 10, false);
            try {
                try {
                    second.tryLock(5, 10, false);
                    fail("Overlapping locks in the same JVM must be rejected");
                } catch (OverlappingFileLockException expected) {
                }
                try (java.nio.channels.FileLock disjoint = second.tryLock(10, 10, false)) {
                    assertNotNull(disjoint);
                    assertTrue(disjoint.isValid());
                }
            } finally {
                held.release();
            }
            try (java.nio.channels.FileLock acquired = second.tryLock(0, 10, false)) {
                assertNotNull(acquired);
                assertTrue(acquired.isValid());
            }
        }
    }

    public void testPathFileChannelCloseReleasesLock() throws Exception {
        if (testedFS.isReadOnly() || FileUtil.toFile(getFileObject()) == null) {
            return;
        }
        Path path = getPath();
        java.nio.channels.FileLock held;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            held = channel.lock();
            assertTrue(held.isValid());
        }
        assertFalse(held.isValid());
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE);
                java.nio.channels.FileLock acquired = channel.tryLock()) {
            assertNotNull(acquired);
            assertTrue(acquired.isValid());
        }
    }

    public void testPathFileChannelRead() throws Exception {
        Path path = getPath();
        if (!testedFS.isReadOnly()) {
            Files.writeString(path, "channel read");
        }
        byte[] expected = Files.readAllBytes(path);
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        try (channel) {
            assertEquals(expected.length, channel.size());
            ByteBuffer data = ByteBuffer.allocate(expected.length + 1);
            while (data.hasRemaining() && channel.read(data) != -1) {
            }
            assertEquals(expected.length, data.position());
            assertTrue(Arrays.equals(expected, Arrays.copyOf(data.array(), data.position())));
            assertEquals(expected.length, channel.position());
            assertEquals(-1, channel.read(ByteBuffer.allocate(1)));
            assertEquals(0, channel.read(ByteBuffer.allocate(0)));
            try {
                channel.read(ByteBuffer.allocate(1).asReadOnlyBuffer());
                fail("Reading into a read-only buffer must fail");
            } catch (IllegalArgumentException ex) {
            }
            assertEquals(expected.length, channel.position());
            if (expected.length != 0) {
                assertEquals(1, channel.read(ByteBuffer.allocate(1), 0));
                assertEquals(expected.length, channel.position());
            }
        }
        try {
            channel.read(ByteBuffer.allocate(1));
            fail("Reading a closed channel must fail");
        } catch (ClosedChannelException ex) {
        }
    }

    public void testVirtualPathFileChannelLockBridgesFileObject() throws Exception {
        if (testedFS.isReadOnly() || FileUtil.toFile(getFileObject()) != null) {
            return;
        }
        FileObject file = getFileObject();
        Path path = FileUtil.toPath(file);
        try (FileLock fileLock = file.lock()) {
            try {
                FileChannel.open(path, StandardOpenOption.WRITE).close();
                fail("Opening a writable channel must respect the FileObject lock");
            } catch (FileAlreadyLockedException expected) {
            }
        }
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            assertTrue("The channel owns the FileObject lock", file.isLocked());
            try (FileChannel reader = FileChannel.open(path, StandardOpenOption.READ)) {
                assertTrue(reader.isOpen());
            }
            try {
                FileChannel.open(path, StandardOpenOption.WRITE).close();
                fail("A second writable channel must not open");
            } catch (FileAlreadyLockedException expected) {
            }
            java.nio.channels.FileLock nioLock = channel.tryLock();
            try (nioLock) {
                assertNotNull(nioLock);
                assertTrue(file.isLocked());
                channel.write(ByteBuffer.wrap(new byte[] {1, 2, 3}));
                channel.force(false);
                assertTrue(Arrays.equals(new byte[] {1, 2, 3}, file.asBytes()));
                try {
                    file.lock().releaseLock();
                    fail("FileObject lock must conflict with the NIO lock");
                } catch (FileAlreadyLockedException expected) {
                }
                assertSame("Repeated acquisition returns the channel's lock", nioLock, channel.tryLock());
                assertSame("Range requests use the whole-file lock", nioLock, channel.tryLock(1, 2, false));
                assertSame("Shared requests use the exclusive lock", nioLock, channel.lock(2, 1, true));
                assertEquals(0, nioLock.position());
                assertEquals(Long.MAX_VALUE, nioLock.size());
                assertFalse(nioLock.isShared());
            }
            assertTrue("Releasing the NIO lock has no effect", nioLock.isValid());
            assertTrue("Releasing an NIO lock must retain channel ownership", file.isLocked());
            try {
                file.lock().releaseLock();
                fail("The channel must keep the FileObject lock until close");
            } catch (FileAlreadyLockedException expected) {
            }
            try (java.nio.channels.FileLock again = channel.lock()) {
                assertSame(nioLock, again);
                assertTrue(file.isLocked());
                assertTrue(again.isValid());
            }
        }
        assertFalse(file.isLocked());
        java.nio.channels.FileLock held;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            held = channel.lock();
            assertTrue(file.isLocked());
        }
        assertFalse(held.isValid());
        assertFalse(file.isLocked());
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            try {
                channel.tryLock(0, Long.MAX_VALUE, true);
                fail("Read-only virtual channels cannot acquire even a shared lock");
            } catch (NonWritableChannelException expected) {
            }
        }
    }

    public void testPathFileChannelWriteAndPosition() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath();
        Files.writeString(path, "abcdef");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            assertEquals(2, channel.write(ByteBuffer.wrap(new byte[] {'X', 'Y'}), 2));
            assertEquals(0, channel.position());
            channel.force(false);
            assertEquals("abXYef", Files.readString(path));
            channel.position(5);
            assertEquals(1, channel.write(ByteBuffer.wrap(new byte[] {'Z'})));
            assertEquals(6, channel.position());
            channel.force(false);
            channel.truncate(3);
            assertEquals(3, channel.position());
            assertEquals(3, channel.size());
            channel.force(false);
            assertEquals("abX", Files.readString(path));
            ByteArrayOutputStream transferred = new ByteArrayOutputStream();
            assertEquals(2, channel.transferTo(1, 2, Channels.newChannel(transferred)));
            assertEquals("bX", transferred.toString("UTF-8"));
            assertEquals(1, channel.transferFrom(
                    Channels.newChannel(new ByteArrayInputStream(new byte[] {'Z'})), 1, 1));
            channel.force(false);
            assertEquals("aZX", Files.readString(path));
            assertEquals(3, channel.position());
        }
    }

    public void testPathFileChannelCreateAndAppend() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath().resolveSibling("channel-created.txt");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            channel.write(ByteBuffer.wrap(new byte[] {'a', 'b'}));
        }
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.APPEND)) {
            assertEquals(2, channel.position());
            channel.position(0);
            channel.write(ByteBuffer.wrap(new byte[] {'c'}));
        }
        assertEquals("abc", Files.readString(path));
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            assertEquals(0, channel.size());
        }
        assertEquals("", Files.readString(path));
    }

    public void testPathByteChannelOptionsAndLocking() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path path = getPath();
        FileObject file = FileUtil.toFileObject(path);
        boolean virtual = FileUtil.toFile(file) == null;
        Files.writeString(path, "abcdef");
        try (SeekableByteChannel channel = Files.newByteChannel(path,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            assertEquals(0, channel.size());
            if (virtual) {
                assertTrue(file.isLocked());
            }
            channel.write(ByteBuffer.wrap(new byte[] {'x'}));
        }
        if (virtual) {
            assertFalse(file.isLocked());
        }
        assertEquals("x", Files.readString(path));

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.APPEND)) {
            assertEquals(1, channel.position());
            channel.position(0);
            channel.write(ByteBuffer.wrap(new byte[] {'y'}));
        }
        assertEquals("xy", Files.readString(path));

        try {
            Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            fail("CREATE_NEW must reject an existing file");
        } catch (FileAlreadyExistsException expected) {
        }
    }

    public void testMIMETypeAfterPathConversion() throws Exception {
        Path path = getPath();
        if (!testedFS.isReadOnly()) {
            Files.writeString(path, "Plain text content\n");
        }
        FileObject file = FileUtil.toFileObject(path);
        assertNotNull(file);
        file.refresh();
        assertEquals(Files.probeContentType(path), file.getMIMEType());
    }

    public void testWatchServiceAndFileChangeListener() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject watchedFolder = getFileObject().getParent();
        FileObject[] children = watchedFolder.getChildren();
        Path folderPath = FileUtil.toPath(watchedFolder);
        Path createdPath = folderPath.resolve("nio-watched.txt");
        CountDownLatch created = new CountDownLatch(1);
        AtomicReference<FileObject> createdFile = new AtomicReference<>();
        FileChangeListener listener = new FileChangeAdapter() {
            @Override
            public void fileDataCreated(FileEvent event) {
                if (event.getFile().getNameExt().equals("nio-watched.txt")) {
                    createdFile.set(event.getFile());
                    created.countDown();
                }
            }
        };
        watchedFolder.addFileChangeListener(listener);
        try (WatchService watcher = folderPath.getFileSystem().newWatchService()) {
            WatchEvent.Kind<?>[] events = {StandardWatchEventKinds.ENTRY_CREATE};
            WatchKey key = folderPath.register(watcher, events);
            assertTrue(key.isValid());
            WatchKey keyWithModifiers = folderPath.register(watcher, events, new WatchEvent.Modifier[0]);
            assertTrue(keyWithModifiers.isValid());

            Files.createFile(createdPath);

            WatchKey eventKey = watcher.poll(5, TimeUnit.SECONDS);
            assertNotNull(eventKey);
            List<WatchEvent<?>> watchEvents = eventKey.pollEvents();
            assertEquals(1, watchEvents.size());
            assertEquals(createdPath.getFileName(), watchEvents.get(0).context());

            watchedFolder.refresh();
            assertTrue("FileChangeListener was not notified", created.await(5, TimeUnit.SECONDS));
            assertEquals(FileUtil.toFileObject(createdPath), createdFile.get());
        } finally {
            watchedFolder.removeFileChangeListener(listener);
        }
        assertEquals(2, children.length);
    }

    public void testWatchServicePollEventsAfterFileObjectChanges() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject folder = getFileObject().getParent();
        FileObject[] children = folder.getChildren();
        Path folderPath = FileUtil.toPath(folder);
        try (WatchService watcher = folderPath.getFileSystem().newWatchService()) {
            WatchKey key = folderPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            assertNull("Registration must not signal a key", watcher.poll());
            assertTrue(key.pollEvents().isEmpty());

            folder.createData("first-watch-event.txt");
            assertSame(key, watcher.poll(5, TimeUnit.SECONDS));
            List<WatchEvent<?>> events = key.pollEvents();
            assertEquals(1, events.size());
            assertEquals(StandardWatchEventKinds.ENTRY_CREATE, events.get(0).kind());
            assertEquals(folderPath.getFileSystem().getPath("first-watch-event.txt"), events.get(0).context());
            assertTrue(key.pollEvents().isEmpty());

            folder.createData("second-watch-event.txt");
            assertNull("A signalled key must not be queued again before reset",
                    watcher.poll(200, TimeUnit.MILLISECONDS));
            assertTrue(key.reset());
            assertSame(key, watcher.poll(5, TimeUnit.SECONDS));
            events = key.pollEvents();
            assertEquals(1, events.size());
            assertEquals(StandardWatchEventKinds.ENTRY_CREATE, events.get(0).kind());
            assertEquals(folderPath.getFileSystem().getPath("second-watch-event.txt"), events.get(0).context());
        }
        assertEquals(2, children.length);
    }

    public void testWatchServiceCloseWakesWaiters() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path folder = getPath().getParent();
        WatchService watcher = folder.getFileSystem().newWatchService();
        FutureTask<WatchKey> take = new FutureTask<>(watcher::take);
        FutureTask<WatchKey> poll = new FutureTask<>(() -> watcher.poll(1, TimeUnit.MINUTES));
        Thread takeThread = new Thread(take, "watch-take");
        Thread pollThread = new Thread(poll, "watch-poll");
        try {
            WatchKey key = folder.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            takeThread.start();
            pollThread.start();
            awaitWatchWaiter(takeThread);
            awaitWatchWaiter(pollThread);
            watcher.close();
            assertFalse(key.isValid());
            assertFalse(key.reset());
            assertWatchClosed(take);
            assertWatchClosed(poll);
            try {
                watcher.poll();
                fail("Polling a closed watcher must fail");
            } catch (ClosedWatchServiceException expected) {
            }
            try {
                folder.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
                fail("Registering with a closed watcher must fail");
            } catch (ClosedWatchServiceException expected) {
            }
            watcher.close();
        } finally {
            watcher.close();
            take.cancel(true);
            poll.cancel(true);
            takeThread.join(5000);
            pollThread.join(5000);
        }
    }

    public void testWatchKeyCancellation() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject folder = getFileObject().getParent();
        Path folderPath = FileUtil.toPath(folder);
        try (WatchService watcher = folderPath.getFileSystem().newWatchService()) {
            WatchKey key = folderPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            folder.createData("before-cancel.txt");
            assertSame(key, watcher.poll(5, TimeUnit.SECONDS));
            assertTrue(key.reset());
            key.cancel();
            assertFalse(key.isValid());
            assertFalse(key.reset());
            assertSame("A queued key remains available after cancellation", key, watcher.poll(5, TimeUnit.SECONDS));
            List<WatchEvent<?>> pending = key.pollEvents();
            assertEquals(1, pending.size());
            assertEquals(folderPath.getFileSystem().getPath("before-cancel.txt"), pending.get(0).context());
            folder.createData("after-cancel.txt");
            assertNull(watcher.poll(200, TimeUnit.MILLISECONDS));
            assertTrue(key.pollEvents().isEmpty());
            key.cancel();
        }
    }

    public void testWatchKeyWhenDirectoryIsDeleted() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject directory = getFileObject().getParent().createFolder("watched-deleted");
        Path path = FileUtil.toPath(directory);
        try (WatchService watcher = path.getFileSystem().newWatchService()) {
            WatchKey key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            directory.delete();
            assertSame("Deleting the watched directory must signal its key",
                    key, watcher.poll(5, TimeUnit.SECONDS));
            assertFalse(key.isValid());
            assertFalse(key.reset());
        }
    }

    private void awaitWatchWaiter(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != Thread.State.WAITING
                && thread.getState() != Thread.State.TIMED_WAITING) {
            assertTrue("Watch thread did not block: " + thread.getState(), System.nanoTime() < deadline);
            Thread.sleep(10);
        }
    }

    private void assertWatchClosed(FutureTask<WatchKey> operation) throws Exception {
        try {
            operation.get(5, TimeUnit.SECONDS);
            fail("A blocked watch operation must fail when closed");
        } catch (ExecutionException ex) {
            assertTrue("Expected ClosedWatchServiceException: " + ex.getCause(),
                    ex.getCause() instanceof ClosedWatchServiceException);
        }
    }

    public void testWatchServiceFileObjectChangeSequence() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject folder = getFileObject().getParent();
        FileObject[] children = folder.getChildren();
        Path folderPath = FileUtil.toPath(folder);
        Path originalName = folderPath.getFileSystem().getPath("watch-sequence.txt");
        Path renamedName = folderPath.getFileSystem().getPath("watch-renamed.txt");
        try (WatchService watcher = folderPath.getFileSystem().newWatchService()) {
            folderPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            FileObject file = folder.createData(originalName.toString());
            assertWatchEvents(watcher, new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_CREATE, originalName));

            try (OutputStream out = file.getOutputStream()) {
                out.write(42);
            }
            assertWatchEvents(watcher, new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_MODIFY, originalName));

            try (FileLock lock = file.lock()) {
                file.rename(lock, "watch-renamed", "txt");
            }
            assertWatchEvents(watcher,
                    new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_DELETE, originalName),
                    new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_CREATE, renamedName));

            try (OutputStream out = file.getOutputStream()) {
                out.write(43);
            }
            assertWatchEvents(watcher, new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_MODIFY, renamedName));

            file.delete();
            assertWatchEvents(watcher, new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_DELETE, renamedName));
        }
        assertEquals(2, children.length);
    }

    public void testWatchServiceRoutesRenameToWatchedDirectory() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject folder = getFileObject().getParent();
        FileObject[] children = folder.getChildren();
        FileObject nested = folder.createFolder("watch-nested");
        nested.createData("before.txt");
        FileObject[] nestedChildren = nested.getChildren();
        Path parentPath = FileUtil.toPath(folder);
        Path nestedPath = parentPath.resolve("watch-nested");
        try (WatchService watcher = parentPath.getFileSystem().newWatchService()) {
            WatchKey parentKey = parentPath.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey nestedKey = nestedPath.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            Files.move(nestedPath.resolve("before.txt"), nestedPath.resolve("after.txt"));

            Set<String> expected = Set.of("ENTRY_DELETE:before.txt", "ENTRY_CREATE:after.txt");
            Set<String> remaining = new HashSet<>(expected);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!remaining.isEmpty()) {
                long timeout = deadline - System.nanoTime();
                assertTrue("Expected nested directory events " + remaining, timeout > 0);
                WatchKey key = watcher.poll(timeout, TimeUnit.NANOSECONDS);
                assertNotNull("Expected nested directory events " + remaining, key);
                assertSame("Event delivered to the wrong directory", nestedKey, key);
                for (WatchEvent<?> event : key.pollEvents()) {
                    String actual = event.kind().name() + ":" + event.context();
                    assertTrue("Unexpected event " + actual, expected.contains(actual));
                    remaining.remove(actual);
                }
                assertTrue(key.reset());
            }
            WatchKey extra = watcher.poll(200, TimeUnit.MILLISECONDS);
            assertNotSame("Rename must not signal the parent directory", parentKey, extra);
        }
        assertEquals(2, children.length);
        assertEquals(1, nestedChildren.length);
    }

    public void testRenameFileThroughPath() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject file = getFileObject();
        FileObject parent = file.getParent();
        FileObject[] children = parent.getChildren();
        Path filePath = getPath();
        Path renamedPath = filePath.resolveSibling("nio-renamed.txt");
        BlockingQueue<String> fileChangeEvents = new LinkedBlockingQueue<>();
        FileChangeListener listener = createEventListener(fileChangeEvents);
        parent.addFileChangeListener(listener);
        try (WatchService watcher = filePath.getFileSystem().newWatchService()) {
            filePath.getParent().register(
                    watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            Files.move(filePath, renamedPath);

            assertWatchEvents(
                    watcher,
                    new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_DELETE, filePath.getFileName()),
                    new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_CREATE, renamedPath.getFileName()));
            parent.refresh();
            assertRenameEvents(fileChangeEvents, "renamed:nio-renamed.txt",
                    "deleted:file.txt", "dataCreated:nio-renamed.txt");
            assertNotNull(parent.getFileObject("nio-renamed.txt"));
        } finally {
            parent.removeFileChangeListener(listener);
        }
        assertEquals(2, children.length);
    }

    public void testRenameFolderThroughPath() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        Path folderPath = getPath().getParent();
        Path parentPath = folderPath.getParent();
        Path renamedPath = parentPath.resolve("nio-renamed-folder");
        FileObject parent = getFileObject().getParent().getParent();
        FileObject[] children = parent.getChildren();
        FileObject folder = parent.getFileObject(folderPath.getFileName().toString());
        assertNotNull(folder);
        BlockingQueue<String> fileChangeEvents = new LinkedBlockingQueue<>();
        FileChangeListener listener = createEventListener(fileChangeEvents);
        parent.addFileChangeListener(listener);
        try (WatchService watcher = parentPath.getFileSystem().newWatchService()) {
            parentPath.register(
                    watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            Files.move(folderPath, renamedPath);

            assertWatchEvents(
                    watcher,
                    new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_DELETE, folderPath.getFileName()),
                    new ExpectedWatchEvent(StandardWatchEventKinds.ENTRY_CREATE, renamedPath.getFileName()));
            parent.refresh();
            assertRenameEvents(fileChangeEvents, "renamed:nio-renamed-folder",
                    "deleted:dir", "folderCreated:nio-renamed-folder");
            assertNotNull(parent.getFileObject("nio-renamed-folder"));
        } finally {
            parent.removeFileChangeListener(listener);
        }
        assertEquals(1, children.length);
    }

    public void testCreateDeleteAndRecreateThroughPath() throws Exception {
        if (testedFS.isReadOnly()) {
            return;
        }
        FileObject parent = getFileObject().getParent();
        FileObject[] children = parent.getChildren();
        Path parentPath = getPath().getParent();
        Path filePath = parentPath.resolve("nio-cycle.txt");
        BlockingQueue<String> fileChangeEvents = new LinkedBlockingQueue<>();
        FileChangeListener listener = createEventListener(fileChangeEvents);
        parent.addFileChangeListener(listener);
        try (WatchService watcher = parentPath.getFileSystem().newWatchService()) {
            parentPath.register(
                    watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            Files.createFile(filePath);
            assertWatchEvents(watcher, new ExpectedWatchEvent(
                    StandardWatchEventKinds.ENTRY_CREATE, filePath.getFileName()));
            parent.refresh();
            assertFileChangeEvents(fileChangeEvents, "dataCreated:nio-cycle.txt");
            FileObject created = parent.getFileObject("nio-cycle.txt");
            assertNotNull(created);

            Files.delete(filePath);
            assertWatchEvents(watcher, new ExpectedWatchEvent(
                    StandardWatchEventKinds.ENTRY_DELETE, filePath.getFileName()));
            parent.refresh();
            assertFileChangeEvents(fileChangeEvents, "deleted:nio-cycle.txt");
            assertFalse(created.isValid());
            assertNull(parent.getFileObject("nio-cycle.txt"));

            Files.createFile(filePath);
            assertWatchEvents(watcher, new ExpectedWatchEvent(
                    StandardWatchEventKinds.ENTRY_CREATE, filePath.getFileName()));
            parent.refresh();
            assertFileChangeEvents(fileChangeEvents, "dataCreated:nio-cycle.txt");
            FileObject recreated = parent.getFileObject("nio-cycle.txt");
            assertNotNull(recreated);
            assertNotSame(created, recreated);
        } finally {
            parent.removeFileChangeListener(listener);
        }
        assertEquals(2, children.length);
    }

    private FileChangeListener createEventListener(BlockingQueue<String> events) {
        return new FileChangeAdapter() {
            @Override
            public void fileFolderCreated(FileEvent event) {
                events.add("folderCreated:" + event.getFile().getNameExt());
            }

            @Override
            public void fileDataCreated(FileEvent event) {
                events.add("dataCreated:" + event.getFile().getNameExt());
            }

            @Override
            public void fileDeleted(FileEvent event) {
                events.add("deleted:" + event.getFile().getNameExt());
            }

            @Override
            public void fileRenamed(FileRenameEvent event) {
                events.add("renamed:" + event.getFile().getNameExt());
            }
        };
    }

    private void assertFileChangeEvents(BlockingQueue<String> events, String... expected)
            throws InterruptedException {
        List<String> remaining = new ArrayList<>(Arrays.asList(expected));
        while (!remaining.isEmpty()) {
            String event = events.poll(5, TimeUnit.SECONDS);
            assertNotNull("Expected FileChangeListener event " + remaining, event);
            assertTrue("Unexpected FileChangeListener event " + event, remaining.remove(event));
        }
    }

    private void assertRenameEvents(BlockingQueue<String> events, String renamed,
            String deleted, String created) throws InterruptedException {
        String first = events.poll(5, TimeUnit.SECONDS);
        assertNotNull("Expected a FileChangeListener rename event", first);
        if (renamed.equals(first) || deleted.equals(first) || created.equals(first)) {
            return;
        }
        String second = events.poll(5, TimeUnit.SECONDS);
        assertNotNull("Expected a second FileChangeListener event", second);
        Set<String> actual = new HashSet<>(Arrays.asList(first, second));
        assertEquals("Expected delete/create or rename event",
                new HashSet<>(Arrays.asList(deleted, created)), actual);
    }

    private void assertWatchEvents(WatchService watcher, ExpectedWatchEvent... expected) throws Exception {
        List<ExpectedWatchEvent> remaining = new ArrayList<>(Arrays.asList(expected));
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!remaining.isEmpty()) {
            long timeout = deadline - System.nanoTime();
            assertTrue("Expected WatchService events " + remaining, timeout > 0);
            WatchKey key = watcher.poll(timeout, TimeUnit.NANOSECONDS);
            assertNotNull("Expected WatchService events " + remaining, key);
            for (WatchEvent<?> event : key.pollEvents()) {
                for (Iterator<ExpectedWatchEvent> it = remaining.iterator(); it.hasNext();) {
                    ExpectedWatchEvent candidate = it.next();
                    if (candidate.kind == event.kind() && candidate.context.equals(event.context())) {
                        it.remove();
                        break;
                    }
                }
            }
            assertTrue("WatchService key is no longer valid", key.reset());
        }
    }

    private void assertWatchEventPresent(WatchService watcher, ExpectedWatchEvent expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (true) {
            long timeout = deadline - System.nanoTime();
            assertTrue("Expected WatchService event " + expected, timeout > 0);
            WatchKey key = watcher.poll(timeout, TimeUnit.NANOSECONDS);
            assertNotNull("Expected WatchService event " + expected, key);
            boolean found = false;
            for (WatchEvent<?> event : key.pollEvents()) {
                assertEquals("Unexpected WatchService event kind", expected.kind, event.kind());
                assertEquals("Unexpected WatchService event context", expected.context, event.context());
                found = true;
            }
            assertTrue("WatchService key is no longer valid", key.reset());
            if (found) {
                return;
            }
        }
    }

    private static final class ExpectedWatchEvent {
        private final WatchEvent.Kind<?> kind;
        private final Path context;

        ExpectedWatchEvent(WatchEvent.Kind<?> kind, Path context) {
            this.kind = kind;
            this.context = context;
        }

        @Override
        public String toString() {
            return kind.name() + ": " + context;
        }
    }

    private FileObject getFileObject() {
        FileObject fileObject = root.getFileObject("pathtest/dir/file.txt");
        assertNotNull(fileObject);
        return fileObject;
    }

    private Path getPath() {
        return FileUtil.toPath(getFileObject());
    }
}
