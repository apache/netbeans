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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputReader;
import org.netbeans.api.extexecution.base.input.InputReaders;

/**
 * This class is <i>NotThreadSafe</i>.
 *
 * @author Petr Hejl
 */
public class FileInputReader implements InputReader {

    private static final Logger LOGGER = Logger.getLogger(FileInputReader.class.getName());

    private static final int BUFFER_SIZE = 512;

    private final InputReaders.FileInput.Provider fileProvider;

    private final char[] buffer = new char[BUFFER_SIZE];

    private InputReaders.FileInput currentFile;

    private Reader reader;

    private long fileLength;

    private boolean closed;

    public FileInputReader(InputReaders.FileInput.Provider fileProvider) {
        assert fileProvider != null;

        this.fileProvider = fileProvider;
    }

    @Override
    public int readInput(InputProcessor inputProcessor) {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        int fetched = 0;
        try {
            InputReaders.FileInput file = fileProvider.getFileInput();

            if ((currentFile != file && (currentFile == null || !currentFile.equals(file)))
                    || fileLength > currentFile.getFile().length() || reader == null) {

                if (reader != null) {
                    reader.close();
                }

                currentFile = file;

                if (currentFile != null && currentFile.getFile().exists()
                        && currentFile.getFile().canRead()) {

                    reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(currentFile.getFile()), currentFile.getCharset()));
                }
                if (fileLength > 0) {
                    inputProcessor.reset();
                }
                fileLength = 0;
            }

            if (reader == null) {
                return fetched;
            }

            int size = reader.read(buffer);
            if (size > 0) {
                fileLength += size;
                fetched += size;

                if (inputProcessor != null) {
                    char[] toProcess = new char[size];
                    System.arraycopy(buffer, 0, toProcess, 0, size);
                    inputProcessor.processInput(toProcess);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            // we will try the next loop (if any)
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException iex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }

        return fetched;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

}
