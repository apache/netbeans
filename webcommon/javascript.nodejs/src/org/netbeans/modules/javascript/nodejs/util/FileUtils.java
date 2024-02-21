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
package org.netbeans.modules.javascript.nodejs.util;

import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

// XXX copied from PHP
public final class FileUtils {

    static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    public static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir")); // NOI18N

    private static final boolean IS_WINDOWS = Utilities.isWindows();
    private static final String JAVASCRIPT_MIME_TYPE = "text/javascript"; // NOI18N
    private static final String NODEJS_SOURCES_URL = "https://nodejs.org/dist/v%1$s/node-v%1$s.tar.gz"; // NOI18N
    private static final String IOJS_SOURCES_URL = "https://iojs.org/dist/v%1$s/iojs-v%1$s.tar.gz"; // NOI18N


    private FileUtils() {
    }

    public static boolean isJavaScriptFile(FileObject file) {
        assert file != null;
        return FileUtil.getMIMEType(file, JAVASCRIPT_MIME_TYPE, null) != null;
    }

    public static boolean isJavaScriptFile(File file) {
        return isJavaScriptFile(FileUtil.toFileObject(file));
    }

    public static String relativizePath(Project project, String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return ""; // NOI18N
        }
        File file = new File(filePath);
        String path = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), file);
        if (path == null
                || path.startsWith("../")) { // NOI18N
            // cannot be relativized or outside project
            path = file.getAbsolutePath();
        }
        return path;
    }

    public static String resolvePath(Project project, String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), filePath).getAbsolutePath();
    }

    public static List<File> sortFiles(Collection<File> files) {
        final Collator collator = Collator.getInstance();
        List<File> sortedFiles = new ArrayList<>(files);
        sortedFiles.sort(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return collator.compare(file1.getName(), file2.getName());
            }
        });
        return sortedFiles;
    }

    /**
     * Find all the files (absolute path) with the given "filename" on user's PATH.
     * <p>
     * This method is suitable for *nix as well as windows.
     * @param filename the name of a file to find.
     * @return list of absolute paths of found files.
     * @see #findFileOnUsersPath(String[])
     */
    public static List<String> findFileOnUsersPath(String filename) {
        Parameters.notNull("filename", filename); // NOI18N
        return findFileOnUsersPath(new String[]{filename});
    }

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

    /**
     * Validate a file path and return {@code null} if it is valid, otherwise an error.
     * <p>
     * This method simply calls {@link #validateFile(String, String, boolean)} with "File"
     * (localized) as a {@code source}.
     * @param filePath a file path to validate
     * @param writable {@code true} if the file must be writable, {@code false} otherwise
     * @return {@code null} if it is valid, otherwise an error
     * @see #validateFile(String, String, boolean)
     */
    @NbBundle.Messages("FileUtils.validateFile.file=File")
    @CheckForNull
    public static String validateFile(String filePath, boolean writable) {
        return validateFile(Bundle.FileUtils_validateFile_file(), filePath, writable);
    }

    /**
     * Validate a file path and return {@code null} if it is valid, otherwise an error.
     * <p>
     * A valid file means that the <tt>filePath</tt> represents a valid, readable file
     * with absolute file path.
     * @param source source used in error message (e.g. "Script", "Config file")
     * @param filePath a file path to validate
     * @param writable {@code true} if the file must be writable, {@code false} otherwise
     * @return {@code null} if it is valid, otherwise an error
     */
    @NbBundle.Messages({
        "# {0} - source",
        "FileUtils.validateFile.missing={0} must be selected.",
        "# {0} - source",
        "FileUtils.validateFile.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "FileUtils.validateFile.notFile={0} must be a valid file.",
        "# {0} - source",
        "FileUtils.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "FileUtils.validateFile.notWritable={0} is not writable."
    })
    @CheckForNull
    public static String validateFile(String source, String filePath, boolean writable) {
        if (!StringUtils.hasText(filePath)) {
            return Bundle.FileUtils_validateFile_missing(source);
        }

        File file = new File(filePath);
        if (!file.isAbsolute()) {
            return Bundle.FileUtils_validateFile_notAbsolute(source);
        } else if (!file.isFile()) {
            return Bundle.FileUtils_validateFile_notFile(source);
        } else if (!file.canRead()) {
            return Bundle.FileUtils_validateFile_notReadable(source);
        } else if (writable && !file.canWrite()) {
            return Bundle.FileUtils_validateFile_notWritable(source);
        }
        return null;
    }

    /**
     * Get the OS-dependent script extension.
     * <ul>Currently it returns (for dotted version):
     *   <li><tt>.bat/.cmd</tt> on Windows
     *   <li><tt>.sh</tt> anywhere else
     * </ul>
     * @param withDot return "." as well, e.g. <tt>.sh</tt>
     * @param cmdInsteadBatOnWin if {@code true}, return "cmd" instead of "bat" on Windows
     * @return the OS-dependent script extension
     */
    public static String getScriptExtension(boolean withDot, boolean cmdInsteadBatOnWin) {
        StringBuilder sb = new StringBuilder(4);
        if (withDot) {
            sb.append("."); // NOI18N
        }
        if (IS_WINDOWS) {
            sb.append(cmdInsteadBatOnWin ? "cmd" : "bat"); // NOI18N
        } else {
            sb.append("sh"); // NOI18N
        }
        return sb.toString();
    }

    /**
     * Opens the file and optionally set cursor to the line. This action is always run in AWT thread.
     * @param file path of a file to open
     * @param line line of a file to set cursor to, {@code -1} if no specific line is needed
     */
    public static void openFile(File file, int line) {
        assert file != null;

        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            LOGGER.log(Level.INFO, "FileObject not found for {0}", file);
            return;
        }

        DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.INFO, "DataObject not found for {0}", file);
            return;
        }

        if (line == -1) {
            // simply open file
            EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
            ec.open();
            return;
        }

        // open at specific line
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            LOGGER.log(Level.INFO, "LineCookie not found for {0}", file);
            return;
        }
        Line.Set lineSet = lineCookie.getLineSet();
        try {
            final Line currentLine = lineSet.getCurrent(line - 1);
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    currentLine.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                }
            });
        } catch (IndexOutOfBoundsException exc) {
            LOGGER.log(Level.FINE, null, exc);
        }
    }

    public static boolean downloadNodeSources(Version version, boolean iojs) throws NetworkException, IOException {
        assert !EventQueue.isDispatchThread();
        assert version != null;
        deleteExistingNodeSources(version);
        File nodeSources = NodeJsUtils.getNodeSources();
        String nodeVersion = version.toString();
        File archive = new File(nodeSources, (iojs ? "iojs" : "nodejs") + "-" + nodeVersion + ".tar.gz"); // NOI18N
        if (!downloadNodeSources(archive, nodeVersion, iojs)) {
            return false;
        }
        // unpack
        boolean success = false;
        try {
            String foldername = decompressTarGz(archive, nodeSources, false);
            assert foldername != null : version;
            success = new File(nodeSources, foldername).renameTo(new File(nodeSources, nodeVersion));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, archive.getAbsolutePath(), ex);
            throw ex;
        }
        if (!archive.delete()) {
            archive.deleteOnExit();
        }
        return success;
    }

    private static void deleteExistingNodeSources(Version version) throws IOException {
        assert version != null;
        if (NodeJsUtils.hasNodeSources(version)) {
            final FileObject fo = FileUtil.toFileObject(NodeJsUtils.getNodeSources(version));
            assert fo != null : version;
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    fo.delete();
                }
            });
        }
    }

    @NbBundle.Messages({
        "# {0} - version",
        "FileUtils.sources.downloading.nodejs=Downloading sources for node.js version {0}...",
        "# {0} - version",
        "FileUtils.sources.downloading.iojs=Downloading sources for io.js version {0}...",
    })
    private static boolean downloadNodeSources(File archive, String nodeVersion, boolean iojs) throws IOException {
        assert archive != null;
        assert nodeVersion != null;
        String url = String.format(iojs ? IOJS_SOURCES_URL : NODEJS_SOURCES_URL, nodeVersion);
        // download
        try {
            String msg = iojs ? Bundle.FileUtils_sources_downloading_iojs(nodeVersion) : Bundle.FileUtils_sources_downloading_nodejs(nodeVersion);
            NetworkSupport.downloadWithProgress(url, archive, msg);
            return true;
        } catch (InterruptedException ex) {
            // download cancelled
            LOGGER.log(Level.FINE, "Download cancelled for {0}", url);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, url, ex);
            throw ex;
        }
        return false;
    }

    @CheckForNull
    public static String decompressTarGz(File archive, File destination, boolean skipArchiveRoot) throws IOException {
        String archiveRoot = null;
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)))) {
            int archiveRootLength = -1;
            TarArchiveEntry tarEntry = tarInputStream.getNextTarEntry();
            if (tarEntry != null) {
                archiveRoot = tarEntry.getName();
                if (skipArchiveRoot) {
                    archiveRootLength = archiveRoot.length();
                    tarEntry = tarInputStream.getNextTarEntry();
                }
            }
            while (tarEntry != null) {
                String name = tarEntry.getName();
                if (skipArchiveRoot) {
                    name = name.substring(archiveRootLength);
                }
                File destPath = new File(destination, name);
                if (tarEntry.isDirectory()) {
                    if (!destPath.isDirectory()
                            && !destPath.mkdirs()) {
                        throw new IOException("Cannot create directory " + destPath);
                    }
                } else {
                    if (!destPath.isFile()
                            && !destPath.createNewFile()) {
                        throw new IOException("Cannot create new file " + destPath);
                    }
                    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destPath))) {
                        FileUtil.copy(tarInputStream, outputStream);
                    }
                }
                tarEntry = tarInputStream.getNextTarEntry();
            }
        }
        return archiveRoot;
    }

    public static boolean isSubdirectoryOf(File folder, File child) {
        if (!folder.isDirectory()) {
            return false;
        }
        String fp;
        try {
            fp = folder.getCanonicalPath();
        } catch (IOException ioex) {
            fp = folder.getAbsolutePath();
        }
        String chp;
        try {
            chp = child.getCanonicalPath();
        } catch (IOException ioex) {
            chp = child.getAbsolutePath();
        }
        if (!chp.startsWith(fp)) {
            return false;
        }
        int fl = fp.length();
        if (chp.length() == fl) {
            return true;
        }
        char separ = chp.charAt(fl);
        if (File.separatorChar == separ) {
            return true;
        } else {
            return false;
        }
    }

}
