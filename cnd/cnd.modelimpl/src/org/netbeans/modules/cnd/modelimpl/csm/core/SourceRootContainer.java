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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class SourceRootContainer {
    private final Map<CharSequence,Integer> projectRoots = new ConcurrentHashMap<>();
    private final boolean isFixedRoots;
    
    public SourceRootContainer(boolean isFixedRoots) {
        this.isFixedRoots = isFixedRoots;
    }
    
    public boolean isMySource(CharSequence includePath){
        if (projectRoots.size() < 10) {
            boolean check = false;
            for(Map.Entry<CharSequence,Integer> entry : projectRoots.entrySet()) {
                if (CharSequenceUtils.startsWith(includePath, entry.getKey())) {
                    if (includePath.length() == entry.getKey().length()) {
                        return true;
                    }
                    check = true;
                    break;
                }
            }
            if (!check) {
                return false;
            }
        } else {
            if (projectRoots.containsKey(DefaultCache.getManager().getString(includePath))){
                return true;
            }
        }
        while (true){
            int i = CharSequenceUtils.lastIndexOf(includePath, '\\');
            if (i <= 0) {
                i = CharSequenceUtils.lastIndexOf(includePath, '/');
            }
            if (i <= 0) {
                return false;
            }
            includePath = includePath.subSequence(0,i);
            Integer val = projectRoots.get(DefaultCache.getManager().getString(includePath));
            if (val != null) {
                if (isFixedRoots) {
                    if (val > Integer.MAX_VALUE/4) {
                        return true;
                    }
                } else {
                    if (val > 0) {
                        return true;
                    }
                }
            }
        }
    }
    
    public void fixFolder(CharSequence path){
        if (path != null) {
            projectRoots.put(FilePathCache.getManager().getString(path), Integer.MAX_VALUE / 2);
        }
    }
    
    public void addSources(Collection<NativeFileItem> items){
        for( NativeFileItem nativeFileItem : items ) {
            addFileItemBasedPath(nativeFileItem);
        }
    }
    
    private void addFileItemBasedPath(NativeFileItem nativeFileItem) {
        FileObject fo = nativeFileItem.getFileObject();
        FileObject parent = fo.getParent();
        String path = CndFileUtils.normalizePath(parent);
        addPath(path);
        String canonicalPath;
        try {
            canonicalPath = CndFileUtils.getCanonicalPath(parent);
            if (!path.equals(canonicalPath)) {
                addPath(canonicalPath);
            }
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
    }
    
    private CharSequence findParent(CharSequence path) {
        while (true){
            Integer val = projectRoots.get(DefaultCache.getManager().getString(path));
            if (val != null) {
                if (val > Integer.MAX_VALUE/4) {
                    return path;
                }
            }
            int i = CharSequenceUtils.lastIndexOf(path, '\\');
            if (i <= 0) {
                i = CharSequenceUtils.lastIndexOf(path, '/');
            }
            if (i <= 0) {
                return null;
            }
            path = path.subSequence(0,i);
        }
    }
    
    private void addPath(CharSequence path) {
        CharSequence parent = findParent(path);
        if (parent != null) {
            path = parent;
        }
        CharSequence added = FilePathCache.getManager().getString(path);
        Integer integer = projectRoots.get(added);
        if (integer == null) {
            projectRoots.put(added, 1);
        } else {
            projectRoots.put(added, integer + 1);
        }
    }
    
//    public void removeSources(List<NativeFileItem> items){
//        for( NativeFileItem nativeFileItem : items ) {
//            removeFile(nativeFileItem.getFile());
//        }
//    }
//
//    private void removeFile(File file){
//        String path = FileUtil.normalizeFile(file).getParent();
//        Integer integer = projectRoots.get(path);
//        if (integer != null) {
//            if (integer.intValue() > 1) {
//                projectRoots.put(path, integer - 1);
//            } else {
//                projectRoots.remove(path);
//            }
//        }
//    }

    void clear() {
        projectRoots.clear();
    }
}
