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

package org.netbeans.modules.extexecution.base.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputReader;

/**
 * This class is <i>NotThreadSafe</i>.
 *
 * @author Petr.Hejl
 */
public class DefaultInputReader implements InputReader {

    private static final Logger LOGGER = Logger.getLogger(DefaultInputReader.class.getName());

    private static final int BUFFER_SIZE = 512;

    private final Reader reader;

    private final char[] buffer;

    private final boolean greedy;

    private boolean closed;

    public DefaultInputReader(Reader reader, boolean greedy) {
        assert reader != null;

        this.reader = reader;
        this.greedy = greedy;
        this.buffer = new char[greedy ? BUFFER_SIZE * 2 : BUFFER_SIZE];
    }

    @Override
    public int readInput(InputProcessor inputProcessor) throws IOException {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        if (!reader.ready()) {
            return 0;
        }

        int fetched = 0;
        // TODO optimization possible
        StringBuilder builder = new StringBuilder();
        do {
            int size = reader.read(buffer);
            if (size > 0) {
                builder.append(buffer, 0, size);
                fetched += size;
            }
        } while (reader.ready() && greedy);

        if (inputProcessor != null && fetched > 0) {
            inputProcessor.processInput(builder.toString().toCharArray());
        }

        return fetched;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        reader.close();
        LOGGER.log(Level.FINEST, "Reader closed");
    }

}
