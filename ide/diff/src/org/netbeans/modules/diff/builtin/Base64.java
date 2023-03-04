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
package org.netbeans.modules.diff.builtin;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Base64 utility methods.
 *
 * @author Maros Sandor
 */
class Base64 {

    private static final java.util.Base64.Decoder DECODER = java.util.Base64.getMimeDecoder();

    private Base64() {
    }

    /**
     * Decodes multiple Base64 strings into a single byteArray
     */
    public static byte[] decode(List<String> ls) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (String s : ls) {
            decode(s, bos);
        }
        return bos.toByteArray();
    }

    private static void decode(String s, ByteArrayOutputStream bos) {
        final byte[] decoded = DECODER.decode(s);
        bos.write(decoded, 0, decoded.length);
    }

}
