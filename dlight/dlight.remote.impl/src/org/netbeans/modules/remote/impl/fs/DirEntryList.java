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

package org.netbeans.modules.remote.impl.fs;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class DirEntryList {
    private final List<DirEntry> entries;
    private final long timestamp;

    public DirEntryList(List<DirEntry> entries, long timestamp) {
        this.entries = Collections.unmodifiableList(entries);
        this.timestamp = timestamp;
    }

    public List<DirEntry> getEntries() {
        return entries;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(' ').append(entries.size()).append(" entries"); //NOI18N
        for (DirEntry e : entries) {
            sb.append('\n').append(e.toString()); //NOI18N
        }
        return sb.toString();
    }
}
