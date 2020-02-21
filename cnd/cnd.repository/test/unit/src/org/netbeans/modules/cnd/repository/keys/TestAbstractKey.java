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
package org.netbeans.modules.cnd.repository.keys;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

public abstract class TestAbstractKey implements Key, SelfPersistent {

    private final String key;
    private final CharSequence unitName;
    private final int unitID;

    public TestAbstractKey(RepositoryDataInput stream) throws IOException {
        this.key = stream.readUTF();
        this.unitID = stream.readUnitId();
        this.unitName = Repository.getUnitName(unitID);
    }

    public TestAbstractKey(String key, int unitID) {
        this.key = key;
        this.unitID = unitID;
        this.unitName = Repository.getUnitName(unitID);
    }

    @Override
    public int getSecondaryAt(int level) {
        return 0;
    }

    @Override
    public String getAt(int level) {
        return key;
    }

    @Override
    public CharSequence getUnit() {
        return unitName;
    }

    @Override
    public int getUnitId() {
        return unitID;
    }

    @Override
    public int getSecondaryDepth() {
        return 0;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return TestValuePersistentFactory.getInstance();
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {        
        output.writeUTF(key);
        output.writeUnitId(getUnitId());
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (thisUnitID != objectUnitID) {
            return false;
        }
        final TestAbstractKey other = (TestAbstractKey) object;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unitName != other.unitName && (this.unitName == null || !this.unitName.equals(other.unitName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(int unitID) {
        int hash = this.key != null ? this.key.hashCode() : 0;
        hash = 59 * hash + (this.unitName != null ? this.unitName.hashCode() : 0);
        return hash + unitID;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }
        final TestAbstractKey other = (TestAbstractKey) object;
        return equals(unitID, other, other.unitID);
    }

    @Override
    public int hashCode() {
        return hashCode(unitID);
    }       
    
    abstract protected short getHandler();
}