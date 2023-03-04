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
package org.netbeans.api.java.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Returns compiler options explicitely set for given Java source file.
 * @see org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation
 * @author Tomas Zezula
 * @since 1.64
 */
public final class CompilerOptionsQuery {

    private static final Lookup.Result<CompilerOptionsQueryImplementation> impls =
            Lookup.getDefault().lookupResult(CompilerOptionsQueryImplementation.class);

    private CompilerOptionsQuery() {
        throw new IllegalStateException("No instance allowed");
    }

    /**
     * Returns explicit compiler options for the given Java file, Java package or source folder.
     * @param file the Java source file, Java package or source folder in question
     * @return a {@link Result} object encapsulating the compiler options
     */
    @NonNull
    public static Result getOptions(@NonNull final FileObject file) {
        Parameters.notNull("file", file);   //NOI18N
        final List<CompilerOptionsQueryImplementation.Result> collector = new ArrayList<>();
        for (CompilerOptionsQueryImplementation impl : impls.allInstances()) {
            final CompilerOptionsQueryImplementation.Result res = impl.getOptions(file);
            if (res != null) {
                collector.add(res);
            }
        }
        return collector.isEmpty() ?
                Result.EMPTY :
                new Result(collector);
    }

    /**
     * Result of finding compiler options, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public static final class Result {
        static final Result EMPTY = new Result(Collections.<CompilerOptionsQueryImplementation.Result>singletonList(
            new CompilerOptionsQueryImplementation.Result() {
                @Override public List<? extends String> getArguments() { return Collections.emptyList();}
                @Override public void addChangeListener(ChangeListener l) {}
                @Override public void removeChangeListener(ChangeListener l) {}
            }));

        private final List<? extends CompilerOptionsQueryImplementation.Result> results;
        private final ChangeSupport listeners;
        //@GuaredeBy("this")
        private ChangeListener changeListener;

        Result (@NonNull final List<? extends CompilerOptionsQueryImplementation.Result> results) {
            Parameters.notNull("results", results);
            this.results = results;
            this.listeners = new ChangeSupport(this);
        }

        /**
         * Gets the explicit compiler options.
         * @return the list of the compiler options
         */
        @NonNull
        public List<? extends String> getArguments() {
            final List<String> arguments = new ArrayList<>();
            for (CompilerOptionsQueryImplementation.Result result : results) {
                final List<? extends String> l = result.getArguments();
                assert l != null;
                arguments.addAll(l);
            }
            return Collections.unmodifiableList(arguments);
        }

        /**
         * Add a listener to changes of compiler options.
         * @param listener a listener to add
         */
        public void addChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.addChangeListener(listener);
            synchronized (this) {
                if (this.changeListener == null) {
                    this.changeListener = (e) -> listeners.fireChange();
                    for (CompilerOptionsQueryImplementation.Result result : results) {
                        result.addChangeListener(WeakListeners.change(changeListener, result));
                    }
                }
            }
        }

        /**
         * Remove a listener to changes of compiler options.
         * @param listener a listener to remove
         */
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.removeChangeListener(listener);
        }
    }

}
