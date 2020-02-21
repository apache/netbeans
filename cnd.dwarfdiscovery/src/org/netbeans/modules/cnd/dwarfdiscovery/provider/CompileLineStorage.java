/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import org.openide.util.Exceptions;

/**
 *
 */
public class CompileLineStorage {
    private File file;

    public CompileLineStorage() {
        try {
            file = File.createTempFile("lines", ".log"); // NOI18N
            file.deleteOnExit();
        } catch (IOException ex) {
        }
    }

    private static final int MAX_STRING_LENGTH = 65535/3 - 4;
    public synchronized int putCompileLine(String line) {
        if (file != null) {
            RandomAccessFile os= null;
            try {
                os = new RandomAccessFile(file, "rw"); // NOI18N
                int res = (int) os.length();
                os.seek(res);
                try {
                    os.writeUTF(line);
                } catch (UTFDataFormatException ex) {
                    if (line.length() > MAX_STRING_LENGTH) {
                        line = line.substring(0, MAX_STRING_LENGTH)+" ..."; // NOI18N
                        os.writeUTF(line);
                    }
                }
                return res;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return -1;
    }

    public synchronized String getCompileLine(int handler) {
        if (file != null && handler >= 0) {
            RandomAccessFile is= null;
            try {
                is = new RandomAccessFile(file, "r"); // NOI18N
                is.seek(handler);
                return is.readUTF();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return null;
    }
}
