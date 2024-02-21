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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;

/**
 * Used for downloaded files.
 * <p>
 * <b>WARNING:</b> Do not forget to call {@link TmpLocalFile#cleanup()}
 * when the file is not needed anymore.
 */
public abstract class TmpLocalFile {

    private static final Logger LOGGER = Logger.getLogger(TmpLocalFile.class.getName());


    TmpLocalFile() {
    }

    /**
     * Suitable for small remote files.
     * @return tmp local file in memory, never {@code null}
     */
    public static TmpLocalFile inMemory(int size) {
        return new MemoryTmpLocalFile(size);
    }

    /**
     * Suitable for big remote files, uses local temp file.
     * @return tmp local file or {@code null} if any error occurs.
     */
    public static TmpLocalFile onDisk() {
        return onDisk(null);
    }

    /**
     * Suitable for big remote files, uses local temp file.
     * @param extension file extension, can be {@code null}
     * @return tmp local file or {@code null} if any error occurs.
     */
    public static TmpLocalFile onDisk(String extension) {
        try {
            return new DiskTmpLocalFile(extension);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot create local tmp file", ex);
        }
        return null;
    }

    /**
     * Return {@code true} if the tmp file is in memory only.
     * @return {@code true} if the tmp file is in memory only
     */
    public abstract boolean isInMemory();

    /**
     * Return absolute path on disk, if available.
     * @return absolute path on disk, if available; {@code null} otherwise
     */
    public abstract String getAbsolutePath();

    /**
     * Get the file output stream, can be {@code null} if any error occurs.
     * @return file output stream, can be {@code null} if any error occurs
     */
    public abstract OutputStream getOutputStream();

    /**
     * Get the file input stream, can be {@code null} if any error occurs.
     * @return file input stream, can be {@code null} if any error occurs
     */
    public abstract InputStream getInputStream();

    /**
     * Cleanup, e.g. delete the temporary file (if any) etc.
     */
    public abstract void cleanup();

    @Override
    public abstract String toString();



    //~ Inner classes

    private static final class MemoryTmpLocalFile extends TmpLocalFile {

        private final ByteArrayOutputStream outputStream;


        public MemoryTmpLocalFile(int size) {
            this.outputStream = new ByteArrayOutputStream(size);
        }

        @Override
        public void cleanup() {
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public String getAbsolutePath() {
            return null;
        }

        @Override
        public OutputStream getOutputStream() {
            outputStream.reset();
            return outputStream;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(outputStream.toByteArray());
        }

        @Override
        public String toString() {
            return "<MemoryTmpLocalFile>"; // NOI18N
        }

    }

    private static final class DiskTmpLocalFile extends TmpLocalFile {

        private static final Logger LOGGER = Logger.getLogger(DiskTmpLocalFile.class.getName());

        private final File file;


        public DiskTmpLocalFile(String extension) throws IOException {
            file = FileUtil.normalizeFile(Files.createTempFile("nb-php-remote-tmp-file-", extension != null ? "." + extension : null).toFile()); // NOI18N
            file.deleteOnExit();
        }

        @Override
        public void cleanup() {
            if (!file.delete()) {
                LOGGER.info("Cannot delete temporary file");
            }
        }

        @Override
        public boolean isInMemory() {
            return false;
        }

        @Override
        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        @Override
        public OutputStream getOutputStream() {
            try {
                return new BufferedOutputStream(new FileOutputStream(file));
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.INFO, "Cannot create output stream for local tmp file", ex);
                return null;
            }
        }

        @Override
        public InputStream getInputStream() {
            try {
                return new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.INFO, "Cannot create input stream for local tmp file", ex);
                return null;
            }
        }

        @Override
        public String toString() {
            return "<DiskTmpLocalFile<" + file.getAbsolutePath() + ">>"; // NOI18N
        }

    }

}
