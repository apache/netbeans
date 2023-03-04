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

package org.netbeans.modules.php.api.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Miscellaneous file utilities.
 * @author Tomas Mysik
 */
public final class FileUtils {

    static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    /**
     * Constant for PHP MIME type.
     * @see #isPhpFile(FileObject)
     */
    public static final String  PHP_MIME_TYPE = "text/x-php5"; // NOI18N

    private static final boolean IS_UNIX = Utilities.isUnix();
    private static final boolean IS_MAC = Utilities.isMac();
    private static final boolean IS_WINDOWS = Utilities.isWindows();
    private static final ZipEntryFilter DUMMY_ZIP_ENTRY_FILTER = new ZipEntryFilter() {
        @Override
        public boolean accept(ZipEntry zipEntry) {
            return true;
        }
        @Override
        public String getName(ZipEntry zipEntry) {
            return zipEntry.getName();
        }
    };


    private FileUtils() {
    }

    /**
     * Returns <code>true</code> if the file is a PHP file.
     * @param file file to check
     * @return <code>true</code> if the file is a PHP file
     * @see #PHP_MIME_TYPE
     */
    public static boolean isPhpFile(FileObject file) {
        Parameters.notNull("file", file); // NOI18N
        return FileUtil.getMIMEType(file, PHP_MIME_TYPE, null) != null;
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
     * Get the OS-dependent script extension.
     * <ul>Currently it returns (for dotted version):
     *   <li><tt>.bat</tt> on Windows
     *   <li><tt>.sh</tt> anywhere else
     * </ul>
     * @param withDot return "." as well, e.g. <tt>.sh</tt>
     * @return the OS-dependent script extension
     */
    public static String getScriptExtension(boolean withDot) {
        StringBuilder sb = new StringBuilder(4);
        if (withDot) {
            sb.append("."); // NOI18N
        }
        if (IS_WINDOWS) {
            sb.append("bat"); // NOI18N
        } else {
            sb.append("sh"); // NOI18N
        }
        return sb.toString();
    }

    /**
     * Get {@link FileObject} for the given {@link Lookup context}.
     * @param context {@link Lookup context} where the {@link FileObject} is searched for
     * @return {@link FileObject} for the given {@link Lookup context} or <code>null</code> if not found
     */
    @CheckForNull
    public static FileObject getFileObject(Lookup context) {
        FileObject fo = context.lookup(FileObject.class);
        if (fo != null) {
            return fo;
        }
        DataObject d = context.lookup(DataObject.class);
        if (d != null) {
            return d.getPrimaryFile();
        }
        return null;
    }

    /**
     * Create {@link org.xml.sax.XMLReader} from {javax.xml.parsers.SAXParser}.
     * @return {@link org.xml.sax.XMLReader} from {javax.xml.parsers.SAXParser}
     * @throws SAXException if the parser cannot be created
     */
    public static XMLReader createXmlReader() throws SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            return factory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Cannot create SAX parser", ex);
        }
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
     * @see #validateDirectory(String, String, boolean)
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
     * Validate a directory path and return {@code null} if it is valid, otherwise an error.
     * <p>
     * This method simply calls {@link #validateDirectory(String, String, boolean)} with "Directory"
     * (localized) as a {@code source}.
     * @param dirPath a file path to validate
     * @param writable {@code true} if the directory must be writable, {@code false} otherwise
     * @return {@code null} if it is valid, otherwise an error
     * @see #validateDirectory(String, String, boolean)
     * @see #isDirectoryWritable(File)
     */
    @NbBundle.Messages("FileUtils.validateDirectory.directory=Directory")
    @CheckForNull
    public static String validateDirectory(String dirPath, boolean writable) {
        return validateDirectory(Bundle.FileUtils_validateDirectory_directory(), dirPath, writable);
    }

    /**
     * Validate a directory path and return {@code null} if it is valid, otherwise an error.
     * <p>
     * A valid directory means that the <tt>dirPath</tt> represents an existing, readable, optionally
     * writable directory with absolute file path.
     * @param source source used in error message (e.g. "Project directory", "Working directory")
     * @param dirPath a file path to validate
     * @param writable {@code true} if the directory must be writable, {@code false} otherwise
     * @return {@code null} if it is valid, otherwise an error
     * @see #isDirectoryWritable(File)
     */
    @NbBundle.Messages({
        "# {0} - source",
        "FileUtils.validateDirectory.missing={0} must be selected.",
        "# {0} - source",
        "FileUtils.validateDirectory.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "FileUtils.validateDirectory.notDir={0} must be a valid directory.",
        "# {0} - source",
        "FileUtils.validateDirectory.notReadable={0} is not readable.",
        "# {0} - source",
        "FileUtils.validateDirectory.notWritable={0} is not writable."
    })
    @CheckForNull
    public static String validateDirectory(String source, String dirPath, boolean writable) {
        if (!StringUtils.hasText(dirPath)) {
            return Bundle.FileUtils_validateDirectory_missing(source);
        }

        File dir = new File(dirPath);
        if (!dir.isAbsolute()) {
            return Bundle.FileUtils_validateDirectory_notAbsolute(source);
        } else if (!dir.isDirectory()) {
            return Bundle.FileUtils_validateDirectory_notDir(source);
        } else if (!dir.canRead()) {
            return Bundle.FileUtils_validateDirectory_notReadable(source);
        } else if (writable && !isDirectoryWritable(dir)) {
            return Bundle.FileUtils_validateDirectory_notWritable(source);
        }
        return null;
    }

    // #144928, #157417
    /**
     * Handles correctly 'feature' of Windows (read-only flag, "Program Files" directory on Windows Vista).
     * @param directory a directory to check
     * @return <code>true</code> if directory is writable
     */
    public static boolean isDirectoryWritable(File directory) {
        if (!directory.isDirectory()) {
            // #157591
            LOGGER.log(Level.FINE, "{0} is not a folder", directory);
            return false;
        }
        boolean windows = IS_WINDOWS;
        LOGGER.log(Level.FINE, "On Windows: {0}", windows);

        boolean canWrite = directory.canWrite();
        LOGGER.log(Level.FINE, "Folder {0} is writable: {1}", new Object[] {directory, canWrite});
        if (!windows) {
            // we are not on windows => result is ok
            return canWrite;
        }

        // on windows
        LOGGER.fine("Trying to create temp file");
        try {
            File tmpFile = File.createTempFile("netbeans", null, directory);
            LOGGER.log(Level.FINE, "Temp file {0} created", tmpFile);
            if (!tmpFile.delete()) {
                tmpFile.deleteOnExit();
            }
            LOGGER.log(Level.FINE, "Temp file {0} deleted", tmpFile);
        } catch (IOException exc) {
            LOGGER.log(Level.FINE, exc.getMessage(), exc);
            return false;
        }
        return true;
    }

    /**
     * Test whether the given file is a folder and is a symlink.
     * <p>
     * In other words, a file is never considered to be a symlink. Directory is checked
     * and correct result is returned.
     * @param directory file to be checked, cannot be {@code null}
     * @return {@code true} if the file is a folder and a symlink, {@code false} otherwise
     */
    public static boolean isDirectoryLink(File directory) {
        Parameters.notNull("directory", directory); // NOI18N
        if (!IS_UNIX && !IS_MAC) {
            return false;
        }
        if (!directory.isDirectory()) {
            return false;
        }
        final File canDirectory;
        try {
            canDirectory = directory.getCanonicalFile();
        } catch (IOException ioe) {
            return false;
        }
        final String dirPath = directory.getAbsolutePath();
        final String canDirPath = canDirectory.getAbsolutePath();
        return IS_MAC ? !dirPath.equalsIgnoreCase(canDirPath) : !dirPath.equals(canDirPath);
    }

    /**
     * Reformat the file. If the does not exist, nothing is done.
     * @param file file to be reformatted
     * @throws IOException if any error occurs
     * @see #reformatFile(DataObject)
     * @since 2.54
     */
    public static void reformatFile(@NonNull File file) throws IOException {
        Parameters.notNull("file", file); // NOI18N
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null) {
            return;
        }
        reformatFile(DataObject.find(fileObject));
    }

    // XXX see AssertionError at HtmlIndenter.java:68
    // NbReaderProvider.setupReaders(); cannot be called because of deps
    /**
     * Reformat the file.
     * @param dataObject file to be reformatted
     * @throws IOException if any error occurs
     * @see #reformatFile(File)
     * @since 2.54
     */
    public static void reformatFile(@NonNull final DataObject dataObject) throws IOException {
        Parameters.notNull("dataObject", dataObject); // NOI18N

        EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
        assert ec != null : "No editorcookie for " + dataObject;

        Document doc = ec.openDocument();
        assert doc instanceof BaseDocument;

        // reformat
        final BaseDocument baseDoc = (BaseDocument) doc;
        final Reformat reformat = Reformat.get(baseDoc);
        reformat.lock();
        try {
            // seems to be synchronous but no info in javadoc
            baseDoc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    try {
                        reformat.reformat(0, baseDoc.getLength());
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.INFO, "Cannot reformat file " + dataObject.getName(), ex);
                    }
                }
            });
        } finally {
            reformat.unlock();
        }

        // save
        saveFile(dataObject);
    }

    /**
     * Save a file.
     * @param dataObject file to be saved
     * @since 2.54
     */
    public static void saveFile(@NonNull DataObject dataObject) {
        Parameters.notNull("dataObject", dataObject); // NOI18N
        SaveCookie saveCookie = dataObject.getLookup().lookup(SaveCookie.class);
        if (saveCookie != null) {
            try {
                try {
                    saveCookie.save();
                } catch (UserQuestionException uqe) {
                    // #216194
                    NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                        uqe.confirmed();
                        saveCookie.save();
                    }
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, ioe.getLocalizedMessage(), ioe);
            }
        }
    }

    /**
     * Save a file.
     * @param fileObject file to be saved
     * @since 2.54
     */
    public static void saveFile(@NonNull FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject); // NOI18N
        try {
            DataObject dobj = DataObject.find(fileObject);
            if (dobj != null) {
                saveFile(dobj);
            }
        } catch (DataObjectNotFoundException donfe) {
            LOGGER.log(Level.SEVERE, donfe.getLocalizedMessage(), donfe);
        }
    }

    /**
     * Open file.
     * @param file file to be opened
     * @see #openFile(File, int)
     * @since 2.54
     */
    public static void openFile(@NonNull File file) {
        Parameters.notNull("file", file); // NOI18N
        openFile(file, -1);
    }

    /**
     * Open the file and optionally set cursor to the line. If the file does not exist,
     * nothing is done.
     * <p>
     * <i>Note:</i> This action is always run in AWT thread.
     * @param file file to be opened
     * @param line line of a file to set cursor to, {@code -1} if no specific line is needed
     * @see #openFile(File)
     * @since 2.54
     */
    public static void openFile(@NonNull File file, int line) {
        Parameters.notNull("file", file); // NOI18N

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
            final Line currentLine = lineSet.getOriginal(line - 1);
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

    /**
     * Recursively unzip the given ZIP archive to the given target directory.
     * @param zipPath path of ZIP archive to be extracted
     * @param targetDirectory target directory
     * @param zipEntryFilter {@link ZipEntryFilter}, can be {@code null} (in such case, all entries are accepted with their original names)
     * @throws IOException if any error occurs
     */
    public static void unzip(String zipPath, File targetDirectory, ZipEntryFilter zipEntryFilter) throws IOException {
        Parameters.notEmpty("zipPath", zipPath); // NOI18N
        Parameters.notNull("targetDirectory", targetDirectory); // NOI18N

        if (zipEntryFilter == null) {
            zipEntryFilter = DUMMY_ZIP_ENTRY_FILTER;
        }
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntryFilter.accept(zipEntry)) {
                    continue;
                }
                File destinationFile = new File(targetDirectory, zipEntryFilter.getName(zipEntry));
                ensureParentExists(destinationFile);
                copyZipEntry(zipFile, zipEntry, destinationFile);
            }
        }
    }

    /**
     * Get common root of the given file objects.
     * @param fo1 first fileobject
     * @param fo2 second fileobject
     * @return common root of the given file objects, {@code null} if there is none
     * @since 2.31
     */
    @CheckForNull
    public static FileObject getCommonRoot(@NonNull FileObject fo1, @NonNull FileObject fo2) {
        Parameters.notNull("fo1", fo1); // NOI18N
        Parameters.notNull("fo2", fo2); // NOI18N
        FileObject tmp = fo1;
        while (tmp != null) {
            if (tmp.equals(fo2)
                    || FileUtil.isParentOf(tmp, fo2)) {
                return tmp;
            }
            tmp = tmp.getParent();
        }
        return null;
    }

    private static void ensureParentExists(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create parent directories for " + file.getAbsolutePath());
            }
        }
    }

    private static void copyZipEntry(ZipFile zipFile, ZipEntry zipEntry, File destinationFile) throws IOException {
        if (zipEntry.isDirectory()) {
            return;
        }
        try (InputStream inputStream = zipFile.getInputStream(zipEntry); FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            FileUtil.copy(inputStream, outputStream);
        }
    }

    //~ Inner classes

    /**
     * Filter for {@link ZipEntry}s.
     * <p>
     * Instances of this interface may be passed to the {@link #unzip(String, File, ZipEntryFilter)}code> method.
     * @see #unzip(String, File, ZipEntryFilter)
     */
    public interface ZipEntryFilter {

        /**
         * Test whether or not the specified {@link ZipEntry} should be
         * included in a list.
         *
         * @param zipEntry the {@link ZipEntry} to be tested
         * @return {@ code true} if {@link ZipEntry} should be included, {@code false} otherwise
         */
        boolean accept(ZipEntry zipEntry);

        /**
         * Get the name of the specified {@link ZipEntry}; in other words, this method allows
         * to rename the specified {@link ZipEntry}.
         * <p>
         * Typical implementation simply returns {@link ZipEntry#getName() original name}.
         * @param zipEntry the {@link ZipEntry} to be got name of
         * @return the name of the specified {@link ZipEntry}
         */
        String getName(ZipEntry zipEntry);

    }

}
