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

package org.netbeans.modules.javascript2.debug.sources;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin
 */
public final class SourceFilesCache {
    
    public static final String URL_PROTOCOL = "js-scripts"; // NOI18N
    
    private static final SourceFilesCache DEFAULT = new SourceFilesCache();
    
    private final SourceFS fs;
    
    private SourceFilesCache() {
        fs = new SourceFS();
    }
    
    public static SourceFilesCache getDefault() {
        return DEFAULT;
    }
    
    public URL getSourceFile(String name, int hash, String content) {
        return getSourceFile(name, hash, new StringContent(content));
    }
    
    public URL getSourceFile(String name, int hash, SourceContent content) {
        return getSourceFile(name, Integer.toHexString(hash), content);
    }

    public URL getSourceFile(String name, String hash, SourceContent content) {
        Objects.requireNonNull(hash, "hash was null");
        String path = hash + '/' + name;
        FileObject fo = fs.findResource(path);
        if (fo == null) {
            fo = fs.createFile(path, content);
        }
        return fo.toURL();
    }
    
    static final class StringContent implements SourceContent {
        
        private final String content;
        
        public StringContent(String content) {
            this.content = content;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public long getLength() {
            return content.length();
        }
    }

}
