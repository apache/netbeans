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
package org.netbeans.spi.editor.completion;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Interface for computing and collecting completions. Clients can use this interface
 * to collect completions and send them for presentation outside of NetBeans,
 * e.g. using the Language Server Protocol. Implementations of the interface
 * should be registered in MimeLookup.
 *
 * @author Dusan Balek
 * @since 1.57
 */
public interface CompletionCollector {

    /**
     * Computes and collects completions for a document at a given offset.
     * This method is called outside of AWT to collect completions and e.g.
     * send them via the Language Server Protocol to client for display.
     *
     * @param doc a text document
     * @param offset an offset inside the text document
     * @param consumer an operation accepting collected completions
     *
     * @return true if the list of collected completion is complete
     */
    public boolean collectCompletions(Document doc, int offset, Consumer<Completion> consumer);

    public final static class Completion {

        /**
         * Creates a new completion builder.
         *
         * @param label the completion label. By default also the text that is
         *              inserted when selecting the completion created by this builder.
         */
        @NonNull
        public static Builder newBuilder(@NonNull String label) {
            return new Builder(label);
        }

        private final String label;
        private final Kind kind;
        private final List<Tag> tags;
        private final String detail;
        private final String documentation;
        private final boolean preselect;
        private final String sortText;
        private final String filterText;
        private final String insertText;
        private final TextFormat insertTextFormat;
        private final TextEdit textEdit;
        private final List<TextEdit> additionalTextEdits;
        private final List<String> commitCharacters;
        private final Command command;
        private final Object data;

        private Completion(String label, Kind kind, List<Tag> tags, String detail, String documentation,
                boolean preselect, String sortText, String filterText, String insertText, TextFormat insertTextFormat,
                TextEdit textEdit, List<TextEdit> additionalTextEdits, List<String> commitCharacters, Command command, Object data) {
            this.label = label;
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
            this.additionalTextEdits = additionalTextEdits;
            this.commitCharacters = commitCharacters;
            this.command = command;
            this.data = data;
        }

        /**
	 * The label of this completion. By default also the text that is inserted
         * when selecting this completion.
	 */
        @NonNull
        public String getLabel() {
            return label;
        }

        /**
	 * The kind of this completion.
	 */
        @CheckForNull
        public Kind getKind() {
            return kind;
        }

        /**
         * Tags for this completion.
         */
        @CheckForNull
        public List<Tag> getTags() {
            return tags;
        }

        /**
	 * A human-readable string with additional information
	 * about this completion, like type or symbol information.
	 */
        @CheckForNull
        public String getDetail() {
            return detail;
        }

        /**
	 * A human-readable string that represents a doc-comment.
	 */
        @CheckForNull
        public String getDocumentation() {
            return documentation;
        }

        /**
	 * Select this completion when showing.
	 */
        public boolean isPreselect() {
            return preselect;
        }

        /**
	 * A string that should be used when comparing this completion with other
         * completions. When {@code null} the label is used as the sort text.
	 */
        @CheckForNull
        public String getSortText() {
            return sortText;
        }

        /**
	 * A string that should be used when filtering a set of completions.
         * When {@code null} the label is used as the filter.
	 */
        @CheckForNull
        public String getFilterText() {
            return filterText;
        }

        /**
	 * A string that should be inserted into a document when selecting
	 * this completion. When {@code null} the label is used as the insert text.
         */
        @CheckForNull
	public String getInsertText() {
            return insertText;
        }

        /**
	 * The format of the insert text. The format applies to both the
	 * {@code insertText} property and the {@code newText} property of a provided
	 * {@code textEdit}. If omitted defaults to {@link TextFormat#PlainText}.
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
	 */
        @CheckForNull
        public TextEdit getTextEdit() {
            return textEdit;
        }

        /**
	 * A list of additional text edits that are applied when selecting this
         * completion. Edits must not overlap (including the same insert position)
         * with the main edit nor with themselves.
	 * Additional text edits should be used to change text unrelated to the
	 * current cursor position (for example adding an import statement at the
	 * top of the file if the completion item will insert an unqualified type).
	 */
        @CheckForNull
        public List<TextEdit> getAdditionalTextEdits() {
            return additionalTextEdits;
        }

        /**
	 * A list of characters that when pressed while this completion is
	 * active will accept it first and then type that character. All
	 * commit characters should have length one and that superfluous
         * characters will be ignored.
	 */
        @CheckForNull
        public List<String> getCommitCharacters() {
            return commitCharacters;
        }

        /**
	 * A command that is executed after inserting this completion.
	 */
        @CheckForNull
        public Command getCommand() {
            return command;
        }

        /**
	 * A data entry to identify this completion.
	 */
        @CheckForNull
        public Object getData() {
            return data;
        }

        public static final class Builder {

            private final String label;
            private Kind kind;
            private List<Tag> tags;
            private String detail;
            private String documentation;
            private boolean preselect;
            private String sortText;
            private String filterText;
            private String insertText;
            private TextFormat insertTextFormat;
            private TextEdit textEdit;
            private List<TextEdit> additionalTextEdits;
            private List<String> commitCharacters;
            private Command command;
            private Object data;

            private Builder(@NonNull String label) {
                this.label = label;
            }

            /**
             * The kind of this completion.
             */
            @NonNull
            public Builder kind(@NonNull Kind kind) {
                this.kind = kind;
                return this;
            }

            /**
             * Tags for this completion.
             */
            @NonNull
            public Builder tags(@NonNull List<Tag> tags) {
                this.tags = tags;
                return this;
            }

            /**
             * A human-readable string with additional information
             * about this completion, like type or symbol information.
             */
            @NonNull
            public Builder detail(@NonNull String detail) {
                this.detail = detail;
                return this;
            }

            /**
             * A human-readable string that represents a doc-comment.
             */
            @NonNull
            public Builder documentation(@NonNull String documentation) {
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
            public Builder insertTextFormat(@NonNull TextFormat insertTextFormat) {
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
            public Builder additionalTextEdits(@NonNull List<TextEdit> additionalTextEdits) {
                this.additionalTextEdits = additionalTextEdits;
                return this;
            }

            /**
             * A list of characters that when pressed while this completion is
             * active will accept it first and then type that character. All
             * commit characters should have length one and that superfluous
             * characters will be ignored.
             */
            @NonNull
            public Builder commitCharacters(@NonNull List<String> commitCharacters) {
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
             * A data entry to identify this completion.
             */
            @NonNull
            public Builder data(@NonNull Object data) {
                this.data = data;
                return this;
            }

            /**
             * Builds completion.
             */
            @NonNull
            public Completion build() {
                return new Completion(label, kind, tags, detail, documentation, preselect,
                        sortText, filterText, insertText, insertTextFormat, textEdit,
                        additionalTextEdits, commitCharacters, command, data);
            }
        }
    }

    public static enum Kind {

        Text(1),
	Method(2),
	Function(3),
	Constructor(4),
	Field(5),
	Variable(6),
	Class(7),
	Interface(8),
	Module(9),
	Property(10),
	Unit(11),
	Value(12),
	Enum(13),
	Keyword(14),
	Snippet(15),
	Color(16),
	File(17),
	Reference(18),
	Folder(19),
	EnumMember(20),
	Constant(21),
	Struct(22),
	Event(23),
	Operator(24),
	TypeParameter(25);

        private final int value;

	private Kind(int value) {
            this.value = value;
	}

	public int getValue() {
	    return value;
	}
    }

    public static enum Tag {

        Deprecated(1);

        private final int value;

        private Tag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum TextFormat {

        /**
         * The primary text to be inserted is treated as a plain string.
         */
        PlainText(1),

        /**
         * The primary text to be inserted is treated as a snippet.
         */
        Snippet(2);

        private final int value;

        private TextFormat(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final class TextEdit {

        private final int start;
        private final int end;
        private final String newText;

        public TextEdit(int start, int end, @NonNull String newText) {
            this.start = start;
            this.end = end;
            this.newText = newText;
        }

        /**
	 * The start offset of the text document range to be manipulated. To insert
	 * text into a document create edit where {@code startOffset == endOffset}.
	 */
        public int getStartOffset() {
            return start;
        }

        /**
	 * The end offset of the text document range to be manipulated. To insert
	 * text into a document create edit where {@code startOffset == endOffset}.
	 */
        public int getEndOffset() {
            return end;
        }

        /**
         * The string to be inserted. For delete operations use an empty string.
         */
        @NonNull
        public String getNewText() {
            return newText;
        }
    }

    public static final class Command {

        private final String title;
        private final String command;
        private final List<Object> arguments;

        public Command(@NonNull String title, @NonNull String command, @NullAllowed List<Object> arguments) {
            this.title = title;
            this.command = command;
            this.arguments = arguments;
        }

        /**
         * Title of the command, like `save`.
         */
        @NonNull
        public String getTitle() {
            return title;
        }

        /**
	 * The identifier of the actual command handler.
	 */
        @NonNull
        public String getCommand() {
            return command;
        }

        /**
	 * Arguments that the command handler should be invoked with.
	 */
        @CheckForNull
        public List<Object> getArguments() {
            return arguments;
        }
    }
}
