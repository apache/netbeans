/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.sites;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.NbBundle;

public final class SiteHelper {

    private static final Logger LOGGER = Logger.getLogger(SiteHelper.class.getName());
    private static final String JS_LIBS_DIR = "jslibs"; // NOI18N


    private SiteHelper() {
    }

    /**
     * Return <i>&lt;var/cache>/jslibs</i> directory.
     * @return <i>&lt;var/cache>/jslibs</i> directory
     */
    public static File getJsLibsDirectory() {
        return Places.getCacheSubdirectory(JS_LIBS_DIR);
    }

    /**
     * Download the given URL to the target file.
     * @param url URL to be downloaded
     * @param target target file
     * @param progressHandle progress handle, can be {@code null}
     * @throws NetworkException if any network error occurs
     * @throws IOException if any error occurs
     * @deprecated Use any download method from {@link NetworkSupport}.
     */
    @Deprecated
    public static void download(String url, File target, @NullAllowed ProgressHandle progressHandle) throws NetworkException, IOException {
        assert !EventQueue.isDispatchThread();
        try {
            if (progressHandle != null) {
                NetworkSupport.downloadWithProgress(url, target, progressHandle);
            } else {
                NetworkSupport.download(url, target);
            }
        } catch (InterruptedException ex) {
            // cancelled - what to do?
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     * Unzip the given ZIP file to the given target directory. The target directory must exist.
     * @param zipFile ZIP file to be extracted
     * @param targetDirectory existing target directory
     * @param progressHandle progress handle, can be {@code null}
     * @throws IOException if any error occurs
     */
    @NbBundle.Messages({
        "# {0} - file name",
        "SiteHelper.progress.unzip=Unziping file {0}"
    })
    public static void unzipProjectTemplate(@NonNull FileObject targetDir, @NonNull File zipFile, @NullAllowed ProgressHandle progressHandle, String... ignoredFiles) throws IOException {
        assert targetDir != null;
        if (progressHandle != null) {
            progressHandle.progress(Bundle.SiteHelper_progress_unzip(zipFile.getName()));
        }
        String rootFolder = getZipRootFolder(new FileInputStream(zipFile));
        unzipProjectTemplateFile(targetDir, new FileInputStream(zipFile), rootFolder, ignoredFiles);
    }

    /**
     * Strip possible root folder of the given paths.
     * <p>
     * <b>Warning:</b> only "/" as path separator expected.
     * <p>
     * The typical usage is for file paths from a ZIP file.
     * @param paths relative paths (with "/" as path separator) to be processed, never empty paths or {@code null}
     * @return list of paths without possible root folder
     */
    public static List<String> stripRootFolder(List<String> paths) {
        List<String> stripped = new ArrayList<String>(paths.size());
        String rootFolder = null;
        for (String path : paths) {
            assert StringUtilities.hasText(path) : "Empty path not allowed";
            String top;
            int slashIndex = path.indexOf('/'); // NOI18N
            if (slashIndex == -1) {
                top = path;
            } else {
                top = path.substring(0, slashIndex);
            }
            if (rootFolder == null) {
                rootFolder = top;
            }
            if (!rootFolder.equals(top)) {
                return paths;
            }
            if (slashIndex != -1 && path.length() > slashIndex) {
                stripped.add(path.substring(slashIndex + 1));
            }
        }
        return stripped;
    }

    @NbBundle.Messages("SiteHelper.error.emptyZip=ZIP file with site template is either empty or its download failed.")
    private static void unzipProjectTemplateFile(FileObject targetDir, InputStream source, String rootFolder, String... ignoredFiles) throws IOException {
        boolean firstItem = true;
        try {
            int stripLen = rootFolder != null ? rootFolder.length() : 0;
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            Set<String> ignored = Collections.emptySet();
            if (ignoredFiles != null && ignoredFiles.length > 0) {
                ignored = new HashSet<String>(Arrays.asList(ignoredFiles));
            }
            while ((entry = str.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (stripLen > 0) {
                    entryName = entryName.substring(stripLen);
                }
                if (entryName.length() == 0) {
                    continue;
                }
                if (ignored.contains(entryName)) {
                    continue;
                }
                firstItem = false;
                if (entry.isDirectory()) {
                    // ignore build folder from mobile boilerplate; unrelated junk IMO.
                    if (entryName.startsWith("build") || entryName.startsWith("nbproject")) { //NOI18N
                        continue;
                    }
                    if (targetDir.getFileObject(entryName) == null) {
                        FileUtil.createFolder(targetDir, entryName);
                    }
                } else {
                    // ignore internal GIT files:
                    if (entryName.startsWith(".git") || entryName.contains("/.git")) { //NOI18N
                        continue;
                    }
                    // ignore build folder from mobile boilerplate; unrelated junk IMO.
                    if (entryName.startsWith("build/") || entryName.startsWith("nbproject/")) { //NOI18N
                        continue;
                    }
                    // NetBeans LOCK files
                    if (entryName.contains("/.LCK") && entryName.endsWith("~")) { //NOI18N
                        continue;
                    }
                    FileObject fo = FileUtil.createData(targetDir, entryName);
                    writeFile(str, fo);
                }
            }
        } finally {
            source.close();
            if (firstItem) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.SiteHelper_error_emptyZip()));
            }
        }
    }

    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(str, out);
        } finally {
            out.close();
        }
    }

    private static String getZipRootFolder(InputStream source) throws IOException {
        String folder = null;
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            boolean first = true;
            while ((entry = str.getNextEntry()) != null) {
                if (first) {
                    first = false;
                    if (entry.isDirectory()) {
                        folder = entry.getName();
                    } else {
                        String fileName = entry.getName();
                        int slashIndex = fileName.indexOf('/');
                        if (slashIndex != -1) {
                            String name = fileName.substring(slashIndex+1);
                            folder = fileName.substring(0, slashIndex);
                            if (name.length() == 0 || folder.length() == 0) {
                                return null;
                            }
                            folder += "/"; //NOI18N
                        } else {
                            return null;
                        }
                    }
                } else {
                    if (!entry.getName().startsWith(folder)) {
                        return null;
                    }
                }
            }
        } finally {
            source.close();
        }
        return folder;
    }

}
