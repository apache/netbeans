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

package org.netbeans.modules.cnd.repository.test;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


/**
 * Test interface Implementation 
 * for tests
 */
public class TestKey implements Key, SelfPersistent {
    
    private final String key;
    private final String unit;
    private final int unitId;
    private final Behavior behavior;
    
    @Override
    public Behavior getBehavior() {
	return Behavior.Default;
    }
    
    public TestKey(String key, int unitId, String unit, Behavior behavior) {
	this.key = key;
        this.unit = unit;
        this.unitId = unitId;
        this.behavior = behavior;
    }
    

    public TestKey(RepositoryDataInput stream) throws IOException {
        this(stream.readUTF(), stream.readInt(), stream.readUTF(),
                stream.readBoolean() ? Behavior.LargeAndMutable : Behavior.Default);
    }
    
    
    @Override
    public String getAt(int level) {
	return key;
    }
    
    @Override
    public int getDepth() {
	return 1;
    }
    
    @Override
    public PersistentFactory getPersistentFactory() {
	return TestFactory.instance();
    }
    
    @Override
    public int getSecondaryAt(int level) {
	return 0;
    }
    
    @Override
    public int getSecondaryDepth() {
	return 0;
    }

    @Override
    public final boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }        
        final TestKey other = (TestKey) object;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unit != other.unit && (this.unit == null || !this.unit.equals(other.unit))) {
            return false;
        }
        return this.behavior == other.behavior;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        } 
        
        final TestKey other = (TestKey) object;
        if (unitId != other.unitId) {
            return false;
        }
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unit != other.unit && (this.unit == null || !this.unit.equals(other.unit))) {
            return false;
        }
        return this.behavior == other.behavior;        
    }

    @Override
    public final int hashCode(int unitID) {
        int hash = this.key != null ? this.key.hashCode() : 0;
        hash = 59 * hash + (this.unit != null ? this.unit.hashCode() : 0);
        hash = 59 * hash + (this.behavior != null ? this.behavior.hashCode() : 0);
        return hash + unitID;
    }

    @Override
    public final int hashCode() {
         return hashCode(unitId);
    }
    
    @Override
    public String toString() {
	return unitId + ' ' + unit + ':' + key + ' ' + behavior;
    }

    @Override
    public String getUnit() {
	return unit;
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        output.writeUTF(key);
        output.writeInt(unitId);
        output.writeUTF(unit);
        output.writeBoolean(behavior == Behavior.LargeAndMutable);
    }

    @Override
    public boolean hasCache() {
        return false;
    }
}
