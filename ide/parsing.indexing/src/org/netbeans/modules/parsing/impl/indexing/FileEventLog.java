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

package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater.Work;
import org.openide.filesystems.FileEvent;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
class FileEventLog implements Runnable {

    private static final Logger LOG = Logger.getLogger(FileEventLog.class.getName());

    public static enum FileOp {
       DELETE,
       CREATE
    };

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private final ThreadLocal<Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>>> changes;

    public FileEventLog() {
        this.changes = new ThreadLocal<Map<URL, Map<String, Pair<FileOp, Work>>>>();
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    public void record (final FileOp operation, final URL root, String relativePath, FileEvent event, final Work work) {
        assert operation != null;
        assert root != null;
        assert PathRegistry.noHostPart(root) : root;
        if (relativePath == null) {
            relativePath = "";  //NOI18N
        }
        final Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> myChanges = getChanges(true);
        Map<String,Pair<FileOp,Work>> rootSlot = myChanges.get(root);
        if (rootSlot == null) {
            rootSlot = new HashMap<String,Pair<FileOp,Work>>();
            myChanges.put(root, rootSlot);
        }
        rootSlot.put(relativePath, Pair.<FileOp,Work>of(operation,work));
        event.runWhenDeliveryOver(this);
    }

    public void run () {
        try {
            commit();
        } finally {
            cleanUp();
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> getChanges(final boolean create) {
        Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> res = changes.get();
        if (res == null && create) {
            res = new HashMap<URL,Map<String,Pair<FileEventLog.FileOp,Work>>>();
            changes.set(res);
        }
        return res;
    }

    private void commit () {
        final List<Work> first = new LinkedList<Work>();
        final List<Work> rest = new LinkedList<Work>();
        final IdentityHashMap<Work,Work> seenDelete = new IdentityHashMap<Work, Work>();
        final Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> myChanges = getChanges(false);
        if (myChanges != null) {
            for (Map<String,Pair<FileOp,Work>> changesInRoot : myChanges.values()) {
                for (Pair<FileOp,Work> desc : changesInRoot.values()) {
                    if (desc.first() == FileOp.DELETE) {
                        if (!seenDelete.containsKey(desc.second())) {
                            first.add(desc.second());
                            seenDelete.put(desc.second(), desc.second());
                        }
                    }
                    else {
                        rest.add(desc.second());
                    }
                }
            }
        }
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("SCHEDULING: " + first); //NOI18N
        }
        ru.scheduleWork(first);
        
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("SCHEDULING: " + rest); //NOI18N
        }
        ru.scheduleWork(rest);
    }

    private void cleanUp() {
        this.changes.remove();
    }


}
