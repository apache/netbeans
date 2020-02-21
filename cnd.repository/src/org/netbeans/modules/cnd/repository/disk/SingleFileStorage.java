/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;

/**
 *
 */
public final class SingleFileStorage implements FileStorage {

    private final File baseDir;

    SingleFileStorage(File baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public boolean open(boolean forWriting) {
        if (forWriting && !baseDir.isDirectory()) {
            baseDir.mkdirs();
        }
        return true;
    }

    @Override
    public boolean hasKey(LayerKey key) throws IOException{
        File file = getFile(key);

        if (!file.exists() || !file.canRead()) {
            return false;
        }
        return true;    
    }
    
    
    @Override
    public ByteBuffer read(LayerKey key) throws IOException {
        File file = getFile(key);

        if (!file.canRead()) {
            return null;
        }

        final long fileSize = file.length();

        if (fileSize >= Integer.MAX_VALUE) {
            throw new InternalError();
        }

        ByteBuffer result = null;
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "r"); // NOI18N
            result = ByteBuffer.allocate((int) fileSize);
                    //SharedReadByteBuffer.get((int) fileSize);
            f.getChannel().read(result);
        } finally {
            if (f != null) {
                f.close();
            }
        }
        return result;
    }

    @Override
    public void write(LayerKey key, ByteBuffer data) throws IOException {
        File file = getFile(key);

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw"); // NOI18N
            raf.getChannel().write(data);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
    }

    @Override
    public void remove(LayerKey key) throws IOException {
        File removedFile = getFile(key);
        if (removedFile == null) {
            return;
        }
        removedFile.delete();
    }

    @Override
    public void close() throws IOException {
    }

    private File getFile(LayerKey key) throws IOException {
        assert key != null;
        String fileName = RepositoryImplUtil.getKeyFileName(key);
        return new File(baseDir, fileName);
    }

    @Override
    public void debugDump(LayerKey key) {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }


    @Override
    public void dump(PrintStream ps) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public void dumpSummary(PrintStream ps) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public int getObjectsCount() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public boolean maintenance(long timeout) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public String toString() {
        return "SnglFileStorage: " + baseDir; // NOI18N
    }
}
