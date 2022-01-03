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
package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.repository.KeyHolder;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * help class for CsmUID based on repository Key
 */
public abstract class KeyBasedUID<T> implements CsmUID<T>, KeyHolder, SelfPersistent, Comparable<CsmUID<T>> {

    private final Key key;

    protected KeyBasedUID(Key key) {
        assert key != null;
        this.key = key;
    }

    @Override
    public T getObject() {
        return RepositoryUtils.get(this);
    }

    @Override
    public Key getKey() {
        return key;
    }

    public abstract void dispose(T obj);

    @Override
    public String toString() {
        String retValue;

        retValue = key.toString();
        return "KeyBasedUID on " + retValue; // NOI18N
    }

    @Override
    public int hashCode() {
        int retValue;

        retValue = key.hashCode();
        return retValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        KeyBasedUID<?> other = (KeyBasedUID<?>) obj;
        return this.key.equals(other.key);
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        KeyObjectFactory.getDefaultFactory().writeKey(key, aStream);
    }

    /* package */ KeyBasedUID(RepositoryDataInput aStream) throws IOException {
        key = KeyObjectFactory.getDefaultFactory().readKey(aStream);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(CsmUID<T> o) {
        assert o != null;
        assert o instanceof KeyBasedUID;
        Comparable o1 = (Comparable) this.key;
        Comparable o2 = (Comparable) ((KeyBasedUID) o).key;
        return o1.compareTo(o2);
    }
}    
