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

package org.netbeans.modules.cnd.apt.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;

/**
 */
final class DummyProjectKey implements Key, PersistentFactory, Persistent {

    private static final Map<FileSystem, DummyProjectKey> instances = new HashMap<FileSystem, DummyProjectKey>();
    private static final Object lock = new Object();
    private static boolean first = true;
    
    public static DummyProjectKey getOrCreate(FileSystem fileSystem) {
        synchronized (lock) {
            DummyProjectKey key = instances.get(fileSystem);
            if (key == null) {
                key = new DummyProjectKey(fileSystem);
                instances.put(fileSystem, key);
            }
            return key;
        }
    }

    private final CharSequence unitName;
    private final int unitId;

    private DummyProjectKey(FileSystem fileSystem) {
        synchronized (lock) {
            if (first) {
                first = false;
                Repository.startup(RepositoryUtils.getPersistenceVersion());
            }
        }
        unitName = "dummy";
        unitId = Repository.getUnitId(new UnitDescriptor(unitName, fileSystem));
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return this;
    }

    @Override
    public CharSequence getUnit() {
        return unitName;
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public Behavior getBehavior() {
        return Behavior.Default;
    }

    @Override
    public boolean hasCache() {
        return false;
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public CharSequence getAt(int level) {
        return unitName;
    }

    @Override
    public int getSecondaryDepth() {
        return 0;
    }

    @Override
    public int getSecondaryAt(int level) {
        return 0;
    }

    @Override
    public int hashCode(int unitID) {
        return unitID;
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(RepositoryDataOutput out, Persistent obj) throws IOException {
    }

    @Override
    public Persistent read(RepositoryDataInput in) throws IOException {
        return this;
    }
    
}
