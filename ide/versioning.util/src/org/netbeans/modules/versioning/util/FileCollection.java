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

import org.netbeans.modules.versioning.spi.VersioningSupport;

import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Collection of Files that has special contracts for add, remove and contains methods, see below.
 * 
 * @author Maros Sandor
 */
public class FileCollection {
    
    private static final char FLAT_FOLDER_MARKER = '*';
    
    private final Set<File> storage = new HashSet<File>(1);
    
    public synchronized void load(Preferences prefs, String key) {
        List<String> paths = Utils.getStringList(prefs, key);
        storage.clear();        
        for (String path : paths) {
            if (path.charAt(0) == FLAT_FOLDER_MARKER) {
                storage.add(VersioningSupport.getFlat(path.substring(1)));        
            } else {
                storage.add(new File(path));        
            }
        }
    }

    public synchronized void save(Preferences prefs, String key) {
        List<String> paths = new ArrayList<String>(storage.size());
        for (File file : storage) {
            if (VersioningSupport.isFlat(file)) {
                paths.add(FLAT_FOLDER_MARKER + file.getAbsolutePath());        
            } else {
                paths.add(file.getAbsolutePath());        
            }
        }
        Utils.put(prefs, key, paths);
    }

    /**
     * A file is contained in the collection either if it is in the colelction itself or there is any of its parents. 
     * 
     * @param file a file to query
     * @return true if the file is contained in the collection, false otherwise
     */
    public synchronized boolean contains(File file) {
        for (File element : storage) {
            if (Utils.isAncestorOrEqual(element, file)) return true;
        }
        return false;
    }

    /**
     * Adds a file to the collection. If any of its parent files is already in the collection, the file is NOT added.
     * All children of the supplied file are removed from the collection.
     * 
     * @param file a file to add
     */
    public synchronized void add(File file) {
        for (Iterator<File> i = storage.iterator(); i.hasNext(); ) {
            File element = i.next();
            if (Utils.isAncestorOrEqual(element, file)) return;
            if (Utils.isAncestorOrEqual(file, element)) {
                i.remove();
            }
        }
        storage.add(file);
    }

    /**
     * Removes a file from the collection. This method also removes all its parents and also all its children.
     * 
     * @param file a file to remove
     */
    public synchronized void remove(File file) {
        for (Iterator<File> i = storage.iterator(); i.hasNext(); ) {
            File element = i.next();
            if (Utils.isAncestorOrEqual(element, file) || Utils.isAncestorOrEqual(file, element)) {
                i.remove();
            }
        }
    }
}
