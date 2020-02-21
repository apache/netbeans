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
