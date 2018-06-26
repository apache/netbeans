/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
