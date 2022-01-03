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
import java.io.Serializable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.cnd.repository.Logger;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.disk.index.KeysListFile;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.storage.FilePathsDictionaryPersistentFactory;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataInputStream;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataOutputStream;
import org.netbeans.modules.cnd.repository.testbench.BaseStatistics;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Implements FilesAccessStrategy
 *
 */
public final class FilesAccessStrategyImpl implements ReadLayerCapability, WriteLayerCapability {
    private static final boolean TRACE_CONFLICTS = Boolean.getBoolean("cnd.repository.trace.conflicts");
    private static final long PURGE_OLD_UNITS_TIMEOUT = 14 * 24 * 3600 * 1000l; // 14 days
    private final ConcurrentHashMap<Integer, UnitStorage> unitStorageCache = new ConcurrentHashMap<Integer, UnitStorage>();
    private final URI cacheLocationURI;
    private final File cacheLocationFile;
    private final LayeringSupport layeringSupport;
    private final LayerDescriptor layerDescriptor;
    // Statistics
    private final AtomicInteger readCnt = new AtomicInteger();
    private final AtomicInteger readHitCnt = new AtomicInteger();
    private final AtomicInteger writeCnt = new AtomicInteger();
    private final AtomicInteger writeHitCnt = new AtomicInteger();
    private final BaseStatistics<String> writeStatistics = new BaseStatistics<String>("Writes", BaseStatistics.LEVEL_MEDIUM); // NOI18N
    private final BaseStatistics<String> readStatistics = new BaseStatistics<String>("Reads", BaseStatistics.LEVEL_MEDIUM); // NOI18N
    private final LayerIndex layerIndex;
    private final KeysListFile removedKeysFile;
    private final String removedKeysTable = "removed-files";//NOI18N
    private final boolean isWritable;
    private static final java.util.logging.Logger log = Logger.getInstance();

    public FilesAccessStrategyImpl(LayerIndex layerIndex, URI cacheLocation, 
            LayerDescriptor layerDescriptor, LayeringSupport layeringSupport) {
        this.layeringSupport = layeringSupport;
        this.layerDescriptor = layerDescriptor;
        this.layerIndex = layerIndex;
        this.cacheLocationURI = cacheLocation;
        this.cacheLocationFile = Utilities.toFile(cacheLocation);
        this.isWritable = layerDescriptor.isWritable();
        KeysListFile f = null;
        RepositoryDataInputStream din = null;
        try {
            final File file = new File(cacheLocationFile, removedKeysTable);
            if (file.exists()) {
                din = new RepositoryDataInputStream(RepositoryImplUtil.getBufferedDataInputStream(file),
                        LayerConvertersProvider.getInstance(layeringSupport, layerDescriptor));
                f = new KeysListFile(din);
            } 
        } catch (FileNotFoundException ex) {
            //Exceptions.printStackTrace(ex);
            f = null;
        } catch (IOException ex) {
            f = null;
            //Exceptions.printStackTrace(ex);
        }finally {
            if (din != null) {
                try {
                    din.close();
                } catch (IOException ex) {
                }
            }
        }    
        removedKeysFile = f == null ? new KeysListFile() : f;
        if (Stats.multyFileStatistics) {
            resetStatistics();
        }
    }
    
    /**
     * @param unitId
     * @throws IOException
     */
    @Override
    public void closeUnit(final int unitID, boolean cleanRepository) {
        UnitStorage storage = unitStorageCache.remove(unitID);
        if (storage != null) {
            storage.close();
            if (cleanRepository) {
                storage.cleanUnitDirectory();
            }            
        }
        if (Stats.multyFileStatistics) {
            printStatistics();
            resetStatistics();
        }
    }

    public void shutdown(boolean writable) {
        maintenance(Long.MAX_VALUE);
        for (Map.Entry<Integer, UnitStorage> entry : unitStorageCache.entrySet()) {
            closeUnit(entry.getKey(), false);
        }        
        if (!writable) {
            return;
        }
        RepositoryDataOutputStream dos = null;
        try {
            
            final File file = new File(cacheLocationFile, removedKeysTable);
            //delete and create again
            if (file.exists()) {
                file.delete();
            }
            //store removed tables on disk
            dos = new RepositoryDataOutputStream(RepositoryImplUtil.getBufferedDataOutputStream(file),
                    LayerConvertersProvider.getInstance(layeringSupport, layerDescriptor));
            removedKeysFile.write(dos);
        } catch (FileNotFoundException ex) {
            RepositoryExceptions.throwException(this, ex);
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException ex) {
                    RepositoryExceptions.throwException(this, ex);
                }
            }
        }
    }

    @Override
    public void remove(LayerKey key, boolean hasReadOnlyLayersInStorage) {
        //do use now: implement delete from the writable layer and 
        //remove and put to the removed_table
        //when shutdown wtite it on the disk
        //if put with the same key - remove from the table
        //remove phisically
        //add to the removed_table
        //we can use FileIndex for removed objects
        if (hasReadOnlyLayersInStorage) {
            removedKeysFile.put(key);
        }
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());       
        unitStorage.remove(key);
    }

    /*package*/ void testCloseUnit(int unitId) throws IOException {
        closeUnit(unitId, false);
    }

    // package-local - for test purposes
    void printStatistics() {
        System.out.printf("\nFileAccessStrategy statistics: reads %d hits %d (%d%%) writes %d hits %d (%d%%)\n", // NOI18N
                readCnt.get(), readHitCnt.get(), percentage(readHitCnt.get(), readCnt.get()), writeCnt.get(), writeHitCnt.get(), percentage(writeHitCnt.get(), writeCnt.get()));
        if (writeStatistics != null) {
            readStatistics.print(System.out);
        }
        if (writeStatistics != null) {
            writeStatistics.print(System.out);
        }
    }

    private static int percentage(int numerator, int denominator) {
        return (denominator == 0) ? 0 : numerator * 100 / denominator;
    }

    private void resetStatistics() {
        writeStatistics.clear();
        readStatistics.clear();
        readCnt.set(0);
        readHitCnt.set(0);
        writeCnt.set(0);
        writeHitCnt.set(0);
    }

    private static String getBriefClassName(Object o) {
        if (o == null) {
            return "null"; // NOI18N
        } else {
            String name = o.getClass().getName();
            int pos = name.lastIndexOf('.');
            return (pos < 0) ? name : name.substring(pos + 1);
        }
    }

    @Override
    public String toString() {
        return "FilesAccessStrategyImpl: " + cacheLocationURI.toString(); // NOI18N
    }

    @Override
    public boolean knowsKey(LayerKey key) {
        //check if not removed already
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        FileStorage fileStorage = unitStorage.getFileStorage(key, isWritable);
        if (fileStorage == null) {
            return false;
        }
        try {
            return fileStorage.hasKey(key);
        } catch (IOException ex) {
           // Exceptions.printStackTrace(ex);
        }
        return false;        
    }

    @Override
    public ByteBuffer read(LayerKey key) {
        readCnt.incrementAndGet(); // always increment counters
        if (Stats.multyFileStatistics) {
            readStatistics.consume(getBriefClassName(key), 1);
        }
        //check if not removed already
        if (this.removedKeysFile.keySet().contains(key)) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, " the key with unit id:{0} and behaviour: {1} is "
                        + "removed from the layer, will not read from the disk", new Object[]{key.getUnitId(), key.getBehavior()});//NOI18N
            }
            return null;
        }
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        FileStorage fileStorage = unitStorage.getFileStorage(key, isWritable);
         try {
             if (fileStorage != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Storage is found for the key with unit id:{0} and behaviour: {1} is "
                            , new Object[]{key.getUnitId(), key.getBehavior()});                 
                }
                 return fileStorage.read(key);
             }
         } catch (IOException ex) {
             RepositoryExceptions.throwException(this, key, ex);
         }
         return null;
    }

    @Override
    public void write(LayerKey key, ByteBuffer data) {
        writeCnt.incrementAndGet(); // always increment counters
        if (Stats.multyFileStatistics) {
            writeStatistics.consume(getBriefClassName(key), 1);
        }
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        FileStorage fileStorage = unitStorage.getFileStorage(key, true);
        try {
            if (fileStorage != null) {
                fileStorage.write(key, data);
            }
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, key, ex);
        }

    }

    @Override
    public void removeUnit(int unitIDInLayer) {
        layerIndex.removeUnit(unitIDInLayer);
        UnitStorage unitStorage = getUnitStorage(unitIDInLayer);
        unitStorage.cleanUnitDirectory();
    }


    /**
     * For test purposes ONLY! - gets read hit count
     */
    // package-local
    int getReadHitCnt() {
        return readHitCnt.get();
    }

    /**
     * For test purposes ONLY! - gets read hit percentage
     */
    // package-local
    int getReadHitPercentage() {
        return percentage(readHitCnt.get(), readCnt.get());
    }

    /**
     * For test purposes ONLY! - gets write hit count
     */
    // package-local
    int getWriteHitCnt() {
        return writeHitCnt.get();
    }

    /**
     * For test purposes ONLY! - gets read hit percentage
     */
    // package-local
    int getWriteHitPercentage() {
        return percentage(writeHitCnt.get(), writeCnt.get());
    }

    public void debugDump(LayerKey key) {
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        unitStorage.debugDump(key);
    }

    @Override
    public boolean maintenance(long timeout) {
        if (Stats.traceDefragmentation) {
            System.out.println("-------layer " + cacheLocationURI + " start defragmenting------");//NOI18N
        }
        final UnitStorage[] values = unitStorageCache.values().toArray(new UnitStorage[0]);
        Arrays.sort(values, new MaintenanceStorageComparator());
        long start = System.currentTimeMillis();
        long rest = timeout;
        boolean needMoreTime = false;
        int counter = 0;
        int storagesCount = values.length;
        for (UnitStorage storage : values){
            counter++;
            int weight = 0;
            try {
                weight = storage.dblStorage.isOpened() ? storage.dblStorage.getFragmentationPercentage() : 0;
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
            if (weight < Stats.defragmentationThreashold) {
                //we are done, no need to go inside, no maintenance is required
                return needMoreTime;
            }
            needMoreTime = storage.maintenance(rest);
            rest = timeout - (System.currentTimeMillis() - start);
            if (rest <= 0 && counter < storagesCount) {
                //do it for at least one
                needMoreTime = true;
                break;
            }

        }
        return needMoreTime;
    }

    private UnitStorage getUnitStorage(int unitID) {
        UnitStorage result = unitStorageCache.get(unitID);
        if (result == null) {
            result = new UnitStorage(cacheLocationFile, layerDescriptor, layeringSupport, unitID);
            unitStorageCache.put(unitID, result);
        }
        return result;
    }

    @Override
    public int registerNewUnit(UnitDescriptor unitDescriptor) {
        return layerIndex.registerUnit(unitDescriptor);
    }

    @Override
    public int registerClientFileSystem(FileSystem fileSystem) {
        return layerIndex.registerFileSystem(fileSystem);
    }

    Collection<LayerKey> removedTableKeySet() {
        return removedKeysFile.keySet();
    }

    @Override
    public int getMaintenanceWeight() throws IOException {
        int weight = 0;
        for (UnitStorage storage : unitStorageCache.values()) {
            weight += storage.dblStorage.isOpened() ? storage.dblStorage.getFragmentationPercentage() : 0;
        }
        return weight;
    }

    private static class UnitStorage {

        private final DoubleFileStorage dblStorage;
        private final SingleFileStorage singleStorage;
        private final File baseDir;

        private UnitStorage(File cacheLocationFile, LayerDescriptor layerDescriptor, LayeringSupport layeringSupport, int unitID) {
            baseDir = new File(cacheLocationFile, "" + unitID); // NOI18N
            dblStorage = new DoubleFileStorage(baseDir, layerDescriptor, layeringSupport);
            singleStorage = new SingleFileStorage(baseDir);
        }

        private void close() {
            try {
                dblStorage.close();
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
        }

        private FileStorage getFileStorage(LayerKey key, boolean forWriting) {
            FileStorage storage;
            if (Key.Behavior.LargeAndMutable.equals(key.getBehavior())) {
                storage = singleStorage;
            } else {
                storage = dblStorage;
            }

            if (!storage.open(forWriting)) {
                return null;
            }

            return storage;
        }


        
        private void remove(LayerKey key) {
            FileStorage fileStorage = getFileStorage(key, true);
            try {
                if (fileStorage != null) {
                    fileStorage.remove(key);
                }
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
                        
        }
        
        private void debugDump(LayerKey key) {
            // if (Key.Behavior.LargeAndMutable.equals(key.getBehavior())) {
            dblStorage.debugDump(key);
        }

        /**
         * Returns true if more time needed
         * @param timeout
         * @return 
         */
        private boolean maintenance(long timeout) {
            try {
                return dblStorage.maintenance(timeout);
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
            return false;
        }

        @Override
        public String toString() {
            return "UnitStorage: " + dblStorage + " & " + singleStorage; // NOI18N
        }

        private void cleanUnitDirectory() {
            ArrayList<String> excludedNames = new ArrayList<String>();
            try{
                excludedNames.add(FilePathsDictionaryPersistentFactory.getFilePathsDictionaryKeyFileName());
            } catch (IOException ex) {
            }
            RepositoryImplUtil.deleteDirectory(baseDir, excludedNames, false);
        }
    }


    private static class MaintenanceStorageComparator  implements Comparator<UnitStorage>, Serializable {

        @Override
        public int compare(UnitStorage storage1, UnitStorage storage2) {
            try {
                int weight1 = storage1.dblStorage.isOpened() ? storage1.dblStorage.getFragmentationPercentage() : 0;
                int weight2 = storage2.dblStorage.isOpened() ? storage2.dblStorage.getFragmentationPercentage() : 0;
                return weight2 - weight1;
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
            return 0;
        }
    }
}
