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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.Layer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.UnitDescriptorsList;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.openide.filesystems.FileSystem;

/**
 *
 */
final class DiskLayerImplDelegate implements Layer {

    private static final Map<DiskLayerImpl, AtomicInteger> refCounter = new HashMap<DiskLayerImpl, AtomicInteger>();
    private static final Set<DiskLayerImpl> writableLayers = new HashSet<DiskLayerImpl>();
    private final LayerDescriptor layerDescriptor;
    private final DiskLayerImpl impl;

    public DiskLayerImplDelegate(DiskLayerImpl impl, LayerDescriptor layerDescriptor) {
        this.impl = impl;
        this.layerDescriptor = layerDescriptor;
        AtomicInteger counter = refCounter.get(impl);
        if (counter == null) {
            counter = new AtomicInteger();
            refCounter.put(impl, counter);
        }
        counter.incrementAndGet();
        if (layerDescriptor.isWritable()) {
            writableLayers.add(impl);
        }
    }

    @Override
    public Collection<LayerKey> removedTableKeySet() {
        return impl.removedTableKeySet();
    }
    
    

    @Override
    public boolean startup(int persistMechanismVersion, boolean recreate) {
        return impl.startup(persistMechanismVersion, recreate, layerDescriptor.isWritable());
    }

    @Override
    public ReadLayerCapability getReadCapability() {
        return impl.getReadCapability();
    }

    @Override
    public WriteLayerCapability getWriteCapability() {
        return layerDescriptor.isWritable() ? impl.getWriteCapability() : null;
    }

    @Override
    public LayerDescriptor getLayerDescriptor() {
        return layerDescriptor;
    }

    @Override
    public void shutdown() {
        AtomicInteger counter = refCounter.get(impl);
        if (counter.decrementAndGet() == 0) {
            if (writableLayers.remove(impl)) {
                impl.storeIndex();
            }
            impl.shutdown();
        }
    }

    @Override
    public void openUnit(int unitIdInLayer) {
        impl.openUnit(unitIdInLayer);
    }

    @Override
    public void closeUnit(int unitIdInLayer, boolean cleanRepository, Set<Integer> requiredUnits) {
        impl.closeUnit(unitIdInLayer, cleanRepository, requiredUnits, writableLayers.contains(impl));
    }


    @Override
    public UnitDescriptorsList getUnitsTable() {
        return impl.getUnitsTable();
    }

    @Override
    public List<FileSystem> getFileSystemsTable() {
        return impl.getFileSystemsTable();
    }

    @Override
    public int findMatchedFileSystemIndexInLayer(FileSystem clientFileSystem) {
        return impl.findMatchedFileSystemIndexInLayer(clientFileSystem);
    }
}
