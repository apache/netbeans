/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.sql.Clob;
import java.sql.SQLException;
import org.openide.util.Exceptions;

/**
 * A storage implementing a _subset_ of the Blob Interface backed by a file
 * 
 * Currently the following function are not implemented:
 * - all position functions
 * - getCharacterStream(long pos, long length)
 * - setAsciiStream
 * - getAsciiStream
 * 
 * @author mblaesing
 */
public class FileBackedClob implements Clob {

    private boolean freed = false;
    private File backingFile;

    public FileBackedClob() throws SQLException {
        try {
            backingFile = File.createTempFile("netbeans-db-blob", null);
            backingFile.deleteOnExit();
        } catch (IOException ex) {
            throw new SQLException(ex);
        }
    }

    public FileBackedClob(String init) throws SQLException {
        this();
        this.setString(1, init);
    }

    public FileBackedClob(Reader r) throws SQLException {
        this();
        Writer w = setCharacterStream(1);
        int read = 0;
        char[] buffer = new char[(int) Math.pow(2, 18)];
        try {
            while ((read = r.read(buffer)) > 0) {
                w.write(buffer, 0, read);
            }
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public long length() throws SQLException {
        checkFreed();
        return backingFile.length() / 4;
    }

    @Override
    public void truncate(long len) throws SQLException {
        checkFreed();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(backingFile, "rw");
            raf.setLength(len * 4);
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

    @Override
    public String getSubString(long pos, int length) throws SQLException {
        checkFreed();
        checkPos(pos);
        checkLength(length);
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(backingFile), "UTF_32BE");
            r.skip(pos - 1);
            CharBuffer c = CharBuffer.allocate(length);
            r.read(c);
            c.rewind();
            return c.toString();
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                r.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    @Override
    public Reader getCharacterStream() throws SQLException {
        checkFreed();
        Reader r;
        try {
            r = new InputStreamReader(new FileInputStream(backingFile), "UTF_32BE");
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new SQLException(ex);
        }
        return r;
    }

    @Override
    public InputStream getAsciiStream() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long position(String searchstr, long start) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long position(Clob searchstr, long start) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int setString(long pos, String str) throws SQLException {
        return setString(pos, str, 0, str.length());
    }

    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
        checkFreed();
        checkPos(pos);
        Writer w = null;
        try {
            w = setCharacterStream(pos);
            w.write(str.substring(offset, Math.min(offset + len, str.length())));
            return len;
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                w.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public OutputStream setAsciiStream(long pos) throws SQLException {
        checkFreed();
        checkPos(pos);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Writer setCharacterStream(long pos) throws SQLException {
        checkFreed();
        checkPos(pos);

        try {
            final RandomAccessFile raf = new RandomAccessFile(backingFile, "rw");
            try {
                raf.seek((pos - 1) * 4);
            } catch (IOException ex) {
                raf.close();
                throw new SQLException(ex);
            }

            return new OutputStreamWriter(new RandomAccessOutputStream(raf), "UTF_32BE");
        } catch (IOException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        checkFreed();
        checkPos(pos);
        checkLength(length);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }

    File getBackingFile() {
        return backingFile;
    }
}
