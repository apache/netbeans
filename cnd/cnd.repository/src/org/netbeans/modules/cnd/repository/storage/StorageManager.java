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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptorProvider;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.openide.util.Lookup;

/**
 * Repository Service Implementation provides Layered Persistence Capability.
 * StorageManager is the manager that deals with several storages and proxies
 * queries from Repository Client to the right Storage (associated with the Key)
 *
 */
public final class StorageManager {

    private static final Logger LOG = Logger.getLogger(StorageManager.class.getName());
    private static final int DENOM = 100000;
    private final int persistMechanismVersion;
    private final Object lock = new Object();
    private final Map<Integer, Storage> storages = new HashMap<Integer, Storage>();
    private final AtomicInteger storageCounter = new AtomicInteger();
    private volatile boolean isShuttedDown  = false;
    private final WeakHashMap<UnitDescriptor, Integer> cache = new WeakHashMap<UnitDescriptor, Integer>();
    private static final  Storage NULL_STORAGE = new Storage(-1, -1, Collections.<LayerDescriptor>emptyList(), null);    

    public StorageManager(int persistMechanismVersion) {
        this.persistMechanismVersion = persistMechanismVersion;
    }

    public RepositoryDataInput getInputStream(final Key key) {
        return getStorage(key).getInputStream(key);
    }
    
    
    // maintain 
    public boolean maintenance (long interval) {
        if (Stats.traceDefragmentation) {
            System.out.println("-------StorageManager start defragmenting------");//NOI18N
        }
        boolean needMaintence = false;
        long workTime = 0;        
        int storagesCount = storages.size();
        Storage[] values = storages.values().toArray(new Storage[0]);
        Arrays.sort(values, new MaintenanceComparator());
        int counter= 0;
        for (Storage storage : values) {
            counter++;
            long time = System.currentTimeMillis();
            int weight = storage.getMaintananceWeight();
            if (weight < Stats.defragmentationThreashold) {
                //we are done, no need to go inside, no maintenance is required
                return needMaintence;
            }
            needMaintence = needMaintence || storage.maintain(interval);
            workTime += System.currentTimeMillis() - time;
            if( counter < storagesCount && workTime > interval ) {
                return true;
            }
            if (counter == storagesCount) {
                return needMaintence;
            }
        }
        return needMaintence;
    }
    
    public void remove(final Key key) {        
        final Storage storage = getStorage(key);
        storage.remove(key);
    }


    public RepositoryDataOutput getOutputStream(final Key key) {
        return getStorage(key).getOutputStream(key);
    }
    
    public void flush() {
        synchronized (lock) {
            for (Storage storage : storages.values()) {
                try {
                    storage.flush();
                } catch (Exception ex) {
                    RepositoryExceptions.throwException(this, ex);
                }
            }
        }        
    }

    public void shutdown() {
        isShuttedDown = true;
        synchronized (lock) {
            for (Storage storage : storages.values()) {
                try {
                    storage.shutdown();
                } catch (Exception ex) {
                    RepositoryExceptions.throwException(this, ex);
                }
            }
            storages.clear();
        }
    }

    /**
     * @param unitDescriptor
     * @return unitID (i.e. 100001)
     */
    public int getUnitID(UnitDescriptor unitDescriptor) {
        return getUnitID(unitDescriptor, -1);
    }

    /**
     * Returns unitID (i.e. 100001) for unitDescriptor and sourceUnitID.
     *
     * If storageID == -1 - create a new Storage otherwise use already
     * existent storage.
     *
     *
     * @param unitDescriptor
     * @param storageID
     * @return unitID (i.e. 100001)
     */
    public int getUnitID(UnitDescriptor unitDescriptor, int storageID) {
        Storage storage = null;
        if (storageID != -1) {
            synchronized (lock) {
                storage = storages.get(storageID);
            }
            return storage.getUnitID(unitDescriptor);
        } else {
            List<LayerDescriptor> descriptors = getLayerDescriptors(unitDescriptor);
            synchronized (lock) {
                for (Storage st : storages.values()) {
                    if (compareDescriptors(descriptors, st.getLayerDescriptors())) {
                        storage = st;
                        break;
                    }
                }
                if (storage == null) {
                    int newStorageID = storageCounter.incrementAndGet();
                    storage = new Storage(persistMechanismVersion, newStorageID, descriptors, new UnitIDConverterImpl(newStorageID));
                    storages.put(newStorageID, storage);
                }
            }
        }
        return storage.getUnitID(unitDescriptor);
    }

    /**
     *
     * @param sourceUnitId - 200001
     * @return - 2
     */
    public int getStorageID(int sourceUnitId) {
        return sourceUnitId / DENOM;
    }

    private List<LayerDescriptor> getLayerDescriptors(UnitDescriptor unitDescriptor) {
        for (LayerDescriptorProvider provider : Lookup.getDefault().lookupAll(LayerDescriptorProvider.class)) {
            List<LayerDescriptor> layerDescriptors = provider.getLayerDescriptors(unitDescriptor);
            if (layerDescriptors != null) {
                return layerDescriptors;
            }
        }
        return null;
    }

    private boolean compareDescriptors(final List<LayerDescriptor> list1, final List<LayerDescriptor> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).getURI().equals(list2.get(i).getURI())) {
                return false;
            }
        }
        return true;
    }

    public void open(int unitID) {
        Storage unitStorage = getStorage(getStorageID(unitID));
        if (unitStorage != null) {
            unitStorage.openUnit(unitID);
        }
    }

    public void close(int unitID, boolean cleanRepository, Set<Integer> requiredUnits) {
        Storage unitStorage = getStorage(getStorageID(unitID));
        if (unitStorage != null) {
            unitStorage.closeUnit(unitID, cleanRepository, requiredUnits);
        }
    }

    /**
     *
     * @param unitID - 100001
     * @param fileIdx
     * @return
     */
    public CharSequence getFileNameByIdx(int unitID, int fileIdx) {
        Storage storage = getStorage(getStorageID(unitID));
        return storage.getFileName(unitID, fileIdx);
    }

    public int getFileIdByName(int unitID, CharSequence fileName) {
        Storage storage = getStorage(getStorageID(unitID));
        int fileID = storage.getFileID(unitID, fileName);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ID for file {0} [UID=={1}] in storage [{2}] is {3}",
                    new Object[]{fileName, unitID, System.identityHashCode(storage), fileID});
        }
        return fileID;
    }

    public CharSequence getUnitName(int unitID) {
        Storage storage = getStorage(getStorageID(unitID));
        return storage.getUnitName(unitID);
    }

//    public URI getStorageLocation(int storageID) {
//        Storage storage = getStorage(storageID);
//        return storage.getStorageLocation();
//    }

    private Storage getStorage(final Key key) {
        return getStorage(getStorageID(key.getUnitId()));
    }

    private Storage getStorage(final int storageID) {
        synchronized (lock) {
            if (isShuttedDown) {
                return NULL_STORAGE;
            }
            Storage out = storages.get(storageID);
            if (out == null) {
                return NULL_STORAGE;
            }
            return out;
        }
    }

    public void removeUnit(int unitID) {
        Storage storage = getStorage(getStorageID(unitID));
        storage.removeUnit(unitID);
    }

    public LayeringSupport getLayeringSupport(int clientUnitID) {
        Storage storage = getStorage(getStorageID(clientUnitID));
        return storage.getLayeringSupport();
    }

    private static class UnitIDConverterImpl implements UnitsConverter {

        private final int storageID;

        public UnitIDConverterImpl(int storageID) {
            this.storageID = storageID;
        }

        @Override
        public int clientToLayer(int unitID) {
            return unitID % DENOM;
        }

        @Override
        public int layerToClient(int unitID) {
            return storageID * DENOM + unitID;
        }
    }    

    private static class MaintenanceComparator implements Comparator<Storage>, Serializable{
        private static final long serialVersionUID = 7249049246763182397L;

        @Override
        public int compare(Storage storage1, Storage storage2) {
            return storage2.getMaintananceWeight() - storage1.getMaintananceWeight();
        }
    }
}
