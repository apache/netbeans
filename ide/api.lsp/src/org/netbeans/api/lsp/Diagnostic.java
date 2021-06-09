/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.lsp;

import java.util.List;
import java.util.function.Consumer;

/**
 * A diagnostic for LSP. Use a {@link Diagnostic.Builder} to create an instance.
 *
 * @since 1.3
 */
public class Diagnostic {

    private final Position startPosition;
    private final Position endPosition;
    private final String description;
    private final Severity severity;
    private final String code;
    private final LazyCodeActions actions;

    private Diagnostic(Position startPosition, Position endPosition, String description,
                       Severity severity, String code, LazyCodeActions actions) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.description = description;
        this.severity = severity;
        this.code = code;
        this.actions = actions;
    }

    /**
     * The start offset of the diagnostic.
     *
     * @return the start offset of the diagnostic
     */
    public Position getStartPosition() {
        return startPosition;
    }

    /**
     * The end offset of the diagnostic.
     * @return the end offset of the diagnostic
     */
    public Position getEndPosition() {
        return endPosition;
    }

    /**
     * The description of the diagnostic.
     * @return the description of the diagnostic
     */
    public String getDescription() {
        return description;
    }

    /**
     * The severity of the diagnostic.
     * @return the severity of the diagnostic
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * The code of the diagnostic.
     * @return the code of the diagnostic
     */
    public String getCode() {
        return code;
    }

    /**
     * The actions associated with the diagnostic.
     * @return the actions associated with the diagnostic
     */
    public LazyCodeActions getActions() {
        return actions;
    }

    public enum Severity {
        Error,
        Warning,
        Hint,
        Information;
    }

    /**
     * The build for the Diagnostic.
     */
    public static final class Builder {
        private final Position startPosition;
        private final Position endPosition;
        private final String description;
        private Severity severity;
        private String code;
        private LazyCodeActions actions;

        private Builder(Position startPosition, Position endPosition, String description) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.description = description;
        }

        /**
         * Create a new {@code Builder}
         *
         * @param startOffset the start offset of the diagnostic
         * @param endOffset the end offset of the diagnostic
         * @param description the description of the diagnostic
         * @return a new builder
         */
        public static Builder create(Position startPosition, Position endPosition, String description) {
            return new Builder(startPosition, endPosition, description);
        }

        /**
         * Set the severity of the diagnostic.
         *
         * @param severity severity of the diagnostic
         * @return this builder
         */
        public Builder setSeverity(Severity severity) {
            this.severity = severity;
            return this;
        }

        /**
         * Set the code of the diagnostic.
         *
         * @param code code of the diagnostic
         * @return this builder
         */
        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        /**
         * Set the actions associated with the diagnostic.
         *
         * @param actions set the actions associated with the diagnostic
         * @return this builder
         */
        public Builder addActions(LazyCodeActions actions) {
            this.actions = actions;
            return this;
        }

        /**
         * Create the {@code Diagnostic} using the values set to the builder.
         *
         * @return the new Diagnostic
         */
        public Diagnostic build() {
            return new Diagnostic(startPosition, endPosition, description, severity, code, actions);
        }
    }

    /**
     * Interface to compute Diagnostic's CodeActions lazily.
     */
    public interface LazyCodeActions {
        /**
         * Compute the code actions for the {@code Diagnostic} to which this instance
         * is attached.
         *
         * @param errorReporter a sink for any exceptions that appear during the computation
         * @return the list of {@code CodeAction}s for the current {@code Diagnostic}
         */
        public List<CodeAction> computeCodeActions(Consumer<Exception> errorReporter);
    }
}
