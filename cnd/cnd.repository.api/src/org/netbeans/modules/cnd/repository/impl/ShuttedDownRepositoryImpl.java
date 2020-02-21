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
public final class ShuttedDownRepositoryImpl implements RepositoryImplementation {

    private final String msg;

    public ShuttedDownRepositoryImpl(String msg) {
        this.msg = msg;
    }

    @Override
    public Persistent get(Key key) {
        return null;
    }

    @Override
    public void put(Key key, Persistent obj) {
    }

    @Override
    public void remove(Key key) {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits) {
    }

    @Override
    public void openUnit(int unitId) {
    }

    @Override
    public void removeUnit(int unitId) {
    }

    @Override
    public void hang(Key key, Persistent obj) {
    }

    @Override
    public void debugDistribution() {
    }

    @Override
    public void debugDump(Key key) {
    }

    @Override
    public int getFileIdByName(int unitId, CharSequence fileName) {
        return -1;
    }

    @Override
    public CharSequence getFileNameById(int unitId, int fileId) {
        return null;
    }

    @Override
    public CharSequence getFileNameByIdSafe(int unitId, int fileId) {
        return null;
    }

    @Override
    public CharSequence getUnitName(int unitId) {
        return null;
    }

    @Override
    public int getUnitID(UnitDescriptor unitDescriptor, int storageID) {
        return -1;
    }

    @Override
    public int getUnitID(UnitDescriptor unitDescriptor) {
        return -1;
    }

    @Override
    public int getRepositoryID(int sourceUnitId) {
        return -1;
    }

    @Override
    public LayeringSupport getLayeringSupport(int clientUnitID) {
        return null;
    }
}
