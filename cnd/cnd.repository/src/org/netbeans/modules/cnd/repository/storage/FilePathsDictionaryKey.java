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

import java.io.IOException;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
final  class FilePathsDictionaryKey implements Key, SelfPersistent {
    private static final String UNITS_INDEX_FILE_NAME = "project-index";//NOI18N

    private final int unitId;

    public FilePathsDictionaryKey(int unitId) {
        this.unitId = unitId;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return FilePathsDictionaryPersistentFactory.instance();
    }

    @Override
    public CharSequence getUnit() {
        return Repository.getUnitName(this.unitId);
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public Behavior getBehavior() {
        return Behavior.LargeAndMutable;
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
        return UNITS_INDEX_FILE_NAME;
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
        return 37 + unitID;
    }

    @Override
    public int hashCode() {
        return hashCode(unitId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || (this.getClass() != obj.getClass())) {
             return false;
         }
        FilePathsDictionaryKey other = (FilePathsDictionaryKey) obj;
        return equals(unitId, other, other.unitId);
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (this == object) {
            return true;
        }
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }
        return thisUnitID == objectUnitID;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        output.writeUnitId(unitId);
    }

}
