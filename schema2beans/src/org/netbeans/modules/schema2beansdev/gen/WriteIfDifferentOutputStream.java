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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.schema2beansdev.gen;

import java.util.*;
import java.io.*;

public class WriteIfDifferentOutputStream extends OutputStream {
    private RandomAccessFile randFile;
    private long initialRandFileLength;
    private boolean justWrite = false;

    public WriteIfDifferentOutputStream(RandomAccessFile randFile) throws IOException {
        this.randFile = randFile;
        this.initialRandFileLength = randFile.length();
    }

    public WriteIfDifferentOutputStream(String filename) throws IOException {
        this(new RandomAccessFile(filename, "rw"));	// NOI18N
    }

    public WriteIfDifferentOutputStream(File file) throws IOException {
        this(new RandomAccessFile(file, "rw"));	// NOI18N
    }

    public void write(int b) throws IOException {
        if (justWrite) {
            randFile.write(b);
            return;
        }
        long fp = randFile.getFilePointer();
        if (fp + 1 > initialRandFileLength) {
            justWrite = true;
            randFile.write(b);
            return;
        }
        int fromFile = randFile.read();
        if (fromFile != b) {
            //System.out.println("different: fromFile="+fromFile+" b="+b);
            randFile.seek(fp);
            randFile.write(b);
            justWrite = true;
            return;
        }
    }

    private byte[] writeBuf;
    private int lastLen = -1;
    public void write(byte[] b, int off, int len) throws IOException {
        if (justWrite) {
            randFile.write(b, off, len);
            return;
        }
        long fp = randFile.getFilePointer();
        if (fp + len > initialRandFileLength) {
            justWrite = true;
            randFile.write(b, off, len);
            return;
        }
        if (len > lastLen) {
            // Allocate a new buffer only if the last one wasn't big enough.
            writeBuf = new byte[len];
            lastLen = len;
        }
        randFile.read(writeBuf, 0, len);
        for (int i = off, j = 0; j < len; ++i, ++j) {
            if (writeBuf[j] != b[i]) {
                //System.out.println("different: i="+i+" j="+j);
                randFile.seek(fp);
                randFile.write(b, off, len);
                justWrite = true;
                return;
            }
        }
    }

    /*
      public void flush() throws IOException {
      randFile.flush();
      }
    */

    /**
     * At this point, have we changed the file?
     * It's possible to not change a file until close() is called.
     */
    public boolean isChanged() {
        return justWrite;
    }

    public void close() throws IOException {
        // Truncate it.
        //System.out.println("truncating to "+randFile.getFilePointer());
        if (randFile.getFilePointer() < randFile.length()) {
            justWrite = true;
            randFile.setLength(randFile.getFilePointer());
        }
        randFile.close();
    }
}
