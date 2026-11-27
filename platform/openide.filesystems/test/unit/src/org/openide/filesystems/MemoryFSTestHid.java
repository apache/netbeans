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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Radek Matous
 */
public class MemoryFSTestHid extends TestBaseHid {

    /** Creates a new instance of MemoryFSTestHid */
    public MemoryFSTestHid(String testName) {
        super(testName);
    }

    protected String[] getResources(String testName) {
        return new String[]{};
    }


    public void test58331() throws Exception {
        FileObject p = this.testedFS.getRoot();
        FileObject fo = p.createData("test58331");//NOI18N
        assertEquals(fo.getParent(), p);
        String n = fo.getName();
        fo.delete();
        fo.refresh();
        fo.isFolder(); 
        p.createData(n);
    }

    public void testRootAttributes () throws Exception {
        FileObject file = FileUtil.createData(this.testedFS.getRoot(), "/folder/file");
        assertNotNull(file);
        FileObject root = this.testedFS.getRoot();
        assertNotNull(root);
        file.setAttribute("name", "value");
        assertEquals(file.getAttribute("name"), "value");
        root.setAttribute("rootName", "rootValue");
        assertEquals(root.getAttribute("rootName"), "rootValue");        
    }

    public void testURLs() throws Exception {
        FileObject file = FileUtil.createData(testedFS.getRoot(), "/folder/file");
        OutputStream os = file.getOutputStream();
        os.write("hello".getBytes());
        os.close();
        file.setAttribute("mimeType", "text/x-hello");
        URL u = file.toURL();
        assertEquals("/folder/file", u.getPath());
        URLConnection conn = u.openConnection();
        conn.connect();
        assertEquals(5, conn.getContentLength());
        assertEquals(file.lastModified().getTime(), conn.getLastModified());
        assertEquals("text/x-hello", conn.getContentType());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        conn.getInputStream().transferTo(baos);
        assertEquals("hello", baos.toString());
        assertEquals(file, URLMapper.findFileObject(u));
        assertEquals(null, URLMapper.findURL(file, URLMapper.EXTERNAL));
        assertEquals(null, URLMapper.findURL(file, URLMapper.NETWORK));
        assertEquals(u, new URL(file.getParent().toURL(), file.getNameExt()));
        assertEquals(testedFS.getRoot(), URLMapper.findFileObject(testedFS.getRoot().toURI().toURL()));
        assertEquals(file.getParent(), URLMapper.findFileObject(file.getParent().toURI().toURL()));
    }

    public void testURIs() throws Exception{
        FileObject file = FileUtil.createData(testedFS.getRoot(), "/folder/file.txt");
        URI uri = file.toURI();
        assertEquals("memory", uri.getScheme());
        Path path = FileUtil.toPath(file);
        URI u2 = path.toUri();
        assertEquals("memory", u2.getScheme());
        assertEquals("memory", path.getFileSystem().provider().getScheme());
    }

    public void testCopyAttributesThroughPath() throws Exception {
        FileObject source = testedFS.getRoot().createData("source.txt");
        source.setAttribute("custom", "value");
        Path target = FileUtil.toPath(source).resolveSibling("copy.txt");

        Files.copy(FileUtil.toPath(source), target, StandardCopyOption.COPY_ATTRIBUTES);
        assertEquals("value", FileUtil.toFileObject(target).getAttribute("custom"));
        assertNotNull(testedFS.getRoot().getFileObject("source.txt"));
    }

    public void testPathOutputStreamUsesFileObjectStream() throws Exception {
        FileObject file = testedFS.getRoot().createData("output-stream.txt");
        AbstractFileSystem fs = (AbstractFileSystem) testedFS;
        AbstractFileSystem.Info original = fs.info;
        AtomicInteger opens = new AtomicInteger();
        try {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("outputStream")) {
                    opens.incrementAndGet();
                }
            });
            List<Integer> direct = outputStreamOpenCounts(file.getOutputStream(), opens);
            for (StandardOpenOption[] options : new StandardOpenOption[][] {
                {},
                {StandardOpenOption.TRUNCATE_EXISTING},
                {StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
            }) {
                opens.set(0);
                List<Integer> path = outputStreamOpenCounts(
                        Files.newOutputStream(FileUtil.toPath(file), options), opens);
                assertEquals("Path writes should use the FileObject stream", direct, path);
            }
            for (StandardOpenOption[] options : new StandardOpenOption[][] {
                {},
                {StandardOpenOption.CREATE_NEW}
            }) {
                file.delete();
                opens.set(0);
                List<Integer> path = outputStreamOpenCounts(
                        Files.newOutputStream(FileUtil.toPath(testedFS.getRoot()).resolve("output-stream.txt"), options),
                        opens);
                assertEquals("A new path should use the FileObject stream", direct, path);
                file = testedFS.getRoot().getFileObject("output-stream.txt");
            }
        } finally {
            fs.info = original;
        }
    }

    private static List<Integer> outputStreamOpenCounts(OutputStream output, AtomicInteger opens)
            throws IOException {
        List<Integer> counts = new ArrayList<>();
        try (output) {
            counts.add(opens.get());
            output.write('A');
            counts.add(opens.get());
            output.write('B');
            counts.add(opens.get());
        }
        counts.add(opens.get());
        return counts;
    }

    public void testDirectoryStreamRelativePathAndNonDirectory() throws Exception {
        FileObject folder = testedFS.getRoot().createFolder("directory-stream");
        FileObject file = folder.createData("entry.txt");
        Path relative = FileUtil.toPath(folder).getFileSystem().getPath("directory-stream");
        assertFalse(relative.isAbsolute());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(relative)) {
            Iterator<Path> entries = stream.iterator();
            assertTrue(entries.hasNext());
            assertEquals(relative.resolve("entry.txt"), entries.next());
            assertFalse(entries.hasNext());
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileUtil.toPath(file))) {
            fail("A file is not a directory: " + stream);
        } catch (NotDirectoryException expected) {
        }
    }

    public void testDirectoryStreamIndependentIterators() throws Exception {
        FileObject folder = testedFS.getRoot().createFolder("directory-stream");
        folder.createData("first.txt");
        folder.createData("second.txt");
        DirectoryStream<Path> stream = Files.newDirectoryStream(FileUtil.toPath(folder));
        try {
            Iterator<Path> first = stream.iterator();
            Iterator<Path> second = stream.iterator();
            Iterator<Path> untouched = stream.iterator();
            Path firstEntry = first.next();
            assertEquals("Each iterator must start at the beginning", firstEntry, second.next());
            assertTrue(first.hasNext());
            stream.close();
            Path secondEntry = first.next();
            assertFalse(firstEntry.equals(secondEntry));
            assertFalse(first.hasNext());
            assertEquals(secondEntry, second.next());
            assertFalse(second.hasNext());
            assertEquals(firstEntry, untouched.next());
            assertEquals(secondEntry, untouched.next());
            assertFalse(untouched.hasNext());
            Iterator<Path> afterClose = stream.iterator();
            assertEquals(firstEntry, afterClose.next());
            assertEquals(secondEntry, afterClose.next());
            assertFalse(afterClose.hasNext());
        } finally {
            stream.close();
        }
    }

    public void testDirectoryStreamFilterCanCloseStream() throws Exception {
        FileObject folder = testedFS.getRoot().createFolder("directory-stream");
        folder.createData("entry.txt");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<DirectoryStream<Path>> reference = new AtomicReference<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileUtil.toPath(folder), entry -> {
            try {
                executor.submit(() -> {
                    reference.get().close();
                    return null;
                }).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            } catch (ExecutionException | TimeoutException ex) {
                throw new IOException(ex);
            }
            return true;
        })) {
            reference.set(stream);
            Iterator<Path> entries = stream.iterator();
            assertTrue(entries.hasNext());
            assertEquals(FileUtil.toPath(folder).resolve("entry.txt"), entries.next());
            assertFalse(entries.hasNext());
        } finally {
            executor.shutdownNow();
        }
    }

    public void testDirectoryStreamConcurrentIteration() throws Exception {
        FileObject folder = testedFS.getRoot().createFolder("directory-stream");
        folder.createData("first.txt");
        folder.createData("second.txt");
        CountDownLatch filtering = new CountDownLatch(2);
        AtomicInteger calls = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileUtil.toPath(folder), entry -> {
            if (calls.incrementAndGet() <= 2) {
                filtering.countDown();
                try {
                    if (!filtering.await(5, TimeUnit.SECONDS)) {
                        throw new IOException("Concurrent filter calls did not start");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException(ex);
                }
            }
            return true;
        })) {
            Iterator<Path> entries = stream.iterator();
            Future<Path> first = executor.submit(entries::next);
            Future<Path> second = executor.submit(entries::next);
            Set<Path> result = new HashSet<>();
            result.add(first.get(5, TimeUnit.SECONDS));
            result.add(second.get(5, TimeUnit.SECONDS));
            assertEquals(Set.of(FileUtil.toPath(folder).resolve("first.txt"),
                    FileUtil.toPath(folder).resolve("second.txt")), result);
            assertFalse(entries.hasNext());
        } finally {
            executor.shutdownNow();
        }
    }

    public void testChannelDoesNotHoldMonitorsInFileSystemLock() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject first = fs.getRoot().createData("first.txt");
        FileObject second = fs.getRoot().createData("second.txt");
        AbstractFileSystem.Info original = fs.info;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicInteger callbacks = new AtomicInteger();
        try (FileChannel other = FileChannel.open(FileUtil.toPath(second), StandardOpenOption.WRITE)) {
            fs.info = withInfoCallback(original, (method, args) -> {
                if ((method.equals("lock") || method.equals("unlock")
                        || method.equals("inputStream") || method.equals("outputStream"))
                        && "first.txt".equals(args[0])) {
                    callbacks.incrementAndGet();
                    Future<?> parallel = executor.submit(() -> {
                        try (java.nio.channels.FileLock held = other.tryLock()) {
                            assertNotNull(held);
                            other.position();
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    });
                    parallel.get(5, TimeUnit.SECONDS);
                }
            });
            try (FileChannel channel = FileChannel.open(FileUtil.toPath(first), StandardOpenOption.WRITE)) {
                try (java.nio.channels.FileLock held = channel.tryLock()) {
                    assertNotNull(held);
                }
                channel.write(ByteBuffer.wrap(new byte[] {1}));
                channel.force(false);
            }
            assertTrue("I/O and unlock callbacks must run", callbacks.get() >= 2);
        } finally {
            fs.info = original;
            executor.shutdownNow();
        }
    }

    public void testChannelCloseFlushesBufferedWriteAndReleasesFileObjectLock() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("writing.txt");
        AbstractFileSystem.Info original = fs.info;
        AtomicInteger unlocks = new AtomicInteger();
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("unlock") && "writing.txt".equals(args[0])) {
                    unlocks.incrementAndGet();
                }
            });
            channel.write(ByteBuffer.wrap(new byte[] {1}));
            assertEquals("Buffered content is not visible before close", 0, file.getSize());
            channel.close();
            assertEquals("Close writes buffered content", 1, file.getSize());
            assertEquals("Close releases the FileObject lock", 1, unlocks.get());
            assertFalse(file.isLocked());
        } finally {
            fs.info = original;
        }
    }

    public void testInterruptedOperationClosesOutsideStateMonitor() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("interrupt-writing.txt");
        FileObject otherFile = fs.getRoot().createData("interrupt-other.txt");
        AbstractFileSystem.Info original = fs.info;
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        CountDownLatch waitingStarted = new CountDownLatch(1);
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE);
                FileChannel other = FileChannel.open(FileUtil.toPath(otherFile), StandardOpenOption.WRITE)) {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("outputStream") && "interrupt-writing.txt".equals(args[0])) {
                    entered.countDown();
                    if (!proceed.await(5, TimeUnit.SECONDS)) {
                        throw new java.io.IOException("Timed out waiting for test");
                    }
                }
                if (method.equals("unlock") && "interrupt-writing.txt".equals(args[0])) {
                    Future<Long> probe = executor.submit(() -> other.position());
                    assertEquals(0, probe.get(5, TimeUnit.SECONDS).longValue());
                }
            });
            channel.write(ByteBuffer.wrap(new byte[] {1}));
            Future<?> force = executor.submit(() -> {
                channel.force(false);
                return null;
            });
            assertTrue("Force did not begin", entered.await(5, TimeUnit.SECONDS));
            Future<?> waiting = executor.submit(() -> {
                waitingThread.set(Thread.currentThread());
                waitingStarted.countDown();
                channel.position(1);
                return null;
            });
            assertTrue(waitingStarted.await(5, TimeUnit.SECONDS));
            assertWaiting(waitingThread.get());
            waitingThread.get().interrupt();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (channel.isOpen() && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            assertFalse("The interrupted operation must close the channel", channel.isOpen());
            proceed.countDown();
            try {
                waiting.get(5, TimeUnit.SECONDS);
                fail("An interrupted operation must fail");
            } catch (ExecutionException expected) {
                assertTrue(expected.getCause() instanceof ClosedByInterruptException);
            }
            try {
                force.get(5, TimeUnit.SECONDS);
                fail("The force must observe the close");
            } catch (ExecutionException expected) {
                assertTrue(expected.getCause() instanceof ClosedChannelException);
            }
            assertFalse(file.isLocked());
        } finally {
            proceed.countDown();
            fs.info = original;
            executor.shutdownNow();
        }
    }

    public void testChannelClosePersistsBufferedTruncateAndReleasesFileObjectLock() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("pending-update.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write(new byte[] {1, 2});
        }
        AbstractFileSystem.Info original = fs.info;
        AtomicInteger unlocks = new AtomicInteger();
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("unlock") && "pending-update.txt".equals(args[0])) {
                    unlocks.incrementAndGet();
                }
            });
            channel.truncate(1);
            assertEquals("Truncate is buffered until close", 2, file.getSize());
            channel.close();
            assertEquals(1, file.getSize());
            assertEquals("Close releases the FileObject lock", 1, unlocks.get());
            assertEquals("The lock must be released exactly once", 1, unlocks.get());
            assertFalse(file.isLocked());
        } finally {
            fs.info = original;
        }
    }

    public void testChannelBufferGrowthAndTruncateGap() throws Exception {
        FileObject file = testedFS.getRoot().createData("buffer-growth.txt");
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file),
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            channel.write(ByteBuffer.wrap(new byte[] {1, 2, 3, 4}));
            channel.truncate(1);
            channel.write(ByteBuffer.wrap(new byte[] {9}), 6);
            assertEquals(7, channel.size());
            ByteBuffer contents = ByteBuffer.allocate(7);
            assertEquals(7, channel.read(contents, 0));
            assertTrue(Arrays.equals(new byte[] {1, 0, 0, 0, 0, 0, 9}, contents.array()));
            channel.force(false);
            assertTrue(Arrays.equals(contents.array(), file.asBytes()));
            channel.write(ByteBuffer.wrap(new byte[] {8}), 7);
        }
        assertTrue(Arrays.equals(new byte[] {1, 0, 0, 0, 0, 0, 9, 8}, file.asBytes()));
    }

    public void testChannelOpenReleasesFileObjectLockOnFailure() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("failure.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write(1);
        }
        AbstractFileSystem.Info original = fs.info;
        AtomicBoolean fail = new AtomicBoolean(true);
        try {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("lock") && fail.getAndSet(false)) {
                    throw new java.io.IOException("Test lock failure");
                }
            });
            try {
                FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE).close();
                fail("A filesystem lock failure must be propagated");
            } catch (java.io.IOException expected) {
                assertEquals("Test lock failure", expected.getMessage());
            }
            assertFalse(file.isLocked());
            FileChannel truncated = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            fs.info = (AbstractFileSystem.Info) Proxy.newProxyInstance(
                    AbstractFileSystem.Info.class.getClassLoader(),
                    new Class<?>[] {AbstractFileSystem.Info.class}, (proxy, method, args) -> {
                        try {
                            Object result = method.invoke(original, args);
                            if (method.getName().equals("outputStream")) {
                                return new FilterOutputStream((OutputStream) result) {
                                    @Override
                                    public void write(byte[] b, int off, int len) throws java.io.IOException {
                                        throw new java.io.IOException("Test truncate failure");
                                    }
                                };
                            }
                            return result;
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    });
            try {
                truncated.force(false);
                fail("A truncate failure must be propagated");
            } catch (java.io.IOException expected) {
                assertEquals("Test truncate failure", expected.getMessage());
            }
            assertTrue("A failed force keeps the channel lock until close", file.isLocked());
            fs.info = original;
            truncated.close();
            assertFalse("Closing the channel releases its FileObject lock", file.isLocked());
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("size") && "failure.txt".equals(args[0])) {
                    throw new IllegalStateException("Test append initialization failure");
                }
            });
            try {
                FileChannel.open(FileUtil.toPath(file), StandardOpenOption.APPEND).close();
                fail("An append initialization failure must be propagated");
            } catch (IllegalStateException expected) {
                assertEquals("Test append initialization failure", expected.getMessage());
            }
            assertFalse("Failed append initialization must release its FileObject lock", file.isLocked());
            fs.info = original;
            try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
                assertTrue(channel.isOpen());
            }
        } finally {
            fs.info = original;
        }
    }

    public void testChannelForceFailureRetainsBufferedChanges() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("write-failure.txt");
        AbstractFileSystem.Info original = fs.info;
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            fs.info = (AbstractFileSystem.Info) Proxy.newProxyInstance(
                    AbstractFileSystem.Info.class.getClassLoader(),
                    new Class<?>[] {AbstractFileSystem.Info.class}, (proxy, method, args) -> {
                        try {
                            Object result = method.invoke(original, args);
                            if (method.getName().equals("outputStream") && "write-failure.txt".equals(args[0])) {
                                return new FilterOutputStream((OutputStream) result) {
                                    @Override
                                    public void write(byte[] b, int off, int len) throws java.io.IOException {
                                        throw new java.io.IOException("Test write failure");
                                    }
                                };
                            }
                            return result;
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    });
            ByteBuffer source = ByteBuffer.allocateDirect(4);
            source.put(new byte[] {'-', 'x', 'y', '-'}).flip();
            source.position(1);
            source.limit(3);
            source = source.asReadOnlyBuffer();
            channel.position(1);
            assertEquals(2, channel.write(source));
            assertEquals("A buffered write consumes its source", 3, source.position());
            assertEquals("A buffered write advances the channel", 3, channel.position());
            try {
                channel.force(false);
                fail("A force failure must be propagated");
            } catch (java.io.IOException expected) {
                assertEquals("Test write failure", expected.getMessage());
            }
            fs.info = original;
            channel.force(false);
            assertTrue(Arrays.equals(new byte[] {0, 'x', 'y'}, file.asBytes()));
        } finally {
            fs.info = original;
        }
    }

    public void testGatheringWriteCommitsPositionAtEnd() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("gathering-position.txt");
        AbstractFileSystem.Info original = fs.info;
        AtomicInteger writes = new AtomicInteger();
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("outputStream") && "gathering-position.txt".equals(args[0])) {
                    writes.incrementAndGet();
                    assertEquals("Force preserves the committed position", 2, channel.position());
                }
            });
            assertEquals(2, channel.write(new ByteBuffer[] {
                ByteBuffer.wrap(new byte[] {'a'}), ByteBuffer.wrap(new byte[] {'b'})
            }));
            assertEquals(0, writes.get());
            assertEquals(2, channel.position());
            channel.force(false);
            assertEquals(1, writes.get());
            assertEquals("ab", new String(file.asBytes(), StandardCharsets.UTF_8));
        } finally {
            fs.info = original;
        }
    }

    public void testGatheringWriteFailureCommitsCompletedBuffers() throws Exception {
        FileObject file = testedFS.getRoot().createData("gathering-failure.txt");
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            ByteBuffer first = ByteBuffer.wrap(new byte[] {'a'});
            try {
                channel.write(new ByteBuffer[] {first, null});
                fail("A null buffer must be rejected");
            } catch (NullPointerException expected) {
            }
            assertEquals(1, first.position());
            assertEquals("The completed write must advance the channel", 1, channel.position());
            channel.force(false);
            assertEquals("a", new String(file.asBytes(), StandardCharsets.UTF_8));
        }
    }

    public void testRelativeReadWaitsForPositionChangingWrite() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("read-during-write.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write("abc".getBytes(StandardCharsets.UTF_8));
        }
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file),
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            assertEquals(1, channel.write(ByteBuffer.wrap(new byte[] {'X'})));
            assertEquals(1, channel.position());
            ByteBuffer read = ByteBuffer.allocate(1);
            assertEquals(1, channel.read(read));
            assertEquals("b", new String(read.array(), StandardCharsets.UTF_8));
            assertEquals(2, channel.position());
            channel.force(false);
            assertEquals("Xbc", new String(file.asBytes(), StandardCharsets.UTF_8));
        }
    }

    public void testConcurrentPositionedWritesDoNotLoseUpdates() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("concurrent-write.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write("abcd".getBytes(StandardCharsets.UTF_8));
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            Future<Integer> write1 = executor.submit(() -> channel.write(ByteBuffer.wrap(new byte[] {'X'}), 0));
            Future<Integer> write2 = executor.submit(() -> channel.write(ByteBuffer.wrap(new byte[] {'Y'}), 1));
            assertEquals(1, write1.get(5, TimeUnit.SECONDS).intValue());
            assertEquals(1, write2.get(5, TimeUnit.SECONDS).intValue());
            channel.force(false);
            assertEquals("XYcd", new String(file.asBytes(), StandardCharsets.UTF_8));
            assertEquals("Positioned writes must not change the channel position", 0, channel.position());
        } finally {
            executor.shutdownNow();
        }
    }

    public void testReentrantWriteIsRejected() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("reentrant-write.txt");
        AbstractFileSystem.Info original = fs.info;
        AtomicBoolean checked = new AtomicBoolean();
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.WRITE)) {
            fs.info = withInfoCallback(original, (method, args) -> {
                if (method.equals("outputStream") && "reentrant-write.txt".equals(args[0])) {
                    try {
                        channel.write(ByteBuffer.wrap(new byte[] {'Y'}), 0);
                        fail("A reentrant write must not overwrite an in-progress update");
                    } catch (java.io.IOException expected) {
                        assertEquals("Reentrant channel operation during a write", expected.getMessage());
                        checked.set(true);
                    }
                }
            });
            assertEquals(1, channel.write(ByteBuffer.wrap(new byte[] {'X'})));
            channel.force(false);
            assertTrue("The filesystem callback must run", checked.get());
            assertEquals("X", new String(file.asBytes(), StandardCharsets.UTF_8));
        } finally {
            fs.info = original;
        }
    }

    public void testChannelReadsUseCachedContent() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("read-retry.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write("abcdef".getBytes(StandardCharsets.UTF_8));
        }
        AbstractFileSystem.Info original = fs.info;
        AtomicInteger inputStreams = new AtomicInteger();
        fs.info = withInfoCallback(original, (method, args) -> {
            if (method.equals("inputStream") && "read-retry.txt".equals(args[0])) {
                inputStreams.incrementAndGet();
            }
        });
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.READ)) {
            assertEquals("Opening the channel loads the file once", 1, inputStreams.get());
            ByteBuffer dst = ByteBuffer.allocate(2);
            assertEquals(2, channel.read(dst));
            assertEquals("ab", new String(dst.array(), StandardCharsets.UTF_8));
            channel.position(3);
            ByteBuffer positioned = ByteBuffer.allocate(2);
            assertEquals(2, channel.read(positioned, 3));
            assertEquals("de", new String(positioned.array(), StandardCharsets.UTF_8));
            ByteBuffer[] parts = {ByteBuffer.allocate(1), ByteBuffer.allocate(1)};
            channel.position(4);
            assertEquals(2, channel.read(parts, 0, parts.length));
            assertEquals("e", new String(parts[0].array(), StandardCharsets.UTF_8));
            assertEquals("f", new String(parts[1].array(), StandardCharsets.UTF_8));
            assertEquals("Reads reuse the content loaded on open", 1, inputStreams.get());
        } finally {
            fs.info = original;
        }
    }

    public void testChannelReadsOnlyRequestDestinationCapacity() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("bounded-read.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write(new byte[10000]);
        }
        AbstractFileSystem.Info original = fs.info;
        AtomicInteger inputStreams = new AtomicInteger();
        fs.info = withInfoCallback(original, (method, args) -> {
            if (method.equals("inputStream") && "bounded-read.txt".equals(args[0])) {
                inputStreams.incrementAndGet();
            }
        });
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.READ)) {
            assertEquals(1, inputStreams.get());
            ByteBuffer relative = ByteBuffer.allocate(4);
            relative.position(2);
            assertEquals(2, channel.read(relative));
            assertEquals(1, channel.read(ByteBuffer.allocate(1), 10));
            assertEquals(3, channel.read(new ByteBuffer[] {ByteBuffer.allocate(1), ByteBuffer.allocate(2)}));
            assertEquals(8192, channel.read(ByteBuffer.allocate(10000)));
            assertEquals("Reads do not reopen the file", 1, inputStreams.get());
        } finally {
            fs.info = original;
        }
    }

    public void testConcurrentChannelReadsCommitDistinctRanges() throws Exception {
        MemoryFileSystem fs = (MemoryFileSystem) testedFS;
        FileObject file = fs.getRoot().createData("concurrent-read.txt");
        try (OutputStream out = file.getOutputStream()) {
            out.write("abcdef".getBytes(StandardCharsets.UTF_8));
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try (FileChannel channel = FileChannel.open(FileUtil.toPath(file), StandardOpenOption.READ)) {
            Future<String> first = executor.submit(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(2);
                assertEquals(2, channel.read(buffer));
                return new String(buffer.array(), StandardCharsets.UTF_8);
            });
            Future<String> second = executor.submit(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(2);
                assertEquals(2, channel.read(buffer));
                return new String(buffer.array(), StandardCharsets.UTF_8);
            });
            Set<String> actual = new HashSet<>();
            actual.add(first.get(5, TimeUnit.SECONDS));
            actual.add(second.get(5, TimeUnit.SECONDS));
            assertEquals(Set.of("ab", "cd"), actual);
            assertEquals(4, channel.position());
        } finally {
            executor.shutdownNow();
        }
    }

    private void assertWaiting(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertEquals("The second operation must be waiting", Thread.State.WAITING, thread.getState());
    }

    @FunctionalInterface
    private interface InfoCallback {
        void invoke(String method, Object[] args) throws Exception;
    }

    private static AbstractFileSystem.Info withInfoCallback(
            AbstractFileSystem.Info original, InfoCallback callback) {
        return (AbstractFileSystem.Info) Proxy.newProxyInstance(
                AbstractFileSystem.Info.class.getClassLoader(),
                new Class<?>[] {AbstractFileSystem.Info.class}, (proxy, method, args) -> {
                    callback.invoke(method.getName(), args);
                    try {
                        return method.invoke(original, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                });
    }
}
