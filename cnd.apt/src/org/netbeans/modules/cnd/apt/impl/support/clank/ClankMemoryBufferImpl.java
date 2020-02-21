/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
