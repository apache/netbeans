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

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileBufferFile extends AbstractFileBuffer {
    
    private volatile Reference<char[]> cachedArray;
    private long crc = Long.MIN_VALUE;
    private final Object lock = new Object();
    private volatile long lastModifiedWhenCachedString;
    private int lineCount = -1;

    public FileBufferFile(FileObject fileObject) {
        super(fileObject);
    }
    
    @Override
    public CharSequence getText() throws IOException {
        char[] buf = doGetChar();
        return new MyCharSequence(buf);
    }
    
    @Override
    public String getText(int start, int end) {
        try {
            char[] buf = doGetChar();
            if( end > buf.length ) {
                new IllegalArgumentException("" + start + ":" + end + " vs. " + buf.length).printStackTrace(System.err); // NOI18N
                end = buf.length;
                if (start > end) {
                    start = end;
                }
            }
            return new String(buf, start, end - start);
        } catch( IOException e ) {
            DiagnosticExceptoins.register(e);
            return ""; // NOI18N
        }
    }

    @Override
    public long getCRC() {
        synchronized (lock) {
            if (crc == Long.MIN_VALUE) {
                crc = super.getCRC();
            }
            return crc;
        }
    }

    private char[] doGetChar() throws IOException {
        synchronized (lock) {
            Reference<char[]> aCachedArray = cachedArray;
            if (aCachedArray != null) {
                char[] res = aCachedArray.get();
                if (res != null) {
                        if (lastModifiedWhenCachedString == lastModified()) {
                            return res;
                        }
                }
            }
            crc = Long.MIN_VALUE;
            FileObject fo = getFileObject();
            long length = fo.getSize();
            if (length > Integer.MAX_VALUE) {
                new IllegalArgumentException("File is too large: " + fo.getPath()).printStackTrace(System.err); // NOI18N
            }
            if (length == 0) {
                return new char[0];
            }
            length++;
            PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(CndFileUtils.READ_FILE_PERFORMANCE_EVENT, fo);
            char[] readChars = new char[(int)length];
            InputStream is = getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, getEncoding()));
            try {
                String line;
                int position = 0;
                int lines = 0;
                while((line = reader.readLine())!= null) {
                    lines++;
                    for(int i = 0; i < line.length(); i++) {
                        if (length == position) {
                            length = length*2;
                            char[] copyChars = new char[(int)length];
                            System.arraycopy(readChars, 0, copyChars, 0, position);
                            readChars = copyChars;
                        }
                        readChars[position++] = line.charAt(i);
                    }
                    if (length == position) {
                        length = length*2;
                        char[] copyChars = new char[(int)length];
                        System.arraycopy(readChars, 0, copyChars, 0, position);
                        readChars = copyChars;
                    }
                    readChars[position++] = '\n'; // NOI18N
                }
                lineCount = lines;
                // no need to copy if the same size
                // TODO: can we also skip case readChars.length = position + 1 due to last '\n'?
                if (readChars.length > position) {
                    char[] copyChars = new char[position];
                    System.arraycopy(readChars, 0, copyChars, 0, position);
                    readChars = copyChars;
                }
            } finally {
                reader.close();
                is.close();
            }
            performanceEvent.log(readChars.length, lineCount);
            if (CndTraceFlags.WEAK_REFS_HOLDERS || MIMENames.isCppOrCOrFortran(fo.getMIMEType())) {
                cachedArray = new WeakReference<>(readChars);
            } else {
                cachedArray = new SoftReference<>(readChars);
            }
            lastModifiedWhenCachedString = lastModified();
            return readChars;
        }
    }

    protected final String getEncoding() {
        FileObject fo = getFileObject();
        Charset cs = null;
        if (fo != null && fo.isValid()) {
            cs = FileEncodingQuery.getEncoding(fo);
        }
        if (cs == null) {
            cs = FileEncodingQuery.getDefaultEncoding();
        }
        return cs.name();
    }

    @Override
    public int getLineCount() throws IOException {
        if (lineCount == -1) {
            doGetChar();
        }
        return lineCount;
    }

    private InputStream getInputStream() throws IOException {
        InputStream is;
        FileObject fo = getFileObject();
        if (fo != null && fo.isValid()) {
            is = fo.getInputStream();
        } else {
            throw new FileNotFoundException("Null file object for " + this.getAbsolutePath()); // NOI18N
        }
        return new BufferedInputStream(is, TraceFlags.BUF_SIZE);
    }
    
    @Override
    public boolean isFileBased() {
        return true;
    }
    
    @Override
    public long lastModified() {
	return getFileObject().lastModified().getTime();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public FileBufferFile(RepositoryDataInput input) throws IOException {
        super(input);
    }

    @Override
    public char[] getCharBuffer() throws IOException {
        return doGetChar();
    }

    public static final class MyCharSequence implements CharSequence {
        private final char[] buf;
        private final int start;
        private final int end;

        public MyCharSequence(char[] buf) {
            this(buf, 0, buf.length);
        }

        MyCharSequence(char[] buf, int start, int end) {
            this.buf = buf;
            this.start = start;
            this.end = end;
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            return buf[start+index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new MyCharSequence(buf, this.start + start, this.start + end);
        }

        @Override
        public String toString() {
            return new String(buf, start, end - start);
        }
    }
}
