/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.support;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class Encrypter {

    private Encrypter() {
    }

    public static long getFileChecksum(String fname) {
        File file = new File(fname);
        if (file == null || !file.exists()) {
            return -1;
        }

        Checksum checksum = new CRC32();

        BufferedInputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (is == null) {
            return -1;
        }

        byte[] bytes = new byte[1024];
        int len = 0;

        try {
            while ((len = is.read(bytes)) >= 0) {
                checksum.update(bytes, 0, len);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return checksum.getValue();
    }

    public static boolean checkCRC32(String fname, long checksum) {
        return checksum == getFileChecksum(fname);
    }

}
