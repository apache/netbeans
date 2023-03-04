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
package org.netbeans.modules.nativeexecution.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.openide.util.Exceptions;

/**
 *
 * @author akrasny
 */
public class SSHKeyFileFilter implements FileFilter {

    private static final Pattern p = Pattern.compile("-+ *BEGIN.*PRIVATE.*KEY *-+.*"); // NOI18N
    private static final Charset cs = StandardCharsets.US_ASCII;
    private static final SSHKeyFileFilter instance = new SSHKeyFileFilter();

    private SSHKeyFileFilter() {
    }

    public static SSHKeyFileFilter getInstance() {
        return instance;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        if (!file.canRead()) {
            return false;
        }

        final long flen = file.length();

        // Will not consider too small or too big files ...
        if (flen < 100 || flen > 10000) {
            return false;
        }

        FileInputStream fis = null;
        byte[] buffer = new byte[30];

        // fast sanity check
        try {
            fis = new FileInputStream(file);
            fis.read(buffer, 0, 30);

            Matcher m = p.matcher(new String(buffer, 0, 30, cs));
            if (!m.matches()) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return Authentication.isValidSSHKeyFile(file.getAbsolutePath());
    }
}
