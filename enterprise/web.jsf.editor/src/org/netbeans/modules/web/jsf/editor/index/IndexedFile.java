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

package org.netbeans.modules.web.jsf.editor.index;

import org.openide.filesystems.FileObject;

/**
 * The need for this class seems strange to me since I would expect such functionality
 * (providing timestamp for each index result) being part of the indexing support itself.
 *
 * Possibly remove if there's a standard way of doing this.
 *
 * @author marekfukala
 */
public class IndexedFile {

    private long timestamp;
    private String md5checksum;
    private FileObject file;

    public IndexedFile(long timestamp, String md5checksum, FileObject file) {
        this.timestamp = timestamp;
        this.md5checksum = md5checksum;
        this.file = file;
    }

    public FileObject getFile() {
        return file;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public String getMD5Checksum() {
        return md5checksum == null ? "n/a" : md5checksum; //NOI18N
    }

    
}
