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

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import java.util.TreeMap;

/**
 * Helper class to scan a directory tree for symlinks. This class is a helper
 * for {@link RemoteClientImplementation}. The FTP/SFTP upload checks whether
 * files lie "beneath" the project root folder. On windows it was observed that
 * this fails for composer, which symlinks dependencies for local projects. On
 * Linux this works because File normalization on linux uses the absolute path
 * as basis, while Windows uses the canoncial path. The latter means, that
 * symlinked files have a normalized path outside the projects root.
 *
 * <p>This class is not thread safe. Calls to {@link #scan} must not overlap.</p>
 */
public class CanonicalPathWalker {

    private static final System.Logger LOG = System.getLogger(CanonicalPathWalker.class.getName());

    // The deepest observed directory structure found in the authors src folder
    // was 30 layers deep. While that is true, it is assumed, that the common
    // case for PHP/Web Projects are directory symlinks at the higher levels
    // of a project.
    private static final int MAX_DEPTH = 20;

    private final Set<Path> alreadyScanned = new HashSet<>();
    private final Map<Path, List<Path>> normalizedMap = new HashMap<>();
    private int depth = 0;

    private CanonicalPathWalker() {
    }

    /**
     * Scans the directory rooted in {@code startPath} for symlinks/junctions
     * (windows directory links) and maps their "real path" to the path from the
     * perspective of the startPath.
     * <p>
     * The resulting map uses normalized paths as the keys which point to a
     * list of noon-normalized paths that were found while descending through
     * the directory rooted at the start path.</p>
     *
     * @param startPath Path to scan
     * @return
     */
    public static NavigableMap<Path, List<Path>> scan(Path startPath)  {
        return new CanonicalPathWalker().scan0(startPath);
    }

    private NavigableMap<Path, List<Path>> scan0(Path startPath)  {
        this.depth = 0;
        this.normalizedMap.clear();
        try {
            Path startPathReal = startPath.toRealPath();
            if (!startPath.equals(startPathReal)) {
                normalizedMap.computeIfAbsent(startPathReal, p -> new ArrayList<>()).add(startPath);
            }
            scan0(startPath, startPath.toRealPath());
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Failed to resolve path " + startPath, ex);
        }
        TreeMap<Path, List<Path>> result = new TreeMap<>();
        normalizedMap
                .entrySet()
                .stream()
                .forEach(e -> {
                    result.put(e.getKey(), List.copyOf(e.getValue()));
                });
        return result;
    }

    private void scan0(Path currentPath, Path currentPathNormalized) throws IOException {
        // Limit recursion depth - see comment on constant
        if (depth >= MAX_DEPTH) {
            return;
        }
        // Prevent loops and multiple scans
        if (!alreadyScanned.add(currentPathNormalized)) {
            return;
        }
        depth++;
        LOG.log(Level.DEBUG, () -> "Scanning: " + currentPath + " // " + currentPathNormalized + " (depth: " + depth + ")");
        List<Path> children = Files.list(currentPathNormalized).toList();
        for (Path child : children) {
            try {
                // Potential optimization that in tests reduced scan time by 33%
                // but limits support to directories
                // if (!(Files.isDirectory(child) || Files.isSymbolicLink(child)))   {
                //    continue;
                // }
                Path plainChild = currentPath.resolve(child.getFileName());
                Path onlyChildNotResolved = currentPathNormalized.resolve(child.getFileName());
                Path onlyChildResolved = onlyChildNotResolved.toRealPath();
                if (!onlyChildNotResolved.equals(onlyChildResolved)) {
                    normalizedMap.computeIfAbsent(onlyChildResolved, p -> new ArrayList<>()).add(plainChild);
                }
                boolean targetsDirectory = Files.isDirectory(onlyChildResolved);
                if (targetsDirectory) {
                    scan0(plainChild, onlyChildResolved);
                }
            } catch (FileSystemException ex) {
                LOG.log(Level.INFO, "Failed to resolve path " + child, ex);
            }
        }
        depth--;
    }

}
