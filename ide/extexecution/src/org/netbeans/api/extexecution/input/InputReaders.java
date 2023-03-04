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

package org.netbeans.api.extexecution.input;

import org.netbeans.modules.extexecution.input.BaseInputProcessor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.openide.util.Parameters;

/**
 * Factory methods for {@link InputReader} classes.
 *
 * @author Petr Hejl
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputReaders}
 */
@Deprecated
public final class InputReaders {

    private InputReaders() {
        super();
    }

    /**
     * Returns the input reader backed by the given reader.
     * <p>
     * The client should not use the reader passed as argument anymore. When
     * the returned input reader is closed reader passed as argument is closed
     * respectively.
     * <p>
     * Returned reader will never call reset on {@link InputProcessor} while
     * reading.
     * <p>
     * Returned reader is <i>not thread safe</i> so it can't be used in
     * multiple instances of {@link InputReaderTask}.
     *
     * @param reader real source of the data
     * @return input reader backed by the given reader
     */
    @NonNull
    public static InputReader forReader(@NonNull Reader reader) {
        final org.netbeans.api.extexecution.base.input.InputReader delegate = org.netbeans.api.extexecution.base.input.InputReaders.forReader(reader);
        return new InputReader() {

            @Override
            public int readInput(org.netbeans.api.extexecution.input.InputProcessor processor) throws IOException {
                return delegate.readInput(processor == null ? null : new BaseInputProcessor(processor));
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    /**
     * Returns the input reader backed by the given stream. To convert read
     * bytes to characters specified charset is used.
     * <p>
     * The client should not use the stream passed as argument anymore. When
     * the returned input reader is closed stream is closed respectively.
     * <p>
     * Returned reader will never call reset on {@link InputProcessor} while
     * reading.
     * <p>
     * Returned reader is <i>not thread safe</i> so it can't be used in
     * multiple instances of {@link InputReaderTask}.
     *
     * @param stream real source of the data
     * @param charset bytes to characters conversion charset
     * @return input reader backed by the given stream
     */
    @NonNull
    public static InputReader forStream(@NonNull InputStream stream, @NonNull Charset charset) {
        Parameters.notNull("stream", stream);

        return forReader(new InputStreamReader(stream, charset));
    }

    /**
     * Returns the input reader for the given file. To convert read bytes
     * to characters specified charset is used.
     * <p>
     * Returned reader will never call reset on {@link InputProcessor} while
     * reading.
     * <p>
     * Returned reader is <i>not thread safe</i> so it can't be used in
     * multiple instances of {@link InputReaderTask}.
     *
     * @param file file to read from
     * @param charset bytes to characters conversion charset
     * @return input reader for the given file
     */
    @NonNull
    public static InputReader forFile(@NonNull File file, @NonNull Charset charset) {
        Parameters.notNull("file", file);
        Parameters.notNull("charset", charset);

        final FileInput fileInput = new FileInput(file, charset);
        return forFileInputProvider(new FileInput.Provider() {

            public FileInput getFileInput() {
                return fileInput;
            }
        });
    }

    /**
     * Returns the input reader reading data from the given provider.
     * <p>
     * This means that the actual file (and the corresponding charset) used
     * can change during the processing. This is specifically useful for
     * rotating log files.
     * <p>
     * Before each read cycle reader invokes {@link FileInput.Provider#getFileInput()}
     * to determine the actual file to read.
     * <p>
     * When processing the input {@link InputProcessor#reset()} is called on
     * each file change (when provided file input differs from the previous one).
     * <p>
     * Returned reader is <i>not thread safe</i> so it can't be used in
     * multiple instances of {@link InputReaderTask}.
     *
     * @param fileProvider provider used to get the file to process
     * @return input reader for the given provider
     */
    @NonNull
    public static InputReader forFileInputProvider(@NonNull final FileInput.Provider fileProvider) {
        Parameters.notNull("fileProvider", fileProvider);

        final org.netbeans.api.extexecution.base.input.InputReader delegate = org.netbeans.api.extexecution.base.input.InputReaders.forFileInputProvider(new org.netbeans.api.extexecution.base.input.InputReaders.FileInput.Provider() {

            private org.netbeans.api.extexecution.base.input.InputReaders.FileInput proxy;

            private FileInput input;

            @Override
            public org.netbeans.api.extexecution.base.input.InputReaders.FileInput getFileInput() {
                FileInput fresh = fileProvider.getFileInput();
                if (input != fresh && (input == null || !input.equals(fresh))) {
                    input = fresh;
                    proxy = new org.netbeans.api.extexecution.base.input.InputReaders.FileInput(
                            input.getFile(), input.getCharset());
                }
                return proxy;
            }
        });

        return new InputReader() {

            @Override
            public int readInput(org.netbeans.api.extexecution.input.InputProcessor processor) throws IOException {
                return delegate.readInput(processor == null ? null : new BaseInputProcessor(processor));
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    /**
     * Represents the file with associated charset for reading from it.
     *
     * This class is <i>immutable</i>.
     */
    public static final class FileInput {

        private final File file;

        private final Charset charset;

        /**
         * Creates the new input representing the given file.
         *
         * @param file file to represent
         * @param charset associated charset
         */
        public FileInput(@NonNull File file, @NonNull Charset charset) {
            Parameters.notNull("file", file);
            Parameters.notNull("charset", charset);

            this.file = file;
            this.charset = charset;
        }

        /**
         * Returns the charset for reading the file.
         *
         * @return the charset for reading the file
         */
        @NonNull
        public Charset getCharset() {
            return charset;
        }

        /**
         * Returns the file represented by this input.
         *
         * @return the file represented by this input
         */
        @NonNull
        public File getFile() {
            return file;
        }

        /**
         * Provides the file input.
         *
         * @see InputReaders#forFileInputProvider(org.netbeans.api.extexecution.input.InputReaders.FileInput.Provider)
         */
        public interface Provider {

            /**
             * Returns the file input to use or <code>null</code> if there is
             * no file to read currently.
             *
             * @return the file input to use or <code>null</code> if there is
             * no file to read currently
             */
            @CheckForNull
            FileInput getFileInput();

        }
    }
}
