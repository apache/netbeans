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
package org.netbeans.modules.localtasks.util;

import java.io.*;
import java.util.logging.Level;
import org.netbeans.modules.localtasks.LocalRepository;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author pkuzel
 */
public class FileUtils {

    /**
     * Copies the specified sourceFile to the specified targetFile.
     */
    public static void copyFile (File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(sourceFile);
            copyStreamToFile(inputStream, targetFile);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    public static void copyDirFiles (File sourceDir, File targetDir) {
        copyDirFiles(sourceDir, targetDir, false);
    }

    public static void copyDirFiles (File sourceDir, File targetDir, boolean preserveTimestamp) {
        File[] files = sourceDir.listFiles();

        if (files == null || files.length == 0) {
            targetDir.mkdirs();
            if (preserveTimestamp) {
                targetDir.setLastModified(sourceDir.lastModified());
            }
            return;
        }
        if (preserveTimestamp) {
            targetDir.setLastModified(sourceDir.lastModified());
        }
        for (int i = 0; i < files.length; i++) {
            try {
                File target = FileUtil.normalizeFile(new File(targetDir.getAbsolutePath() + "/" + files[i].getName())); // NOI18N
                if (files[i].isDirectory()) {
                    copyDirFiles(files[i], target, preserveTimestamp);
                } else {
                    FileUtils.copyFile(files[i], target);
                    if (preserveTimestamp) {
                        target.setLastModified(files[i].lastModified());
                    }
                }
            } catch (IOException ex) {
                LocalRepository.LOG.log(Level.INFO, null, ex); // should not happen
            }
        }
    }

    /**
     * Copies the specified sourceFile to the specified targetFile. It
     * <b>closes</b> the input stream.
     */
    public static void copyStreamToFile (InputStream inputStream, File targetFile) throws IOException {
        if (inputStream == null || targetFile == null) {
            throw new NullPointerException("sourcStream and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        OutputStream outputStream = null;
        try {
            outputStream = createOutputStream(targetFile);
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                        readBytes > 0;
                        readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            } catch (IOException ex) {
                targetFile.delete();
                throw ex;
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Reads the data from the <code>file</code> and returns it as an array of
     * bytes.
     *
     * @param file file to be read
     * @return file contents as a byte array
     * @throws java.io.IOException
     */
    public static byte[] getFileContentsAsByteArray (File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 5);
        BufferedInputStream bis = null;
        try {
            bis = createInputStream(file);
            byte[] buffer = new byte[1024];
            for (int byteRead = bis.read(buffer); byteRead > 0; byteRead = bis.read(buffer)) {
                baos.write(buffer, 0, byteRead);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
        return baos.toByteArray();
    }

    /**
     * Recursively deletes all files and directories under a given
     * file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively (File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    /**
     * Do the best to rename the file.
     *
     * @param orig regular file
     * @param dest regular file (if exists it's rewritten)
     */
    public static void renameFile (File orig, File dest) throws IOException {
        boolean destExists = dest.exists();
        if (destExists) {
            for (int i = 0; i < 3; i++) {
                if (dest.delete()) {
                    destExists = false;
                    break;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        if (destExists == false) {
            for (int i = 0; i < 3; i++) {
                if (orig.renameTo(dest)) {
                    return;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        // requires less permisions than renameTo
        FileUtils.copyFile(orig, dest);

        for (int i = 0; i < 3; i++) {
            if (orig.delete()) {
                return;
            }
            try {
                Thread.sleep(71);
            } catch (InterruptedException e) {
            }
        }
        throw new IOException("Can not delete: " + orig.getAbsolutePath());  // NOI18N
    }

    /**
     * This utility class needs not to be instantiated anywhere.
     */
    private FileUtils () {
    }

    public static BufferedInputStream createInputStream (File file) throws IOException {
        int retry = 0;
        while (true) {
            try {
                return new BufferedInputStream(new FileInputStream(file));
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }
    }

    public static BufferedOutputStream createOutputStream (File file) throws IOException {
        int retry = 0;
        while (true) {
            try {
                return new BufferedOutputStream(new FileOutputStream(file));
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }
    }

    /**
     * Creates new tmp dir in java.io.tmpdir
     */
    public static File createTmpFolder (String prefix) {
        String tmpDir = System.getProperty("java.io.tmpdir");  // NOI18N
        File tmpFolder = new File(tmpDir);
        File checkoutFolder = null;
        try {
            // generate unique name for tmp folder
            File tmp = File.createTempFile(prefix, "", tmpFolder);  // NOI18N
            if (tmp.delete() == false) {
                return checkoutFolder;
            }
            if (tmp.mkdirs() == false) {
                return checkoutFolder;
            }
            checkoutFolder = FileUtil.normalizeFile(tmp);
        } catch (IOException e) {
            LocalRepository.LOG.log(Level.SEVERE, null, e);
        }
        return checkoutFolder;
    }

    /**
     * Returns the first found file whose filename is the same (in a case
     * insensitive way) as given <code>file</code>'s.
     *
     * @param file
     * @return the first found file with the same name, but ignoring case, or
     * <code>null</code> if no such file is found.
     */
    public static String getExistingFilenameInParent (File file) {
        String filename = null;
        if (file == null) {
            return filename;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return filename;
        }
        File[] children = parent.listFiles();
        for (File child : children) {
            if (file.getName().equalsIgnoreCase(child.getName())) {
                filename = child.getName();
                break;
            }
        }
        return filename;
    }

    /**
     * Copies all content from the supplied reader to the supplies writer and
     * closes both streams when finished.
     *
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails
     */
    public static void copyStreamsCloseAll (OutputStream writer, InputStream reader) throws IOException {
        byte[] buffer = new byte[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    /**
     * Copies all content from the supplied reader to the supplies writer and
     * closes both streams when finished.
     *
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails
     */
    public static void copyStreamsCloseAll (Writer writer, Reader reader) throws IOException {
        char[] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
}
