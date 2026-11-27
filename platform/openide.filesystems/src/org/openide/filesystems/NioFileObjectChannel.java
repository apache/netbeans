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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

final class NioFileObjectChannel extends FileChannel {
    /*
     * State.class guards channel state. A writable channel owns its FileObject lock
     * until close, even if an operation is still running. All operations reserve
     * the channel while accessing its in-memory content; a reentrant operation
     * cannot safely wait for its own reservation.
     */
    private final FileObject file;
    private final NioLock nioLock;
    private final boolean readable;
    private final boolean append;
    // A reentrant close cannot wait for its own operation, so that operation
    // releases the FileObject lock on exit. Keep this outside State: changing
    // its snapshot during the operation would invalidate its completion.
    private volatile boolean closeLockDeferred;
    // @GuardedBy("State.class") Every transition creates a fresh snapshot.
    // all methods manipulating this field should be inside of {@link State} class
    private State state;
    private NioFileObjectChannel(FileObject file, FileLock fileLock, boolean readable, boolean append)
            throws IOException {
        this.file = file;
        this.nioLock = fileLock == null ? null : new NioLock(this, fileLock);
        this.readable = readable;
        this.append = append;
        ByteBuffer content = ByteBuffer.wrap(file.asBytes());
        this.state = new State(append ? content.limit() : 0, null, content, false);
    }

    static FileChannel open(NioPathForFileObject path, Set<? extends OpenOption> options,
            FileAttribute<?>... attrs) throws IOException {
        boolean read = options.contains(StandardOpenOption.READ);
        boolean write = options.contains(StandardOpenOption.WRITE);
        boolean append = options.contains(StandardOpenOption.APPEND);
        if (append && read || append && options.contains(StandardOpenOption.TRUNCATE_EXISTING)) {
            throw new IllegalArgumentException("Invalid append options");
        }
        write |= append;
        if (!read && !write) {
            read = true;
        }
        for (OpenOption option : options) {
            Objects.requireNonNull(option, "option");
            if (
                option != StandardOpenOption.READ &&
                option != StandardOpenOption.WRITE &&
                option != StandardOpenOption.APPEND &&
                option != StandardOpenOption.CREATE &&
                option != StandardOpenOption.CREATE_NEW &&
                option != StandardOpenOption.TRUNCATE_EXISTING
            ) {
                throw new UnsupportedOperationException("Unsupported open option: " + option);
            }
        }
        if (attrs.length != 0) {
            throw new UnsupportedOperationException("Initial attributes are not supported");
        }
        FileObject file = findFile(path, options, write);
        FileLock token = write ? file.lock() : null;
        NioFileObjectChannel channel = null;
        try {
            if (token != null && !token.isValid()) {
                throw new IOException("FileObject does not support locking: " + file);
            }
            channel = new NioFileObjectChannel(file, token, read, append);
            if (write && options.contains(StandardOpenOption.TRUNCATE_EXISTING)) {
                channel.truncate(0);
            }
            return channel;
        } catch (IOException | RuntimeException | Error ex) {
            try {
                if (channel != null) {
                    channel.close();
                } else if (token != null) {
                    token.releaseLock();
                }
            } catch (IOException | RuntimeException | Error closeException) {
                ex.addSuppressed(closeException);
            }
            throw ex;
        }
    }

    static FileObject findFile(NioPathForFileObject path, Set<? extends OpenOption> options,
            boolean write) throws IOException {
        FileObject file = path.getFileObject();
        if (file != null && file.isValid() && write && options.contains(StandardOpenOption.CREATE_NEW)) {
            throw new FileAlreadyExistsException(path.toString());
        }
        if (file == null || !file.isValid()) {
            if (!write || !(options.contains(StandardOpenOption.CREATE) || options.contains(StandardOpenOption.CREATE_NEW))) {
                throw new NoSuchFileException(path.toString());
            }
            NioPathForFileObject parent = (NioPathForFileObject) path.getParent();
            FileObject folder = parent == null ? null : parent.getFileObject();
            if (folder == null || !folder.isValid()) {
                throw new NoSuchFileException(path.toString());
            }
            file = folder.createData(path.getFileName().toString());
        }
        if (!file.isData()) {
            throw new IOException("Not a regular file: " + path);
        }
        if (write && file.isReadOnly()) {
            throw new java.nio.file.AccessDeniedException(path.toString());
        }
        return file;
    }

    private void checkOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    private ClosedByInterruptException interruptedOperation() throws IOException {
        try {
            close();
        } finally {
            Thread.currentThread().interrupt();
        }
        return new ClosedByInterruptException();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        checkReadable(dst);
        if (!dst.hasRemaining()) {
            return 0;
        }
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            int count = readImpl(dst, previous.position(), previous.content());
            if (count > 0) {
                next = previous.withPosition(previous.position() + count);
            }
            return count;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    @Override
    public int read(ByteBuffer dst, long at) throws IOException {
        checkReadable(dst);
        if (at < 0) {
            throw new IllegalArgumentException("Negative position");
        }
        if (!dst.hasRemaining()) {
            return 0;
        }
        State previous = State.beginOperation(this);
        try {
            return readImpl(dst, at, previous.content());
        } finally {
            State.endOperation(this, previous, previous);
        }
    }

    private void checkReadable(ByteBuffer dst) throws IOException {
        checkOpen();
        if (!readable) {
            throw new NonReadableChannelException();
        }
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        }
    }

    private static int readImpl(ByteBuffer dst, long at, ByteBuffer content) {
        int size = content.limit();
        if (at >= size) {
            return -1;
        }
        int start = (int) at;
        int count = Math.min(Math.min(dst.remaining(), 8192), size - start);
        dst.put(content.array(), start, count);
        return count;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        checkOpen();
        if (!readable) {
            throw new NonReadableChannelException();
        }
        Objects.checkFromIndexSize(offset, length, dsts.length);
        int maximum = 0;
        for (int i = offset; i < offset + length; i++) {
            checkReadable(dsts[i]);
            maximum += Math.min(dsts[i].remaining(), 8192 - maximum);
        }
        if (maximum == 0) {
            return 0;
        }
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            if (previous.position() >= previous.size()) {
                return -1;
            }
            int copied = 0;
            long at = previous.position();
            for (int i = offset; i < offset + length && copied < maximum; i++) {
                int count = Math.min(dsts[i].remaining(), maximum - copied);
                count = Math.min(count, previous.size() - (int) at);
                if (count > 0) {
                    dsts[i].put(previous.content().array(), (int) at, count);
                    at += count;
                    copied += count;
                }
            }
            if (copied > 0) {
                next = previous.withPosition(previous.position() + copied);
            }
            return copied;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            long at = append ? previous.size() : previous.position();
            int start = src.position();
            next = writeImpl(previous, src, at);
            int n = src.position() - start;
            next = next.withPosition(at + n);
            return n;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    @Override
    public int write(ByteBuffer src, long at) throws IOException {
        State previous = State.beginOperation(this);
        State next = previous;
        int start = src.position();
        try {
            next = writeImpl(previous, src, at);
            return src.position() - start;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    private State writeImpl(State state, ByteBuffer src, long at) throws IOException {
        if (nioLock == null) {
            throw new NonWritableChannelException();
        }
        if (at < 0) {
            throw new IllegalArgumentException("Negative position");
        }
        int start = src.position();
        int count = src.remaining();
        if (count == 0) {
            return state;
        }
        if (at > Integer.MAX_VALUE - count) {
            throw new IOException("File is too large for virtual channel");
        }
        int end = Math.max(state.size(), (int) at + count);
        ByteBuffer content = ensureCapacity(state.content(), end);
        if (at > state.size()) {
            Arrays.fill(content.array(), state.size(), (int) at, (byte) 0);
        }
        src.get(start, content.array(), (int) at, count);
        src.position(start + count);
        content.limit(end);
        return state.withContent(content).withDirty(true);
    }

    private static ByteBuffer ensureCapacity(ByteBuffer content, int size) {
        if (size > content.capacity()) {
            int doubled = content.capacity() <= Integer.MAX_VALUE / 2
                    ? content.capacity() * 2 : Integer.MAX_VALUE;
            return ByteBuffer.wrap(Arrays.copyOf(content.array(), Math.max(size, doubled)));
        }
        return content.duplicate();
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            if (nioLock == null) {
                throw new NonWritableChannelException();
            }
            Objects.checkFromIndexSize(offset, length, srcs.length);
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                long at = append ? next.size() : next.position();
                int start = srcs[i].position();
                next = writeImpl(next, srcs[i], at);
                int n = srcs[i].position() - start;
                next = next.withPosition(at + n);
                total += n;
            }
            return total;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    @Override
    public long position() throws IOException {
        checkOpen();
        return State.snapshot(this).position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            if (newPosition < 0) {
                throw new IllegalArgumentException("Negative position");
            }
            next = previous.withPosition(newPosition);
            return this;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    @Override
    public long size() throws IOException {
        checkOpen();
        return State.snapshot(this).size();
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            if (nioLock == null) {
                throw new NonWritableChannelException();
            }
            if (size < 0) {
                throw new IllegalArgumentException("Negative size");
            }
            if (size < previous.size()) {
                ByteBuffer content = previous.content().duplicate();
                content.limit((int) size);
                next = previous.withContent(content).withDirty(true);
            }
            next = next.withPosition(Math.min(previous.position(), size));
            return this;
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    @Override
    public void force(boolean metaData) throws IOException {
        State previous = State.beginOperation(this);
        State next = previous;
        try {
            if (previous.dirty()) {
                writeBytes(previous.content());
                next = previous.withDirty(false);
            }
        } finally {
            State.endOperation(this, previous, next);
        }
    }

    private void writeBytes(ByteBuffer content) throws IOException {
        try (OutputStream output = file.getOutputStream(nioLock.fileLock)) {
            output.write(content.array(), 0, content.limit());
        }
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        checkOpen();
        if (!readable) {
            throw new NonReadableChannelException();
        }
        Objects.requireNonNull(target, "target");
        if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        }
        long total = 0;
        ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(count, 8192));
        while (total < count) {
            buffer.clear();
            buffer.limit((int) Math.min(buffer.capacity(), count - total));
            int n = read(buffer, position + total);
            if (n <= 0) {
                break;
            }
            buffer.flip();
            int written = target.write(buffer);
            total += written;
            if (written < n) {
                break;
            }
        }
        return total;
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        checkOpen();
        if (nioLock == null) {
            throw new NonWritableChannelException();
        }
        Objects.requireNonNull(src, "src");
        if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        }
        if (position > size()) {
            return 0;
        }
        long total = 0;
        ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(count, 8192));
        while (total < count) {
            buffer.clear();
            buffer.limit((int) Math.min(buffer.capacity(), count - total));
            int n = src.read(buffer);
            if (n <= 0) {
                break;
            }
            buffer.flip();
            total += write(buffer, position + total);
        }
        return total;
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) {
        throw new UnsupportedOperationException("Mapping virtual files is not supported");
    }

    @Override
    public java.nio.channels.FileLock lock(long position, long size, boolean shared) throws IOException {
        return tryLock(position, size, shared);
    }

    @Override
    public java.nio.channels.FileLock tryLock(long position, long size, boolean shared) throws IOException {
        checkOpen();
        if (nioLock == null) {
            throw new NonWritableChannelException();
        }
        return nioLock;
    }

    @Override
    protected void implCloseChannel() throws IOException {
        State.channelClosed();
        if (!State.awaitOperationOnClose(this)) {
            closeLockDeferred = nioLock != null;
            return;
        }
        try {
            State snapshot = State.snapshot(this);
            if (nioLock != null && snapshot.dirty()) {
                writeBytes(snapshot.content());
            }
        } finally {
            if (nioLock != null) {
                nioLock.releaseFileLock();
            }
        }
    }

    private static final class NioLock extends java.nio.channels.FileLock {
        private final NioFileObjectChannel channel;
        private final FileLock fileLock;

        NioLock(NioFileObjectChannel channel, FileLock fileLock) {
            super(channel, 0, Long.MAX_VALUE, false);
            this.channel = channel;
            this.fileLock = fileLock;
        }

        @Override
        public boolean isValid() {
            return channel.isOpen();
        }

        @Override
        public void release() {
            // The FileObject lock belongs to the channel and is released on close.
        }

        private void releaseFileLock() {
            fileLock.releaseLock();
        }
    }

    // The buffer limit is the logical size; its shared array is accessed only
    // by the reserved operation or by close after the operation completes.
    private record State(long position, Thread operationOwner, ByteBuffer content, boolean dirty) {
        private static synchronized State snapshot(NioFileObjectChannel self) {
            return self.state;
        }

        private static synchronized boolean update(NioFileObjectChannel self, State previous, State next) {
            if (self.state != previous) {
                return false;
            } else {
                self.state = next;
                return true;
            }
        }

        private State withPosition(long value) {
            return new State(value, operationOwner, content, dirty);
        }

        private State withContent(ByteBuffer value) {
            return new State(position, operationOwner, value, dirty);
        }

        private State withDirty(boolean value) {
            return new State(position, operationOwner, content, value);
        }

        private int size() {
            return content.limit();
        }

        private static State beginOperation(NioFileObjectChannel self) throws IOException {
            try {
                synchronized (State.class) {
                    State previous = awaitOperation(self);
                    State next = new State(previous.position(), Thread.currentThread(),
                            previous.content(), previous.dirty());
                    if (!update(self, previous, next)) {
                        throw new IllegalStateException("Channel state changed during operation reservation");
                    }
                    return next;
                }
            } catch (InterruptedException ex) {
                throw self.interruptedOperation();
            }
        }

        private static synchronized State awaitOperation(NioFileObjectChannel self)
                throws IOException, InterruptedException {
            State snapshot;
            while ((snapshot = snapshot(self)).operationOwner() != null) {
                self.checkOpen();
                if (snapshot.operationOwner() == Thread.currentThread()) {
                    throw new IOException("Reentrant channel operation during a write");
                }
                State.class.wait();
            }
            self.checkOpen();
            return snapshot;
        }

        private static void endOperation(NioFileObjectChannel self, State previous, State next)
                throws IOException {
            boolean closed;
            boolean releaseDeferred;
            synchronized (State.class) {
                if (!update(self, previous,
                        new State(next.position(), null, next.content(), next.dirty()))) {
                    throw new IllegalStateException("Channel state changed during operation");
                }
                State.class.notifyAll();
                closed = !self.isOpen();
                releaseDeferred = closed && self.closeLockDeferred && self.nioLock != null;
                if (releaseDeferred) {
                    self.closeLockDeferred = false;
                }
            }
            if (releaseDeferred) {
                self.nioLock.releaseFileLock();
            }
            if (closed) {
                throw new ClosedChannelException();
            }
        }

        private static boolean awaitOperationOnClose(NioFileObjectChannel self) {
            boolean interrupted = false;
            synchronized (State.class) {
                while (self.state.operationOwner() != null) {
                    if (self.state.operationOwner() == Thread.currentThread()) {
                        return false;
                    }
                    try {
                        State.class.wait();
                    } catch (InterruptedException ex) {
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return true;
        }

        private static synchronized void channelClosed() {
            State.class.notifyAll();
        }

    }
}
