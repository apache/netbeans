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
