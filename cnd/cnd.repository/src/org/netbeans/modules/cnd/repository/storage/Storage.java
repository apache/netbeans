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
package org.netbeans.modules.cnd.repository.storage;

import org.netbeans.modules.cnd.repository.impl.spi.FSConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.api.FilePath;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.FilePathConverter;
import org.netbeans.modules.cnd.repository.impl.spi.Layer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerFactory;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayerListener;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.UnitDescriptorsList;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataInputStream;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataOutputStream;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
/* package */ final class Storage {

    private static boolean PRINT_STACK_FILES = CndUtils.getBoolean("cnd.repository.print.stack.wrong.file", true);// NOI18N
    private static boolean UnitIDReadConverterImpl_layer_to_client = CndUtils.getBoolean("cnd.repository.print.stack.wrong.units.layer_to_client", true);// NOI18N
    private static boolean UnitIDWriteConverterImpl_client_to_layer = CndUtils.getBoolean("cnd.repository.print.stack.wrong.units.client_to_layer", true);// NOI18N
    private static final Logger LOG = Logger.getLogger("repository.support.filecreate.logger"); //NOI18N
    // A list of all layers that belong to this Storage.
    private final List<Layer> layers;
    private final List<LayerDescriptor> layerDescriptors;
    // A list of all client FileSystems: clientFileSystem <-> clientFileSystemID
    private final FileSystemsDictionary clientFileSystemsDictionary = new FileSystemsDictionary();
    // A list of all client UnitDescriptors: clientUnitDescriptor <-> clientShortUnitID
    private final UnitDescriptorsDictionary clientUnitDescriptorsDictionary = new UnitDescriptorsDictionary();
    // For each clientShortUnitID: A list of all client FilePaths: filePathID <-> clientFilePaths
    // (the same file has the same filePathID in any layer, the dictionary should keep file names for the client)
    private final Map<Integer, FilePathsDictionary> filePathDictionaries = new HashMap<Integer, FilePathsDictionary>();
    // For LayerDescriptor -> map of translation clientFileSystemID => fileSystemIndexInLayer
    private final ConcurrentMap<LayerDescriptor, Map<Integer, Integer>> fileSystemsTranslationMap = new ConcurrentHashMap<LayerDescriptor, Map<Integer, Integer>>();
    // For LayerDescriptor -> map of translation clientShortUnitID => unitIDInLayer
    private final ConcurrentMap<LayerDescriptor, Map<Integer, Integer>> unitsTranslationMap = new ConcurrentHashMap<LayerDescriptor, Map<Integer, Integer>>();
    // Encodes/decodes storage ID into unitID: clientShortUnitID <-> clientLongUnitID
    private final UnitsConverter storageMask;
    private final ReentrantLock storageLock = new ReentrantLock();
    private final int storageID;
    private final LayeringSupportImpl layeringSupport;
    private static final java.util.logging.Logger log = org.netbeans.modules.cnd.repository.Logger.getInstance();

    Storage(final int persistMechanismVersion, final int storageID, final List<LayerDescriptor> layerDescriptors, final UnitsConverter unitIDConverter) {
        assert layerDescriptors != null; //&& layerDescriptors.size() > 0;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "New storage with storageID == {0} created",
                    new Object[]{storageID, unitIDConverter});
        }
        this.layeringSupport = new LayeringSupportImpl();
        this.storageID = storageID;
        this.storageMask = unitIDConverter;
        final Collection<? extends LayerListener> lst =
                Lookups.forPath(LayerListener.PATH).lookupAll(LayerListener.class);
        this.layers = Collections.unmodifiableList(createLayers(layerDescriptors, layeringSupport, lst, persistMechanismVersion));
        assert layers != null;// && layers.size() > 0;
        // Initialize layerDescriptors list with descriptors of created layers
        // only.
        List<LayerDescriptor> descriptors = new ArrayList<LayerDescriptor>();
        for (Layer layer : layers) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Layer added: {0}", new Object[]{layer.getLayerDescriptor()});
            }
            descriptors.add(layer.getLayerDescriptor());
        }
        this.layerDescriptors = Collections.unmodifiableList(descriptors);
    }

    /**
     * Returns client unitID (100001) based on clientUnitDescriptor.
     *
     * @param clientUnitDescriptor remote-host-fs:/builds/latest/projects/Test
     * @return - 100001
     */
    // TODO: it would be good to pre-create entries for dependent Units here as
    // well. In this case nothing will be done in layerToClient code...
    public int getUnitID(final UnitDescriptor clientUnitDescriptor) {
        if (clientUnitDescriptorsDictionary.contains(clientUnitDescriptor)) {
            return storageMask.layerToClient(clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor));
        }

        // Register a new ClientShortUnitID and init unitsTranslationMap for it ...
        // clientUnitDescriptor <-> clientShortUnitID
        final int clientShortUnitID = clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor);

        final FileSystem clientFileSystem = clientUnitDescriptor.getFileSystem();
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientFileSystem);

        // 1. find matched filesystem in layers and fill fileSystemsTranslationMap
        // for all layers for this clientFileSystemID
        for (Layer layer : layers) {
            final LayerDescriptor ld = layer.getLayerDescriptor();
            Map<Integer, Integer> map = fileSystemsTranslationMap.get(ld);
            // map: clientFileSystemID => fileSystemIndexInLayer
            if (map == null) {
                map = new ConcurrentHashMap<Integer, Integer>();
                Map<Integer, Integer> prev = fileSystemsTranslationMap.putIfAbsent(ld, map);
                if (prev != null) {
                    map = prev;
                }
            }
            if (!map.containsKey(clientFileSystemID)) {
                int matchedFileSystemIndexInLayer = layer.findMatchedFileSystemIndexInLayer(clientFileSystem);
                if (matchedFileSystemIndexInLayer < 0 && layer.getWriteCapability() != null) {
                    matchedFileSystemIndexInLayer = layer.getWriteCapability().registerClientFileSystem(clientFileSystem);
                }
     //           assert matchedFileSystemIndexInLayer != -1 : "Matched file system not found";
                map.put(clientFileSystemID, matchedFileSystemIndexInLayer);
            }
        }

        updateUnitsTranslationMap(clientUnitDescriptor);
        //dumpStorage();
        return storageMask.layerToClient(clientShortUnitID);
    }

    private void updateUnitsTranslationMap(UnitDescriptor clientUnitDescriptor) {
        final int clientShortUnitID = clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor);
        // 2. find matched Unit in layers and fill unitsTranslationMap for all
        // layers for the clientUnitDescriptor
        for (Layer layer : layers) {
            final LayerDescriptor ld = layer.getLayerDescriptor();
            Map<Integer, Integer> map = unitsTranslationMap.get(ld);
            // map: clientShortUnitID => unitIDInLayer
            if (map == null) {
                map = new ConcurrentHashMap<Integer, Integer>();
                Map<Integer, Integer> old = unitsTranslationMap.putIfAbsent(ld, map);
                if (old != null) {
                    map = old;
                }
            }

            if (!map.containsKey(clientShortUnitID)) {
                int matchedUnitIDInLayer = findMatchedUnitIDInLayer(layer, clientUnitDescriptor);
                if (matchedUnitIDInLayer == -1 && layer.getWriteCapability() != null) {
                    UnitDescriptor layerUnitDescriptor = createLayerUnitDescriptor(layer, clientUnitDescriptor);
                    matchedUnitIDInLayer = layer.getWriteCapability().registerNewUnit(layerUnitDescriptor);

                }
                map.put(clientShortUnitID, matchedUnitIDInLayer);
            }            
        }
    }

    private List<Layer> createLayers(List<LayerDescriptor> layerDescriptors, LayeringSupport layeringSupport, Collection<? extends LayerListener> lst, int persistMechanismVersion) {
        Collection<? extends LayerFactory> factories = Lookup.getDefault().lookupAll(LayerFactory.class);
        List<Layer> result = new ArrayList<Layer>();

        for (LayerDescriptor layerDescriptor : layerDescriptors) {
            Layer layer = null;
            for (LayerFactory factory : factories) {
                if (factory.canHandle(layerDescriptor)) {
                    layer = factory.createLayer(layerDescriptor, layeringSupport);
                    break;
                }
            }
            if (layer != null) {
                //TODO: exceptions listener
                // layer.setExceptionsListener(exceptionsListener);
                //check if layer can be opened by other layering clients
                boolean isOK = true;
                for (LayerListener layerListener : lst) {
                    isOK &= layerListener.layerOpened(layerDescriptor);
                }                
                boolean success = layer.startup(persistMechanismVersion, !isOK);
                //ant check if layers is correct from the other layering clients point of view
                if (success) {
                    result.add(layer);
                }
            }
        }

        return result;
    }
    
    int getMaintananceWeight() {
        int weight = 0;
        for (Layer l : layers) {
            weight += getMaintenanceWeight(l);
        }
        return weight;
    }
    
    
    boolean maintain(long timeout){
        if (layers.isEmpty()) {
            return false;
        }
        if (Stats.traceDefragmentation) {
            System.out.println("-------Storage with id " + storageID + " start defragmenting------");//NOI18N
        }
        Layer[] unitList = layers.toArray(new Layer[layers.size()]);
        Arrays.sort(unitList, new MaintenanceComparator());
        boolean needMoreTime = false;
        long start = System.currentTimeMillis();
        for (int i = 0; i < unitList.length; i++) {
            final WriteLayerCapability writeCapability = unitList[i].getWriteCapability();
            if (writeCapability == null) {
                //no need to maintain read onle layers
                continue;
            }
            int weight = 0;
            try {
                weight = writeCapability.getMaintenanceWeight();
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
            if (weight < Stats.defragmentationThreashold) {
                //we are done, no need to go inside, no maintenance is required
                return needMoreTime;
            }

            try {
                if (writeCapability.maintenance(timeout)) {
                    needMoreTime = true;
                }
            } catch (IOException ex) {
            }
            timeout -= (System.currentTimeMillis() - start);
            if (timeout <= 0 && i < unitList.length -1 ) {
                needMoreTime = true;
                break;
            }
        }
        return needMoreTime;        
    }
    
    
    public RepositoryDataInputStream getInputStream(Key key) {
        int unitId = key.getUnitId();
        openUnit(unitId);
        for (Layer layer : layers) {
            final LayerDescriptor ld = layer.getLayerDescriptor();
            LayerKey layerKey = getReadLayerKey(key, layer);
            if (layerKey == null) {
                // Not in this layer.
                continue;
            }
            if (layer.removedTableKeySet().contains(layerKey)){
                return null;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "will get ByteBuffer from the read capability for the key "
                        + "with unit id:{0} and behaviour: {1}", new Object[]{key.getUnitId(), key.getBehavior()});//NOI18N
            }
            ByteBuffer rawData = layer.getReadCapability().read(layerKey);
            if (rawData != null) {
                return new RepositoryDataInputStream(
                        new ByteArrayInputStream(rawData.array()),
                        LayerConvertersProvider.getReadInstance(
                                new UnitIDReadConverterImpl(layer), 
                                new FSReadConverterImpl(ld), 
                                new FilePathReadConverterImpl(unitId)));
            }
        }
        return null;
    }

    // TODO: For now only single writeable layer is supported. Use multiplexer..
    public RepositoryDataOutputStream getOutputStream(Key key) {
        for (Layer layer : layers) {
            WriteLayerCapability wc = layer.getWriteCapability();
            if (wc != null) {
                UnitIDWriteConverterImpl unitIDConverter = new UnitIDWriteConverterImpl(layer);
                FSConverter fsConverter = new FSWriteConverterImpl(layer);
                FilePathConverter filePathConverter = new FilePathWriteConverterImpl(key.getUnitId());
                LayerKey layerKey = getWriteLayerKey(key, layer);
                if (layerKey != null) {
                    return new RepositoryDataOutputStream(
                            layerKey,
                            wc,
                            LayerConvertersProvider.getWriteInstance(unitIDConverter, fsConverter, filePathConverter));
                }
            }
        }
        RepositoryExceptions.throwException(this, key, new RepositoryException(true));
        return null;
    }

    public List<LayerDescriptor> getLayerDescriptors() {
        return layerDescriptors;
    }

    void shutdown() {
        storageLock.lock();
        try {
            Collection<Integer> clientShortUnitIDs = clientUnitDescriptorsDictionary.getUnitIDs();
            for (Integer clientShortUnitID : clientShortUnitIDs) {
                closeUnit(clientShortUnitID);
            }
            for (Layer layer : layers) {
                layer.shutdown();
            }
        } finally {
            storageLock.unlock();
        }
    }

    private LayerKey getReadLayerKey(Key key, Layer layer) {
        //to convert client key to layer key we need to use
        UnitIDWriteConverterImpl unitIDConverter = new UnitIDWriteConverterImpl(layer);
        // 100007
        int unitId = key.getUnitId();

        Integer layerUnitID = unitIDConverter.clientToLayer(unitId);
        if (layerUnitID < 0) {
            // Not in this layer...
            return null;
        }
        return LayerKey.create(key, layerUnitID);
    }

    private LayerKey getWriteLayerKey(Key key, Layer layer) {
        UnitsConverter unitIDConverter = new UnitIDWriteConverterImpl(layer);
        // 100007
        int clientUnitID = key.getUnitId();
        // 5
        Integer layerUnitID = unitIDConverter.clientToLayer(clientUnitID);
        if (layerUnitID < 0) {
            throw new InternalError();
        }
        return LayerKey.create(key, layerUnitID);
    }
    

    void openUnit(int clientUnitID) {
        // unitID == 100001
        // 1
        //check if layers are opened already, check files table
        Integer clientShortUnitID = storageMask.clientToLayer(clientUnitID);
        synchronized (filePathDictionaries) {
            FilePathsDictionary fsDict = filePathDictionaries.get(clientShortUnitID);
            if (fsDict != null) {
                return;
            }
        }
        UnitDescriptor clientUnitDescriptor = clientUnitDescriptorsDictionary.getUnitDescriptor(clientShortUnitID);
        if (clientUnitDescriptor == null) {
            //was not registered, at all, what should we do here?
            System.err.println(clientUnitDescriptor + " is not registered in the storage!!!!");
           return;
        }
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientUnitDescriptor.getFileSystem());
        Layer layer_to_read_files_from = null;
        int unit_id_layer_to_read_files_from = -1;
        
        for (Layer layer : layers) {
            UnitIDWriteConverterImpl unitIDConverter = new UnitIDWriteConverterImpl(layer);
            // 5
            Integer unitIDInLayer = unitIDConverter.clientToLayer(clientUnitID);
            if (unitIDInLayer < 0) {
                // There is no this Unit in this layer
                continue;
            }
            //here we can be in a situation when we have 2 layers already but 
            //file tables is not listed in one of them?
            layer.openUnit(unitIDInLayer);
            //read files from the layers where file tables exists
            //read using storage
            FilePathsDictionary files = getFilesDictionary(clientUnitID, layer);
            if (layer_to_read_files_from == null || (files != null && files.size() > 0)) {
                layer_to_read_files_from = layer;
                unit_id_layer_to_read_files_from = unitIDInLayer;                
            }
           
            Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
            // map: clientFileSystemID => fileSystemIndexInLayer
            Integer requiredFileSystem = map.get(clientFileSystemID);
            if (requiredFileSystem == null) {
                throw new InternalError();
            }
            if (requiredFileSystem.intValue() < 0) {
                continue;
            }
        }

        // FileName table is the same in all the layers. (Layer0 is 
        // an exception). So we need to read it only once and
        // put converted paths to the fsDictionary (for this unit 
        // (clientUnitID)).
        synchronized (filePathDictionaries) {
            FilePathsDictionary fsDict = filePathDictionaries.get(clientShortUnitID);
            if (fsDict == null) {
                List<CharSequence> convertedTable;
                if (layer_to_read_files_from != null) {
                    FilePathsDictionary dict = getFilesDictionary(clientUnitID, layer_to_read_files_from);
                    List<CharSequence> fileNameTable = dict == null ? Collections.<CharSequence>emptyList() : dict.toList();
                    convertedTable = new ArrayList<CharSequence>(fileNameTable.size());
                    for (CharSequence fname : fileNameTable) {
                        final UnitDescriptor unitDescriptor = layer_to_read_files_from.getUnitsTable().getUnitDescriptor(unit_id_layer_to_read_files_from);
                        if (unitDescriptor == null) {
                            //is it legal that we do not have unit descriptor here?
                            //do not think so, 
                            System.err.println("UnitDesctipor not found  for " + unit_id_layer_to_read_files_from + " and layer " +  layer_to_read_files_from.getLayerDescriptor());
                            CndUtils.threadsDump();
                            continue;
                        }
                        FilePath sourceFSPath = new FilePath(unitDescriptor.getFileSystem(), fname.toString());
                        CharSequence pathInClient = RepositoryMapper.map(clientUnitDescriptor, sourceFSPath);
                        convertedTable.add(pathInClient);
                    }
                } else {
                    convertedTable = new ArrayList<CharSequence>();
                }
                if (convertedTable.isEmpty()) {
                    //should read from another layer
                }
                filePathDictionaries.put(clientShortUnitID, new FilePathsDictionary(convertedTable));
            }
        }
    }

    private FilePathsDictionary getFilesDictionary(int clientUnitID, Layer layer) {
        try {

            final FilePathsDictionaryKey key = new FilePathsDictionaryKey(clientUnitID);
            //read using storage
            final LayerDescriptor ld = layer.getLayerDescriptor();
            LayerKey layerKey = getReadLayerKey(key, layer);
            if (layerKey == null) {
                // Not in this layer.
                return null;
            }
            if (layer.removedTableKeySet().contains(layerKey)) {
                return null;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "will get ByteBuffer from the read capability for the key "
                        + "with unit id:{0} and behaviour: {1}", new Object[]{key.getUnitId(), key.getBehavior()});//NOI18N
            }
            ByteBuffer rawData = layer.getReadCapability().read(layerKey);
            if (rawData == null) {
                return null;
            }
            RepositoryDataInputStream inputStream = new RepositoryDataInputStream(
                    new ByteArrayInputStream(rawData.array()),
                    LayerConvertersProvider.getReadInstance(new UnitIDReadConverterImpl(layer),
                            new FSReadConverterImpl(ld), new FilePathReadConverterImpl(clientUnitID)));
            Persistent obj = key.getPersistentFactory().read(inputStream);
            assert (obj instanceof FilePathsDictionary);
            return (FilePathsDictionary) obj;

        } catch (Throwable ex) {

            RepositoryExceptions.throwException(this, ex);
        }
        return null;
    }

    private void closeUnit(int shortClientUnitID) {
        Integer clientUnitID = storageMask.layerToClient(shortClientUnitID);
        closeUnit(clientUnitID, false, Collections.<Integer>emptySet());
    }

    void closeUnit(int clientUnitID, boolean cleanRepository, Set<Integer> requiredUnits) {
        Integer clientShortUnitID = storageMask.clientToLayer(clientUnitID);
//        if (cleanRepository) {
//            //remove from cache when clean repository, fixing the problem with Reparse Project
//            //Reparse project cleans cache but as we use new scheme to
//            //write project-index and didn't clean the cache
//            //when re-parse is invoked, folder with unit is removed
//            //but after that filePathsDictionary will never be changed (all files are already in cache)
//            //and therefore will never be put into the repository writer queue -> no project-index file on disk
//            synchronized (filePathDictionaries) {
//                //put to the queue  on write                
//                FilePathsDictionary toRemove = filePathDictionaries.remove(clientShortUnitID);
//            }
//        }

        //delete cache
//        List<CharSequence> flist = files == null
//                ? Collections.<CharSequence>emptyList() : files.toList();
        for (Layer layer : layers) {
            Map<Integer, Integer> map = unitsTranslationMap.get(layer.getLayerDescriptor());
            // map: clientShortUnitID => unitIDInLayer
            final Integer unitIDInLayer = map.get(clientShortUnitID);            
            // check for null in case we recover from broken repository read
            // #249412 NullPointerException at org.netbeans.modules.cnd.repository.storage.Storage.closeUnit
            if (unitIDInLayer != null) { 
                layer.closeUnit(unitIDInLayer, cleanRepository, requiredUnits);
                //no need to store file tables, we will add it to the writer queue
    //            WriteLayerCapability writeCapability = layer.getWriteCapability();
    //            if (writeCapability != null) {
    //                if (!cleanRepository) {
    //                    writeCapability.storeFilesTable(unitIDInLayer, flist);
    //                }
    //            }
            }
        }
    }

    /**
     *
     * @param unitID - 10001
     * @param fileIdx
     * @return
     */
    CharSequence getFileName(int unitID, int fileIdx) {
        // 1
        Integer clientShortUnitID = storageMask.clientToLayer(unitID);
        FilePathsDictionary fsDict;
        synchronized (filePathDictionaries) {
            fsDict = filePathDictionaries.get(clientShortUnitID);
            if (fsDict == null) {
                openUnit(unitID);
                fsDict = filePathDictionaries.get(clientShortUnitID);
            }            
        }
        CharSequence res = fsDict.getFilePath(fileIdx);
        if (FilePathsDictionary.WRONG_PATH == res) {
            if (PRINT_STACK_FILES){
                System.err.println("Path by index fileIdx/clientShortUnitID"+fileIdx+"/"+clientShortUnitID+" not found. files dictionary size is " + fsDict.size()); //NOI18N
                System.err.println("filePathDictionaries:" + fsDict);//NOI18N
                dumpStorage();                
                // only once
                PRINT_STACK_FILES = false;
            }
        }
        return res;
    }

    int getFileID(int clientUnitID, CharSequence fileName) {
        Integer clientShortUnitID = storageMask.clientToLayer(clientUnitID);
        FilePathsDictionary fsDict;
        int size;
        int result;
        synchronized (filePathDictionaries) {
            fsDict = filePathDictionaries.get(clientShortUnitID);
            if (fsDict == null) {
                openUnit(clientUnitID);
                fsDict = filePathDictionaries.get(clientShortUnitID);
            }
            size = fsDict.size();
            result = fsDict.getFileID(fileName, clientShortUnitID);
        }
        //each time add to the quue to write
        if (fsDict.size() > size) {
            Repository.put(new FilePathsDictionaryKey(clientUnitID), fsDict);
        }
        return result;
    }

    CharSequence getUnitName(int unitID) {
        openUnit(unitID);
        Integer unmaskedID = storageMask.clientToLayer(unitID);        
        final UnitDescriptor unitDescriptor = clientUnitDescriptorsDictionary.getUnitDescriptor(unmaskedID);
        if (unitDescriptor == null) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "unitDescriptor is null for unitID={0}", unitID);//NOI18N
            }
            return null;
        }
        return unitDescriptor.getName();
    }



    private int findMatchedUnitIDInLayer(Layer layer, UnitDescriptor clientUnitDescriptor) {
        UnitDescriptorsList layerUnitsTable = layer.getUnitsTable();
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientUnitDescriptor.getFileSystem());
        Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
        // map: clientFileSystemID => fileSystemIndexInLayer
        Integer requiredFileSystem = map.get(clientFileSystemID);
        if (requiredFileSystem == null) {
            throw new InternalError();
        }
        if (requiredFileSystem.intValue() < 0 && layer.getWriteCapability() != null) {
            //register file system
            requiredFileSystem = layer.getWriteCapability().registerClientFileSystem(clientUnitDescriptor.getFileSystem());
            map.put(clientFileSystemID, requiredFileSystem);
        }
        Collection<Integer> unitIDs = layerUnitsTable.getUnitIDs();
        for (Integer unitIDInLayer : unitIDs) {
            UnitDescriptor layerUnitDescriptor = layerUnitsTable.getUnitDescriptor(unitIDInLayer);
            FileSystem unitFileSystemInLayer = layerUnitDescriptor.getFileSystem();
            int fileSystemIndexInLayer = layer.getFileSystemsTable().indexOf(unitFileSystemInLayer);
//            assert fileSystemIndexInLayer != -1;
            if (requiredFileSystem.equals(fileSystemIndexInLayer)) {
                if (RepositoryMapper.matches(layerUnitDescriptor, clientUnitDescriptor)) {
                    // units: clientShortUnitID => unitIDInLayer
                    return unitIDInLayer;
                }
            }
        }
        return -1;
    }

    private UnitDescriptor createClientUnitDescriptor(Layer layer, UnitDescriptor layerUnitDescriptor) {
        FileSystem unitFileSystemInLayer = layerUnitDescriptor.getFileSystem();
        Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
        // map: clientFileSystemID => fileSystemIndexInLayer
        int fileSystemIndexInLayer = layer.getFileSystemsTable().indexOf(unitFileSystemInLayer);
        if (fileSystemIndexInLayer == -1) {
            throw new InternalError();
        }
        int clientFileSystemID = -1;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(fileSystemIndexInLayer)) {
                clientFileSystemID = entry.getKey();
                break;
            }
        }
        if (clientFileSystemID == -1) {
            //need to register file system here, when we are here when reading one peristent object which contains 
            //new unit id for which getUnitId() was not invoked yet the file system is not registered in fileSystemsTranslationMap yet
            //need to registe
            clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(unitFileSystemInLayer);
            map.put(clientFileSystemID, fileSystemIndexInLayer);
            //throw new InternalError();
        }
        FileSystem clientFileSystem = clientFileSystemsDictionary.getFileSystem(clientFileSystemID);
        return RepositoryMapper.mapToClient(clientFileSystem, layerUnitDescriptor);
    }

    private UnitDescriptor createLayerUnitDescriptor(Layer layer, UnitDescriptor clientUnitDescriptor) {
        Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
        // map: clientFileSystemID => fileSystemIndexInLayer
        FileSystem clientFileSystem = clientUnitDescriptor.getFileSystem();
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientFileSystem);
        Integer fileSystemIndexInLayer = map.get(clientFileSystemID);
        if (fileSystemIndexInLayer == null || fileSystemIndexInLayer.intValue() == -1) {
            fileSystemIndexInLayer = layer.getWriteCapability().registerClientFileSystem(clientFileSystem);
            map.put(clientFileSystemID, fileSystemIndexInLayer);
        }
        return RepositoryMapper.mapToLayer(layer.getFileSystemsTable().get(fileSystemIndexInLayer), clientUnitDescriptor);
    }

    void removeUnit(int clientLongUnitID) {
        try {
            for (Layer layer : layers) {
                WriteLayerCapability wc = layer.getWriteCapability();
                if (wc != null) {
                    UnitIDWriteConverterImpl unitIDConverter = new UnitIDWriteConverterImpl(layer);
                    Integer unitIDInLayer = unitIDConverter.clientToLayer(clientLongUnitID);
                    wc.removeUnit(unitIDInLayer);
                }
            }
        } finally {
            //do not delete from the descrriptors dictionary ever
            //clientUnitDescriptorsDictionary.remove(storageMask.clientToLayer(clientLongUnitID));
            //!!
            //what we need is to put files dictionary on disk as it was removed from the writer queue
           flush();
        }
    }

//    int clientToLayer(final LayerDescriptor layerDescriptor, int clientUnitID) {
//        int clientShortUnitID = storageMask.clientToLayer(clientUnitID);
//        Map<Integer, Integer> unitsMap = unitsTranslationMap.get(layerDescriptor);
//        if (unitsMap == null) {
//            return -1;
//        }        
//        Integer out = unitsMap.get(clientShortUnitID);
//        // check for null in case we recover from broken repository read
//        // #249412 NullPointerException at org.netbeans.modules.cnd.repository.storage.Storage.closeUnit        
//        return out == null ? -1 : out;
//    }

    UnitsConverter getStorageMask() {
        return storageMask;
    }

    UnitsConverter getReadUnitsConverter(LayerDescriptor layerDescriptor) {
        int layerID = layerDescriptors.indexOf(layerDescriptor);
        assert layerID >= 0;
        Layer layer = layers.get(layerID);
        return new UnitIDReadConverterImpl(layer);
    }

    UnitsConverter getWriteUnitsConverter(LayerDescriptor layerDescriptor) {
        int layerID = layerDescriptors.indexOf(layerDescriptor);
        assert layerID >= 0;
        Layer layer = layers.get(layerID);
        return new UnitIDWriteConverterImpl(layer);
    }

    FSConverter getReadFSConverter(LayerDescriptor layerDescriptor) {
        return new FSReadConverterImpl(layerDescriptor);
    }

    FSConverter getWriteFSConverter(LayerDescriptor layerDescriptor) {
        int layerID = layerDescriptors.indexOf(layerDescriptor);
        assert layerID >= 0;
        Layer layer = layers.get(layerID);
        return new FSWriteConverterImpl(layer);
    }

    LayeringSupport getLayeringSupport() {
        return layeringSupport;
    }
    
    final void remove(Key key) {
        //if the only layer and it is writable - just physically remove
        //need to support removed_table on the level of read layer
        //we will keep the table of removed keys (if any) on this level        
        boolean keepInRemovedTable = false;
        for (Layer layer : layers) {
            final ReadLayerCapability readCapability = layer.getReadCapability();
            WriteLayerCapability wc = layer.getWriteCapability();
            //we have read-only layer, need to keep removed table in some writable layer
            if (readCapability != null && wc == null) {
                LayerKey layerKey = getReadLayerKey(key, layer);
                keepInRemovedTable = keepInRemovedTable || readCapability.knowsKey(layerKey);
            }
            
            if (wc != null) {
                LayerKey layerKey = getWriteLayerKey(key, layer);
                if (layerKey != null) {
                    //if we have read only layer we should fill removed table and persist it
                    //TODO: but we should do it only of the key exists in read-only layer,
                    //otherwise we can remove it completely 
                    wc.remove(layerKey, keepInRemovedTable);
                }
            }
        }        
        
    }

    void flush() {
        storageLock.lock();
        try {
            Collection<Integer> clientShortUnitIDs = clientUnitDescriptorsDictionary.getUnitIDs();
            for (Integer clientShortUnitID : clientShortUnitIDs) {
                FilePathsDictionary fsDict;
                synchronized (filePathDictionaries) {
                    //put to the queue  on write                
                    fsDict = filePathDictionaries.get(clientShortUnitID);
                }        
                if (fsDict != null) {
                    Repository.put(new FilePathsDictionaryKey(storageMask.layerToClient(clientShortUnitID)), fsDict);
                }
            }
        } finally {
            storageLock.unlock();
        }        
    }

    private class LayeringSupportImpl implements LayeringSupport {
        
        @Override
        public List<LayerDescriptor> getLayerDescriptors() {
            return Storage.this.getLayerDescriptors();
        }

        @Override
        public int getStorageID() {
            return storageID;
        }

        @Override
        public UnitsConverter getReadUnitsConverter(LayerDescriptor layerDescriptor) {
            return Storage.this.getReadUnitsConverter(layerDescriptor);
        }

        @Override
        public UnitsConverter getWriteUnitsConverter(LayerDescriptor layerDescriptor) {
            return Storage.this.getWriteUnitsConverter(layerDescriptor);
        }

        @Override
        public FSConverter getReadFSConverter(LayerDescriptor layerDescriptor) {
            return Storage.this.getReadFSConverter(layerDescriptor);
        }

        @Override
        public FSConverter getWriteFSConverter(LayerDescriptor layerDescriptor) {
            return Storage.this.getWriteFSConverter(layerDescriptor);
        }
        
    }

    private class UnitIDReadConverterImpl implements UnitsConverter {

        private final Map<Integer, Integer> map;
        // map: clientShortUnitID => unitIDInLayer
        private final Layer layer;

        private UnitIDReadConverterImpl(Layer layer) {
            this.layer = layer;
            map = unitsTranslationMap.get(layer.getLayerDescriptor());
            if (map == null) {
                throw new InternalError("unitsTranslationMap must contain entry for " + layer + " at this point."); // NOI18N
            }
        }

        /**
         * Gets as a parameter 100001
         *
         * @param unitID - 100001
         * @return 5
         */
        @Override
        public int clientToLayer(int clientLongUnitID) {
            //should never be called, it is used to read operations
            //when you read from the disk short unit id and should transform it to
            //long client unit id (0 -> 100007)
            throw new InternalError("Should not be called"); // NOI18N
        }

        // Gets  as a parameter 5
        // Returns 1000001
        @Override
        public int layerToClient(int unitIDInLayer) {
            Integer result = null;
            // map: clientShortUnitID => unitIDInLayer
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(unitIDInLayer)) {
                    result = entry.getKey();
                }
            }

            if (result == null) {
                // This means that there is a Unit in the Layer (layer's units 
                // are initialized), but it was not registered in a map yet.
                //
                // The situation occurs when reading unit from a stream that
                // has a refference to a different unit.
                // ('Quote' (ID==0) unit was registered, and its corespondent
                // InputStream was passed for instantiation. But there was a 
                // refference (ID==1) in the stream and now instantiator needs
                // a clientUnitID for the ID==1 in the layer..
                // The problem (?) is that we need to find a mapping between 
                // Unit1 in Layer1 and client's Units... 
                // Ideally we need to call getUnitID(clientUnitDescription). But
                // we don't have clientUnitDescription. And to create it we need
                // clientFileSystem and clientUnitName
                //
                // Is it possible to have something like 
                // clientUnitDescriptor = createClientUnitDescriptor(layerUnitDescription)?
                //
                // Perhaps this could be postponed some-how (like perform a fake
                // registration here and when reader asks for getUnitID in its 
                // terms do the actual mapping and return the ID that we get here?
                //
                // Do registration.
                // TODO: HOW TO DEAL WITH THIS ???
                UnitDescriptor layerUnit = layer.getUnitsTable().getUnitDescriptor(unitIDInLayer);
                if (layerUnit == null) {
                    if (UnitIDReadConverterImpl_layer_to_client){
                        //what should we do here?
                        System.err.println("In previous version we had IndexOutOfBounds here! unitID=" + unitIDInLayer + " " //NOI18N
                                + "not found, current units list is " + layer.getUnitsTable());//NOI18N
                        dumpStorage();
                        //only once
                        UnitIDReadConverterImpl_layer_to_client = false;
                    }
                    //BAD situation!!!!
                    return -1;
                }
                // 'Reserve' a new ID for the layer.
                // Put a wrapper around the layer's Unit
                // Once client's unitDescriptor is in the game we will substitute
                // this wrapper with a real clientUnitDescriptor.

                UnitDescriptor clientUnitDescriptor = createClientUnitDescriptor(layer, layerUnit);
                result = clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor);
                map.put(result, unitIDInLayer);
                updateUnitsTranslationMap(clientUnitDescriptor);
            }
            if (result.equals(-1)) {
                //can it be ever?
                if (UnitIDReadConverterImpl_layer_to_client) {
                    //what should we do here?
                    System.err.println("UnitIDReadConverterImpl.layerToClient will return -1 as client unit id! unitID=" + unitIDInLayer + " " //NOI18N
                            + "not found, current units list is " + layer.getUnitsTable());//NOI18N
                    dumpStorage();
                    //only once
                    UnitIDReadConverterImpl_layer_to_client = false;
                }
                //BAD situation!!!!
            }
            return result.equals(-1) ? -1 : storageMask.layerToClient(result);
        }
    }

    private void dumpStorage() {
        System.err.println("--------Dump storage internals--------:\n");//NOI18N
        for (Layer l : layers) {
            System.err.println("Layer.fileSystemsList is: \n" + l.getFileSystemsTable());//NOI18N
            Map<Integer, Integer> map = fileSystemsTranslationMap.get(l.getLayerDescriptor());
            System.err.println("Layer.fileSystemsTranslationMap: \n" + l.getFileSystemsTable());//NOI18N
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                System.err.println(entry.getKey() + " => " + entry.getValue());//NOI18N
            }
            System.err.println("Layer.unitsTable is: \n" + l.getUnitsTable());//NOI18N
            final LayerDescriptor ld = l.getLayerDescriptor();
            // For LayerDescriptor -> map of translation clientShortUnitID => unitIDInLayer
            Map<Integer, Integer> unitsMap = unitsTranslationMap.get(ld);
            System.err.println("for layer:" + ld.getURI());//NOI18N
            System.err.println("clientShortUnitID => unitIDInLayer:");//NOI18N
            for (Map.Entry<Integer, Integer> entry : unitsMap.entrySet()) {
                System.err.println(entry.getKey() + " => " + entry.getValue());//NOI18N
            }

        }
        System.err.println("unitsTranslationMap is " + clientUnitDescriptorsDictionary);//NOI18N
        CndUtils.threadsDump();
    }

    private class UnitIDWriteConverterImpl implements UnitsConverter {

        private final Map<Integer, Integer> map;
        private final Layer layer;
        // map: clientShortUnitID => unitIDInLayer

        private UnitIDWriteConverterImpl(Layer layer) {
            this.layer = layer;
            map = unitsTranslationMap.get(layer.getLayerDescriptor());
            if (map == null) {
                throw new InternalError("unitsTranslationMap must contain entry for " + layer + " at this point."); // NOI18N
            }
        }

        @Override
        public int clientToLayer(int clientUnitID) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "UnitIDWriteConverterImpl.clientToLayer ({0})", clientUnitID);
            }
            int clientShortUnitID = storageMask.clientToLayer(clientUnitID);
            final Integer result;
            synchronized (map) {
                result = map.get(clientShortUnitID);
                if (result == null || result == -1) {
                    //what does it mean? we do not have an information about transformation yet
                    //but it is the same as -1?
                    //now I think it is the same situation as -1, so I will just register new unit
                    UnitDescriptor unitDescriptor = clientUnitDescriptorsDictionary.getUnitDescriptor(clientShortUnitID);
                    if (unitDescriptor == null) {
                        if (UnitIDWriteConverterImpl_client_to_layer) {
                            //what should we do here?
                            System.err.println("UnitIDWriteConverterImpl.clientToLayer. Means somehow it was deleted from the "//NOI18N
                                    + "units table, even though we never delete data from the units list (see LayerIndex implementation) "//NOI18N
                                    + "will return -1 as client unit id! unitID=" + clientUnitID + " " //NOI18N
                                    + "not found, current units list is " + layer.getUnitsTable());//NOI18N
                            dumpStorage();
                            //only once
                            UnitIDWriteConverterImpl_client_to_layer = false;
                        }
                        return -1;
                    }
                    UnitDescriptor layerUnitDescriptor = createLayerUnitDescriptor(layer, unitDescriptor);
                    int res = layer.getWriteCapability().registerNewUnit(layerUnitDescriptor);
                    map.put(clientShortUnitID, res);
                    return res;
                }
            }
            return result;
        }

        @Override
        public int layerToClient(int unitIDInLayer) {
            throw new InternalError("Should not be called"); // NOI18N
        }
    }

    private class FSReadConverterImpl implements FSConverter {

        private final Map<Integer, Integer> map;
        // map: clientFileSystemID => fileSystemIndexInLayer

        /**
         * Convertor created for rmi layer (rmi://akrasny@enum) does the
         * following conversions: layerToClient: 0 ('localhost') -> akrasny@enum
         * clientToLayer: akrasny@enum -> 0 ('localhost');
         *
         * Convertor created for r/o layer (localhost) does the following
         * conversions:
         *
         * layerToClient: 0 ('localhost') -> akrasny@enum clientToLayer:
         * akrasny@enum -> 0 ('localhost');
         *
         */
        private FSReadConverterImpl(LayerDescriptor ld) {
            this.map = fileSystemsTranslationMap.get(ld);
            if (map == null) {
                throw new InternalError("fileSystemsTranslationMap must contain entry for " + ld + " at this point."); // NOI18N
            }
        }

        /**
         *
         * @param fileSystemIndexInLayer - index of 'localhost' in this layer
         * (ld)
         * @return akrasny@enum
         */
        @Override
        public FileSystem layerToClient(int fileSystemIndexInLayer) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "FSReadConverterImpl.clientToLayer ({0})", fileSystemIndexInLayer);
            }
            int clientFileSystemID = -1;
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(fileSystemIndexInLayer)) {
                    clientFileSystemID = entry.getKey();
                }
            }
            final FileSystem fs = clientFileSystemID < 0 ? null : clientFileSystemsDictionary.getFileSystem(clientFileSystemID);
            if (fs == null) {
                Exceptions.printStackTrace(new AssertionError("Read null FileSystem from code model persistence for fileSystemIndexInLayer == " + fileSystemIndexInLayer)); //NOI18N
                dumpStorage();
            }
            return fs;
        }

        /**
         *
         * @param clientFileSystem - akrasny@enum
         * @return index of localhost
         */
        @Override
        public int clientToLayer(FileSystem clientFileSystem) {
            throw new InternalError("Should not be called"); // NOI18N
        }
    }

    private class FSWriteConverterImpl implements FSConverter {

        private final Map<Integer, Integer> map;
        // map: clientFileSystemID => fileSystemIndexInLayer
        private final Layer layer;

        /**
         * Convertor created for rmi layer (rmi://akrasny@enum) does the
         * following conversions: layerToClient: 0 ('localhost') -> akrasny@enum
         * clientToLayer: akrasny@enum -> 0 ('localhost');
         *
         * Convertor created for r/o layer (localhost) does the following
         * conversions:
         *
         * layerToClient: 0 ('localhost') -> akrasny@enum clientToLayer:
         * akrasny@enum -> 0 ('localhost');
         *
         */
        private FSWriteConverterImpl(final Layer layer) {
            this.layer = layer;
            LayerDescriptor ld = layer.getLayerDescriptor();
            this.map = fileSystemsTranslationMap.get(ld);
            if (map == null) {
                throw new InternalError("fileSystemsTranslationMap must contain entry for " + ld + " at this point."); // NOI18N
            }
        }

        /**
         *
         * @param fileSystemIndexInLayer - index of 'localhost' in this layer
         * (ld)
         * @return akrasny@enum
         */
        @Override
        public FileSystem layerToClient(int fileSystemIndexInLayer) {
            throw new InternalError("Should not be called"); // NOI18N
        }

        /**
         *
         * @param clientFileSystem - akrasny@enum
         * @return index of localhost
         */
        @Override
        public int clientToLayer(final FileSystem clientFileSystem) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "FSWriteConverterImpl.clientToLayer ({0})", clientFileSystem);
            }
            int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientFileSystem);
            Integer result = map.get(clientFileSystemID);
            if (result == null) {
                throw new InternalError();
            }
            if (result.intValue() == -1) {
                return layer.getWriteCapability().registerClientFileSystem(clientFileSystem);
            }
            return result.intValue();
        }
    }

    private final class FilePathReadConverterImpl implements FilePathConverter {
        private final int readFromUnitClientId;

        public FilePathReadConverterImpl(int readFromUnitClientId) {
            this.readFromUnitClientId = readFromUnitClientId;
        }

        @Override
        public CharSequence layerToClient(int fileIdx) {
            return getFileName(readFromUnitClientId, fileIdx);
        }

        @Override
        public int clientToLayer(CharSequence filePath) {
            throw new InternalError("Should not be called"); // NOI18N
        }            
    }
    
    private final class FilePathWriteConverterImpl implements FilePathConverter {
        private final int writeToUnitClientId;

        public FilePathWriteConverterImpl(int writeToUnitClientId) {
            this.writeToUnitClientId = writeToUnitClientId;
        }

        @Override
        public CharSequence layerToClient(int fileIdx) {
            throw new InternalError("Should not be called"); // NOI18N
        }

        @Override
        public int clientToLayer(CharSequence filePath) {
            return getFileID(writeToUnitClientId, filePath);
        }            
    }
    
    private static int getMaintenanceWeight(Layer layer) {
        try {
            final WriteLayerCapability writeCapability = layer.getWriteCapability();
            if (writeCapability == null) {
                return 0;
            }
            return writeCapability.getMaintenanceWeight();
        } catch (IOException ex) {
        }
        return 0;
    }

    private static class MaintenanceComparator implements Comparator<Layer>, Serializable {

        private static final long serialVersionUID = 7249059246763182397L;

        @Override
        public int compare(Layer o1, Layer o2) {
            return getMaintenanceWeight(o2) -  getMaintenanceWeight(o1);
        }

    }    
}
