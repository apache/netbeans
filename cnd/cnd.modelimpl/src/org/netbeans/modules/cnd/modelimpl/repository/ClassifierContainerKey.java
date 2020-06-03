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
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;

/**
 *
 */
public final class ClassifierContainerKey extends ProjectContainerKey {

    public ClassifierContainerKey(int unitId) {
        super(unitId);
    }

    ClassifierContainerKey(RepositoryDataInput in) throws IOException {
        super(in);
    }

    ClassifierContainerKey(KeyDataPresentation presentation) {
        super(presentation);
    }

    @Override
    public int getSecondaryAt(int level) {
        assert (level == 0);
        return getHandler();
    }

    @Override
    public short getHandler() {
        return KeyObjectFactory.KEY_CLASSIFIER_CONTAINER_KEY;
    }

    @Override
    public int getSecondaryDepth() {
        return 1;
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }

    @Override
    public boolean hasCache() {
        return true;
    }

    @Override
    public String toString() {
        return "ClassifierContainerKey " + getProjectName(); // NOI18N
    }
}
