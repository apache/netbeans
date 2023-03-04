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


package org.netbeans.modules.editor.java;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.netbeans.editor.ext.DataAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;

/**
 *   DataAccessor for Code Completion DB files via FileObject streams
 *
 *   @author  Martin Roskanin
 */
public class FileObjectAccessor implements DataAccessor {

    FileObject fo;
    InputStream inputStream;
    FileOutputStream fos;
    int actOff;

    public FileObjectAccessor(FileObject fo) {
        this.fo = fo;
    }
    
    /** Appends exactly <code>len</code> bytes, starting at <code>off</code> of the buffer pointer
     * to the end of file resource.
     * @param  buffer the buffer from which the data is appended.
     * @param  off    the start offset of the data in the buffer.
     * @param  len    the number of bytes to append.
     */
    public void append(byte[] buffer, int off, int len) throws IOException {
        fos = new FileOutputStream(FileUtil.toFile(fo).getPath(), true);
        fos.write(buffer, off, len);
        fos.flush();
        fos.close();
        fos = null;
    }
    
    /**
     * Reads exactly <code>len</code> bytes from this file resource into the byte
     * array, starting at the current file pointer. This method reads
     * repeatedly from the file until the requested number of bytes are
     * read. This method blocks until the requested number of bytes are
     * read, the end of the inputStream is detected, or an exception is thrown.
     *
     * @param      buffer     the buffer into which the data is read.
     * @param      off        the start offset of the data.
     * @param      len        the number of bytes to read.
     */
    public void read(byte[] buffer, int off, int len) throws IOException {
        int n = 0;
        off = actOff + off;
	do {
	    int count = this.readStream(buffer, off + n, len - n);
	    if (count < 0)
		throw new EOFException();
	    n += count;
	} while (n < len);
    }
    
    /** Opens DataAccessor file resource 
     *  @param requestWrite if true, file is opened for read/write operation.
     */
    public void open(boolean requestWrite) throws IOException {
        if (!fo.existsExt(fo.getExt())){
            resetFile();
        }
    }
    
    /** Closes DataAccessor file resource  */
    public void close() throws IOException {
        if (inputStream!=null){
            inputStream.close();
        }
        inputStream = null;
    }
    
    /**
     * Returns the current offset in this file. 
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     */
    public long getFilePointer() throws IOException {
        return actOff;
    }
    
    /** Clears the file and sets the offset to 0 */
    public void resetFile() throws IOException {
        FileObject folder = fo.getParent();
        String name = fo.getName();
        String ext  = fo.getExt();
        FileLock lock = fo.lock();
        try {
            fo.delete(lock);
        } finally {
            lock.releaseLock();
        }
        fo = folder.createData(name, ext);
        actOff = 0;
    }
    
    /**
     * Sets the file-pointer offset, measured from the beginning of this
     * file, at which the next read or write occurs.
     */    
    public void seek(long pos) throws IOException {
        actOff = (int)pos;
    }

    /** Reads up to len bytes of data from this file resource into an array of bytes.
     * @param buffer the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     */
    private int readStream(byte[] buffer, int off, int len) throws IOException {
        int read = getStream(off).read(buffer,0,len);
        actOff += read;
        return read;
    }
    
    /** Gets InputStream prepared for reading from <code>off</code> offset position*/
    private InputStream getStream(int off) throws IOException {
        if(inputStream == null) {
            inputStream = fo.getInputStream();
            inputStream.skip(off);
        } else {
            if(off >= actOff) {
                inputStream.skip(off-actOff);
            } else {
                inputStream.close();
                inputStream = fo.getInputStream();
                inputStream.skip(off);
            }
        }
        actOff = off;
        return inputStream;
    }    
    
    public int getFileLength() {
        return (int)fo.getSize();
    }
    
    public String toString() {
        return fo.toString();
    }
    
}
