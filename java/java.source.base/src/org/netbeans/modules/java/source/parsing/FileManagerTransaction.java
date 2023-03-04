/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.*;
import java.net.URL;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.indexing.JavaIndexerWorker;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.openide.filesystems.FileSystem;
import org.openide.util.Parameters;

/**
 * Transaction service for {@link JavaFileManager} IO operations.
 * @author Tomas Zezula
 * @author Svata Dedic
 */
public abstract class FileManagerTransaction extends TransactionContext.Service {
    
    private final boolean writeable;
    private Junction junction;

    protected FileManagerTransaction(boolean writeable) {
        this.writeable = writeable;
    }
    
    /**
     * Returns true if the {@link FileManagerTransaction} supports write
     * operations.
     * @return true if the {@link FileManagerTransaction} is writable.
     */
    public final boolean canWrite() {
        return writeable;
    }

    /**
     * Notifies the {@link FileManagerTransaction} about deletion of file.
     * @param file the deleted file.
     */
    public abstract void delete (@NonNull final File file);

    /**
     * Filters the result of underlaying {@link JavaFileManager#list} operation.
     * The method removes all files which were notified as deleted in given package
     * and adds all files created in given package.
     * @param location location to filter
     * @param packageName the package which was listed.
     * @param files the {@link JavaFileObject}s from underlaying {@link JavaFileManager#list} operation.
     * @return the filtered files.
     */
    @NonNull
    abstract Iterable<JavaFileObject> filter (
        @NonNull Location location,
        @NonNull String packageName,
        @NonNull Iterable<JavaFileObject> files);

    /**
     * Creates {@link JavaFileObject} suitable for output. 
     * @param file file to produce
     * @param root root directory for the package structure
     * @param filter output filter
     * @param encoding desired file encoding
     * @return 
     */
    @NonNull
    public abstract JavaFileObject  createFileObject(
            @NonNull Location location,
            @NonNull File file,
            @NonNull File root,
            @NullAllowed JavaFileFilterImplementation filter,
            @NullAllowed Charset encoding);

    /**
     * Looks up a FileObject suitable for reading. The method MAY return {@code null}, if the FileObject
     * is not part of the transaction.
     * 
     * @param dirName
     * @param relativeName
     * @return 
     */
    @CheckForNull
    JavaFileObject  readFileObject(
            @NonNull Location location,
            @NonNull String dirName,
            @NonNull String relativeName) {
        return null;
    }

    @CheckForNull
    final CompletionHandler<Void,Void> getAsyncHandler() {
        return junction;
    }
    
    /**
     * Creates write back implementation of {@link FileManagerTransaction}.
     * The write back implementation isolates modification from underlaying
     * cache. The changes are made visible by commit.
     * @param root for which the {@link FileManagerTransaction} should be created.
     * @return the write back implementation of {@link FileManagerTransaction}.
     */
    public static FileManagerTransaction writeBack(URL root) {
        return new WriteBackTransaction(root);
    }

    /**
     * Creates write through implementation of {@link FileManagerTransaction}.
     * The write through implementation propagates write operations directly to caches,
     * the changes are visible before commit.
     * @return the write through implementation of {@link FileManagerTransaction}.
     */
    public static FileManagerTransaction writeThrough() {
        return new WriteThrogh();
    }

    /**
     * Creates read only implementation of {@link FileManagerTransaction}.
     * The read only implementation supports only read operations the write operations
     * are throwing {@link UnsupportedOperationException}
     * @return the read only implementation of {@link FileManagerTransaction}.
     */
    public static FileManagerTransaction read() {
        return new Read();
    }
    
    /**
     * Ignores all writes, but acts as if they succeeded.
     */
    public static FileManagerTransaction nullWrite() {
        return new Null();
    }

    /**
     * Writes allowed only to {@link LazyTreeLoader}.
     * @return the new {@link FileManagerTransaction}
     */
    public static FileManagerTransaction treeLoaderOnly() {
        return new TreeLoaderOnly();
    }

    public static Future<Void> runConcurrent(@NonNull final FileSystem.AtomicAction action) throws IOException {
        Parameters.notNull("action", action);   //NOI18N
        final FileManagerTransaction fmtx = TransactionContext.get().get(FileManagerTransaction.class);
        if (fmtx == null) {
            throw new IllegalStateException("No FileManagerTransaction");   //NOI18N
        }
        final Future<Void> res;
        fmtx.fork();
        try {
            action.run();
        } finally {
            res = fmtx.join();
        }
        return res;
    }

    private void fork() {
        junction = new Junction();
    }

    @NonNull
    private Future<Void> join() {
        final Junction result = junction;
        junction = null;
        assert result != null;
        return result;
    }

    private static class WriteThrogh extends FileManagerTransaction {
        
        private WriteThrogh() {
            super(true);
        }

        @Override
        public void delete (@NonNull final File file) {
            assert file != null;
            file.delete();
        }

        @Override
        @NonNull
        Iterable<JavaFileObject> filter(
                @NonNull final Location location,
                @NonNull final String packageName,
                @NonNull final Iterable<JavaFileObject> files) {
            return files;
        }

        @Override
        protected void commit() throws IOException {
        }

        @Override
        protected void rollBack() throws IOException {
            //generally rollBack is not supported
            //but for initial scan followed by other one is NOP OK.
        }

        @Override
        @NonNull
        public JavaFileObject createFileObject(
                @NonNull final Location location,
                @NonNull final File file,
                @NonNull final File root,
                @NullAllowed final JavaFileFilterImplementation filter,
                @NullAllowed final Charset encoding) {
            final CompletionHandler<Void,Void> handler = getAsyncHandler();
            return handler == null || !JavaIndexerWorker.supportsConcurrent()?
                FileObjects.fileFileObject(file, root, filter, encoding) :
                FileObjects.asyncWriteFileObject(
                    file,
                    root,
                    filter,
                    encoding,
                    JavaIndexerWorker.getExecutor(),
                    handler);
        }                
    }
    
    private static class Null extends FileManagerTransaction {

        public Null() {
            super(true);
        }
        
        @Override
        public void delete (@NonNull final File file) {
            // NOP
        }

        @Override
        @NonNull
        public JavaFileObject createFileObject(
                @NonNull final Location location,
                @NonNull final File file,
                @NonNull final File root,
                @NullAllowed final JavaFileFilterImplementation filter,
                @NullAllowed final Charset encoding) {
            InferableJavaFileObject ifo = FileObjects.fileFileObject(file, root, filter, encoding);
            return FileObjects.nullWriteFileObject(ifo);
        }

        @Override
        @NonNull
        Iterable<JavaFileObject> filter(
                @NonNull final Location location,
                @NonNull final String packageName,
                @NonNull final Iterable<JavaFileObject> files) {
            return files;
        }

        @Override
        protected void commit() throws IOException {
            //NOP
        }

        @Override
        protected void rollBack() throws IOException {
            //NOP
        }
    }

    private static class Read extends FileManagerTransaction {
        
        private Read() {
            super(false);
        }

        @Override
        public void delete (@NonNull final File file) {
            throw new UnsupportedOperationException ("Delete not supported, read-only.");   //NOI18N
        }

        @Override
        @NonNull
        public JavaFileObject createFileObject(
                @NonNull final Location location,
                @NonNull final File file,
                @NonNull final File root,
                @NullAllowed final JavaFileFilterImplementation filter,
                @NullAllowed final Charset encoding) {
            throw new UnsupportedOperationException ("Create File not supported, read-only.");   //NOI18N
        }

        @Override
        @NonNull
        Iterable<JavaFileObject> filter(
                @NonNull final Location location,
                @NonNull String packageName,
                @NonNull final Iterable<JavaFileObject> files) {
            return files;
        }

        @Override
        protected void commit() throws IOException {
            //NOP
        }

        @Override
        protected void rollBack() throws IOException {
            //NOP
        }

    }

    private static final class TreeLoaderOnly extends FileManagerTransaction {
        private final FileManagerTransaction nullWrite;
        private final FileManagerTransaction writeThrough;

        TreeLoaderOnly() {
            super(true);
            nullWrite = nullWrite();
            writeThrough = writeThrough();
        }

        @Override
        public void delete(File file) {
            getDelegate().delete(file);
        }

        @Override
        Iterable<JavaFileObject> filter(Location location, String packageName, Iterable<JavaFileObject> files) {
            return getDelegate().filter(location, packageName, files);
        }

        @Override
        public JavaFileObject createFileObject(Location location, File file, File root, JavaFileFilterImplementation filter, Charset encoding) {
            return getDelegate().createFileObject(location, file, root, filter, encoding);
        }

        @Override
        protected void commit() throws IOException {
            nullWrite.commit();
            writeThrough.commit();
        }

        @Override
        protected void rollBack() throws IOException {
            nullWrite.commit();
            writeThrough.commit();
        }

        @NonNull
        private FileManagerTransaction getDelegate() {
            return nullWrite;
        }
    }

    private static class Junction implements Runnable, CompletionHandler<Void, Void>, Future<Void> {

        private final Lock lck;
        private final Condition cnd;
        //@GuardedBy("lck")
        private int running;

        Junction() {
            lck = new ReentrantLock();
            cnd = lck.newCondition();
        }

        @Override
        public void run() {
            lck.lock();
            try {
                running++;
            } finally {
                lck.unlock();
            }
        }

        @Override
        public void completed(Void result, Void attachment) {
            done();
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            done();
        }

        private void done() {
            lck.lock();
            try {
                running--;
                if (running == 0) {
                    cnd.signalAll();
                }
            } finally {
                lck.unlock();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            lck.lock();
            try {
                return running == 0;
            } finally {
                lck.unlock();
            }
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            lck.lock();
            try {
                while (running > 0) {
                    cnd.await();
                }
            } finally {
                lck.unlock();
            }
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            lck.lock();
            try {
                while (running > 0) {
                    if(!cnd.await(timeout, unit)) {
                        throw new TimeoutException();
                    }
                }
            } finally {
                lck.unlock();
            }
            return null;
        }
    }
}
