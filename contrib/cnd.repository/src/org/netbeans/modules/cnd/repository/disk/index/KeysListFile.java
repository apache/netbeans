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

package org.netbeans.modules.cnd.repository.disk.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
public final class KeysListFile implements SelfPersistent {
    //will be used from one thread onle: writing thread,
    //so no sync problems
    private final List<LayerKey> list = new ArrayList<LayerKey>();
    
    public KeysListFile() {}
    
    public KeysListFile (RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            list.add(LayerKey.read(input));
        }
    }
        

    public void put(LayerKey key) {        
        list.add(key);
    }

    public void remove(LayerKey key) {
        list.remove(key);
    }

    public int size() {
        return list.size();
    }

    public Collection<LayerKey> keySet() {
        return Collections.unmodifiableList(list);
    }

    public Iterator<LayerKey> getKeySetIterator() {
        return Collections.unmodifiableCollection(list).iterator();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        output.writeInt(size());
        final Iterator<LayerKey> keySetIterator = getKeySetIterator();        
        while (keySetIterator.hasNext()) {
            LayerKey.write(keySetIterator.next(), output);            
        }
    }
    
}
