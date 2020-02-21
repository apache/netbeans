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
package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import org.netbeans.modules.cnd.repository.Logger;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 */
public final class BufferedRWAccess implements FileRWAccess {

    private static final int BUFFER_SIZE = Stats.bufSize > 0 ? Stats.bufSize : 1 * 1024 * 1024;
    private final boolean writable;
    private final RandomAccessFile randomAccessFile;
    private final FileChannel channel;
    private final String path;
    private final ByteBuffer writeBuffer;
    private final Object lock = new Object();
    private long actualSize;
    private long virtualSize;
    private static final java.util.logging.Logger log = Logger.getInstance();

    public BufferedRWAccess(final File file, final boolean writable) throws IOException {
        this.path = file.getAbsolutePath();
        this.writable = writable;

        File parent = new File(file.getParent());
        if (writable && !parent.exists()) {
            parent.mkdirs();
        }

        randomAccessFile = new RandomAccessFile(file, writable ? "rw" : "r"); // NOI18N
        channel = randomAccessFile.getChannel();
        virtualSize = actualSize = channel.size();
        writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    }

    @Override
    public ByteBuffer readData(long offset, int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        long fileSize = virtualSize - BUFFER_SIZE;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "will read from the file {0} starting from offset {1} and size {2}"
                , new Object[]{path, offset, size});
        }
        
        if (offset + size <= fileSize) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "will read from the channnel, data are already on the disk");
            }
            channel.read(buffer, offset);
        } else {
            synchronized (lock) {
                fileSize = actualSize;
                if (writable && offset + size > fileSize) {
                    // System.out.println("DD: flushWriteBuffer [offset = " + offset + " ; filesize = " + fileSize + "] when looks like this could be avoided... ");
                    flushWriteBuffer();
                }
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "flushed on the disk from the buffer, now will read data from the disk");
                }
                channel.read(buffer, offset);
            }
        }
        buffer.flip();
        return buffer;
    }

    /**
     * @param data
     * @return offset in the file the data was written from
     *
     * @throws IOException
     */
    @Override
    public long appendData(final ByteBuffer data) throws IOException {
        long position;
        int dataSize = data.limit();
        synchronized (lock) {
            position = channel.size() + writeBuffer.position();
            while (data.hasRemaining()) {
                if (!writeBuffer.hasRemaining()) {
                    // System.out.println("DD: flushWriteBuffer ... ");
                    flushWriteBuffer();
                }
                writeBuffer.put(data.get());
            }
            virtualSize += dataSize;
        }
        return position;
    }

    @Override
    public long size() throws IOException {
        return virtualSize;
    }

    @Override
    public void truncate(long size) throws IOException {
        synchronized (lock) {
            writeBuffer.clear();
            channel.truncate(size);
            actualSize = virtualSize = size;
        }
    }

    @Override
    public long move(FileRWAccess from, long offset, int size) throws IOException {
        if (!(from instanceof BufferedRWAccess)) {
            throw new IllegalArgumentException("Illegal class to move from: " + from.getClass().getName()); // NOI18N
        }
        BufferedRWAccess from2 = (BufferedRWAccess) from;
        ByteBuffer buffer = from2.readData(offset, size);
        return appendData(buffer);
    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (writable) {
                flushWriteBuffer();
            }

            assert actualSize == virtualSize;
            channel.close();
        }
    }

    @Override
    public boolean isValid() throws IOException {
        return randomAccessFile.getFD().valid();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + path + ']'; // NOI18N
    }

    private void flushWriteBuffer() throws IOException {
        writeBuffer.flip();
        actualSize += channel.write(writeBuffer, actualSize);
        writeBuffer.clear();
    }
}
