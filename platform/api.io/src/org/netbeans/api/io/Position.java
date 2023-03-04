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
 *
 * Stored position in the output window.
 *
 * <p>
 * Methods of this class can be called in any thread.
 * </p>
 *
 * @author jhavlin
 */
public abstract class Position {

    static final Position UNSUPPORTED = new Position() {

        @Override
        public void scrollTo() {
        }
    };

    private Position() {
    }

    /**
     * Scroll to this position.
     */
    public abstract void scrollTo();

    static <IO, OW extends PrintWriter, P, F> Position create(
            InputOutputProvider<IO, OW, P, F> provider, IO io,
            OW writer, P position) {

        if (position == null) {
            return UNSUPPORTED;
        } else {
            return new Impl<IO, OW, P, F>(provider, io, writer, position);
        }
    }

    private static class Impl<IO, OW extends PrintWriter, P, F>
            extends Position {

        private final InputOutputProvider<IO, OW, P, F> provider;
        private final IO io;
        private final OW ow;
        private final P position;

        public Impl(InputOutputProvider<IO, OW, P, F> provider, IO io, OW ow,
                P position) {
            this.provider = provider;
            this.io = io;
            this.ow = ow;
            this.position = position;
        }

        @Override
        public void scrollTo() {
            provider.scrollTo(io, ow, position);
        }
    }
}
