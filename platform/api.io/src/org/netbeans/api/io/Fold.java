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
package org.netbeans.api.io;

import java.io.PrintWriter;
import org.netbeans.spi.io.InputOutputProvider;

/**
 * A fold (nested or standalone) in the output window.
 *
 * <p>
 * Methods of this class can be called in any thread.
 * </p>
 *
 * @author jhavlin
 */
public abstract class Fold {

    static final Fold UNSUPPORTED = new Fold() {

        @Override
        public void setExpanded(boolean expanded) {
        }

        @Override
        void endFold() {
        }
    };

    private Fold() {
    }

    static <IO, OW extends PrintWriter, P, F> Fold create(
            InputOutputProvider<IO, OW, P, F> provider, IO io, OW writer,
            F fold) {
        if (fold == null) {
            return UNSUPPORTED;
        } else {
            return new Impl<IO, OW, P, F>(provider, io, writer, fold);
        }
    }

    /**
     * Set fold expansion state.
     *
     * @param expanded True to expand the fold, false to collapse it.
     */
    public abstract void setExpanded(boolean expanded);

    abstract void endFold();

    /**
     * Expand the fold.
     */
    public final void expand() {
        setExpanded(true);
    }

    /**
     * Collapse the fold.
     */
    public final void collapse() {
        setExpanded(false);
    }

    private static class Impl<IO, OW extends PrintWriter, P, F> extends Fold {

        private final InputOutputProvider<IO, OW, P, F> provider;
        private final IO io;
        private final OW writer;
        private final F fold;

        public Impl(InputOutputProvider<IO, OW, P, F> provider, IO io,
                OW writer, F fold) {

            this.provider = provider;
            this.io = io;
            this.writer = writer;
            this.fold = fold;
        }

        @Override
        public void setExpanded(boolean expanded) {
            provider.setFoldExpanded(io, writer, fold, expanded);
        }

        @Override
        void endFold() {
            provider.endFold(io, writer, fold);
        }
    }
}
