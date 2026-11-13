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
package org.netbeans.modules.php.project.connections;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;

/**
 * Remote client implementation, exists only for unit tests.
 */
public abstract class RemoteClientImplementation {

    private static final Map<String, NavigableMap<Path, List<Path>>> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(){
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, NavigableMap<Path, List<Path>>> eldest) {
            return size() > 10;
        }
    });

    abstract public String getBaseLocalDirectory();
    abstract public String getBaseRemoteDirectory();
    abstract public List<TransferFile> listLocalFiles(TransferFile file);
    abstract public List<TransferFile> listRemoteFiles(TransferFile file) throws RemoteException;

    /**
     * Check if the supplied file is place "below" the local directory.
     *
     * @param file
     * @return true if file is found below the base local directory
     */
    public boolean isAncestorOfLocal(File file) {
        String localBaseLocalDirectory = getBaseLocalDirectory();
        if (file.getAbsolutePath().startsWith(localBaseLocalDirectory)) {
            return true;
        } else {
            // Check secondary roots formed by symlinks from the project directory, this s
            // relevant for PHP projects using Composer with local dependencies. These
            // are implemented as symlinks on linux and junctions on windows. On linux
            // file normalization yields absolute paths, which "lie" below the directory
            // on windows canonical path is used, which might yield paths outside the
            // project directory
            Path filePath = file.toPath();
            NavigableMap<Path, List<Path>> cachedAliases = CACHE.get(getBaseLocalDirectory());
            if (cachedAliases != null && getRelativePathInProject(cachedAliases, localBaseLocalDirectory, filePath) != null) {
                return true;
            }
            cachedAliases = CanonicalPathWalker.scan(Path.of(localBaseLocalDirectory));
            CACHE.put(getBaseLocalDirectory(), cachedAliases);
            if (cachedAliases != null && getRelativePathInProject(cachedAliases, localBaseLocalDirectory, filePath) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the relative path for the given absolutePath with respect to the
     * project directory.
     *
     * @param absolutePath to scan
     * @return relative path below the project directory
     */
    public String getLocalRelativePath(String absolutePath) {
        String localBaseLocalDirectory = getBaseLocalDirectory();
        if (absolutePath.startsWith(localBaseLocalDirectory)) {
            return absolutePath.substring(getBaseLocalDirectory().length() + File.separator.length());
        } else {
            // See #isAncestorOfLocal for explanation - these calls need to be symmetric
            Path filePath = Path.of(absolutePath);
            NavigableMap<Path, List<Path>> cachedAliases = CACHE.get(getBaseLocalDirectory());
            if (getRelativePathInProject(cachedAliases, localBaseLocalDirectory, filePath) instanceof Path prefix) {
                return File.separator + prefix.toString();
            }
            cachedAliases = CanonicalPathWalker.scan(Path.of(localBaseLocalDirectory));
            CACHE.put(getBaseLocalDirectory(), cachedAliases);
            if (getRelativePathInProject(cachedAliases, localBaseLocalDirectory, filePath) instanceof Path prefix) {
                return File.separator + prefix.toString();
            }
        }
        throw new RuntimeException("Failed to find prefix for " + absolutePath);
    }

    private Path getRelativePathInProject(NavigableMap<Path, List<Path>> cachedAliases, String localBaseLocalDirectory, Path filePath) {
        // The idea here: The keys of cachedAliases are normalized paths and the
        // values represent non normalized paths that point to them. Resolving
        // is now a two step process:
        // 1. Is there a key (normalized path) that is a prefix of the target path
        // 2. If so, check the list of potential non-normalized paths and find the
        //    first non-normalized path that is a prefix of the projects directory
        // 3. Now two informations are present:
        //    - the normalized prefix
        //    - the non-normalized prefix
        //    We need the relative path to the file in the non-normalized prefix.
        //    (1) Determine the relative path from the found normalized prefix
        //        to the path
        //    (2) Determine the relative path of the non-normalized prefix we
        //        found in reference to the localBaseLocalDirectory
        //    (3) resolve the (1) against (2) to determine the non-normalized
        //        relative path from the localBaseLocalDirectory to the file
        if (cachedAliases != null) {
            Path firstKeyToCheck = nvl(cachedAliases.floorKey(filePath), filePath);
            Path lastKeyToCheck = nvl(cachedAliases.ceilingKey(filePath), filePath);
            SortedSet<Path> pathsToCheck = cachedAliases
                    .navigableKeySet()
                    .subSet(firstKeyToCheck, true, lastKeyToCheck, true);
            for (Path pathToCheck : pathsToCheck) {
                if (filePath.startsWith(pathToCheck)) {
                    List<Path> nonNormalizedCandidates = cachedAliases.get(pathToCheck);
                    for (Path nonNormalizedCandiate : nonNormalizedCandidates) {
                        if (nonNormalizedCandiate.toString().startsWith(localBaseLocalDirectory)) {
                            Path relativePathToNormalized = pathToCheck.relativize(filePath);
                            Path relativePathInBaseLocal = Path.of(localBaseLocalDirectory)
                                    .relativize(nonNormalizedCandiate);
                            return relativePathInBaseLocal.resolve(relativePathToNormalized);
                        }
                    }
                }
            }
        }
        return null;
    }

    private <T> T nvl(T nullable, T nonNull) {
        if (nullable == null) {
            return nonNull;
        } else {
            return nullable;
        }
    }
}
