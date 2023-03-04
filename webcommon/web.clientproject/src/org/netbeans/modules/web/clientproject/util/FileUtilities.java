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
package org.netbeans.modules.web.clientproject.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Miscellaneous utility methods for files.
 */
public final class FileUtilities {

    private static final Logger LOGGER = Logger.getLogger(FileUtilities.class.getName());

    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    private static final String XHTML_MIME_TYPE = "text/xhtml"; // NOI18N
    private static final String CSS_MIME_TYPE = "text/css"; // NOI18N
    private static final String JAVASCRIPT_MIME_TYPE = "text/javascript"; // NOI18N


    private FileUtilities() {
    }

    /**
     * Check whether the given file is an (X)HTML file.
     * @param file file to be checked
     * @return {@code true} if the given file is an (X)HTML file, {@code false} otherwise
     */
    public static boolean isHtmlFile(FileObject file) {
        return FileUtil.getMIMEType(file, HTML_MIME_TYPE, XHTML_MIME_TYPE, null) != null;
    }

    /**
     * Check whether the given file is a CSS file.
     * @param file file to be checked
     * @return {@code true} if the given file is a CSS file, {@code false} otherwise
     */
    public static boolean isCssFile(FileObject file) {
        return FileUtil.getMIMEType(file, CSS_MIME_TYPE, null) != null;
    }

    /**
     * Check whether the given file is a JavaScript file.
     * @param file file to be checked
     * @return {@code true} if the given file is a JavaScript file, {@code false} otherwise
     */
    public static boolean isJavaScriptFile(FileObject file) {
        return FileUtil.getMIMEType(file, JAVASCRIPT_MIME_TYPE, null) != null;
    }

    /**
     * Cleanup the given folder. The folder itself is not removed.
     * @param fileObject folder to be cleaned up
     * @throws IOException if any error occurs
     */
    public static void cleanupFolder(FileObject fileObject) throws IOException {
        for (FileObject child : fileObject.getChildren()) {
            child.delete();
        }
    }

    @CheckForNull
    public static FileObject lookupSourceFileOnly(Lookup context) {
        Collection<? extends FileObject> fileObjects = context.lookupAll(FileObject.class);
        if (fileObjects.size() != 1) {
            return null;
        }
        FileObject fileObject = fileObjects.iterator().next();
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        ClientSideProject clientSideProject = project.getLookup().lookup(ClientSideProject.class);
        if (clientSideProject == null) {
            return null;
        }
        FileObject sourcesFolder = clientSideProject.getSourcesFolder();
        if (sourcesFolder == null) {
            return null;
        }
        if (!FileUtil.isParentOf(sourcesFolder, fileObject)) {
            return null;
        }
        FileObject siteRootFolder = clientSideProject.getSiteRootFolder();
        if (siteRootFolder != null
                && FileUtil.isParentOf(siteRootFolder, fileObject)
                // #250009
                && !siteRootFolder.equals(sourcesFolder)) {
            return null;
        }
        FileObject testsFolder = clientSideProject.getTestsFolder(false);
        if (testsFolder != null
                && FileUtil.isParentOf(testsFolder, fileObject)) {
            // #254033
            return null;
        }
        FileObject testsSeleniumFolder = clientSideProject.getTestsSeleniumFolder(false);
        if (testsSeleniumFolder != null
                && FileUtil.isParentOf(testsSeleniumFolder, fileObject)) {
            // Hide Run/Debug File actions from javascript files under Selenium Tests Folder, as
            // more appropriate actions will be registered and handled by selenium support
            return null;
        }
        return fileObject;
    }

    /**
     * Move content of the source to the target. If the target file already exists and is a file
     * (not folder), the source file is moved and renamed (suffix <i>_0</i>, <i>_1</i> etc.).
     * @param source source file object
     * @param target target file object
     * @throws IOException if any error occurs
     */
    public static void moveContent(FileObject source, FileObject target) throws IOException {
        for (FileObject child : source.getChildren()) {
            FileObject newChild = target.getFileObject(child.getNameExt());
            if (newChild == null) {
                // does not exists
                FileUtil.moveFile(child, target, child.getName());
            } else if (newChild.isFolder()) {
                // copy directory content
                moveContent(child, newChild);
                child.delete();
            } else {
                // file already exists => rename
                int i = 0;
                for (;;) {
                    String newName = child.getName() + "_" + i++; // NOI18N
                    if (target.getFileObject(newName) == null) {
                        FileUtil.moveFile(child, target, newName);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Run task on the given ZIP file.
     * @param zipFile ZIP file to be processed
     * @param entryTask task to be applied on the ZIP file entries
     * @param entryFilter filter to be applied on the ZIP file entries, can be {@code null}
     * @throws IOException if any error occurs
     */
    public static void runOnZipEntries(@NonNull File zipFile, @NonNull ZipEntryTask entryTask, @NullAllowed ZipEntryFilter entryFilter) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                boolean accept = true;
                if (entryFilter != null) {
                    accept = entryFilter.accept(zipEntry);
                }
                if (accept) {
                    entryTask.run(zipEntry);
                    InputStream inputStream = zip.getInputStream(zipEntry);
                    try {
                        entryTask.run(inputStream);
                    } finally {
                        inputStream.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Get list of files from the given ZIP file according to the given {@link ZipEntryFilter filter}.
     * @param zipFile ZIP file to be listed
     * @param entryFilter filter to be applied on the ZIP file entries
     * @return list of files from the given ZIP file according to the given {@link ZipEntryFilter filter}
     * @throws IOException if any error occurs
     */
    public static List<String> listZipFiles(@NonNull File zipFile, @NonNull ZipEntryFilter entryFilter) throws IOException {
        assert zipFile != null;
        assert entryFilter != null;
        final List<String> files = new ArrayList<String>();
        runOnZipEntries(zipFile, new ZipEntryTask() {
            @Override
            public void run(ZipEntry zipEntry) {
                files.add(zipEntry.getName());
            }
            @Override
            public void run(InputStream zipEntryInputStream) {
                // noop
            }
        }, entryFilter);
        return files;
    }

    /**
     * Get list of JS files (filenames with relative path) from the given ZIP file.
     * <p>
     * If any error occurs, this error is logged with INFO level and an empty list is returned.
     * @param zipFile ZIP file to be listed
     * @return list of JS files (filenames with relative path) from the given ZIP file.
     * @see #listZipFiles(File, ZipEntryFilter)
     * @see #listJsFilenamesFromZipFile(File)
     */
    public static List<String> listJsFilesFromZipFile(File zipFile) {
        try {
            return listZipFiles(zipFile, new ZipEntryFilter() {
                @Override
                public boolean accept(ZipEntry zipEntry) {
                    return !zipEntry.isDirectory()
                            && zipEntry.getName().toLowerCase().endsWith(".js"); // NOI18N
                }
            });
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
    }

    //~ Inner classes

    /**
     * Filter for {@link ZipEntry}s.
     * <p>
     * Instances of this interface may be passed to the {@link #listZipFiles(File, ZipEntryFilter)} method.
     * @see #listZipFiles(File, ZipEntryFilter)
     */
    public interface ZipEntryFilter {

        /**
         * Test whether or not the specified {@link ZipEntry} should be
         * accepted.
         *
         * @param zipEntry the {@link ZipEntry} to be tested
         * @return {@ code true} if {@link ZipEntry} should be accepted, {@code false} otherwise
         */
        boolean accept(ZipEntry zipEntry);
    }

    /**
     * Task for {@link ZipEntry}s, their content.
     * @see #runOnZipEntries(File, ZipEntryTask, ZipEntryFilter)
     */
    public interface ZipEntryTask {

        /**
         * Run task on the given ZIP entry.
         * @param zipEntry {@link ZipEntry} to be processed
         */
        void run(ZipEntry zipEntry);

        /**
         * Run task on the given content, typically read it.
         * @param zipEntryInputStream content of the given {@link ZipEntry}
         */
        void run(InputStream zipEntryInputStream);
    }

}
