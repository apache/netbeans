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

package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Martin Entlicher
 */
final class DebuggerConnection {
    
    private DebuggerConnection() {}
    
    public static final Charset CHAR_SET = StandardCharsets.UTF_8;
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
