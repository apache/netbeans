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
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
public abstract class FileComponent implements Persistent, SelfPersistent {
    private final Key key;
    /**
     * 
     * @param key if key is not null then it's persistent instance, otherwise in-memory
     */
    protected FileComponent(Key key) {
        this.key = key;
    }

    protected FileComponent(FileComponent other) {
        this.key = other.key;
        assert key != null;
    }
    
    public FileComponent(RepositoryDataInput in) throws IOException {
        key = KeyFactory.getDefaultFactory().readKey(in);
        assert key != null;
    }

    public Key getKey() {
        assert key != null;
        return key;
    }

    void put() {
        assert key != null;
        RepositoryUtils.put(key, this);
    }

    @Override
    public void write(RepositoryDataOutput out) throws IOException {
        assert key != null;
        KeyFactory.getDefaultFactory().writeKey(key, out);
    }
}

