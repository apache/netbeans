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

import java.util.Set;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Repository implementation.
 *
 * Some basic facts about repository
 *
 * the main responsibility is to instantiate Persistent objects It knows keys,
 * it can get factories that will read-out and instantiate objects from provided
 * repostreams...
 *
 * - no encoding is done here. Encoding is 'done' in repostreams....
 *
 */
public interface RepositoryImplementation {

    // Main responsibility
    Persistent get(Key key);

    void put(Key key, Persistent obj);

    void remove(Key key);

    void shutdown();

    // -------------------------------------------------------
    // Performance hints -------------------------------------------------------
    // ?? performance should not be managed outside
    // closeUnit is a notification that repository may 'forget' about some data
    // associated with the unit
    void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits);

    // Do not garbage-collect related data in 10 seconds... 
    void openUnit(int unitId);

    // Remove not only this Key, but all subsequent info
    void removeUnit(int unitId);

    // Cache....
    // Stick object in memory (cache) 
    void hang(Key key, Persistent obj);

    public int getFileIdByName(int unitId, CharSequence fileName);

    public CharSequence getFileNameById(int unitId, int fileId);

    public CharSequence getFileNameByIdSafe(int unitId, int fileId);

    public CharSequence getUnitName(int unitId);

    public int getUnitID(UnitDescriptor unitDescriptor, int storageID);

    public int getUnitID(UnitDescriptor unitDescriptor);
    
    public LayeringSupport getLayeringSupport(int clientUnitID);

    // Debugging and logging...
    // --------------------------------------------------------------------
    void debugDistribution();

    void debugDump(Key key);

    public int getRepositoryID(int unitId);
}
