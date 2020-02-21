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
package org.netbeans.modules.cnd.repository.impl.spi;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.openide.filesystems.FileSystem;

/**
 *
 *
 */
public interface Layer {

    public boolean startup(int persistMechanismVersion, boolean recreate);

    public void shutdown();

    public void openUnit(int unitIdInLayer);

    public void closeUnit(int unitIdInLayer, boolean cleanRepository,
            Set<Integer> requiredUnits);



    public UnitDescriptorsList getUnitsTable();

    public List<FileSystem> getFileSystemsTable();

    public ReadLayerCapability getReadCapability();

    public WriteLayerCapability getWriteCapability();
    
    public Collection<LayerKey> removedTableKeySet();

    //-----------------------------------------        
    public LayerDescriptor getLayerDescriptor();

    /**
     * Returns an index in the FileSystemsTable for an equivalent client
     * FileSystem or -1 if no matching found.
     *
     * @param clientFileSystem
     */
    public int findMatchedFileSystemIndexInLayer(FileSystem clientFileSystem);
}
