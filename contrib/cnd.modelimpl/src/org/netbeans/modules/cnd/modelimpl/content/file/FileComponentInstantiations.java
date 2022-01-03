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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.FileInstantiationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Instantiations are not created during parsing process.
 * And they do not have container that will dispose then on reparse action.
 * So this container is created to solve this issue.
 *
 */
public class FileComponentInstantiations extends FileComponent {

    private Set<CsmUID<CsmInstantiation>> instantiations = createInstantiations();
    private final ReadWriteLock instantiationsLock = new ReentrantReadWriteLock();

    // empty stub
    private static final FileComponentInstantiations EMPTY = new FileComponentInstantiations() {

        @Override
        public void addInstantiation(CsmInstantiation inst) {
        }

        @Override
        void put() {
        }
    };

    public static FileComponentInstantiations empty() {
        return EMPTY;
    }

    FileComponentInstantiations(FileComponentInstantiations other, boolean empty) {
        super(other);
        if (!empty) {
            try {
                other.instantiationsLock.readLock().lock();
                instantiations.addAll(other.instantiations);
            } finally {
                other.instantiationsLock.readLock().unlock();
            }
        }
    }

    public FileComponentInstantiations(FileImpl file) {
        super(new FileInstantiationsKey(file));
    }

    public FileComponentInstantiations(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.readUIDCollection(this.instantiations, input);
    }

    // only for EMPTY static field
    private FileComponentInstantiations() {
        super((org.netbeans.modules.cnd.repository.spi.Key)null);
    }

    void clean() {
        _clearInstantiations();
        // PUT should be done by FileContent
//        put();
    }

    private void _clearInstantiations() {
        try {
            instantiationsLock.writeLock().lock();
            RepositoryUtils.remove(instantiations);
            instantiations = createInstantiations();
        } finally {
            instantiationsLock.writeLock().unlock();
        }
    }

    public void addInstantiation(CsmInstantiation inst) {
        // TODO: is it safe to put smth into repository directly?
        CsmUID<CsmInstantiation> instUID = RepositoryUtils.put(inst);
        assert instUID != null;
        try {
            instantiationsLock.writeLock().lock();
            instantiations.add(instUID);
        } finally {
            instantiationsLock.writeLock().unlock();
        }
        // TODO: PUT should be done by FileContent?
        put();
    }

    private Set<CsmUID<CsmInstantiation>> createInstantiations() {
        return new HashSet<>();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            instantiationsLock.readLock().lock();
            factory.writeUIDCollection(instantiations, output, false);
        } finally {
            instantiationsLock.readLock().unlock();
        }
    }
}
