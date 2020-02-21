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

package org.netbeans.modules.cnd.apt.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.openide.util.CharSequences;

/**
 *
 */
public final class APTIncludePathStorage {
    private final ConcurrentMap<CharSequence, List<IncludeDirEntry>> allIncludes = new ConcurrentHashMap<CharSequence, List<IncludeDirEntry>>();
    
    public APTIncludePathStorage() {
    }

    public List<IncludeDirEntry> get(CharSequence configID,  List<IncludePath> includes) {
        CharSequence key = CharSequences.create(configID);
        List<IncludeDirEntry> list = allIncludes.get(key);
        if (list == null) {
            // create new one with light char sequences and put in map
            list = new ArrayList<IncludeDirEntry>(includes.size());
            for (IncludePath cs : includes) {
                IncludeDirEntry inclEntry = IncludeDirEntry.get(cs.getFSPath(), cs.isFramework(), cs.ignoreSysRoot());
                list.add(inclEntry);
            }
            List<IncludeDirEntry> old = allIncludes.putIfAbsent(key, list);
            if (old != null) {
                list = old;
            }
        } else {
            for (IncludeDirEntry e : list) {
                e.resetNonExistanceFlag();
            }
        }
        return list;
    }
    
    public void dispose() {
        allIncludes.clear();
    }
}
