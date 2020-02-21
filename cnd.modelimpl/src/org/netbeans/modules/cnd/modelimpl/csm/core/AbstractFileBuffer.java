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

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 *
 */
public abstract class AbstractFileBuffer implements FileBuffer {
    private final CharSequence absPath;
    private final FileSystem fileSystem;
    private Reference<FileObject> fileObject;
    private Reference<Line2Offset> lines = new WeakReference<>(null);
    private final BufferType bufType;

    protected AbstractFileBuffer(FileObject fileObject) {
        this.absPath = FilePathCache.getManager().getString(CndFileUtils.normalizePath(fileObject));
        assert this.absPath != null : "no path for " + fileObject;
        this.fileSystem = getFileSystem(fileObject);
        this.fileObject = new WeakReference<>(fileObject);
        this.bufType = MIMENames.isCppOrCOrFortran(fileObject.getMIMEType()) ? APTFileBuffer.BufferType.START_FILE : APTFileBuffer.BufferType.INCLUDED;
// remote link file objects are just lightweight delegating wrappers, so they have multiple instances
//        if (CndUtils.isDebugMode()) {
//            FileObject fo2 = fileSystem.findResource(absPath.toString());
//            CndUtils.assertTrue(fileObject == fo2, "File objects differ: " + fileObject + " vs " + fo2); //NOI18N
//        }
    }

    private static FileSystem getFileSystem(FileObject fileObject) {
        try {
            return fileObject.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return InvalidFileObjectSupport.getDummyFileSystem();
        }       
    }

    @Override
    public BufferType getType() {
        return bufType;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public CharSequence getUrl() {
        return CndFileSystemProvider.toUrl(fileSystem, absPath);
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }    

    @Override
    public FileObject getFileObject() {
        FileObject result;
        synchronized(this) {
            result = fileObject.get();
            if (result == null) {
                result = CndFileUtils.toFileObject(fileSystem, absPath);
                if (result == null) {
                    result = InvalidFileObjectSupport.getInvalidFileObject(fileSystem, absPath);
                }
                fileObject = new WeakReference<>(result);
            } else {
                if (!result.isValid()) {
                    result = CndFileUtils.toFileObject(fileSystem, absPath);
                    if (result == null) {
                        result = InvalidFileObjectSupport.getInvalidFileObject(fileSystem, absPath);
                    }
                    fileObject = new WeakReference<>(result);
                }
            }
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    // final is important here - see PersistentUtils.writeBuffer/readBuffer
    public final void write(RepositoryDataOutput output) throws IOException {
        assert this.absPath != null;
        PersistentUtils.writeFileSystem(fileSystem, output);        
        output.writeFilePathForFileSystem(fileSystem, absPath);
        output.writeByte((byte) bufType.ordinal());
    }  
    
    protected AbstractFileBuffer(RepositoryDataInput input) throws IOException {
        this.fileSystem = PersistentUtils.readFileSystem(input);
        this.absPath = input.readFilePathForFileSystem(fileSystem);
        assert this.absPath != null;
        fileObject = new WeakReference<>(null);
        bufType = BufferType.values()[input.readByte()];
    }

    @Override
    public int[] getLineColumnByOffset(int offset) throws IOException {
        return getLine2Offset().getLineColumnByOffset(offset);
    }

    @Override
    public int getOffsetByLineColumn(int line, int column) throws IOException {
        return getLine2Offset().getOffsetByLineColumn(line, column);
    }

    @Override
    public int getLineCount() throws IOException {
        return getLine2Offset().getLineColumnByOffset(Integer.MAX_VALUE)[0];
    }

    private Line2Offset getLine2Offset() throws IOException{
        Line2Offset lines2Offset = null;
        Reference<Line2Offset> aLines = lines;
        if (aLines != null) {
            lines2Offset = aLines.get();
        }
        if (lines2Offset == null) {
            lines2Offset = new Line2Offset(getCharBuffer());
            lines = new WeakReference<>(lines2Offset);
        }
        return lines2Offset;
    }

    protected void clearLineCache() {
        Reference<Line2Offset> aLines = lines;
        if (aLines != null) {
            aLines.clear();
        }
    }

    @Override
    public long getCRC() {        
        try {
            Checksum checksum = new Adler32();
            char[] chars = getCharBuffer();
            for (char c : chars) {
                checksum.update(c);
            }
            return checksum.getValue();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return -1; // Adler never returns negative values
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + fileSystem.getDisplayName() + ' ' + absPath; //NOI18N
    }
}
