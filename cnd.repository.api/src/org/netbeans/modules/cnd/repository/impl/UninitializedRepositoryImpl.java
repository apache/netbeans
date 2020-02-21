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
package org.netbeans.modules.cnd.repository.impl;

import java.util.Set;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.RepositoryImplementation;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 */
public final class UninitializedRepositoryImpl implements RepositoryImplementation {

    private final String msg;

    public UninitializedRepositoryImpl(String msg) {
        this.msg = msg;
    }

    @Override
    public Persistent get(Key key) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void put(Key key, Persistent obj) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void remove(Key key) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void openUnit(int unitId) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void removeUnit(int unitId) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void hang(Key key, Persistent obj) {
        throw new IllegalStateException(msg);
    }

    @Override
    public void debugDistribution() {
        throw new IllegalStateException(msg);
    }

    @Override
    public void debugDump(Key key) {
        throw new IllegalStateException(msg);
    }

    @Override
    public int getFileIdByName(int unitId, CharSequence fileName) {
        throw new IllegalStateException(msg);
    }

    @Override
    public CharSequence getFileNameById(int unitId, int fileId) {
        throw new IllegalStateException(msg);
    }

    @Override
    public CharSequence getFileNameByIdSafe(int unitId, int fileId) {
        throw new IllegalStateException(msg);
    }

    @Override
    public CharSequence getUnitName(int unitId) {
        throw new IllegalStateException(msg);
    }

    @Override
    public int getUnitID(UnitDescriptor unitDescriptor, int storageID) {
        throw new IllegalStateException(msg);
    }

    @Override
    public int getUnitID(UnitDescriptor unitDescriptor) {
        throw new IllegalStateException(msg);
    }

    @Override
    public int getRepositoryID(int sourceUnitId) {
        throw new IllegalStateException(msg);
    }

    @Override
    public LayeringSupport getLayeringSupport(int clientUnitID) {
        return null;
    }
}
