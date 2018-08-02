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
package org.netbeans.modules.docker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
//@NotThreadSafe
public class Demuxer implements StreamItem.Fetcher {

    private static final Logger LOGGER = Logger.getLogger(Demuxer.class.getName());

    private final InputStream is;

    private byte[] buffer = new byte[8];

    private byte[] content = new byte[256];

    public Demuxer(InputStream is) {
        this.is = is;
    }

    @Override
    public StreamItem fetch() {
        try {
            int sum = 0;
            do {
                int read = is.read(buffer, sum, buffer.length - sum);
                if (read < 0) {
                    return null;
                }
                sum += read;
            } while (sum < 8);
            // now we have 8 bytes
            assert buffer.length == 8;

            boolean error;
            int size = ByteBuffer.wrap(buffer).getInt(4);
            if (buffer[0] == 0 || buffer[0] == 1) {
                error = false;
            } else if (buffer[0] == 2) {
                error = true;
            } else {
                throw new IOException("Unparsable stream " + buffer[0]);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
            sum = 0;
            do {
                int read = is.read(content, 0, Math.min(size - sum, content.length));
                if (read < 0) {
                    return null;
                }
                bos.write(content, 0, read);
                sum += read;
            } while (sum < size);
            return new StreamItem(ByteBuffer.wrap(bos.toByteArray()), error);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }
}
