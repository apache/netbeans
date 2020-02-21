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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.io.InputStream;
import org.clank.support.NativePointer;
import org.clank.support.aliases.char$ptr;
import org.llvm.support.MemoryBuffer;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

/**
 *
 */
final class ClankMemoryBufferImpl extends MemoryBuffer {
    // for remote paths it is url; for locals it is normalized system absolute path
    private final char$ptr fileUrl;

    public static ClankMemoryBufferImpl create(FileObject fo, CharSequence foURL) throws IOException {
        InputStream is = null;
        try {
            if (fo.getSize() >= Integer.MAX_VALUE) {
                throw new IOException("Can't read file: " + fo + ". The file is too long: " + fo.getSize()); // NOI18N
            }
            String text = fo.asText();
            assert foURL.toString().contentEquals(CndFileSystemProvider.toUrl(FSPath.toFSPath(fo))) : foURL + " vs. " + CndFileSystemProvider.toUrl(FSPath.toFSPath(fo));
            return create(foURL, text.toCharArray()); // not optimal at all... but will dye quite soon
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static ClankMemoryBufferImpl create(CharSequence url, char[] chars) throws IOException {
        int nullTermIndex = chars.length;
        byte[] array = new byte[nullTermIndex+1];
        for (int i = 0; i < nullTermIndex; i++) {
            char c = chars[i];
            if (c > 127) {
                // convert all non ascii to spaces
                array[i] = ' ';
            } else {
                array[i] = (byte)c;
            }
        }
        array[nullTermIndex] = 0;
        char$ptr start = NativePointer.create_char$ptr(array);
        char$ptr end = start.$add(nullTermIndex);
        ClankMemoryBufferImpl out = new ClankMemoryBufferImpl(url, start, end, true);
        return out;
    }    

    private ClankMemoryBufferImpl(CharSequence url, char$ptr start, char$ptr end, boolean RequiresNullTerminator) {
        super();
        this.fileUrl = NativePointer.create_char$ptr_utf8(url);
        init(start, end, RequiresNullTerminator);
    }

    @Override
    public char$ptr getBufferIdentifier() {
        return fileUrl;
    }

    @Override
    public BufferKind getBufferKind() {
        return BufferKind.MemoryBuffer_Malloc;
    }
}
