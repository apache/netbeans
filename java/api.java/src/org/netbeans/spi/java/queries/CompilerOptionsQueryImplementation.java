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
package org.netbeans.spi.java.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Permits providers to return explicit compiler options for Java source file.
 * @author Tomas Zezula
 * @since 1.64
 */
public interface CompilerOptionsQueryImplementation {

    /**
     * Returns explicit compiler options for the given Java file, Java package or source folder.
     * @param file the Java source file, Java package or source folder in question
     * @return a {@link Result} object encapsulating the compiler options or null
     * when the file is unknown to the provider
     */
    @CheckForNull
    Result getOptions(@NonNull final FileObject file);

    /**
     * Result of finding compiler options, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public abstract static class Result {
        /**
         * Gets the explicit compiler options.
         * @return the list of the compiler options
         */
        @NonNull
        public abstract List<? extends String> getArguments();

        /**
         * Add a listener to changes of compiler options.
         * @param listener a listener to add
         */
        public abstract void addChangeListener(@NonNull final ChangeListener listener);

        /**
         * Remove a listener to changes of compiler options.
         * @param listener a listener to remove
         */
        public abstract void removeChangeListener(@NonNull final ChangeListener listener);

        /**
         * Utility method the tokenize the command line into individual arguments.
         * @param commandLine the command line to be tokenized
         * @return a list of command line arguments
         */
        protected final List<String> parseLine(@NonNull final String commandLine) {
            final List<String> result = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean escape = false, doubleQuote = false, quote = false;
            for (int i = 0; i < commandLine.length(); i++) {
                final char c = commandLine.charAt(i);
                switch (c) {
                    case '\\':  //NOI18N
                        if (!quote) {
                            escape = !escape;
                        }
                        break;
                    case '\'':  //NOI18N
                        if (!escape && !doubleQuote) {
                            quote = !quote;
                        }
                        escape = false;
                        break;
                    case '"':   //NOI18N
                        if (!escape && !quote) {
                            doubleQuote = !doubleQuote;
                        }
                        escape = false;
                        break;
                    case ' ':   //NOI18N
                    case '\t':  //NOI18N
                        if (!escape && !quote && !doubleQuote) {
                            if (current.length() > 0) {
                                result.add(current.toString());
                                current = new StringBuilder();
                            }
                        } else {
                            current.append(c);
                        }
                        escape = false;
                        break;
                    default:
                        current.append(c);
                        escape = false;
                        break;
                }
            }
            if (current.length() > 0) {
                result.add(current.toString());
            }
            return Collections.unmodifiableList(result);
        }
    }
}
