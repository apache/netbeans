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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
/*package*/ abstract class NamespaceBaseKey extends ProjectContainerKey {

    private final CharSequence fqn;
    private int hashCode; // cashed hash code

    NamespaceBaseKey(CsmNamespace ns) {
        super(((ProjectBase) ns.getProject()).getUnitId());
        this.fqn = ns.getQualifiedName();
    }

    NamespaceBaseKey(KeyDataPresentation presentation) {
        super(presentation);
        fqn = presentation.getNamePresentation();
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }

    @Override
    public int hashCode(int unitID) {
        return 17*fqn.hashCode() + super.hashCode(unitID);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = hashCode(getUnitId());
        }
        return hashCode;
    }


    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (!super.equals(thisUnitID, object, objectUnitID)) {
            return false;
        }
        NamespaceBaseKey other = (NamespaceBaseKey) object;
        return this.fqn.equals(other.fqn);
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        assert fqn != null;
        PersistentUtils.writeUTF(fqn, aStream);
    }

    /*package*/ NamespaceBaseKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        fqn = PersistentUtils.readUTF(aStream, QualifiedNameCache.getManager());
        assert fqn != null;
    }

    @Override
    public int getDepth() {
        assert super.getDepth() == 0;
        return 1;
    }

    @Override
    public CharSequence getAt(int level) {
        assert super.getDepth() == 0 && level < getDepth();
        return this.fqn;
    }

    @Override
    public int getSecondaryDepth() {
        return 1;
    }

    @Override
    public boolean hasCache() {
        return true;
    }

    @Override
    public final CharSequence getNamePresentation() {
        return fqn;
    }
}
