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
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * help class for CsmUID based on CsmObject
 */
public abstract class ObjectBasedUID<T> implements CsmUID<T>, SelfPersistent {

    private final T ref;

    protected ObjectBasedUID(T ref) {
        this.ref = ref;
    }

    @Override
    public T getObject() {
        return this.ref;
    }

    @Override
    public String toString() {
        String retValue = "UID for " + ref.toString(); // NOI18N
        return retValue;
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ObjectBasedUID other = (ObjectBasedUID) obj;
        return this.ref.equals(other.ref);
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl for Persistent 
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        assert ref == null || ref instanceof Persistent;
        CsmObjectFactory.instance().write(output, (Persistent) ref);
    }

    @SuppressWarnings("unchecked")
    public ObjectBasedUID(RepositoryDataInput input) throws IOException {
        ref = (T) CsmObjectFactory.instance().read(input);
    }
}
