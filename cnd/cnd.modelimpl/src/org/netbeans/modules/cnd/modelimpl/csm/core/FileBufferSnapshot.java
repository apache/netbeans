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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class FileBufferSnapshot implements APTFileBuffer {
    
    private final CharSequence absPath;
    private final FileSystem fileSystem;
    private final char[] buffer;
    private final long timeStamp;
    private Reference<Line2Offset> lines;
    private final APTFileBuffer.BufferType bufType;
    
    public FileBufferSnapshot(FileSystem fileSystem, CharSequence absPath, APTFileBuffer.BufferType bufType, char[] buffer, int[] linesCache, long timeStamp) {
        this.absPath = absPath;
        this.fileSystem = fileSystem;
        this.buffer = buffer;
        this.timeStamp = timeStamp;
        if (linesCache != null) {
             lines = new WeakReference<>(new Line2Offset(buffer, linesCache));
        } else {
             lines = new WeakReference<>(null);
        }
        this.bufType = bufType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public APTFileBuffer.BufferType getType() {
        return bufType;
    }
    
    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public char[] getCharBuffer() throws IOException {
        return buffer;
    }

    public int[] getLineColumnByOffset(int offset) throws IOException {
        return getLine2Offset().getLineColumnByOffset(offset);
    }

    public int getOffsetByLineColumn(int line, int column) throws IOException {
        return getLine2Offset().getOffsetByLineColumn(line, column);
    }

    private Line2Offset getLine2Offset() throws IOException{
        Line2Offset lines2Offset = null;
        Reference<Line2Offset> aLines = lines;
        if (aLines != null) {
            lines2Offset = aLines.get();
        }
        if (lines2Offset == null) {
            lines2Offset = new Line2Offset(getText());
            lines = new WeakReference<>(lines2Offset);
        }
        return lines2Offset;
    }
    
    public String getText(int start, int end) {
        return new String(buffer, start, end - start);
    }
    
    public String getText() {
        return new String(buffer);
    }
}
