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
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;

/**
 *
 */
public final class LayerKey implements Key {
    
    private final Key delegate;
    private final int unitId;

    private LayerKey(Key delegate, int unitIdInLayer) {
        this.delegate = delegate;
        this.unitId = unitIdInLayer;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return delegate.getPersistentFactory();
    }

    @Override
    public CharSequence getUnit() {
        return delegate.getUnit();
    }

    @Override
    public int getUnitId() {
        return unitId;
    }

    @Override
    public Behavior getBehavior() {
        return delegate.getBehavior();
    }

    @Override
    public boolean hasCache() {
        return delegate.hasCache();
    }

    @Override
    public int getDepth() {
        return delegate.getDepth();
    }

    @Override
    public CharSequence getAt(int level) {
        return delegate.getAt(level);
    }

    @Override
    public int getSecondaryDepth() {
        return delegate.getSecondaryDepth();
    }

    @Override
    public int getSecondaryAt(int level) {
        return delegate.getSecondaryAt(level);
    }



    @Override
    public String toString() {
        return "LayerKey unitId=" + unitId + " delegate=" + delegate.getClass().getName(); // NOI18N
    }

    @Override
    public int hashCode() {
        final int res = delegate.hashCode();
        assert delegate.hashCode(0) + delegate.getUnitId()== res;
        return res - delegate.getUnitId() + unitId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LayerKey)) {
            return false;
        }
        final LayerKey other = (LayerKey) obj;
        if (other.delegate == delegate) {
            return true;
        }
        if (other.getUnitId() != unitId) {
            return false;
        }
        return delegate.equals(unitId, other.delegate, other.getUnitId());
    }

    public static LayerKey create(Key key, int unitIdInLayer) {
        return new LayerKey(key, unitIdInLayer);
    }

    public static void write(LayerKey key, RepositoryDataOutput output) throws IOException {
        output.writeInt(key.getUnitId());
        KeyFactory.getDefaultFactory().writeKey(key.delegate, output);
    }

    public static LayerKey read(RepositoryDataInput input) throws IOException {
        int unitIdInLayer = input.readInt();
        Key key = KeyFactory.getDefaultFactory().readKey(input);
        return new LayerKey(key, unitIdInLayer);
    }
    

    @Override
    public int hashCode(int unitID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        throw new UnsupportedOperationException();
    }
    

    
}
