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
package org.netbeans.modules.cnd.utils.filters;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class CoreFileFilter extends FileAndFileObjectFilter {

    // SORTED (as it is used in Arrays.binarySearch()) array of all suitable
    // mime types
    private static final String[] MIME_TYPES = new String[]{MIMENames.ELF_CORE_MIME_TYPE};
    private static final CoreFileFilter instance = new CoreFileFilter();

    public static CoreFileFilter getInstance() {
        return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(CoreFileFilter.class, "FILECHOOSER_CORE_FILEFILTER"); // NOI18N
    }

    @Override
    protected boolean mimeAccept(File f) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }

        FileObject fo = CndFileSystemProvider.toFileObject(f);

        if (fo != null) {
            return mimeAccept(fo);
        }

        // Fall back... Normally should not be called.
        return checkElfCoreHeader(f);
    }

    @Override
    protected boolean mimeAccept(FileObject fo) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }

        return Arrays.binarySearch(MIME_TYPES, fo.getMIMEType(MIME_TYPES)) >= 0;
    }

    /**
     * Check if this file's header represents a core file in ELF format.
     *
     * ELF format is used in modern Linux, System V, Solaris, and BSD systems.
     *
     * magic numbers are:
     *
     * 0     string          \177ELF         ELF
     * 4     byte            1               32-bit
     * 5     byte            1               LSB
     * 5     byte            2               MSB
     * ...
     * 16    short           4               core file
     */
    private static final int ELF_CORE_HEADER_SIZE = 18;
    private static final byte[] elf_ms = new byte[]{0x7f, 'E', 'L', 'F'};

    private boolean checkElfCoreHeader(final File f) {
        if (!f.isFile()) {
            return false;
        }

        FileChannel channel = null;
        byte[] ms = new byte[4];

        try {
            channel = new RandomAccessFile(f, "r").getChannel(); // NOI18N
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, ELF_CORE_HEADER_SIZE).load();
            buffer.get(ms, 0, 4);
            if (Arrays.equals(elf_ms, ms)) {
                switch (buffer.get(5)) {
                    case 1:
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        break;
                    case 2:
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        break;
                    default:
                        return false;
                }

                return buffer.getShort(16) == 4;
            }
        } catch (IOException ex) {
            // ignore
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return false;
    }

    @Override
    protected String[] getSuffixes() {
        return new String[]{};
    }
}
