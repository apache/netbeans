/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
    public static abstract class Result {
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
