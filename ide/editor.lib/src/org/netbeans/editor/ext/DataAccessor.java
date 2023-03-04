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

import java.io.IOException;

/**
 *   DataAccessor for Code Completion DB files
 *
 *   @author  Martin Roskanin
 */
public interface DataAccessor {

    /** Opens DataAccessor file resource
     *  @param requestWrite if true, file is opened for read/write operation.
     */
    public void open(boolean requestWrite) throws IOException;

    /** Closes DataAccessor file resource */
    public void close() throws IOException;

    /** Reads up to len bytes of data from this file resource into an array of bytes.
     * @param buffer the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     */
    public void read(byte buffer[], int off, int len) throws IOException;
    
    /** Appends exactly <code>len</code> bytes, starting at <code>off</code> of the buffer pointer
     *  to the end of file resource.
     *  @param  buffer the buffer from which the data is appended.
     *  @param  off    the start offset of the data in the buffer.
     *  @param  len    the number of bytes to append.
     *  @return        the actual file offset.
     */
    public void append(byte buffer[], int off, int len) throws IOException;
    
    /**
     * Returns the current offset in this file.
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     */
    public long getFilePointer() throws IOException;
    
    /** Clears the file and sets the offset to 0 */
    public void resetFile() throws IOException;
    
    /**
     * Sets the file-pointer offset, measured from the beginning of this
     * file, at which the next read or write occurs.
     */
    public void seek(long pos) throws IOException;
    
    public int getFileLength();
}

