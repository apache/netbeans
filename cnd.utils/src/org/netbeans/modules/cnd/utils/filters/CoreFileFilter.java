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
