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
package org.netbeans.modules.cnd.api.xml;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;

/**
 * Taken from LineSeparatorConversion.InitialSeparatorReader
 * 
 */
public final class LineSeparatorDetector {

    private static final char LS = 0x2028; //Unicode line separator (0x2028).
    private static final char PS = 0x2029; // Unicode paragraph separator (0x2029).
    private static final String LS_LS = String.valueOf(LS);
    private static final String LS_PS = String.valueOf(PS);

    private static final int AFTER_CR_STATUS = -1;
    private static final int INITIAL_STATUS = 0;
    private static final int CR_SEPARATOR = 1;
    private static final int LF_SEPARATOR = 2;
    private static final int CRLF_SEPARATOR = 3;
    private static final int LS_SEPARATOR = 4;
    private static final int PS_SEPARATOR = 5;
    
    private int status = INITIAL_STATUS;
    private String defaultSeparator = System.getProperty("line.separator"); // NOI18N

    public LineSeparatorDetector(FileObject fo, FileObject projectDir) {
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(projectDir);
        if (executionEnvironment.isRemote()) {
            defaultSeparator = "\n";// NOI18N
        }
        if (fo != null && fo.isValid()) {
            InputStream stream = null;
            try {
                stream = fo.getInputStream();
                int c;
                while((c = stream.read())!= -1) {
                    resolveSeparator((char) c);
                    if (isSeparatorResolved()) {
                        break;
                    }
                }
            } catch (IOException ex) {
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    public String getInitialSeparator() {
        String separator;
        switch (status) {
            case CR_SEPARATOR:
                separator = "\r"; // NOI18N
                break;

            case LF_SEPARATOR:
                separator = "\n"; // NOI18N
                break;

            case CRLF_SEPARATOR:
                separator = "\r\n"; // NOI18N
                break;

            case AFTER_CR_STATUS: // '\r' was last char
                separator = "\r"; // NOI18N
                break;

            case LS_SEPARATOR:
                separator = LS_LS;
                break;

            case PS_SEPARATOR:
                separator = LS_PS;
                break;

            default:
                separator = defaultSeparator;
                break;
        }

        return separator;
    }

    private void resolveSeparator(char ch) {
        switch (status) {
            case INITIAL_STATUS:
                switch (ch) {
                    case '\r':
                        status = AFTER_CR_STATUS;
                        break;
                    case '\n':
                        status = LF_SEPARATOR;
                        break;
                    case LS:
                        status = LS_SEPARATOR;
                        break;
                    case PS:
                        status = PS_SEPARATOR;
                        break;
                }
                break;

            case AFTER_CR_STATUS:
                switch (ch) {
                    case '\n':
                        status = CRLF_SEPARATOR;
                        break;
                    default:
                        status = CR_SEPARATOR;
                        break;
                }
                break;

            default:
                switch (ch) {
                    case '\r':
                        status = AFTER_CR_STATUS;
                        break;
                    case '\n':
                        status = LF_SEPARATOR;
                        break;
                }
                break;
        }
    }

    private boolean isSeparatorResolved() {
        return (status > 0);
    }
}
