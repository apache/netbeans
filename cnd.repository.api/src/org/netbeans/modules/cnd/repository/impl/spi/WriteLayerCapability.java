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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface WriteLayerCapability extends LayerCapability {

    public void write(LayerKey key, ByteBuffer byteBuffer);

    public void remove(LayerKey key, boolean keepRemovedTable);

    public void removeUnit(int unitIDInLayer);
    
    public void closeUnit(int unitIDInLayer, boolean cleanRespository);

    public int registerNewUnit(UnitDescriptor unitDescriptor);

    public int registerClientFileSystem(FileSystem clientFileSystem);
    
/** 
     * Determines the necessity of maintenance.
     * When a maintenancy is to be done, repository
     * sorts all units in accordance with the returned value.
     * So greater is the value, more need in maintenance unit has.
     */
    public int getMaintenanceWeight() throws IOException;
    
/**
     * Performes necessary maintenance (such as defragmentation) during the given timeout
     * @return true if maintenance was finished by timeout and needs more time to be completed
     */
    public boolean maintenance(long timeout)  throws IOException;    
}
