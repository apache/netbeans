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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 *
 * @author Petr Hejl
 */
public class DerParser {

    private final InputStream is;

    public DerParser(InputStream is) {
        this.is = is;
    }

    public Asn1Object read() throws IOException {
        int tag = is.read();

        if (tag < 0) {
            throw new IOException("No more data to read");
        }
        int length = readLength();
        return new Asn1Object(tag, readValue(length));
    }

    private int readLength() throws IOException {
        int read = is.read();
        if (read < 0) {
            throw new IOException("No more data to read");
        }
        // sigle byte
        if ((read & ~0x7F) == 0) {
            return read;
        }

        // multibyte
        int num = read & 0x7F;
        if (read >= 0xFF || num > 4) {
            throw new IOException("Length too big to be used");
        }
        return new BigInteger(1, readValue(num)).intValue();
    }

    private byte[] readValue(int length) throws IOException {
        byte[] value = new byte[length];
        int count = 0;
        while (count < length) {
            int real = is.read(value, count, length - count);
            if (real < 0) {
                throw new IOException("Can't read the requested value");
            }
            count += real;
        }
        return value;
    }

}
