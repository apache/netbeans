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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.repository.util.IntToValueList;

/**
 * A list of all client FilePaths per Unit.
 *
 * filePathID (idx of the array) -- clientFilePaths
 *
 */
/* package */ final class FilePathsDictionary implements Persistent, SelfPersistent{
    private static final Logger LOG = Logger.getLogger("repository.support.filecreate.logger"); //NOI18N

    static final String WRONG_PATH = "<WRONG FILE>"; // NOI18N
    private final List<CharSequence> paths;
    private final Map<CharSequence, Integer> map = new HashMap<CharSequence, Integer>();
    private final Object lock = new Object();

    FilePathsDictionary(final List<CharSequence> initData) {
        if (initData != null) {
            paths = new ArrayList<CharSequence>(initData);
            int idx = 0;
            for (CharSequence path : initData) {
                map.put(path, idx++);
            }
        } else {
            paths = new ArrayList<CharSequence>();
        }
    }

    CharSequence getFilePath(final int fileIdx) {
        synchronized (lock) {
            if (fileIdx >= paths.size()) {
                return WRONG_PATH;
            } else {
                return paths.get(fileIdx);
            }
        }
    }    
    
    int size() {
        synchronized (lock) {
            return paths.size();
        }
    }

    int getFileID(final CharSequence filePath, int clientShortUnitID) {
        synchronized (lock) {
            Integer idx = map.get(filePath);
            if (idx == null) {
                int new_idx = paths.size();
                paths.add(filePath);
                map.put(filePath, new_idx);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Create index {0}/{1}={2}", new Object[]{new_idx, clientShortUnitID, filePath}); //NOI18N
                }
                return new_idx;
            } else {
                return idx.intValue();
            }
        }
    }
        

    List<CharSequence> toList() {
        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<CharSequence>(paths));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n[clientFilePathID <-> clientFilePaths]\n"); // NOI18N
        int idx = 0;
        for (CharSequence path : toList()) {
            sb.append(idx++).append(" => ").append(path.toString()).append("\n"); // NOI18N
        }
        return sb.toString();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        IntToValueList<CharSequence> list = IntToValueList.<CharSequence>createEmpty("traceName");//NOI18N
        int i = 0;
        final List<CharSequence> toList = toList();
        for (CharSequence file : toList) {
            list.set(i++, file);
        }
        list.write(output);
    }
}
