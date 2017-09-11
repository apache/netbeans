/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uihandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This output stream assures, that data written to it by a single write() call,
 * will either be all written into the file, or none of them will be.
 * 
 * @author Martin Entlicher
 */
class DataConsistentFileOutputStream extends BufferedOutputStream {
    
    private FileOutputStream fos;
    private long lastConsistentLength;
    
    public DataConsistentFileOutputStream(File file, boolean append) throws FileNotFoundException {
        this(file, append, new FileOutputStream[] { null });
    }
    
    private DataConsistentFileOutputStream(File file, boolean append, FileOutputStream[] fosPtr) throws FileNotFoundException {
        super(fosPtr[0] = new FileOutputStream(file, append));
        this.fos = fosPtr[0];
        if (append) {
            lastConsistentLength = file.length();
        } else {
            lastConsistentLength = 0L;
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        lastConsistentLength++;
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        try {
            super.write(b, off, len);
            lastConsistentLength = lastConsistentLength + len;
        } catch (IOException ioex) {
            truncateFileToConsistentSize(fos, lastConsistentLength);
            throw ioex;
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        super.flush();
        FileChannel fch = fos.getChannel();
        fch.force(true);
    }
    
    static void truncateFileToConsistentSize(FileOutputStream fos, long size) {
        try {
            FileChannel fch = fos.getChannel();
            fch.truncate(size);
            fch.force(true);
        } catch (IOException ex) {
            Logger.getLogger(DataConsistentFileOutputStream.class.getName()).log(
                    Level.INFO,
                    "Not able to truncate file to the data consistent size of "+size+" bytes.",
                    ex);
        }
    }

}
