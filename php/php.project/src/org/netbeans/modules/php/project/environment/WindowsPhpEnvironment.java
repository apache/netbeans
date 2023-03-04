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

package org.netbeans.modules.php.project.environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
final class WindowsPhpEnvironment extends PhpEnvironment {

    private static final String PHP = "php.exe"; // NOI18N
    private static final PhpEnvironment XAMPP = new XamppPhpEnvironment();


    WindowsPhpEnvironment() {
    }

    @Override
    protected List<DocumentRoot> getDocumentRoots(String projectName) {
        File htDocs = null;
        for (File root : getFsRoots()) {
            // standard apache installation
            File programFiles = new File(root, "Program Files"); // NOI18N
            htDocs = findHtDocsDirectory(programFiles, APACHE_FILENAME_FILTER);
            if (htDocs != null) {
                // one htdocs is enough
                break;
            }
        }
        if (htDocs != null) {
            String documentRoot = getFolderName(htDocs, projectName);
            String url = getDefaultUrl(projectName);
            String hint = NbBundle.getMessage(WindowsPhpEnvironment.class, "TXT_HtDocs");
            return Collections.singletonList(new DocumentRoot(documentRoot, url, hint, FileUtils.isDirectoryWritable(htDocs)));
        }
        // xampp
        return XAMPP.getDocumentRoots(projectName);
    }

    @Override
    public List<String> getAllPhpInterpreters() {
        return getAllPhpInterpreters(PHP);
    }

    /**
     * Get FS roots without {@link #isFloppy(File) floppy drives}.
     * @return list of FS roots, never {@code null}.
     */
    private static List<File> getFsRoots() {
        File[] fsRoots = File.listRoots();
        if (fsRoots == null) {
            // should not happen
            return Collections.emptyList();
        }
        List<File> result = new ArrayList<>(fsRoots.length);
        for (File root : fsRoots) {
            LOGGER.log(Level.FINE, "FS root: {0}", root);
            if (isFloppy(root)) {
                LOGGER.log(Level.FINE, "Skipping floppy: {0}", root);
                continue;
            }
            result.add(root);
        }
        return result;
    }

    private static boolean isFloppy(File root) {
        String absolutePath = root.getAbsolutePath();
        LOGGER.log(Level.FINE, "Testing floppy on {0}", absolutePath);
        return absolutePath.toLowerCase().startsWith("a:") // NOI18N
                || absolutePath.toLowerCase().startsWith("b:"); // NOI18N
    }

    //~ Inner classes

    /**
     * {@link PhpEnvironment} implementation for XAMPP.
     */
    private static final class XamppPhpEnvironment extends PhpEnvironment {

        private static final String XAMPP = "xampp"; // NOI18N
        private static final FilenameFilter XAMPP_FILENAME_FILTER = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith(XAMPP);
            }
        };


        @Override
        protected List<DocumentRoot> getDocumentRoots(String projectName) {
            File htDocs = null;
            for (File root : getFsRoots()) {
                htDocs = new File(new File(root, XAMPP), HTDOCS);
                if (htDocs.isDirectory()) {
                    // one htdocs is enough
                    break;
                }
                // standard apache installation
                File programFiles = new File(root, "Program Files"); // NOI18N
                htDocs = findHtDocsDirectory(programFiles, XAMPP_FILENAME_FILTER);
                if (htDocs != null) {
                    // one htdocs is enough
                    break;
                }
            }
            if (htDocs != null) {
                String documentRoot = getFolderName(htDocs, projectName);
                String url = getDefaultUrl(projectName);
                String hint = NbBundle.getMessage(WindowsPhpEnvironment.class, "TXT_XamppHtDocs");
                return Collections.singletonList(new DocumentRoot(documentRoot, url, hint, FileUtils.isDirectoryWritable(htDocs)));
            }
            return Collections.<DocumentRoot>emptyList();
        }

        @Override
        public List<String> getAllPhpInterpreters() {
            return Collections.emptyList();
        }

    }

}
