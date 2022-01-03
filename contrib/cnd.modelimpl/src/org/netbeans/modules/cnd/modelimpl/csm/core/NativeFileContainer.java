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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;

/**
 *
 */
public class NativeFileContainer {
    private final Map<CsmUID<CsmFile>, NativeFileItem> myFiles = new ConcurrentHashMap<>();

    /*package-local*/ NativeFileContainer(){
    }

    public final NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
	return myFiles.get(file);
    }
    
    /*package-local*/ final void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
        assert nativeFileItem != null;
	myFiles.put(file, nativeFileItem);
    }
    
    /*package-local*/ final NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file) {
	return myFiles.remove(file);
    }

    /*package-local*/ final void clear() {
	myFiles.clear();
    }
}
