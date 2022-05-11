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
package org.netbeans.modules.cpplite.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 *
 * @author lahvac
 */
public class Utils {
    public static final String KEY_CCLS_PATH = "ccls";
    public static final String KEY_CLANGD_PATH = "clangd";

    private static final String[] CCLS_NAMES = new String[] {"ccls"};
    private static final String[] CLANGD_NAMES = new String[] {"clangd-10", "clangd", "clangd-9"};

    public static Preferences settings() {
        return NbPreferences.forModule(Utils.class);
    }

    private static List<String> cclsAutodetectedPaths;

    public static synchronized String getCCLSPath() {
        String path = settings().get(KEY_CCLS_PATH, null);
        if (path == null || path.isEmpty()) {
            if (cclsAutodetectedPaths == null) {
                cclsAutodetectedPaths = findFileOnUsersPath(CCLS_NAMES);
            }
            if (!cclsAutodetectedPaths.isEmpty()) {
                path = cclsAutodetectedPaths.get(0);
            }
        }
        if (path == null || path.isEmpty()) {
            return null;
        }
        return path;
    }

    private static List<String> clangdAutodetectedPaths;

    public static synchronized String getCLANGDPath() {
        String path = settings().get(KEY_CLANGD_PATH, null);
        if (path == null || path.isEmpty()) {
            if (clangdAutodetectedPaths == null) {
                clangdAutodetectedPaths = findFileOnUsersPath(CLANGD_NAMES);
            }
            if (!clangdAutodetectedPaths.isEmpty()) {
                path = clangdAutodetectedPaths.get(0);
            }
        }
        if (path == null || path.isEmpty()) {
            return null;
        }
        return path;
    }

    //TODO: copied from webcommon/javascript.nodejs/src/org/netbeans/modules/javascript/nodejs/util/FileUtils.java:
    /**
     * Find all the files (absolute path) with the given "filename" on user's PATH.
     * <p>
     * This method is suitable for *nix as well as windows.
     * @param filenames the name of a file to find, more names can be provided.
     * @return list of absolute paths of found files (order preserved according to input names).
     * @see #findFileOnUsersPath(String)
     */
    public static List<String> findFileOnUsersPath(String... filenames) {
        Parameters.notNull("filenames", filenames); // NOI18N

        String path = System.getenv("PATH"); // NOI18N
        LOGGER.log(Level.FINE, "PATH: [{0}]", path);
        if (path == null) {
            return Collections.<String>emptyList();
        }
        // on linux there are usually duplicities in PATH
        Set<String> dirs = new LinkedHashSet<>(Arrays.asList(path.split(File.pathSeparator)));
        LOGGER.log(Level.FINE, "PATH dirs: {0}", dirs);
        List<String> found = new ArrayList<>(dirs.size() * filenames.length);
        for (String filename : filenames) {
            Parameters.notNull("filename", filename); // NOI18N
            for (String dir : dirs) {
                File file = new File(dir, filename);
                if (file.isFile()) {
                    String absolutePath = FileUtil.normalizeFile(file).getAbsolutePath();
                    LOGGER.log(Level.FINE, "File ''{0}'' found", absolutePath);
                    // not optimal but should be ok
                    if (!found.contains(absolutePath)) {
                        LOGGER.log(Level.FINE, "File ''{0}'' added to found files", absolutePath);
                        found.add(absolutePath);
                    }
                }
            }
        }
        LOGGER.log(Level.FINE, "Found files: {0}", found);
        return found;
    }

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

}
