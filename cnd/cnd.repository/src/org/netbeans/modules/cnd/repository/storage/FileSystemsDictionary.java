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
package org.netbeans.modules.cnd.repository.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.openide.filesystems.FileSystem;

/**
 * A list of all client FileSystems per Storage.
 *
 * clientFileSystem --- clientFileSystemID
 *
 */
/* package */ final class FileSystemsDictionary {

    private final Map<FileSystem, Integer> map = new HashMap<FileSystem, Integer>();
    private final AtomicInteger counter = new AtomicInteger(12);

    FileSystem getFileSystem(int fileSystemIdx) {
        for (Map.Entry<FileSystem, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(fileSystemIdx)) {
                return entry.getKey();
            }
        }
        return null;
    }

    int getFileSystemID(FileSystem clientFileSystem) {
        Integer result = map.get(clientFileSystem);
        if (result == null) {
            result = counter.getAndIncrement();
            map.put(clientFileSystem, result);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n[clientFileSystem <-> clientFileSystemID]\n"); // NOI18N
        for (Map.Entry<FileSystem, Integer> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n"); // NOI18N
        }
        return sb.toString();
    }
}
