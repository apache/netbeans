/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.base.input;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.base.input.FileInputReader;
import org.netbeans.modules.extexecution.base.input.DefaultInputReader;
import org.openide.util.Parameters;

/**
 * Factory methods for {@link InputReader} classes.
 *
 * @author Petr Hejl
 */
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
        if (reader instanceof StringReader) {
            // unfortunatelly the string reader is always
            // ready (isReady() returns true) which I would consider a bug
            // when end of string is reached
            return new DefaultInputReader(reader, false);
        }
        return new DefaultInputReader(reader, true);
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

            @Override
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
    public static InputReader forFileInputProvider(@NonNull FileInput.Provider fileProvider) {
        Parameters.notNull("fileProvider", fileProvider);

        return new FileInputReader(fileProvider);
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
         * @see InputReaders#forFileInputProvider(org.netbeans.api.extexecution.base.input.InputReaders.FileInput.Provider)
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
