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

package org.netbeans.modules.versioning.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tomas
 */
public class RootsToFile {
    private final Map<File, File> files = new LinkedHashMap<File, File>() {
        @Override
        protected boolean removeEldestEntry (Map.Entry<File, File> eldest) {
            return size() > 1500;
        }
    };
    private long cachedAccesCount = 0;
    private long accesCount = 0;
    private final int statisticsFrequency;
    private final Callback callback;
    private final Logger log;

    public RootsToFile (Callback callback, Logger log, int statisticsFrequency) {
        this.statisticsFrequency = statisticsFrequency;
        this.callback = callback;
        this.log = log;
    }
    synchronized void put (Collection<File> files, File root) {
        for (File file : files) {
            put(file, root);
        }
    }
    synchronized void put (File file, File root) {
        files.put(file, root);
    }
    synchronized File get (File file) {
        return get(file, false);
    }
    synchronized File get (File file, boolean statistics) {
        File root = files.get(file);
        if(statistics && log.isLoggable(Level.FINEST)) {
           cachedAccesCount += root != null ? 1 : 0;
           accesCount++;
        }
        return root;
    }
    synchronized int size () {
        return files.size();
    }
    synchronized void logStatistics () {
        if(!log.isLoggable(Level.FINEST) ||
           (statisticsFrequency > 0 && (accesCount % statisticsFrequency != 0)))
        {
            return;
        }

        log.finest("Repository roots cache statistics:\n" +                                 // NOI18N
                 "  cached roots size       = " + files.size() + "\n" +                         // NOI18N
                 "  access count            = " + accesCount + "\n" +                           // NOI18N
                 "  cached access count     = " + cachedAccesCount + "\n" +                     // NOI18N
                 "  not cached access count = " + (accesCount - cachedAccesCount) + "\n");      // NOI18N
    }

    public synchronized void clear () {
        files.clear();
        cachedAccesCount = 0;
        accesCount = 0;
    }

    public File getRepositoryRoot (File file) {
        File oFile = file;

        logStatistics();
        File root = get(file, true);
        if(root != null) {
            return root;
        }

        root = callback.getTopmostManagedAncestor(file);
        if(root != null) {
            if(file.isFile()) file = file.getParentFile();
            List<File> folders = new ArrayList<File>();
            for (; file != null && !file.getAbsolutePath().equals(root.getAbsolutePath()) ; file = file.getParentFile()) {
                File knownRoot = get(file);
                if(knownRoot != null) {
                    put(folders, knownRoot);
                    put(oFile, knownRoot);
                    return knownRoot;
                }
                folders.add(file);
                if(callback.repositoryExistsFor(file)) {
                    put(folders, file);
                    put(oFile, file);
                    return file;
                }
            }
            folders.add(root);
            put(folders, root);
            put(oFile, root);
            return root;
        }
        return null;
    }

    public static interface Callback {
        public boolean repositoryExistsFor (File file);
        public File getTopmostManagedAncestor(File file);
    }
}
