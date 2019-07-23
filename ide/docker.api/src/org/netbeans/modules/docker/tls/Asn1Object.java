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
package org.netbeans.modules.docker.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

/*
 * The first byte in ASN.1
 *
 *-------------------------------------------------
 *|Bit 8|Bit 7|Bit 6|Bit 5|Bit 4|Bit 3|Bit 2|Bit 1|
 *-------------------------------------------------
 *|  Class    | CF  |     +      Type             |
 *-------------------------------------------------
 */
public class Asn1Object {

    private static final int FLAG_CONSTRUCTED = 0x20;

    private static final int TYPE_INTEGER = 0x02;

    private static final int TYPE_MASK = 0x1F;

    private final int tag;

    private final int type;

    private final byte[] value;

    public Asn1Object(int tag, byte[] value) {
        this.tag = tag;
        this.type = tag & TYPE_MASK;
        this.value = value;
    }

    public DerParser read() throws IOException {
        if ((tag & FLAG_CONSTRUCTED) == 0) {
            throw new IOException("This object is not a constructed value");
        }
        return new DerParser(new ByteArrayInputStream(value));
    }

    public BigInteger getBigInteger() throws IOException {
        if (type != TYPE_INTEGER) {
            throw new IOException("This object does not represent integer: " + type);
        }
        return new BigInteger(value);
    }
}
