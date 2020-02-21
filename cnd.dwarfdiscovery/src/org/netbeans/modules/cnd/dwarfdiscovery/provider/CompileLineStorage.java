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
