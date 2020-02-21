/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.disk.index.ChunkInfo;
import org.netbeans.modules.cnd.repository.disk.index.CompactFileIndex;
import org.netbeans.modules.cnd.repository.disk.index.FileIndex;
import org.netbeans.modules.cnd.repository.disk.index.FileIndexFactory;
import org.netbeans.modules.cnd.repository.disk.index.SimpleFileIndex;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataInputStream;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataOutputStream;
import org.netbeans.modules.cnd.repository.testbench.FileStatistics;
import org.netbeans.modules.cnd.repository.testbench.RangeStatistics;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * Represents the data file with the indexed access
 *
 */
/* package */ final class IndexedStorageFile {

    private static final boolean TRACE = false;
    private static final Logger LOG = Logger.getLogger("repository.support.filecreate.logger"); //NOI18N
    private final boolean writable;
    private final File dataFile;
    private final File indexFile;
    private final FileStatistics fileStatistics;
    private final FileRWAccess fileRWAccess;
    private final AtomicLong fileRWAccessSize;
    // used to accumulate the total currently used chunk size;
    // is necessary for tracking fragmentation
    private long usedSize;
    private final FileIndex index;
    private final LayeringSupport layeringSupport;
    private final LayerDescriptor layerDescriptor;

    public IndexedStorageFile(LayeringSupport layeringSupport, LayerDescriptor layerDescriptor,
            final File basePath, final String name, boolean writable) throws IOException {
        if (writable && basePath.exists() && !basePath.canWrite()) {
            writable = false;
            RepositoryImplUtil.warnNotWritable(basePath);
        }
        this.layeringSupport = layeringSupport;
        this.layerDescriptor = layerDescriptor;
        this.writable = writable;
        dataFile = new File(basePath, name + "-data"); // NOI18N
        indexFile = new File(basePath, name + "-index"); // NOI18N
        fileStatistics = new FileStatistics();
        boolean filesExists = (dataFile.exists() && indexFile.exists());
        fileRWAccess = createFileRWAccess(dataFile, writable);

        FileIndex loadedIndex = null;

        if (filesExists) {
            loadedIndex = loadIndex(indexFile);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Load index size={0} from {1}", new Object[]{loadedIndex.size(), indexFile}); //NOI18N
            }
        }

        if (loadedIndex == null) {
            loadedIndex = Stats.useCompactIndex ? new CompactFileIndex() : new SimpleFileIndex();
        }

        index = loadedIndex;
        usedSize = getIndexSize();

        if (writable && usedSize == 0) {
            fileRWAccess.truncate(0);
        }

        // TODO: do we really need to delete it??? If it is created as a
        // storage for WRITING ONLY. Otherwise it could be shared.
        if (writable && indexFile.exists() && !indexFile.delete()) {
            System.err.println("Cannot delete repository index file " + indexFile.getAbsolutePath()); // NOI18N
        }

        fileRWAccessSize = new AtomicLong(fileRWAccess.size());
    }
    
    boolean hasKey(LayerKey key) {
        return index.get(key) != null;
    }

    ByteBuffer read(LayerKey key) throws IOException {
        ByteBuffer buffer = null;
        final ChunkInfo chunkInfo = index.get(key);
        if (chunkInfo != null) {
            try {
                buffer = fileRWAccess.readData(chunkInfo.getOffset(), chunkInfo.getSize());                                
            } catch (BufferOverflowException e) {
                RepositoryExceptions.throwException(this, e);
                throw e;
            } catch (BufferUnderflowException e) {
                RepositoryExceptions.throwException(this, e);
                throw e;
            }
            if (Stats.fileStatisticsLevel > 0) {
                fileStatistics.incrementReadCount(key);
            }
        }
        return buffer;
    }

    void write(final LayerKey key, final ByteBuffer data) throws IOException {
        // external sync
        ChunkInfo oldInfo = index.get(key);
        int oldSize = oldInfo == null ? 0 : oldInfo.getSize();
        int newSize = data.limit();
        long offset = fileRWAccess.appendData(data);
        fileRWAccessSize.addAndGet(newSize);
        usedSize += (newSize - oldSize);
        index.put(key, offset, newSize);
        assert index.get(key).getOffset() == offset && index.get(key).getSize() == newSize : "Cannot write Key "+key; //NOI18N
        assert fileRWAccess.size() == fileRWAccessSize.get() : "Cannot write data for key "+key; //NOI18N
        if (Stats.fileStatisticsLevel > 0) {
            fileStatistics.incrementWriteCount(key, oldSize, newSize);
        }
    }

    void remove(final LayerKey key) throws IOException {
        // external sync
        if (Stats.fileStatisticsLevel > 0) {
            fileStatistics.removeNotify(key);
        }
        final int oldSize = index.remove(key);

        if (oldSize != 0) {
            if (index.size() == 0) {
                fileRWAccess.truncate(0);
                fileRWAccessSize.set(0);
                usedSize = 0;
            } else {
                usedSize -= oldSize;
            }
        }
    }

    int getObjectsCount() {
        return index.size();
    }

    long getSize() throws IOException {
        //return fileRWAccess.size();
        //assert fileRWAccessSize.get() == fileRWAccess.size();
        return fileRWAccessSize.get();
    }

    void close() throws IOException {
        if (Stats.dumoFileOnExit) {
            dump(System.out);
        } else {
            if (Stats.fileStatisticsLevel > 0) {
                dumpSummary(System.out);
            }
        }

        fileRWAccess.close();
        if (writable) {
            storeIndex();
        }
    }

    int getFragmentationPercentage() throws IOException {
        final long fileSize;
        final float delta;

        //fileSize = fileRWAccess.size();
        fileSize = getSize();
        delta = fileSize - usedSize;

        final float percentage = delta * 100 / fileSize;
        return Math.round(percentage);
    }

    void dump(final PrintStream ps) throws IOException {
        if (TRACE) {
            ps.printf("\nDumping %s\n", dataFile.getAbsolutePath()); // NOI18N
            ps.printf("\nKeys:\n"); // NOI18N
        }
        for (LayerKey key : index.keySet()) {
            ChunkInfo chunk = index.get(key);
            if (TRACE) {
                ps.printf("\t%s: ", key); // NOI18N
            }
            print(ps, null, chunk, true);
        }

        if (TRACE) {
            ps.printf("\nChunks:\n"); // NOI18N
        }
        final ChunkInfo[] infos = sortedChunkInfos();
        for (int i = 0; i < infos.length; i++) {
            print(ps, null, infos[i], true);
        }

        dumpSummary(ps, infos);
    }

    private long getIndexSize() {
        long calcUsedSize = 0;
        for (LayerKey key : index.keySet()) {
            ChunkInfo info = index.get(key);
            calcUsedSize += info.getSize();
        }
        return calcUsedSize;
    }

    void dumpSummary(final PrintStream ps) throws IOException {
        dumpSummary(ps, null);
    }

    private void dumpSummary(final PrintStream ps, ChunkInfo[] sortedInfos) throws IOException {
        RangeStatistics write = new RangeStatistics("Writes:", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);   // NOI18N
        RangeStatistics read = new RangeStatistics("Reads: ", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);    // NOI18N
        RangeStatistics size = new RangeStatistics("Sizes: ", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);    // NOI18N
        for (LayerKey key : index.keySet()) {
            ChunkInfo info = index.get(key);
            usedSize += info.getSize();
            read.consume(fileStatistics.getReadCount(key));
            write.consume(fileStatistics.getWriteCount(key));
            size.consume(info.getSize());
        }
        //long channelSize = fileRWAccess.size();

        if (TRACE) {
            long channelSize = getSize();
            ps.printf("\n"); // NOI18N
            ps.printf("Dumping %s\n", dataFile.getAbsolutePath()); // NOI18N
            ps.printf("Entries count: %d\n", index.size()); // NOI18N
            ps.printf("\n"); // NOI18N
            write.print(ps);
            read.print(ps);
            size.print(ps);
            ps.printf("\n"); // NOI18N
            ps.printf("File size:  %16d\n", channelSize); // NOI18N
            ps.printf("Used size:  %16d\n", usedSize); // NOI18N
            ps.printf("Percentage used: %11d%%\n", channelSize == 0 ? 0 : ((100 * usedSize) / channelSize)); // NOI18N
            ps.printf("Fragmentation:   %11d%%\n", getFragmentationPercentage()); // NOI18N
        }
        if (sortedInfos == null) {
            sortedInfos = sortedChunkInfos();
        }
        long firstExtent = (sortedInfos.length > 0) ? sortedInfos[0].getOffset() : 0;
        if (TRACE) {
            ps.printf("First busy extent: %9d (0x%H)\n\n", firstExtent, firstExtent); // NOI18N
        }
    }

    private void print(final PrintStream ps, final LayerKey key, final ChunkInfo chunk, final boolean lf) {
        if (TRACE) {
            final long endOffset = chunk.getOffset() + chunk.getSize() - 1;
            ps.printf("%d-%d %d [0x%H-0x%H] read: %d written: %d (%s) %c", // NOI18N
                    chunk.getOffset(), endOffset, chunk.getSize(), chunk.getOffset(), endOffset,
                    fileStatistics.getReadCount(key), fileStatistics.getWriteCount(key), chunk.toString(),
                    lf ? '\n' : ' '); // NOI18N
        }
    }

    private ChunkInfo[] sortedChunkInfos() {
        ChunkInfo[] infos = new ChunkInfo[index.size()];
        int pos = 0;

        for (LayerKey key : index.keySet()) {
            infos[pos++] = index.get(key);
        }

        Arrays.sort(infos);
        return infos;
    }

    /*packet */ String getTraceString() throws IOException {
        final Formatter formatter = new Formatter();
        formatter.format("%s index size %d  file size %d  fragmentation %d%%", // NOI18N
                dataFile.getName(), index.size(), getSize(), getFragmentationPercentage());
        return formatter.toString();
    }

    /*packet*/ Iterator<LayerKey> getKeySetIterator() {
        return new IndexIterator();
    }

    /* packet */ ChunkInfo getChunkInfo(LayerKey key) {
        return index.get(key);

    }

    /* packet */ String getDataFileName() {
        return dataFile.getName();
    }

    /*packet */ long getDataFileUsedSize() {
        return usedSize;

    }

    /*packet */ void moveDataFromOtherFile(IndexedStorageFile other, long l, int size, LayerKey key) throws IOException {
        FileRWAccess fileRW = other.fileRWAccess;
        ChunkInfo exist = index.get(key);
        if (exist == null) {
            //long newOffset = fileRWAccessSize.get();
            long newOffset = fileRWAccess.move(fileRW, l, size);
            fileRWAccessSize.addAndGet(size);
            index.put(key, newOffset, size);
            assert index.get(key).getOffset() == newOffset && index.get(key).getSize() == size : "Cannot write Key "+key; //NOI18N
            assert fileRWAccess.size() == fileRWAccessSize.get() : "Cannot write data for key "+key; //NOI18N
            usedSize += size;
        }
    }

    /*packet */ FileRWAccess getDataFile() {
        return fileRWAccess;
    }

    private FileRWAccess createFileRWAccess(File file, boolean writable) throws IOException {
        return new BufferedRWAccess(file, writable);
    }

    // returns null of failed to load
    private FileIndex loadIndex(File indexFile) {
        FileIndex idx = null;
        RepositoryDataInputStream din = null;
        try {
            din = new RepositoryDataInputStream(RepositoryImplUtil.getBufferedDataInputStream(indexFile),
                    LayerConvertersProvider.getInstance(layeringSupport, layerDescriptor));
                    //new RepositoryDataInputImpl(RepositoryImplUtil.getBufferedDataInputStream(indexFile));
            idx = FileIndexFactory.getDefaultFactory().readIndex(din);
        } catch (IOException ex) {
            RepositoryExceptions.throwException("IndexedStorageFile for file " + indexFile, ex);//NOI18N
        } finally {
            if (din != null) {
                try {
                    din.close();
                } catch (IOException ex) {
                }
            }
        }
        return idx;
    }

    private void storeIndex() throws IOException {
        RepositoryDataOutputStream dos = null;

        try {
            //i do not think this is correct need to write unit id not client long id
            //as a result of such usage of RepositoryDataOutput we will write on disk unit id = 1000012 (so called long unit id)
            dos = new RepositoryDataOutputStream(RepositoryImplUtil.getBufferedDataOutputStream(indexFile),
                    LayerConvertersProvider.getInstance(layeringSupport, layerDescriptor));
            FileIndexFactory.getDefaultFactory().writeIndex(index, dos);
        } finally {
            if (dos != null) {
                dos.close();
            }
        }
    }

    /*
     *  Iterator<Key> implementation for the index
     *
     */
    private class IndexIterator implements Iterator<LayerKey> {

        private final Iterator<LayerKey> indexIterator;
        private LayerKey currentKey;

        IndexIterator() {
            indexIterator = index.getKeySetIterator();
        }

        @Override
        public boolean hasNext() {
            return indexIterator.hasNext();
        }

        @Override
        public LayerKey next() {
            currentKey = indexIterator.next();
            return currentKey;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + dataFile + " usedSize=" + usedSize + " size=" + dataFile.length(); //NOI18N
    }
    
}
