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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;

/**
 * Keeps information about all files that reside in the directory
 */
public class DirectoryStorage {

    public static final DirectoryStorage EMPTY = new DirectoryStorage(null, Collections.<DirEntry>emptyList()) {

        @Override
        public void store() throws IOException {
            RemoteLogger.assertTrueInConsole(false, "EMPTY.store() is called!"); //NOI18N
        }

        @Override
        public void touch() throws IOException {
            RemoteLogger.assertTrueInConsole(false, "EMPTY.touch() is called!"); //NOI18N
        }

        @Override
        public String toString() {
            return "EMPTY DirectoryStorage"; //NOI18N
        }        
    };
    
    private final Map<String, DirEntry> entries;
    private final File cacheFile;
    private static final int VERSION = 7;
    /* Incompatible version to discard */
    private static final int ODD_VERSION = 6;

    public DirectoryStorage(File file, Collection<DirEntry> newEntries) {
        this.cacheFile = file;
        this.entries = new HashMap<>();
        for (DirEntry entry : newEntries) {
            entries.put(entry.getName(), entry);
        }
    }
    
    static DirectoryStorage load(File storageFile, ExecutionEnvironment env) throws IOException, FormatException {
        BufferedReader br = null;
        try {
            br = Files.newBufferedReader(storageFile.toPath(), Charset.forName("UTF-8")); // NOI18N
            // check version
            String line = br.readLine();
            String prefix = "VERSION="; // NOI18N
            if (line == null || ! line.startsWith(prefix)) {
                throw new FormatException("Wrong file format " + storageFile.getAbsolutePath() + " line " + line, false); //NOI18N)
            }
            int version;
            try {
                version = Integer.parseInt(line.substring(prefix.length()));
            } catch (NumberFormatException nfe) {
                throw new FormatException("wrong version format " + storageFile.getAbsolutePath(), nfe); // NOI18N
            }
            if (version > VERSION) {
                throw new FormatException("directory cache file version " + version +  //NNOI18N
                        " not supported: " + storageFile.getAbsolutePath(), true); //NOI18N
            }
            if (version <= ODD_VERSION) {
                throw new FormatException("Discarding old directory cache file version " + version +  //NNOI18N
                        ' ' + storageFile.getAbsolutePath(), true); //NOI18N
            }
            line = br.readLine();
            prefix = "dummies="; // NOI18N
            if (line == null || ! line.startsWith(prefix)) {
                throw new FormatException("Wrong file format " + storageFile.getAbsolutePath() + " line " + line, false); //NOI18N)
            }
            int invalidsCount;
            try {
                invalidsCount = Integer.parseInt(line.substring(prefix.length()));
            } catch (NumberFormatException nfe) {
                throw new FormatException("wrong dummies count format " + storageFile.getAbsolutePath(), nfe); // NOI18N
            }
            Collection<DirEntry> loadedEntries = new ArrayList<>();

            for (int i = 0; i < invalidsCount; i++) {
                line = br.readLine();
                if (line == null) {
                    throw new FormatException("premature end of file " + storageFile.getAbsolutePath(), false); // NOI18N
                } else {
                    loadedEntries.add(new DirEntryInvalid(line));
                }
            }
            while ((line = br.readLine()) != null) {
                if (line.length() == 0) {
                    continue; // just in case, ignore empty lines
                }
                try {
                    DirEntry entry = DirEntryImpl.fromExternalForm(line);
                    loadedEntries.add(entry);
                } catch (FormatException fe) {
                    RemoteLogger.getInstance().log(Level.INFO, "Error loading cache file " + storageFile.getAbsolutePath(), fe);
                }
            }
            return new DirectoryStorage(storageFile, loadedEntries);
         } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public void touch() throws IOException {
        if (!cacheFile.exists()) {
            store(); //TODO: do we really need this?
            RemoteLogger.assertTrueInConsole(false, "Storage has been unexpectedly deleted: " + cacheFile.getAbsolutePath()); //NOI18N
        }
    }

    static void store(File cacheFile, Collection<? extends DirEntry> entries) throws IOException {
        BufferedWriter wr = null;
        try {
            wr = Files.newBufferedWriter(cacheFile.toPath(), Charset.forName("UTF-8")); //NOI18N 
            wr.write("VERSION=" + VERSION + "\n"); //NOI18N
            Collection<DirEntry> invalid = new ArrayList<>();
            Collection<DirEntry> valid = new ArrayList<>();
            for (DirEntry entry : entries) {
                if (entry.isValid()) {
                    valid.add(entry);
                } else {
                    invalid.add(entry);
                }
            }                
            wr.write("dummies=" + invalid.size() + '\n'); //NOI18N
            for (DirEntry entry: invalid) {
                wr.write(entry.toExternalForm());
                wr.write('\n');
            }
            for (DirEntry entry : valid) {
                wr.write(entry.toExternalForm());
                wr.write('\n');
            }
            wr.close();
            wr = null;
        } finally {
            if (wr != null) {
                wr.close();
            }
        }
    }
        
    public void store() throws IOException {
        synchronized (this) {
            store(cacheFile, entries.values());
        }
    }

    public DirEntry getValidEntry(String fileName) {
        synchronized (this) {
            DirEntry ret = entries.get(fileName);
            return (ret == null || !ret.isValid()) ? null : ret;
        }
    }
    
    public boolean isKnown(String fileName) {
        synchronized (this) {
            return entries.containsKey(fileName);
        }
    }

    public List<DirEntry> listAll() {
        synchronized (this) {
            return new ArrayList<>(entries.values());
        }
    }

    public List<DirEntry> listValid() {
        return listValid(null);
    }

    public List<DirEntry> listValid(String nameToSkip) {
        synchronized (this) {
            ArrayList<DirEntry> result = new ArrayList<>(entries.size());
            for (DirEntry entry : entries.values()) {
                if (entry.isValid()) {
                    if (nameToSkip == null || !nameToSkip.equals(entry.getName())) {
                        result.add(entry);
                    }
                }
            }
            return result;
        }
    }

    /*package*/ void testAddEntry(DirEntry entry) {
        synchronized (this) {
            entries.put(entry.getName(), entry);
        }
    }
    
    /*package*/ void testAddDummy(String dummy) {
        DirEntry entry = new DirEntryInvalid(dummy);
        testAddEntry(entry);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DirectoryStorage"); // NOI18N
        sb.append(" file=").append(cacheFile.getAbsolutePath()); // NOI18N
        sb.append(" entries.size()=").append(entries.size()).append("\n"); // NOI18N
        int cnt = 0;
        for (DirEntry entry : entries.values()) {
            if (cnt > 0) {
                sb.append('\n');
            }
            if (cnt++ <= 10) {
                sb.append(entry);
            } else {
                sb.append("..."); // NOI18N
                break;
            }
        }
        return sb.toString();
    }    
}
