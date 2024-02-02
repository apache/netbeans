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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.lsp.CompletionAccessor;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.RequestProcessor;

/**
 * Interface for computing and collecting completions. Clients can use this interface
 * to collect completions and send them for presentation outside of NetBeans using
 * the Language Server Protocol. Implementations of the interface should be registered
 * in MimeLookup.
 * {@snippet :
 *
 *  {@code @}MimeRegistration(mimeType = "text/foo", service = CompletionCollector.class)
 *  public class FooCompletionCollector implements CompletionCollector {
 *     ...
 *  }
 * }
 *
 * @author Dusan Balek
 * @since 1.0
 */
@MimeLocation(subfolderName = "CompletionCollectors")
public interface CompletionCollector {

    /**
     * Computes and collects completions for a document at a given offset.
     *
     * @param doc a text document
     * @param offset an offset inside the text document
     * @param context an optional completion context
     * @param consumer an operation accepting collected completions
     *
     * @return true if the list of collected completion is complete
     *
     * @since 1.0
     */
    public boolean collectCompletions(@NonNull Document doc, int offset, @NullAllowed Completion.Context context, @NonNull Consumer<Completion> consumer);

    /**
     * Creates a builder for {@link Completion} instances.
     *
     * @param label the label of the completion
     * @return newly created builder
     *
     * @since 1.0
     */
    public static Builder newBuilder(@NonNull String label) {
        return new Builder(label);
    }

    /**
     * Builder for {@link Completion} instances. Its usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/CompletionTest.java" region="builder"}
     *
     * @since 1.0
     */
    public static final class Builder {

        private String label;
        private String labelDetail;
        private String labelDescription;
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
        private Command command;
        private CompletableFuture<List<TextEdit>> additionalTextEdits;
        private List<Character> commitCharacters;

        private Builder(@NonNull String label) {
            this.label = label;
        }

        /**
         * The label of this completion. By default also the text that is inserted
         * when selecting this completion.
         *
         * @since 1.0
         */
        @NonNull
        public Builder label(@NonNull String label) {
            this.label = label;
            return this;
        }

        /**
         * An optional string which is rendered less prominently directly after
         * {@link Completion#getLabel() label}, without any spacing. Should be
         * used for function signatures or type annotations.
         *
         * @since 1.24
         */
        @NonNull
        public Builder labelDetail(@NonNull String labelDetail) {
            this.labelDetail = labelDetail;
            return this;
        }

        /**
         * An optional string which is rendered less prominently after
         * {@link Completion#getLabelDetail() label detail}. Should be used for fully qualified
         * names or file path.
         *
         * @since 1.24
         */
        @NonNull
        public Builder labelDescription(@NonNull String labelDescription) {
            this.labelDescription = labelDescription;
            return this;
        }

        /**
         * The kind of this completion.
         *
         * @since 1.0
         */
        @NonNull
        public Builder kind(@NonNull Completion.Kind kind) {
            this.kind = kind;
            return this;
        }

        /**
         * Adds tag for this completion.
         *
         * @since 1.0
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
         * A human-readable string with additional information about this completion,
         * like type or symbol information. If computing of a full detail is expensive,
         * user should use {@link #detail(java.util.function.Supplier)} to defer
         * computation to the subsequent {@code completionItem/resolve} request.
         *
         * @since 1.0
         */
        @NonNull
        public Builder detail(@NonNull String detail) {
            this.detail = CompletableFuture.completedFuture(detail);
            return this;
        }

        /**
         * A human-readable string with additional information about this completion,
         * like type or symbol information. Use this method to defer computation
         * of a full detail to the subsequent {@code completionItem/resolve} request.
         *
         * @since 1.0
         */
        @NonNull
        public Builder detail(@NonNull Supplier<String> detail) {
            this.detail = new LazyCompletableFuture<>(detail);
            return this;
        }

        /**
         * A human-readable string that represents a doc-comment. An HTML format
         * is supported. If computing of a full documentation is expensive,
         * user should use {@link #documentation(java.util.function.Supplier)} to
         * defer computation to the subsequent {@code completionItem/resolve} request.
         *
         * @since 1.0
         */
        @NonNull
        public Builder documentation(@NonNull String documentation) {
            this.documentation = CompletableFuture.completedFuture(documentation);
            return this;
        }

        /**
         * A human-readable string that represents a doc-comment. An HTML format
         * is supported. Use this method to defer computation of a full documentation
         * to the subsequent {@code completionItem/resolve} request.
         *
         * @since 1.0
         */
        @NonNull
        public Builder documentation(@NonNull Supplier<String> documentation) {
            this.documentation = new LazyCompletableFuture<>(documentation);
            return this;
        }

        /**
         * Select this completion when showing.
         *
         * @since 1.0
         */
        @NonNull
        public Builder preselect(boolean preselect) {
            this.preselect = preselect;
            return this;
        }

        /**
         * A string that should be used when comparing this completion with other
         * completions. When {@code null} the label is used as the sort text.
         *
         * @since 1.0
         */
        @NonNull
        public Builder sortText(@NonNull String sortText) {
            this.sortText = sortText;
            return this;
        }

        /**
         * A string that should be used when filtering a set of completions.
         * When {@code null} the label is used as the filter.
         *
         * @since 1.0
         */
        @NonNull
        public Builder filterText(@NonNull String filterText) {
            this.filterText = filterText;
            return this;
        }

        /**
         * A string that should be inserted into a document when selecting
         * this completion. When {@code null} the label is used as the insert text.
         *
         * @since 1.0
         */
        @NonNull
        public Builder insertText(@NonNull String insertText) {
            this.insertText = insertText;
            this.textEdit = null;
            return this;
        }

        /**
         * The format of the insert text. The format applies to both the
         * {@code insertText} property and the {@code newText} property of a provided
         * {@code textEdit}. If omitted defaults to {@link org.netbeans.api.lsp.Completion.TextFormat#PlainText}.
         *
         * @since 1.0
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
         *
         * @since 1.0
         */
        @NonNull
        public Builder textEdit(@NonNull TextEdit textEdit) {
            this.textEdit = textEdit;
            this.insertText = null;
            return this;
        }

        /**
	 * An optional command that is executed after inserting this completion.
         *
         * @since 1.17
	 */
        @NonNull
        public Builder command(@NonNull Command command) {
            this.command = command;
            return this;
        }
        /**
         * A list of additional text edits that are applied when selecting this
         * completion. Edits must not overlap (including the same insert position)
         * with the main edit nor with themselves.
         * Additional text edits should be used to change text unrelated to the
         * current cursor position (for example adding an import statement at the
         * top of the file if the completion item will insert an unqualified type).
         * If computing of the additional text edits is expensive, user should use
         * {@link #additionalTextEdits(java.util.function.Supplier)} to defer
         * computation to the subsequent {@code completionItem/resolve} request.
         *
         * @since 1.0
         */
        @NonNull
        public Builder additionalTextEdits(@NonNull List<TextEdit> additionalTextEdits) {
            this.additionalTextEdits = CompletableFuture.completedFuture(additionalTextEdits);
            return this;
        }

        /**
         * A list of additional text edits that are applied when selecting this
         * completion. Edits must not overlap (including the same insert position)
         * with the main edit nor with themselves.
         * Additional text edits should be used to change text unrelated to the
         * current cursor position (for example adding an import statement at the
         * top of the file if the completion item will insert an unqualified type).
         * Use this method to defer computation of the additional text edits to
         * the subsequent {@code completionItem/resolve} request.
         *
         * @since 1.0
         */
        @NonNull
        public Builder additionalTextEdits(@NonNull Supplier<List<TextEdit>> additionalTextEdits) {
            this.additionalTextEdits = new LazyCompletableFuture<>(additionalTextEdits);
            return this;
        }

        /**
         * Adds character that when pressed while this completion is active will
         * accept it first and then type that character.
         *
         * @since 1.0
         */
        @NonNull
        public Builder addCommitCharacter(char commitCharacter) {
            if (this.commitCharacters == null) {
                this.commitCharacters = new ArrayList<>();
            }
            this.commitCharacters.add(commitCharacter);
            return this;
        }

        /**
         * Builds completion.
         *
         * @since 1.0
         */
        @NonNull
        public Completion build() {
            return CompletionAccessor.getDefault().createCompletion(label, labelDetail,
                    labelDescription, kind, tags, detail, documentation, preselect, sortText,
                    filterText, insertText, insertTextFormat, textEdit, command,
                    additionalTextEdits, commitCharacters);
        }

        private static class LazyCompletableFuture<T> extends CompletableFuture<T> {

            private static final RequestProcessor ASYNC_WORKER = new RequestProcessor(LazyCompletableFuture.class.getName(), 1);
            private final Supplier<T> supplier;

            private LazyCompletableFuture(Supplier<T> supplier) {
                this.supplier = supplier;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                try {
                    this.complete(supplier.get());
                } catch (Exception ex) {
                    this.completeExceptionally(ex);
                }
                return super.get();
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                CompletableFuture.supplyAsync(supplier, ASYNC_WORKER).thenAccept(t -> {
                    this.complete(t);
                }).exceptionally(ex -> {
                    this.completeExceptionally(ex);
                    return null;
                });
                return super.get(timeout, unit);
            }
        }
    }
}
