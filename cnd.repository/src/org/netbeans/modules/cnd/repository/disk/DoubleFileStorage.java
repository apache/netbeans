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
package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.disk.index.ChunkInfo;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.testbench.WriteStatistics;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Stores Persistent objects in two files; the main purpose of the two files is
 * managing defragmentation: while one file is active (recent changes are
 * written into it), other one might be defragmented
 *
 */
public final class DoubleFileStorage implements FileStorage {

    private static final int MAINTENANCE_PERIOD = 10000;//10 seconds
    private long NextMaintainanceSize = 512*1024*1024;//512Mb
    private static final long SIZE_INCREASE_STEP = 256*1024*1024;//256Mb
    private final AtomicLong lastDefragmentationTime = new AtomicLong(0);
    private final File baseDir;
    private final LayeringSupport layeringSupport;
    private final LayerDescriptor layerDescriptor;
    private IndexedStorageFile cache_0_dataFile;
    private IndexedStorageFile cache_1_dataFile;
    private final AtomicBoolean cache_1_dataFileIsActive = new AtomicBoolean();
    private boolean defragmenting = false;
    private boolean openedForWriting = false;
    private boolean opened = false;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new {@code DoubleFileStorage} instance.
     *
     * @param basePath A File representing path to the storage
     * @param createCleanExistent A flag if the storage should be created, not
     * opened
     */
    //TODO: make it so that cache_X_dataFile is never null...
    DoubleFileStorage(File baseDir, LayerDescriptor layerDescriptor, LayeringSupport layeringSupport) {
        this.baseDir = baseDir;
        this.layeringSupport = layeringSupport;
        this.layerDescriptor = layerDescriptor;
    }

    boolean isOpened() {
        return opened;
    }

    @Override
    public synchronized boolean open(boolean openForWriting) {
        if (cache_0_dataFile == null || (openForWriting && !openedForWriting)) {
            try {
                close();
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }

            boolean writable = openForWriting;

            if (writable && !baseDir.isDirectory()) {
                baseDir.mkdirs();
            }

            IndexedStorageFile cache0 = null;
            IndexedStorageFile cache1 = null;
            try {
                cache0 = new IndexedStorageFile(layeringSupport, layerDescriptor, baseDir, "cache-0", // NOI18N
                        writable);
                cache1 = new IndexedStorageFile(layeringSupport, layerDescriptor, baseDir, "cache-1", // NOI18N
                        writable);

                boolean cache0IsEmpty = cache0.getDataFileUsedSize() == 0;
                boolean cache1IsEmpty = cache1.getDataFileUsedSize() == 0;

                if (cache0IsEmpty && !cache1IsEmpty) {
                    cache_1_dataFileIsActive.set(true);
                } else if (!cache0IsEmpty && !cache1IsEmpty) {
                    cache_1_dataFileIsActive.set(cache0.getFragmentationPercentage() > cache1.getFragmentationPercentage());
                } else {
                    cache_1_dataFileIsActive.set(false);
                }
            } catch (FileNotFoundException ex) {
                // TODO: handle this situation in a better way
                // hint: IndexedStorageFile constructor
                cache0 = null;
                return false;
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            } finally {
                cache_0_dataFile = cache0;
                cache_1_dataFile = cache1;
            }

            if (cache_0_dataFile == null || cache_1_dataFile == null) {
                throw new InternalError("Unhandled situation - failed to create cache_N_dataFile for DoubleFileStorage"); // NOI18N
            }

            if (openForWriting) {
                openedForWriting = true;
            }
        }
        opened = true;
        return true;
    }

    private boolean getFlag() {
        return cache_1_dataFileIsActive.get();
    }

    private IndexedStorageFile getFileByFlag(boolean flag) {
        return (flag ? cache_1_dataFile : cache_0_dataFile);
    }

    private IndexedStorageFile getActive() {
        return (cache_1_dataFileIsActive.get() ? cache_1_dataFile : cache_0_dataFile);
    }

    private IndexedStorageFile getPassive() {
        return (cache_1_dataFileIsActive.get() ? cache_0_dataFile : cache_1_dataFile);
    }

    @Override
    public void close() throws IOException {
        if (cache_0_dataFile != null) {
            cache_0_dataFile.close();
        }
        if (cache_1_dataFile != null) {
            cache_1_dataFile.close();
        }
    }

    @Override
    public boolean hasKey(LayerKey key) throws IOException {
        lock.readLock().lock();
        try {
            //
            boolean activeFlag = getFlag();
            //check in indec
            boolean hasKey =  getFileByFlag(activeFlag).hasKey(key) ;
            if (hasKey) {
                return true;
            }
            return getFileByFlag(!activeFlag).hasKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    

    @Override
    public ByteBuffer read(LayerKey key) throws IOException {
        lock.readLock().lock();
        try {
            boolean activeFlag = getFlag();
            ByteBuffer buffer = getFileByFlag(activeFlag).read(key);
            if (buffer == null) {
                buffer = getFileByFlag(!activeFlag).read(key);
            }
            return buffer;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void maintainIfNeeded() {
        try {
            //maintain is required if:
            //one of the files are bigger than 1 Gb and more than 10 seconds (10000 ms since last defragment)
            boolean activeFlag = getFlag();
            final IndexedStorageFile activeFile = getFileByFlag(activeFlag);
            long activeFileSize = activeFile.getSize();
            long passiveFileSize = getFileByFlag(!activeFlag).getSize();
            int activeFileFragmentationPercentage = activeFile.getFragmentationPercentage();
            if ((activeFileSize >= NextMaintainanceSize || passiveFileSize >= NextMaintainanceSize) &&
                    System.currentTimeMillis() - lastDefragmentationTime.get() >= MAINTENANCE_PERIOD) {
                //need to maintain
                defragment(true, Stats.maintenanceInterval);
                if (activeFileSize >= NextMaintainanceSize && activeFileFragmentationPercentage < 40) {
                    if (Stats.traceDefragmentation) {
                        System.out.printf(">>> Active file size %d  total fragmentation %d%%\n", activeFileSize, activeFileFragmentationPercentage); // NOI18N
                        System.out.printf("\tWill increase file size maintanance trashold on 256Mb\n"); // NOI18N
                    }       
                    NextMaintainanceSize += SIZE_INCREASE_STEP;
                }
            }
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        }
    }

    @Override
    public void write(final LayerKey key, final ByteBuffer data) throws IOException {
        if (Stats.writeStatistics) {
            WriteStatistics.instance().update(1);
        }
        lock.writeLock().lock();
        try {
            maintainIfNeeded();
            boolean activeFlag = getFlag();
            getFileByFlag(activeFlag).write(key, data);
            getFileByFlag(!activeFlag).remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(final LayerKey key) throws IOException {
        lock.writeLock().lock();
        try {
            boolean activeFlag = getFlag();
            getFileByFlag(activeFlag).remove(key);
            getFileByFlag(!activeFlag).remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean defragment(boolean doIt, final long timeout) throws IOException {

        boolean needMoreTime = false;

        if (Stats.writeStatistics) {
            WriteStatistics.instance().update(0);
        }

        if (Stats.traceDefragmentation) {
            System.out.printf(">>> Defragmenting %s; timeout %d ms total fragmentation %d%%\n", baseDir.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
            System.out.printf("\tActive:  %s\n", getActive().getTraceString()); // NOI18N
            System.out.printf("\tPassive: %s\n", getPassive().getTraceString()); // NOI18N
        }

        if (timeout > 0) {
            if (!defragmenting) {
                if (!doIt && (getFragmentationPercentage() < Stats.defragmentationThreashold)) {
                    if (Stats.traceDefragmentation) {
                        System.out.printf("\tFragmentation is too low\n"); // NOI18N
                    }
                    lastDefragmentationTime.set(System.currentTimeMillis());
                    return needMoreTime;
                }
            }
        }


        if (!defragmenting) {
            defragmenting = true;
            cache_1_dataFileIsActive.set(!cache_1_dataFileIsActive.get());
        }
        long startTime = System.currentTimeMillis();
        needMoreTime = _defragment(timeout);

        if (getPassive().getObjectsCount() == 0) {
            defragmenting = false;
        }

        if (Stats.traceDefragmentation) {
            System.out.printf("<<< Defragmenting %s; timeout %d ms total fragmentation %d%%\n", baseDir.getAbsolutePath(), (System.currentTimeMillis() - startTime), getFragmentationPercentage()); // NOI18N
            System.out.printf("\tActive:  %s\n", getActive().getTraceString()); // NOI18N
            System.out.printf("\tPassive: %s\n", getPassive().getTraceString()); // NOI18N
        }
        lastDefragmentationTime.set(System.currentTimeMillis());
        return needMoreTime;
    }

    private void move(IndexedStorageFile from, IndexedStorageFile to, LayerKey key) throws IOException {
        lock.writeLock().lock();
        try {
            ChunkInfo chunk = from.getChunkInfo(key);
            to.moveDataFromOtherFile(from, chunk.getOffset(), chunk.getSize(), key);
            from.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private boolean _defragment(final long timeout) throws IOException {

        boolean needMoreTime = false;
        //final long time = ((timeout > 0) || Stats.traceDefragmentation) ? System.currentTimeMillis() : 0;

        int cnt = 0;
        boolean activeFlag = getFlag();
        final IndexedStorageFile passiveFile = getFileByFlag(!activeFlag);
        final IndexedStorageFile activeFile = getFileByFlag(activeFlag);
        Iterator<LayerKey> it = passiveFile.getKeySetIterator();

        while (it.hasNext()) {
            LayerKey key = it.next();
            ChunkInfo chunk = passiveFile.getChunkInfo(key);
            if (chunk != null) {
                move(passiveFile, activeFile, key);
                cnt++;

//                if ((timeout > 0) && (cnt % 1000 == 0)) {
//                    if (System.currentTimeMillis() - time >= timeout) {
//                        needMoreTime = true;
//                        break;
//                    }
//                }
            } else {
                CndUtils.assertNotNull(chunk, "Null chunk when defragmenting " + passiveFile.getTraceString()); //NOI18N
            }
        }
        if (Stats.traceDefragmentation) {
            String text = it.hasNext() ? " finished by timeout" : " completed"; // NOI18N
            System.out.printf("\t # defragmentinging %s %s; moved: %d remaining: %d \n", // NOI18N
                    getFileByFlag(!activeFlag).getDataFileName(),
                    text,
                    cnt,
                    getFileByFlag(!activeFlag).getObjectsCount()); // NOI18N
        }
        return needMoreTime;
    }

    @Override
    public void dump(PrintStream ps) throws IOException {
        ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", baseDir.getAbsolutePath()); // NOI18N
        ps.printf("\nActive file:\n"); // NOI18N

        boolean activeFlag = getFlag();
        getFileByFlag(activeFlag).dump(ps);
        ps.printf("\nPassive file:\n"); // NOI18N
        getFileByFlag(!activeFlag).dump(ps);

        ps.printf("\n"); // NOI18N
    }

    @Override
    public void dumpSummary(PrintStream ps) throws IOException {
        ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", baseDir.getAbsolutePath()); // NOI18N
        ps.printf("\nActive file:\n"); // NOI18N

        boolean activeFlag = getFlag();
        getFileByFlag(activeFlag).dumpSummary(ps);
        ps.printf("\nPassive file:\n"); // NOI18N
        getFileByFlag(!activeFlag).dumpSummary(ps);

        ps.printf("\n"); // NOI18N
    }

    public int getFragmentationPercentage() throws IOException {
        final long fileSize;
        final float delta;

        boolean activeFlag = getFlag();
        fileSize = getFileByFlag(activeFlag).getSize() + getFileByFlag(!activeFlag).getSize();
        delta = fileSize - (getFileByFlag(activeFlag).getDataFileUsedSize() + getFileByFlag(!activeFlag).getDataFileUsedSize());
        final float percentage = delta * 100 / fileSize;
        return Math.round(percentage);
    }

    public long getSize() throws IOException {
        boolean activeFlag = getFlag();
        return getFileByFlag(activeFlag).getSize() + getFileByFlag(!activeFlag).getSize();
    }

    @Override
    public int getObjectsCount() {
        boolean activeFlag = getFlag();
        return getFileByFlag(activeFlag).getObjectsCount() + getFileByFlag(!activeFlag).getObjectsCount();
    }

    @Override
    public void debugDump(LayerKey key) {
        // not implemented so far
    }

    @Override
    public boolean maintenance(long timeout) throws IOException {
        if (cache_0_dataFile == null || cache_1_dataFile == null) {
            return true;
        }
        return defragment(false, timeout);
    }

    @Override
    public String toString() {
        return "DblFileStorage: " + baseDir; // NOI18N
    }
}
