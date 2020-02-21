/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
