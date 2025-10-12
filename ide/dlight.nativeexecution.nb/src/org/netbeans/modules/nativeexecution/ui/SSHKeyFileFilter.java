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
package org.netbeans.modules.nativeexecution.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.Authentication;

/**
 *
 * @author akrasny
 */
public class SSHKeyFileFilter implements FileFilter {

    private static final Pattern pattern = Pattern.compile("-+ *BEGIN.*PRIVATE.*KEY *-+.*"); // NOI18N
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

        // fast sanity check

        // header contains the name of the tool so we don't know how long it is
        // read until new line but with a limit and avoid buffering the key
        int limit = 40;

        try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()), limit)) {

            StringBuilder head = new StringBuilder(limit);
            int c;
            for (int n = 0; n < limit; n++) {
                c = is.read();
                if (c == -1 || c == '\n' || c == '\r') {
                    break;
                }
                head.append((char) c);  
            }
            if (!pattern.matcher(head).matches()) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }

        return Authentication.isValidSSHKeyFile(file.getAbsolutePath());
    }
}
