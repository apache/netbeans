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
package org.netbeans.spi.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.lsp.CompletionAccessor;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Interface for computing and collecting completions. Clients can use this interface
 * to collect completions and send them for presentation outside of NetBeans using
 * the Language Server Protocol. Implementations of the interface should be registered
 * in MimeLookup.
 *
 * @author Dusan Balek
 */
@MimeLocation(subfolderName = "CompletionCollectors")
public interface CompletionCollector {

    /**
     * Computes and collects completions for a document at a given offset.
     * This method is called outside of AWT to collect completions and
     * send them via the Language Server Protocol to client for display.
     *
     * @param doc a text document
     * @param offset an offset inside the text document
     * @param context an optional completion context
     * @param consumer an operation accepting collected completions
     *
     * @return true if the list of collected completion is complete
     */
    public boolean collectCompletions(@NonNull Document doc, int offset, @NullAllowed Completion.Context context, @NonNull Consumer<Completion> consumer);

    /**
     * Creates a builder for {@link Completion} instances
     *
     * @param label the label of the completion
     * @return newly created builder
     */
    public static Builder newBuilder(@NonNull String label) {
        return new Builder(label);
    }

    public static final class Builder {

        private String label;
        private Completion.Kind kind;
        private List<Completion.Tag> tags;
        private CompletableFuture<String> detail;
        private CompletableFuture<String> documentation;
        private boolean preselect;
        private String sortText;
        private String filterText;
        private String insertText;
        private Completion.TextFormat insertTextFormat;
        private TextEdit textEdit;
        private CompletableFuture<List<TextEdit>> additionalTextEdits;
        private List<Character> commitCharacters;
        private Command command;

        private Builder(@NonNull String label) {
            this.label = label;
        }

        /**
         * The label of this completion. By default also the text that is inserted
         * when selecting this completion.
         */
        @NonNull
        public Builder label(@NonNull String label) {
            this.label = label;
            return this;
        }

        /**
         * The kind of this completion.
         */
        @NonNull
        public Builder kind(@NonNull Completion.Kind kind) {
            this.kind = kind;
            return this;
        }

        /**
         * Adds tag for this completion.
         */
        @NonNull
        public Builder addTag(@NonNull Completion.Tag tag) {
            if (this.tags == null) {
                this.tags = new ArrayList<>();
            }
            this.tags.add(tag);
            return this;
        }

        /**
         * A human-readable string with additional information
         * about this completion, like type or symbol information.
         */
        @NonNull
        public Builder detail(@NonNull CompletableFuture<String> detail) {
            this.detail = detail;
            return this;
        }

        /**
         * A human-readable string that represents a doc-comment. An HTML format
         * is supported.
         */
        @NonNull
        public Builder documentation(@NonNull CompletableFuture<String> documentation) {
            this.documentation = documentation;
            return this;
        }

        /**
         * Select this completion when showing.
         */
        @NonNull
        public Builder preselect(boolean preselect) {
            this.preselect = preselect;
            return this;
        }

        /**
         * A string that should be used when comparing this completion with other
         * completions. When {@code null} the label is used as the sort text.
         */
        @NonNull
        public Builder sortText(@NonNull String sortText) {
            this.sortText = sortText;
            return this;
        }

        /**
         * A string that should be used when filtering a set of completions.
         * When {@code null} the label is used as the filter.
         */
        @NonNull
        public Builder filterText(@NonNull String filterText) {
            this.filterText = filterText;
            return this;
        }

        /**
         * A string that should be inserted into a document when selecting
         * this completion. When {@code null} the label is used as the insert text.
         */
        @NonNull
        public Builder insertText(@NonNull String insertText) {
            this.insertText = insertText;
            return this;
        }

        /**
         * The format of the insert text. The format applies to both the
         * {@code insertText} property and the {@code newText} property of a provided
         * {@code textEdit}. If omitted defaults to {@link TextFormat#PlainText}.
         */
        @NonNull
        public Builder insertTextFormat(@NonNull Completion.TextFormat insertTextFormat) {
            this.insertTextFormat = insertTextFormat;
            return this;
        }

        /**
         * An edit which is applied to a document when selecting this completion.
         * When an edit is provided the value of {@code insertText} is ignored.
         * The range of the edit must be a single line range and it must
         * contain the position at which completion has been requested.
         */
        @NonNull
        public Builder textEdit(@NonNull TextEdit textEdit) {
            this.textEdit = textEdit;
            return this;
        }

        /**
         * A list of additional text edits that are applied when selecting this
         * completion. Edits must not overlap (including the same insert position)
         * with the main edit nor with themselves.
         * Additional text edits should be used to change text unrelated to the
         * current cursor position (for example adding an import statement at the
         * top of the file if the completion item will insert an unqualified type).
         */
        @NonNull
        public Builder additionalTextEdits(@NonNull CompletableFuture<List<TextEdit>> additionalTextEdits) {
            this.additionalTextEdits = additionalTextEdits;
            return this;
        }

        /**
         * Adds character that when pressed while this completion is active will
         * accept it first and then type that character.
         */
        @NonNull
        public Builder addCommitCharacter(@NonNull Character commitCharacter) {
            if (this.commitCharacters == null) {
                this.commitCharacters = new ArrayList<>();
            }
            this.commitCharacters = commitCharacters;
            return this;
        }

        /**
         * A command that is executed after inserting this completion.
         */
        @NonNull
        public Builder command(@NonNull Command command) {
            this.command = command;
            return this;
        }

        /**
         * Builds completion.
         */
        @NonNull
        public Completion build() {
            return CompletionAccessor.getDefault().createCompletion(label, kind,
                    tags, detail, documentation, preselect, sortText, filterText,
                    insertText, insertTextFormat, textEdit, additionalTextEdits,
                    commitCharacters, command);
        }
    }
}
