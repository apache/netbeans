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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.lsp.CompletionAccessor;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 * Represents a completion proposal.
 *
 * @author Dusan Balek
 * @since 1.0
 */
public final class Completion {

    static {
        CompletionAccessor.setDefault(new CompletionAccessor() {
            @Override
            public Completion createCompletion(String label, String labelDetail, String description, Kind kind, List<Tag> tags, CompletableFuture<String> detail, CompletableFuture<String> documentation,
                    boolean preselect, String sortText, String filterText, String insertText, TextFormat insertTextFormat, TextEdit textEdit, Command command,
                    CompletableFuture<List<TextEdit>> additionalTextEdits, List<Character> commitCharacters) {
                return new Completion(label, labelDetail, description, kind, tags, detail, documentation, preselect, sortText, filterText, insertText, insertTextFormat, textEdit, command, additionalTextEdits, commitCharacters);
            }
        });
    }

    private final String label;
    private final String labelDetail;
    private final String labelDescription;
    private final Kind kind;
    private final List<Tag> tags;
    private final CompletableFuture<String> detail;
    private final CompletableFuture<String> documentation;
    private final boolean preselect;
    private final String sortText;
    private final String filterText;
    private final String insertText;
    private final TextFormat insertTextFormat;
    private final TextEdit textEdit;
    private final Command command;
    private final CompletableFuture<List<TextEdit>> additionalTextEdits;
    private final List<Character> commitCharacters;

    private Completion(String label, String labelDetail, String labelDescription, Kind kind, List<Tag> tags, CompletableFuture<String> detail, CompletableFuture<String> documentation,
            boolean preselect, String sortText, String filterText, String insertText, TextFormat insertTextFormat,
            TextEdit textEdit, Command command, CompletableFuture<List<TextEdit>> additionalTextEdits, List<Character> commitCharacters) {
        this.label = label;
        this.labelDetail = labelDetail;
        this.labelDescription = labelDescription;
        this.kind = kind;
        this.tags = tags;
        this.detail = detail;
        this.documentation = documentation;
        this.preselect = preselect;
        this.sortText = sortText;
        this.filterText = filterText;
        this.insertText = insertText;
        this.insertTextFormat = insertTextFormat;
        this.textEdit = textEdit;
        this.command = command;
        this.additionalTextEdits = additionalTextEdits;
        this.commitCharacters = commitCharacters;
    }

    /**
     * The label of this completion. By default also the text that is inserted
     * when selecting this completion.
     *
     * @since 1.0
     */
    @NonNull
    public String getLabel() {
        return label;
    }

    /**
     * An optional string which is rendered less prominently directly after
     * {@link Completion#getLabel() label}, without any spacing. Should be
     * used for function signatures or type annotations.
     *
     * @since 1.24
     */
    @CheckForNull
    public String getLabelDetail() {
        return labelDetail;
    }

    /**
     * An optional string which is rendered less prominently after
     * {@link Completion#getLabelDetail() label detail}. Should be used for fully qualified
     * names or file path.
     *
     * @since 1.24
     */
    @CheckForNull
    public String getLabelDescription() {
        return labelDescription;
    }

    /**
     * The kind of this completion.
     *
     * @since 1.0
     */
    @CheckForNull
    public Kind getKind() {
        return kind;
    }

    /**
     * Tags for this completion.
     *
     * @since 1.0
     */
    @CheckForNull
    public List<Tag> getTags() {
        return tags != null ? Collections.unmodifiableList(tags) : null;
    }

    /**
     * A human-readable string with additional information
     * about this completion, like type or symbol information.
     *
     * @since 1.0
     */
    @CheckForNull
    public CompletableFuture<String> getDetail() {
        return detail;
    }

    /**
     * A human-readable string that represents a doc-comment. An HTML format is
     * supported.
     *
     * @since 1.0
     */
    @CheckForNull
    public CompletableFuture<String> getDocumentation() {
        return documentation;
    }

    /**
     * Select this completion when showing.
     *
     * @since 1.0
     */
    public boolean isPreselect() {
        return preselect;
    }

    /**
     * A string that should be used when comparing this completion with other
     * completions. When {@code null} the label is used as the sort text.
     *
     * @since 1.0
     */
    @CheckForNull
    public String getSortText() {
        return sortText;
    }

    /**
     * A string that should be used when filtering a set of completions.
     * When {@code null} the label is used as the filter.
     *
     * @since 1.0
     */
    @CheckForNull
    public String getFilterText() {
        return filterText;
    }

    /**
     * A string that should be inserted into a document when selecting
     * this completion. When {@code null} the label is used as the insert text.
     *
     * @since 1.0
     */
    @CheckForNull
    public String getInsertText() {
        return insertText;
    }

    /**
     * The format of the insert text. The format applies to both the
     * {@code insertText} property and the {@code newText} property of a provided
     * {@code textEdit}. If omitted defaults to {@link TextFormat#PlainText}.
     *
     * @since 1.0
     */
    @CheckForNull
    public TextFormat getInsertTextFormat() {
        return insertTextFormat;
    }

    /**
     * An edit which is applied to a document when selecting this completion.
     * When an edit is provided the value of {@code insertText} is ignored.
     * The range of the edit must be a single line range and it must
     * contain the position at which completion has been requested.
     *
     * @since 1.0
     */
    @CheckForNull
    public TextEdit getTextEdit() {
        return textEdit;
    }

    /**
     * An optional command that is executed after inserting this completion.
     *
     * @since 1.17
     */
    @CheckForNull
    public Command getCommand() {
        return command;
    }

    /**
     * A list of additional text edits that are applied when selecting this
     * completion. Edits must not overlap (including the same insert position)
     * with the main edit nor with themselves.
     * Additional text edits should be used to change text unrelated to the
     * current cursor position (for example adding an import statement at the
     * top of the file if the completion item will insert an unqualified type).
     *
     * @since 1.0
     */
    @CheckForNull
    public CompletableFuture<List<TextEdit>> getAdditionalTextEdits() {
        return additionalTextEdits;
    }

    /**
     * A list of characters that when pressed while this completion is
     * active will accept it first and then type that character.
     *
     * @since 1.0
     */
    @CheckForNull
    public List<Character> getCommitCharacters() {
        return commitCharacters != null ? Collections.unmodifiableList(commitCharacters) : null;
    }

    /**
     * Computes and collects completions for a document at a given offset. Example
     * usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/CompletionTest.java" region="testCompletionCollect"}
     *
     * @param doc a text document
     * @param offset an offset inside the text document
     * @param context an optional completion context
     * @param consumer an operation accepting collected completions
     *
     * @return true if the list of collected completion is complete. If {@code false},
     * further typing should result in subsequent calls to this method to recompute
     * the completions.
     *
     * @since 1.0
     */
    public static boolean collect(@NonNull Document doc, int offset, @NullAllowed Context context, @NonNull Consumer<Completion> consumer) {
        boolean isComplete = true;
        MimePath mimePath = MimePath.parse(DocumentUtilities.getMimeType(doc));
        for (CompletionCollector collector : MimeLookup.getLookup(mimePath).lookupAll(CompletionCollector.class)) {
            isComplete &= collector.collectCompletions(doc, offset, context, consumer);
        }
        return isComplete;
    }

    /**
     * Contains additional information about the context in which a request for
     * collections completions is triggered.
     *
     * @since 1.0
     */
    public static final class Context {

        private final TriggerKind triggerKind;
        private final Character triggerCharacter;

        public Context(@NonNull TriggerKind triggerKind, @NullAllowed Character triggerCharacter) {
            this.triggerKind = triggerKind;
            this.triggerCharacter = triggerCharacter;
        }

        /**
         * How the completion was triggered.
         *
         * @since 1.0
         */
        @NonNull
        public TriggerKind getTriggerKind() {
            return triggerKind;
        }

        /**
         * The trigger character that has trigger code complete.
         * Is undefined if {@code triggerKind != TriggerKind.TriggerCharacter}.
         *
         * @since 1.0
         */
        @CheckForNull
        public Character getTriggerCharacter() {
            return triggerCharacter;
        }
    }

    /**
     * Specifies how a completion was triggered.
     *
     * @since 1.0
     */
    public enum TriggerKind {

        /**
         * Completion was triggered by typing an identifier (24x7 code
         * complete), manual invocation (e.g Ctrl+Space) or via API.
         *
         * @since 1.0
         */
        Invoked,

        /**
         * Completion was triggered by a trigger character.
         *
         * @since 1.0
         */
        TriggerCharacter,

        /**
         * Completion was re-triggered as the current completion list is incomplete.
         *
         * @since 1.0
         */
        TriggerForIncompleteCompletions
    }

    /**
     * The kind of a completion.
     *
     * @since 1.0
     */
    public static enum Kind {

        Text,
        Method,
        Function,
        Constructor,
        Field,
        Variable,
        Class,
        Interface,
        Module,
        Property,
        Unit,
        Value,
        Enum,
        Keyword,
        Snippet,
        Color,
        File,
        Reference,
        Folder,
        EnumMember,
        Constant,
        Struct,
        Event,
        Operator,
        TypeParameter
    }

    /**
     * Completion item tags are extra annotations that tweak the rendering of a
     * completion.
     *
     * @since 1.0
     */
    public static enum Tag {

        Deprecated
    }

    /**
     * Defines whether the insert text in a completion item should be interpreted
     * as plain text or a snippet.
     *
     * @since 1.0
     */
    public static enum TextFormat {

        /**
         * The primary text to be inserted is treated as a plain string.
         *
         * @since 1.0
         */
        PlainText,

        /**
         * The primary text to be inserted is treated as a snippet.
         *
         * @since 1.0
         */
        Snippet
    }
}
