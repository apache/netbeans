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
package org.netbeans.modules.db.dataview.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import org.openide.util.Exceptions;

/**
 * A storage implementing a _subset_ of the Blob Interface backed by a file
 * 
 * Currently the following function are not implemented:
 * - all position functions
 * - getBinaryStream(long pos, long length)
 * 
 * @author mblaesing
 */
public class FileBackedBlob implements Blob {

    private boolean freed = false;
    private File backingFile;

    public FileBackedBlob() throws SQLException {
        try {
            backingFile = Files.createTempFile("netbeans-db-blob", null).toFile();
            backingFile.deleteOnExit();
        } catch (IOException ex) {
            throw new SQLException(ex);
        }
    }

    public FileBackedBlob(InputStream is) throws SQLException {
        this();
        OutputStream os = null;
        try {
            os = setBinaryStream(1);
            int read = 0;
            byte[] buffer = new byte[(int) Math.pow(2, 18)];
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                if(os != null) os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                if(is != null) is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public long length() throws SQLException {
        checkFreed();
        return backingFile.length();
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        checkFreed();
        checkPos(pos);
        checkLength(length);
        InputStream is = null;
        try {
            is = new FileInputStream(backingFile);
            is.skip(pos - 1);
            byte[] result = new byte[length];
            is.read(result);
            return result;
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        checkFreed();
        try {
            return new FileInputStream(backingFile);
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        checkFreed();
        checkPos(start);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long position(Blob pattern, long start) throws SQLException {
        checkFreed();
        checkPos(start);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        checkFreed();
        checkPos(pos);
        return setBytes(pos, bytes, 0, bytes.length);
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        checkFreed();
        checkPos(pos);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(backingFile, "rw");
            raf.seek(pos - 1);
            int border = Math.min(bytes.length, offset + len);
            int written = 0;
            for (int i = offset; i < border; i++) {
                raf.write(bytes[i]);
                written++;
            }
            return written;
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                raf.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        checkFreed();
        checkPos(pos);

        try {
            final RandomAccessFile raf = new RandomAccessFile(backingFile, "rw");
            try {
                raf.seek(pos - 1);
            } catch (IOException ex) {
                raf.close();
                throw new SQLException(ex);
            }

            return new RandomAccessOutputStream(raf);
        } catch (IOException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public void truncate(long len) throws SQLException {
        checkFreed();
        RandomAccessFile raf = null;
        try {
            raf  = new RandomAccessFile(backingFile, "rw");
            raf.setLength(len);
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                raf.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void free() throws SQLException {
        if (!freed) {
            backingFile.delete();
            freed = true;
        }
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        checkFreed();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void checkFreed() throws SQLException {
        if (freed) {
            throw new SQLException("Blob already freed");
        }
    }

    private void checkPos(long pos) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Illegal Value for position: " + Long.toString(pos));
        }
    }

    private void checkLength(long length) throws SQLException {
        if (length < 0) {
            throw new SQLException("Illegal Value for length: " + Long.toString(length));
        }
    }
    
    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }
    
    File getBackingFile() {
        return backingFile;
    }
}
