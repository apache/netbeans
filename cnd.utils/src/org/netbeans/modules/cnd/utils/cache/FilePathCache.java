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

package org.netbeans.modules.cnd.utils.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.CharSequences;

/**
 * cache entry
 */
public class FilePathCache  extends APTStringManager {
    private static final APTStringManager manager = new FilePathCache();
    private static final APTStringManager instance = 
            APTStringManager.instance(APTStringManager.FILE_PATH_MANAGER, APTStringManager.CacheKind.Sliced);
    
    private FilePathCache() {
    }
    
    @Override
    public CharSequence getString(CharSequence text) {
        text = CharSequences.create(text);
        return instance.getString(text);
    }

    public static List<CharSequence> asList(Collection<? extends CharSequence> paths) {
        List<CharSequence> out = new ArrayList<CharSequence>(paths.size());
        for (CharSequence path : paths) {
            out.add(FilePathCache.getManager().getString(path));
        }
        return out;
    }

    @Override
    public void dispose() {
        instance.dispose();
    }

    public static APTStringManager getManager() {
        return manager;
    }
}
