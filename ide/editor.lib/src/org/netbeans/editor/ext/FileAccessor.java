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


package org.netbeans.editor.ext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 *   DataAccessor for Code Completion DB files via RandomAccessFile implementation
 *
 *   @author  Martin Roskanin
 */
public class FileAccessor implements DataAccessor{

    private File f;
    private RandomAccessFile file;


    /** Creates a new instance of FileAccessor */
    public FileAccessor(File file) {
        f = file;
    }

    /** Appends exactly <code>len</code> bytes, starting at <code>off</code> of the buffer pointer
     * to the end of file resource.
     * @param  buffer the buffer from which the data is appended.
     * @param  off    the start offset of the data in the buffer.
     * @param  len    the number of bytes to append.
     * @return        the actual file offset before appending.
     */
    public void append(byte[] buffer, int off, int len) throws IOException {
        file.write(buffer, off, len);
    }
    
    /** Reads up to len bytes of data from this file resource into an array of bytes.
     * @param buffer the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     */
    
    public void read(byte[] buffer, int off, int len) throws IOException {
        file.readFully(buffer, off, len);
    }
    
    /** Opens DataAccessor file resource 
     *  @param requestWrite if true, file is opened for read/write operation.
     */
    public void open(boolean requestWrite) throws IOException {
        file = new RandomAccessFile(f, requestWrite ? "rw" : "r"); //NOI18N
        if (!f.exists()){
            f.createNewFile();
        }
    }
    
    /** Closes DataAccessor file resource  */
    public void close() throws IOException {
        if (file!=null){
            file.close();
        }
    }
    
    /**
     * Returns the current offset in this file.
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     */
    public long getFilePointer() throws IOException {
        return file.getFilePointer();
    }

    /** Clears the file and sets the offset to 0 */    
    public void resetFile() throws IOException {
        file.setLength(0);
    }
    
    public void seek(long pos) throws IOException {
        file.seek(pos);
    }

    public int getFileLength() {
        return (int)f.length();
    }
    
    public String toString() {
        return f.getAbsolutePath();
    }
    
}
