/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.windows;

import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.Parameters;

/**
 * Folding of group of lines in Output Window.
 * <p>
 * Client usage:
 * </p>
 * <pre>
 *  InputOutput io = ...;
 *  if (!IOFolding.isSupported(io)) {
 *    throw new Exception("Folding is not supported");
 *  }
 *  io.getOut().println("First Line - start of fold");
 *  FoldHandle fold = IOFolding.startFold(io, true);
 *  io.getOut().println("  Fold Content 1");
 *  io.getOut().println("  The first line of nested fold");
 *  FoldHandle nestedFold = fold.startFold(true);
 *  io.getOut().println("     Nested fold content 1");
 *  nestedFold.finish();
 *  io.getOut().println("  Fold Content 2");
 *  fold.finish();
 *  io.getOut().println("Text outside of the fold.");
 * </pre>
 * <p>
 * How to support {@link IOFolding} in own {@link IOProvider} implementation:
 * </p>
 * <ul>
 * <li> {@link InputOutput} provided by {@link IOProvider} has to implement
 * {@link Provider}</li>
 * <li> Extend {@link IOFolding} and implement its abstract methods</li>
 * <li> Extend {@link FoldHandleDefinition}</li>
 * <li> Place instance of {@link IOFolding} to {@link Lookup} provided by
 * {@link InputOutput}</li>
 * </ul>
 *
 * @author jhavlin
 * @since openide.io/1.38
 */
public abstract class IOFolding {

    /**
     * Check whether an {@link InputOutput} supports folding.
     *
     * @param io The InputOutput to check.
     * @return True if {@link #startFold(InputOutput, boolean)} can be used with
     * {@code io}, false otherwise.
     */
    public static boolean isSupported(@NonNull InputOutput io) {
        Parameters.notNull("parent", io);                               //NOI18N
        return findIOFolding(io) != null;
    }

    /**
     * Find folding support in an {@link InputOutput} object.
     *
     * @return IOFolding object if folding is supported by the {@code parent}
     * object, null otherwise.
     */
    private static IOFolding findIOFolding(InputOutput parent) {
        return (parent instanceof Lookup.Provider)
                ? ((Lookup.Provider) parent).getLookup().lookup(IOFolding.class)
                : null;
    }

    /**
     * Create a fold handle definition for the current last line in the output
     * window.
     *
     * @param expanded Initial state of the fold.
     * @return FoldHandleDefinition for the fold handle. Never null.
     *
     * @throws IllegalStateException if the last fold hasn't been finished yet.
     */
    @NonNull
    protected abstract FoldHandleDefinition startFold(boolean expanded);

    /**
     * Create a fold handle for the current last line in the output window.
     *
     * @param io InputOutput to create the fold in.
     * @param expanded Initial state of the fold.
     * @return The fold handle that can be used to finish the fold or to create
     * nested folds.
     * @throws IllegalStateException if the last fold hasn't been finished yet.
     * @throws UnsupportedOperationException if folding is not supported by the
     * InputOutput object.
     */
    @CheckReturnValue
    @NonNull
    public static FoldHandle startFold(
            @NonNull InputOutput io, boolean expanded) {

        Parameters.notNull("io", io);                                   //NOI18N
        IOFolding folding = findIOFolding(io);
        if (folding == null) {
            throw new UnsupportedOperationException(
                    "The InputOutput doesn't support folding");         //NOI18N
        } else {
            return new FoldHandle(folding.startFold(expanded));
        }
    }

    /**
     * An SPI for creating custom FoldHandle implementations.
     */
    protected static abstract class FoldHandleDefinition {

        /**
         * Finish the fold at the current last line. Ensure that nested folds
         * are finished correctly.
         *
         * @throws IllegalStateException if parent fold has been already
         * finished, or if there is an unfinished nested fold.
         */
        public abstract void finish();

        /**
         * Start a new fold at the current last line. Ensure that the parent
         * fold hasn't been finished yet.
         *
         * @param expanded If false, the fold will be collapsed by default,
         * otherwise it will be expanded.
         * @return FoldHandleDefinition of handle for the newly created fold.
         * @throws IllegalStateException if the fold has been already finished,
         * or if the last nested fold hasn't been finished yet.
         */
        public abstract FoldHandleDefinition startFold(boolean expanded);

        /**
         * Set state of the fold.
         *
         * If a nested fold is expanded, expand all parent folds too.
         *
         * @param expanded True to expand the fold, false to collapse it.
         */
        public abstract void setExpanded(boolean expanded);
    }
}
