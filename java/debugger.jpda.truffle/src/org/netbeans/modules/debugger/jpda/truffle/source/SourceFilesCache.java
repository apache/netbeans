/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.truffle.source;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.openide.filesystems.FileObject;

final class SourceFilesCache {
    
    private static final Map<JPDADebugger, SourceFilesCache> MAP = new WeakHashMap<>();
    
    private final SourceFS fs;
    
    private SourceFilesCache() {
        fs = new SourceFS();
    }
    
    public static synchronized SourceFilesCache get(JPDADebugger jpda) {
        SourceFilesCache sfc = MAP.get(jpda);
        if (sfc == null) {
            sfc = new SourceFilesCache();
            MAP.put(jpda, sfc);
        }
        return sfc;
    }
    
    public URL getSourceFile(String name, long hash, URI uri, String content) throws IOException {
        String justName = name;
        int i = justName.lastIndexOf(File.separatorChar);
        if (i >= 0) {
            justName = justName.substring(i + 1);
            if (justName.isEmpty()) {
                justName = name.replace(File.separatorChar, '_');
            }
        }
        String path = Long.toHexString(hash) + '/' + justName;
        FileObject fo = fs.findResource(path);
        if (fo == null) {
            fo = fs.createFile(path, content);
            if (fo == null) {
                throw new IllegalArgumentException("Not able to create file with name '"+justName+"'. Path = "+path);
            }
            fo.setAttribute(Source.ATTR_URI, uri);
        }
        return fo.toURL();
    }

}
