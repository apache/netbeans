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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroCache;
import org.netbeans.modules.cnd.apt.impl.support.SnapshotHolderCache;
import org.netbeans.modules.cnd.apt.impl.support.APTSystemMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankSystemMacroMap;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.FSPath;

/**
 *
 */
public final class APTSystemStorage {
    private final Map<String, PPMacroMap> allMacroMaps = new HashMap<String, PPMacroMap>();
    private final APTIncludePathStorage includesStorage;
    private final static String baseNewName = "#SYSTEM MACRO MAP# "; // NOI18N
    private final static APTSystemStorage instance = new APTSystemStorage();
    
    private APTSystemStorage() {
        includesStorage = new APTIncludePathStorage();
    }
    
    public static APTSystemStorage getInstance() {
        return instance;
    }
    
    public PPMacroMap getMacroMap(String configID, List<String> sysMacros) {
        synchronized (allMacroMaps) {
            PPMacroMap map = allMacroMaps.get(configID);
            if (map == null) {
                // create new one and put in map
                if (APTTraceFlags.USE_CLANK) {
                    map = new ClankSystemMacroMap(sysMacros);
                } else {
                    map = new APTSystemMacroMap(sysMacros);
                }
                allMacroMaps.put(configID, map);
                if (APTUtils.LOG.isLoggable(Level.FINE)) {
                    APTUtils.LOG.log(Level.FINE,
                            "new system macro map was added\n {0}", // NOI18N
                            new Object[] { map });
                }
            }
            return map;
        }
    }
    
//    // it's preferable to use getIncludes(String configID, List sysIncludes)
//    public List<CharSequence> getIncludes(List<CharSequence> sysIncludes) {
//        return includesStorage.get(sysIncludes);
//    }
    
    public List<IncludeDirEntry> getIncludes(CharSequence configID, List<IncludePath> sysIncludes) {
        return includesStorage.get(configID, sysIncludes);
    }   
    
    private void disposeImpl() {
        synchronized (allMacroMaps) {
            allMacroMaps.clear();
        }
        includesStorage.dispose();
    }
    
    public static void dispose() {
        instance.disposeImpl();
        APTMacroCache.getManager().dispose();
        SnapshotHolderCache.getManager().dispose();
        IncludeDirEntry.disposeCache();
    }
}
