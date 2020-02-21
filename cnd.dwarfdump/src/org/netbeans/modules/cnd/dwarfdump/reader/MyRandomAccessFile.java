/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;

/**
 *
 */
public final class MyRandomAccessFile extends RandomAccessFile {
    
    //private static final int MAX_BUF_SIZE = Integer.MAX_VALUE;
    private static final int MAX_BUF_SIZE = 1*1024*1024;

    private MappedByteBuffer buffer;
    private long bufferShift;
    private long bufferSize;
    private final FileChannel channel;
    private boolean doSeek = false;
    private long postponedSeek = 0;
    private final String fileName;

    public MyRandomAccessFile(String fileName) throws IOException {
        super(fileName, "r"); // NOI18N
        this.fileName = fileName;
        channel = getChannel();
        bufferSize = Math.min(channel.size(), MAX_BUF_SIZE - 1);
        bufferShift = 0;
        try {
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, bufferShift, bufferSize);
        } catch (IOException ex) {
            channel.close();
            close();
            throw ex;
        }
    }

    @Override
    public int read() throws IOException {
        if (doSeek) {
            doSeek = false;
            _seek(postponedSeek);
        }
        if (buffer.remaining() == 0) {
            if(bufferShift + bufferSize < channel.size()) {
                long position = bufferShift + bufferSize;
                bufferShift = bufferShift + MAX_BUF_SIZE/2;
                bufferSize = Math.min(channel.size() - bufferShift, MAX_BUF_SIZE - 1);
                ByteOrder order = buffer.order();
                bufferCleaner();
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, bufferShift, bufferSize);
                buffer.order(order);
                buffer.position((int)(position-bufferShift));
                return read();
            }
            return -1;
        } else {
            return 0xff & buffer.get();
        }
    }

    public MappedByteBuffer getBuffer() throws IOException {
        if (doSeek) {
            doSeek = false;
            _seek(postponedSeek);
        }
        return buffer;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (doSeek) {
            doSeek = false;
            _seek(postponedSeek);
        }
        if (buffer.remaining() >= len) {
            buffer.get(b, off, len);
            return len;
        } else {
            long position = getFilePointer();
            if (position >= channel.size()) {
                return -1;
            }
            bufferShift = Math.max(position - MAX_BUF_SIZE/2, 0L);
            bufferSize = Math.min(channel.size() - bufferShift, MAX_BUF_SIZE - 1);
            ByteOrder order = buffer.order();
            bufferCleaner();
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, bufferShift, bufferSize);
            buffer.order(order);
            buffer.position((int)(position-bufferShift));
            if (len <= buffer.remaining()) {
                buffer.get(b, off, len);
                return len;
            } else {
                len = buffer.remaining();
                buffer.get(b, off, len);
                return len;
            }
        }
    }

    public int remaining() throws IOException{
        if (doSeek) {
            doSeek = false;
            _seek(postponedSeek);
        }
        return buffer.remaining();
    }
    
    @Override
    public long getFilePointer() throws IOException {
        if (doSeek) {
            return postponedSeek;
        }
        return buffer.position() + bufferShift;
    }
    
    @Override
    public void seek(long pos) throws IOException {
        if (pos < 0 || pos > channel.size()) {
            throw new IOException("Wrong position "+pos); // NOI18N
        }
        doSeek = true;
        postponedSeek = pos;
    }

    private void _seek(long pos) throws IOException {
        long filePointer = getFilePointer();
        if (pos == filePointer) {
            return;
        }
        if (pos >= bufferShift && pos < bufferShift + bufferSize) {
            buffer.position((int) (pos-bufferShift));
        } else {
            bufferShift = Math.max(pos - MAX_BUF_SIZE/2, 0);
            bufferSize = Math.min(channel.size() - bufferShift, MAX_BUF_SIZE - 1);
            ByteOrder order = buffer.order();
            bufferCleaner();
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, bufferShift, bufferSize);
            buffer.order(order);
            buffer.position((int)(pos - bufferShift));
        }
    }

    private void bufferCleaner() {
        buffer.clear();
        if (!buffer.isDirect()) {
            return;
        }
        // Work around of clear memory
        // See also JDK-4724038 : (fs) Add unmap method to MappedByteBuffer
        // http://bugs.java.com/view_bug.do?bug_id=4724038
        try {
            Method cleanerMethod = buffer.getClass().getMethod("cleaner"); //NOI18N
            cleanerMethod.setAccessible(true);
            // sun.misc.Cleaner in JDK8
            // java.lang.ref.Cleaner in JDK9
            Object cleaner = cleanerMethod.invoke(buffer);
            Method cleanMethod = cleaner.getClass().getMethod("clean"); //NOI18N
            cleanMethod.setAccessible(true);
            cleanMethod.invoke(cleaner);
        } catch (Throwable e) {
            // do nothing
            //e.printStackTrace(System.err);
        }
    }

    public void dispose() {
        try {
            bufferCleaner();
            channel.close();
            close();
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "Cannot close file "+fileName, ex);
        }
    }
}
 