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
package org.netbeans.modules.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Bas64 encode utility class.
 *
 * @author Maros Sandor
 * @deprecated prefer java.util.Base64 instead
 */
@Deprecated
public class Base64Encoder {

    private static final Base64.Encoder MIME_ENCODER = Base64.getMimeEncoder(
            60,
            System.getProperty("line.separator").getBytes(StandardCharsets.ISO_8859_1)
    );

    private Base64Encoder() {
    }

    /**
     *
     * @deprecated use {@link java.util.Base64#getEncoder()}.encode(data)
     * instead.
     */
    @Deprecated
    public static String encode(byte[] data) {
        return encode(data, false);
    }

    /**
     * @deprecated use
     * {@link java.util.Base64#getMimeEncoder(int, byte[])}.encode(s) instead.
     */
    public static String encode(byte[] data, boolean useNewlines) {
        if (useNewlines) {
            return MIME_ENCODER.encodeToString(data);
        } else {
            return Base64.getEncoder().encodeToString(data);
        }
    }

    /**
     *
     * @deprecated use {@link java.util.Base64#getMimeDecoder()}.decode(s)
     * instead.
     */
    @Deprecated
    public static byte[] decode(String s) {
        return Base64.getMimeDecoder().decode(s);
    }

}
