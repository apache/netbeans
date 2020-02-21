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
package org.netbeans.modules.cnd.modelimpl.content.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public final class FakeIncludePair {

    final CsmUID<IncludeImpl> includeUid;
    final CsmUID<CsmOffsetableDeclaration> containerUid;
    private volatile boolean alreadyFixed;

    public FakeIncludePair(CsmUID<IncludeImpl> includeUid, CsmUID<CsmOffsetableDeclaration> containerUID) {
        this.includeUid = includeUid;
        this.containerUid = containerUID;
        this.alreadyFixed = false;
    }

    public boolean isFixed() {
        return alreadyFixed;
    }

    public void markFixed() {
        assert !alreadyFixed;
        alreadyFixed = true;
    }

    public CsmUID<IncludeImpl> getIncludeUid() {
        return includeUid;
    }

    public CsmUID<CsmOffsetableDeclaration> getContainerUid() {
        return containerUid;
    }

    private void write(RepositoryDataOutput output) throws IOException {
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUID(includeUid, output);
        factory.writeUID(containerUid, output);
        output.writeBoolean(alreadyFixed);
    }

    private FakeIncludePair(RepositoryDataInput input) throws IOException {
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        includeUid = factory.readUID(input);
        containerUid = factory.readUID(input);
        alreadyFixed = input.readBoolean();
    }

    public static void write(List<FakeIncludePair> coll, RepositoryDataOutput output) throws IOException {
        assert output != null;
        Collection<FakeIncludePair> copy = new ArrayList<>(coll);
        int collSize = copy.size();
        output.writeInt(collSize);

        for (FakeIncludePair pair : copy) {
            assert pair != null;
            pair.write(output);
        }
    }

    public static void read(List<FakeIncludePair> coll, RepositoryDataInput input) throws IOException {
        int collSize = input.readInt();
        for (int i = 0; i < collSize; i++) {
            FakeIncludePair pair = new FakeIncludePair(input);
            coll.add(pair);
        }
    }

    @Override
    public String toString() {
        return "FakeIncludePair{" + "includeUid=" + includeUid + ", containerUid=" + containerUid + ", alreadyFixed=" + alreadyFixed + '}'; // NOI18N
    }
}
