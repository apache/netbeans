/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author Martin Entlicher
 */
final class DebuggerConnection {
    
    private DebuggerConnection() {}
    
    public static final Charset CHAR_SET = Charset.forName("UTF-8");            // NOI18N
    public static final byte[] EOL = new byte[] { 13, 10 }; // \r\n
    public static final String EOL_STR = "\r\n";                                // NOI18N
    public static final String CONTENT_LENGTH_STR = "Content-Length: ";         // NOI18N
    public static final byte[] CONTENT_LENGTH_BYTES = CONTENT_LENGTH_STR.getBytes(CHAR_SET);
    public static final int BUFFER_SIZE = 4096;
    
    public static int readContentLength(byte[] bytes, int[] from, int to, int[] beginPos) throws IOException {
        int clPos = Utils.indexOf(CONTENT_LENGTH_BYTES, bytes, from[0], to);
        if (clPos < 0) {
            // some garbage to ignore
            return -1;
        }
        beginPos[0] = clPos;
        clPos += CONTENT_LENGTH_BYTES.length;
        int end = Utils.indexOf(EOL, bytes, clPos, to);
        if (end < 0) {
            /*
            Logger.getLogger(NodeJSDebugger.class.getName()).warning("Data inconsistency: no EOL for "+
                             CONTENT_LENGTH_STR+" in "+
                             new String(bytes, CHAR_SET));
            */
            return -1;
        }
        String clStr = new String(bytes, clPos, end - clPos, CHAR_SET);
        int contentLength;
        try {
            contentLength = Integer.parseInt(clStr);
        } catch (NumberFormatException nfex) {
            throw new IOException("Data inconsistency: can not read content length from '"+clStr+"' in "+
                                  new String(bytes, CHAR_SET));
            //return -1;
        }
        from[0] = end + EOL.length;
        return contentLength;
    }

    public static String readTools(byte[] bytes, int[] fromPtr, int n) {
        int end = Utils.indexOf(EOL, bytes, fromPtr[0], n);
        if (end < 0) {
            return null;
        }
        int from = fromPtr[0];
        fromPtr[0] = end + EOL.length;
        return new String(bytes, from, end - from, CHAR_SET);
    }

}
