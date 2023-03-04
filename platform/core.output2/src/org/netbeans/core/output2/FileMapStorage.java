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
package org.netbeans.core.output2;

import java.util.logging.Logger;
import org.openide.util.NbBundle;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * An implementation of the Storage interface over a memory mapped file.
 *
 */
class FileMapStorage implements Storage {
    /** A file channel for reading/writing the mapped file */
    private FileChannel fileChannel;
    /** The base number of bytes to allocate when a getWriteBuffer for writing is
     * needed. */
    private static final int BASE_BUFFER_SIZE = 8196;
    /**
     * max possible range to map.. 1 MB
     */
    private static final long MAX_MAP_RANGE = 1024 * 1024;
    /**
     * Own request processor
     */
    private static final RequestProcessor RP = new RequestProcessor("FileMapStorage"); //NOI18N
    /**
     * List of storages that have not been disposed yet.
     */
    private static final Set<FileMapStorage> undisposed;
    /**
     * The byte getWriteBuffer that write operations write into.  Actual buffers are
     * provided for writing by calling master.slice(); this getWriteBuffer simply
     * pre-allocates a fairly large chunk of memory to reduce repeated
     * allocations.
     */
    private ByteBuffer master;
    /** A byte getWriteBuffer mapped to the contents of the output file, from which
     * content is read. */
    private MappedBufferResource contents;
    /** The number of bytes from the file that have been are currently mapped
     * into the contents ByteBuffer.  This will be checked on calls that read,
     * and if more than the currently mapped bytes are requested, the
     * contents bufffer will be replaced by a larger one */
    private long mappedRange;
    
    /**
     * start of the mapped range..
     */
    private long mappedStart;
    /**
     * The currently in use buffer.
     */
    private ByteBuffer buffer = null;
    /**
     * The number of bytes that have been written.
     */
    protected int bytesWritten = 0;
    /**
     * The file we are writing to.
     */
    private File outfile = null;
    
    private int outstandingBufferCount = 0;
    
    /**
     * Byte in the file that corresponds to logical start of the storage data.
     * Data before this offset are "forgotten".
     */
    private long startOffset = 0;

    private boolean closed;

    static {
        undisposed = new HashSet<FileMapStorage>();

        // Remove all remaining temporary files before exit.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (FileMapStorage fms : undisposed) {
                    if (fms.contents != null) {
                        fms.contents.releaseBuffer();
                    }
                    if (fms.fileChannel != null && fms.fileChannel.isOpen()) {
                        try {
                            fms.fileChannel.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (fms.outfile != null) {
                        fms.outfile.delete();
                    }
                }
            }
        });
    }

    FileMapStorage() {
        init();
    }

    private void init() {
        contents = null;
        mappedRange = -1;
        mappedStart = 0;
        master = ByteBuffer.allocateDirect (BASE_BUFFER_SIZE);
        fileChannel = null;
        buffer = null;
        bytesWritten = 0;
        closed = true;
        addUndisposed(this);
    }

    /**
     * Ensure that the output file exists.
     */
    private void ensureFileExists() throws IOException {
        if (outfile == null) {
            String outdir = System.getProperty("java.io.tmpdir"); //NOI18N
            if (!outdir.endsWith(File.separator)) {
                outdir += File.separator;
            }
            File dir = new File (outdir);
            if (!dir.exists()) {
                //Handle the event that we cannot find the system temporary directory
                IllegalStateException ise = new IllegalStateException ("Cannot find temp directory " + outdir); //NOI18N
                Exceptions.attachLocalizedMessage(ise, NbBundle.getMessage(OutWriter.class, "FMT_CannotWrite", outdir));
                throw ise;
            }
            //#47196 - if user holds down F9, many threads can enter this method
            //simultaneously and all try to create the same file
            synchronized (FileMapStorage.class) {
                StringBuilder fname = new StringBuilder(outdir)
                        .append("output").append(Long.toString(System.currentTimeMillis())); //NOI18N
                outfile = new File (fname.toString());
                while (outfile.exists()) {
                    fname.append('x'); //NOI18N
                    outfile = new File(fname.toString());
                }
                outfile.createNewFile();
                if (!outfile.exists() || !outfile.canWrite()) {
                    //Handle the (unlikely) case we cannot write to the system temporary directory
                    IllegalStateException ise = new IllegalStateException ("Cannot write to " + fname); //NOI18N
                    Exceptions.attachLocalizedMessage(ise, NbBundle.getMessage(OutWriter.class, "FMT_CannotWrite", outdir));
                    throw ise;
                }
                outfile.deleteOnExit();
            }
        }
    }
    
    @Override
    public String toString() {
        return outfile == null ? "[unused or disposed FileMapStorage]" : outfile.getPath();
    }
    
    private FileChannel writeChannel() throws IOException {
        FileChannel channel = fileChannel();
        closed = !channel.isOpen();
        return channel;
    }

    /**
     * Get a FileChannel opened for reading/writing against the output file.
     */
    private FileChannel fileChannel() throws IOException {
        if (fileChannel == null || !fileChannel.isOpen()) {
            ensureFileExists();
            RandomAccessFile raf = new RandomAccessFile(outfile, "rw");
            fileChannel = raf.getChannel();
        }
        return fileChannel;
    }

    /**
     * Fetch a getWriteBuffer of a specified size to use for appending new data to the
     * end of the file.
     */
    public synchronized ByteBuffer getWriteBuffer (int size) {
        if (master.capacity() - master.position() < size) {
            int newSize = Math.max (BASE_BUFFER_SIZE * 2, 
                size + BASE_BUFFER_SIZE);
            
            master = ByteBuffer.allocateDirect (newSize);
        }

        if (buffer == null) {
            buffer = master.slice();
        } else {
            int charsRemaining = AbstractLines.toCharIndex(buffer.capacity() - buffer.position());

            if (charsRemaining < size) {
                buffer.flip();
                buffer = master.slice();
            }
        }
        outstandingBufferCount++;
        return buffer;
    }

    /**
     * Dispose of a ByteBuffer which has been acquired for writing by one of
     * the write methods, writing its contents to the file.
     */
    public int write (ByteBuffer bb) throws IOException {
        synchronized (this) {
            if (bb == buffer) {
                buffer = null;
            }
        }
        int position = size();
        int byteCount = bb.position();
        bb.flip();
        FileChannel channel = writeChannel();
        if (channel.isOpen()) { //If a thread was terminated while writing, it will be closed
            Thread.interrupted(); // #186629: must clear interrupt flag or channel will be broken
            channel.write (bb);
            synchronized (this) {
                bytesWritten += byteCount;
                outstandingBufferCount--;
            }
        }
        return position;
    }

    @Override
    public synchronized void removeBytesFromEnd(int length) throws IOException {
        if (length == 0) {
            return;
        }
        FileChannel channel = writeChannel();
        channel.position(channel.position() - length);
        bytesWritten -= length;
    }

    public synchronized void dispose() {
        if (Controller.LOG) {
            Controller.log ("Disposing file map storage");
            Controller.logStack();
        }
        final FileChannel oldChannel = fileChannel;
        final File oldFile = outfile;
        final MappedBufferResource oldContents = contents;
        fileChannel = null;
        closed = true;
        outfile = null;
        buffer = null;
        contents = null;

        if (oldChannel != null || oldFile != null) {
            RP.post(new Runnable() {

                public void run() {
                    try {
                        if (oldContents != null) {
                            oldContents.releaseBuffer();
                        }
                        if (oldChannel != null && oldChannel.isOpen()) {
                            oldChannel.close();
                        }
                        if (oldFile != null && oldFile.exists()) {
                            oldFile.delete();
                        }
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }
        removeUndisposed(this);
    }

    File getOutputFile() {
        return outfile;
    }

    /**
     * Workaround for JDK issue #4715154 (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154)
     */
    private static void unmap(Object buffer) {
        try {
            Method getCleanerMethod = buffer.getClass().getMethod("cleaner");
            getCleanerMethod.setAccessible(true);
            /*sun.misc.Cleaner*/Object cleaner = getCleanerMethod.invoke(buffer);
            if (cleaner != null) {
                cleaner.getClass().getMethod("clean").invoke(cleaner);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Get a byte buffer representing the a getText of the contents of the
     * output file.  This is optimized to possibly map more of the output file
     * into memory if it is not already mapped.
     */
    public BufferResource<ByteBuffer> getReadBuffer(int start, int byteCount)
            throws IOException {

        ByteBuffer cont;
        long fileStart = startOffset + start;
        synchronized (this) {
            cont = this.contents == null ? null : this.contents.getBuffer();
            if (cont == null || fileStart + byteCount > mappedRange || fileStart < mappedStart) {
                FileChannel ch = fileChannel();
                mappedStart = Math.max((long)0, fileStart - (MAX_MAP_RANGE /2));
                long prevMappedRange = mappedRange;
                long map = byteCount > (MAX_MAP_RANGE / 2) ? (byteCount + byteCount / 10) : (MAX_MAP_RANGE / 2);
                mappedRange = Math.min(ch.size(), fileStart + map);
                try {
                    try {
                        cont = ch.map(FileChannel.MapMode.READ_ONLY, mappedStart, mappedRange - mappedStart);
                        updateContents(cont);
                    } catch (IOException ioe) {
                        Logger.getAnonymousLogger().info("Failed to memory map output file for reading. Trying to read it normally."); //NOI18N

                        // Memory mapping failed, fallback to non-mapped
                        cont = ByteBuffer.allocate((int) (mappedRange - mappedStart));
                        ch.read(cont, mappedStart);
                        updateContents(cont);
                    }
                } catch (Exception e) {
                    String msg = MessageFormat.format(
                            "Failed to read output file. Start:{0} bytes reqd={1}" //NOI18N
                            + " mapped range={2} previous mapped range={3} " //NOI18N
                            + "channel size: {4}", new Object[]{start, byteCount,
                                mappedRange, prevMappedRange, ch.size()});
                    throw new IOException(msg, e);
                }
            }
            if (fileStart - mappedStart > cont.limit() - byteCount) {
                cont.position(Math.max(0, cont.limit() - byteCount));
            } else {
                cont.position((int) (fileStart - mappedStart));
            }
        }
        int limit = Math.min(cont.limit(), byteCount);
        try {
            return new ChildBufferResource((ByteBuffer)cont.slice().limit(limit), this.contents);
        } catch (Exception e) {
            throw new IllegalStateException ("Error setting limit to " + limit //NOI18N
            + " contents size = " + cont.limit() + " requested: read " + //NOI18N
            "buffer from " + start + " to be " + byteCount + " bytes"); //NOI18N
        }
    }

    private void updateContents(ByteBuffer buffer) {
        if (this.contents != null) {
            this.contents.decRefs();
        }
        this.contents = new MappedBufferResource(buffer);
        this.contents.incRefs();
    }

    public synchronized int size() {
        return bytesWritten;
    }

    public void flush() throws IOException {
        if (buffer != null) {
            if (Controller.LOG) Controller.log("FILEMAP STORAGE flush(): " + outstandingBufferCount);
            write (buffer);
            fileChannel.force(false);
            buffer = null;
        }
    }

    public void close() throws IOException {
        if (fileChannel != null) {
            flush();
        }
        closed = true;
    }

    public boolean isClosed() {
        return fileChannel == null || closed;
    }

    private static synchronized void addUndisposed(FileMapStorage fms) {
        undisposed.add(fms);
    }

    private static synchronized void removeUndisposed(FileMapStorage fms) {
        undisposed.remove(fms);
    }

    @Override
    public void shiftStart(int byteOffset) {
        synchronized (this) {
            startOffset += byteOffset;
            bytesWritten -= byteOffset;
        }
    }

    private class ChildBufferResource implements BufferResource<ByteBuffer> {

        private ByteBuffer buffer;
        private MappedBufferResource parentResource;

        public ChildBufferResource(
                ByteBuffer buffer, MappedBufferResource parentResource) {

            this.buffer = buffer;
            this.parentResource = parentResource;
            this.parentResource.incRefs();
        }

        @Override
        public ByteBuffer getBuffer() {
            return buffer;
        }

        @Override
        public void releaseBuffer() {
            buffer = null;
            parentResource.decRefs();
        }
    }

    private class MappedBufferResource
            implements BufferResource<ByteBuffer> {

        private int refs = 0;
        private ByteBuffer buffer;

        public MappedBufferResource(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void releaseBuffer() {
            if (buffer != null) {
                unmap(buffer);
                buffer = null;
            }
        }

        @Override
        public ByteBuffer getBuffer() {
            return buffer;
        }

        synchronized void incRefs() {
            refs++;
        }

        synchronized void decRefs() {
            refs--;
            assert refs >= 0;
            if (refs == 0) {
                unmap(buffer);
                buffer = null;
            }
        }
    }
}
