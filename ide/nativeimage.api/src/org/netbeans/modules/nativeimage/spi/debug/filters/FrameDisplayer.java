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
package org.netbeans.modules.nativeimage.spi.debug.filters;

import java.net.URI;
import java.util.function.Supplier;
import org.netbeans.api.annotations.common.CheckForNull;

import org.netbeans.modules.nativeimage.api.debug.NIFrame;

/**
 * Displayer of stack frames. Modifies the way how frames are presented.
 *
 * @since 1.0
 */
public interface FrameDisplayer {

    /**
     * Provide display information of a stack frame.
     *
     * @param frame the stack frame
     * @return a display information, or <code>null</code> to skip this frame.
     * @since 1.0
     */
    DisplayedFrame displayed(NIFrame frame);

    /**
     * Display information of a frame.
     * @since 1.0
     */
    public static final class DisplayedFrame {

        private final String displayName;
        private final String description;
        private final int line;
        private final Supplier<URI> uriSupplier;

        private DisplayedFrame(String displayName, String description, int line, Supplier<URI> uriSupplier) {
            this.displayName = displayName;
            this.description = description;
            this.line = line;
            this.uriSupplier = uriSupplier;
        }

        /**
         * Creates a new builder with a user visible display name of the frame.
         *
         * @since 1.0
         */
        public static Builder newBuilder(String displayName) {
            return new Builder(displayName);
        }

        /**
         * Get a display name of the frame.
         *
         * @since 1.0
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Get a description of the frame.
         *
         * @since 1.0
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get a 1-based line number of the frame.
         *
         * @since 1.0
         */
        public int getLine() {
            return line;
        }

        /**
         * Get URI of the source file associated with the frame.
         *
         * @return the URI, or <code>null</code> when unknown.
         * @since 1.0
         */
        @CheckForNull
        public URI getSourceURI() {
            if (uriSupplier != null) {
                return uriSupplier.get();
            } else {
                return null;
            }
        }

        /**
         * Builder of the {@link DisplayedFrame}.
         * @since 1.0
         */
        public static final class Builder {

            private final String displayName;
            private String description;
            private int line;
            private Supplier<URI> uriSupplier;

            Builder(String displayName) {
                this.displayName = displayName;
            }

            /**
             * Set frame description.
             *
             * @since 1.0
             */
            public Builder description(String description) {
                this.description = description;
                return this;
            }

            /**
             * 1-based line number information of the frame location.
             *
             * @since 1.0
             */
            public Builder line(int lineNumber) {
                this.line = lineNumber;
                return this;
            }

            /**
             * URI of the source file associated with the frame.
             * @since 1.0
             */
            public Builder sourceURISupplier(Supplier<URI> uriSupplier) {
                this.uriSupplier = uriSupplier;
                return this;
            }

            /**
             * Build the {@link DisplayedFrame}.
             *
             * @since 1.0
             */
            public DisplayedFrame build() {
                return new DisplayedFrame(displayName, description, line, uriSupplier);
            }
        }
    }
}
