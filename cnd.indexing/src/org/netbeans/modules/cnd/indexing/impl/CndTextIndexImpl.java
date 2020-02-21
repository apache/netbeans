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
package org.netbeans.modules.cnd.indexing.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.indexing.api.CndTextIndex;
import org.netbeans.modules.cnd.indexing.api.CndTextIndexKey;

/**
 *
 */
public class CndTextIndexImpl {

    public void put(CndTextIndexKey key, Set<CharSequence> ids) {
        TextIndexStorage index = TextIndexStorageManager.get(key.getUnitId());
        if (index != null) {
            index.put(key, ids);
        }
    }

    public List<CndTextIndexKey> query(int unitID, CharSequence text) {
        TextIndexStorage index = TextIndexStorageManager.get(unitID);

        if (index == null) {
            return Collections.<CndTextIndexKey>emptyList();
        }

        try {
            return index.query(text);
        } catch (Exception ex) {
            Logger.getLogger(CndTextIndex.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.<CndTextIndexKey>emptyList();
        }
    }

    public void remove(CndTextIndexKey key) {
        TextIndexStorage index = TextIndexStorageManager.get(key.getUnitId());
        if (index != null) {
            index.remove(key);
        }
    }
}
