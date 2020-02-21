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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.UnitDescriptorsList;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class DiskLayerImpl {

    private final FilesAccessStrategyImpl fas;
    private final LayerDescriptor layerDescriptor;
    private final LayerIndex layerIndex;

    public DiskLayerImpl(LayerDescriptor layerDescriptor, LayeringSupport layeringSupport) {
        this.layerDescriptor = layerDescriptor;
        URI cacheDirectory = layerDescriptor.getURI();
        layerIndex = new LayerIndex(cacheDirectory);
        fas = new FilesAccessStrategyImpl(layerIndex, cacheDirectory, layerDescriptor, layeringSupport);
    }
        
    

    public Collection<LayerKey> removedTableKeySet() {
        return fas.removedTableKeySet();
    }
        
    
    public boolean startup(int persistMechanismVersion, boolean recreate, boolean isWritable) {
        return layerIndex.load(persistMechanismVersion, recreate, isWritable);
    }

    public LayerDescriptor getLayerDescriptor() {
        return layerDescriptor;
    }

    public void shutdown() {
        fas.shutdown(layerDescriptor.isWritable());
    }

    public void openUnit(int unitIdx) {
        //does  nothing now
    }

    public void closeUnit(int unitIdx, boolean cleanRepository, Set<Integer> requiredUnits) {
        throw new InternalError();
    }

    void closeUnit(int unitIdInLayer, boolean cleanRepository, Set<Integer> requiredUnits, boolean isWritable) {
        layerIndex.closeUnit(unitIdInLayer, cleanRepository, requiredUnits);
        fas.closeUnit(unitIdInLayer, cleanRepository);
    }


    public UnitDescriptorsList getUnitsTable() {
        return layerIndex.getUnitsTable();
    }

    public List<FileSystem> getFileSystemsTable() {
        return layerIndex.getFileSystemsTable();
    }

    public ReadLayerCapability getReadCapability() {
        return fas;
    }

    public WriteLayerCapability getWriteCapability() {
        return fas;
    }

    /**
     * Only layer can do this matching...
     *
     * (i.e. RMI layer may know that enum:/tmp on client side should be treated
     * as 'localhost' on enum... )
     *
     * @param clientFileSystem
     * @return
     */
    public int findMatchedFileSystemIndexInLayer(FileSystem clientFileSystem) {
        return layerIndex.getFileSystemsTable().indexOf(clientFileSystem);
    }

    public void storeIndex() {
        try {
            layerIndex.store();
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        }
    }
}
