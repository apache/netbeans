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
package org.netbeans.modules.php.editor.sql;

import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

final class PHPSQLStatement {

    private final String generatedStatement;
    private int statementOffset;
    private final ArrayList<CodeBlockData> codeBlocks = new ArrayList<>();

    /**
     * Given a caret offset into a PHP document, compute the SQL statement for that
     * location, if any.
     *
     * @param document the PHP source document
     * @param caretOffset the caret location in the document
     * @return the resulting PHPSQLStatement, or null if none could be determined
     */
    public static PHPSQLStatement computeSQLStatement(final Document document, final int caretOffset) {
        final PHPSQLStatement[] result = {null};
        document.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<PHPTokenId> seq = LexUtilities.getPHPTokenSequence(document, caretOffset);
                if (seq == null) {
                    return;
                }

                PHPSQLStatement stmt = new PHPSQLStatement(seq, caretOffset);
                if (stmt.getStatement() != null) {
                    result[0] = stmt;
                }
            }
        });

        return result[0];
    }

    /**
     * Return true if this string could potentially be SQL.
     */
    public static boolean couldBeSQL(TokenSequence seq) {
        String potentialStatement = seq.token().text().toString();
        if (potentialStatement.startsWith("\"") || potentialStatement.startsWith("'")) {
            potentialStatement = potentialStatement.substring(1);
        }
        potentialStatement = potentialStatement.toLowerCase().trim();
        return potentialStatement.startsWith("select") || potentialStatement.startsWith("insert")
                || potentialStatement.startsWith("update") || potentialStatement.startsWith("delete")
                || potentialStatement.startsWith("drop");
    }

    private PHPSQLStatement(TokenSequence seq, int caretOffset) {
        this.generatedStatement = generateSQLStatement(seq, caretOffset);
    }

    public int getStatementOffset() {
        return statementOffset;
    }

    public String getStatement() {
        return generatedStatement;
    }

    public int generatedToSourcePos(int generatedOffset) {
        CodeBlockData codeBlock = getCodeBlockAtGeneratedOffset(generatedOffset);
        if (codeBlock == null) {
            return -1;
        }
        int offsetWithinBlock = generatedOffset - codeBlock.generatedStart;
        int sourceOffset = codeBlock.sourceStart + offsetWithinBlock;
        if (sourceOffset <= codeBlock.sourceEnd) {
            return sourceOffset;
        } else {
            return codeBlock.sourceEnd;
        }
    }

    public int sourceToGeneratedPos(int sourceOffset) {
        CodeBlockData codeBlock = getCodeBlockAtSourceOffset(sourceOffset);
        if (codeBlock == null) {
            return -1;
        }
        int generatedPos;
        int offsetWithinBlock = sourceOffset - codeBlock.sourceStart;
        int generatedOffset = codeBlock.generatedStart + offsetWithinBlock;
        if (generatedOffset <= codeBlock.generatedEnd) {
            generatedPos = generatedOffset;
        } else {
            generatedPos = codeBlock.generatedEnd;
        }
        return generatedPos;
    }

    /**
     * Takes a raw PHP statement and "cleans it up" for the SQL analyzer,
     * replacing non-literal-string bits with the '__UNKNOWN__' tag
     *
     * Also builds up an array of virtual to real offsets that is used to
     * map back to the raw string when we need to work with the original
     * PHP document
     *
     * @param rawStatement
     * @return
     */
    @org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH"})
    private String generateSQLStatement(TokenSequence<PHPTokenId> seq, int caretOffset) {
        statementOffset = StringFinder.findStringBegin(seq, caretOffset);
        if (statementOffset < 0) {
            return null;
        }

        int stringEnd = StringFinder.findStringEnd(seq, caretOffset);
        if (stringEnd < 0) {
            return null;
        }

        if (stringEnd <= statementOffset || caretOffset < statementOffset || caretOffset > stringEnd) {
            return null;
        }

        /** The buffer containing the generated SQL statement */
        StringBuffer buf = new StringBuffer();

        /** We're moving through a variable definition */
        boolean inVariable = false;

        boolean concatenating = false;


        // Move through stripping out anything that isn't a string literal and
        // replacing it with "__UNKNOWN__"
        seq.move(statementOffset);
        seq.moveNext();

        if (!couldBeSQL(seq)) {
            return null;
        }

        for (;;) {
            String text = seq.token().text().toString();

            outer:
            switch (seq.token().id()) {
                case PHP_ENCAPSED_AND_WHITESPACE:
                    concatenating = false;
                    if (inVariable) {
                        addUnknownCodeBlock(seq, buf);
                    } else {
                        addCodeBlock(seq, text, buf);
                    }
                    break;
                case PHP_CONSTANT_ENCAPSED_STRING:
                    if (lastBlockIsUnknown() && !concatenating) {
                        // If we see another string, but we're not concatenating
                        // right now, then this string is part of the unknown bit
                        addUnknownCodeBlock(seq, buf);
                        break;
                    } else if (inVariable) {
                        addUnknownCodeBlock(seq, buf);
                        break;
                    }

                    concatenating = false;

                    if ("\"".equals(text) || "\'".equals(text)) {
                        // Skip past quote string
                        skip(seq);
                        break;
                    }

                    if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("\'") && text.endsWith("\'"))) {
                        // Strip off the quotes and replace them with spaces
                        addCodeBlock(seq, " " + text.substring(1, text.length() - 1) + " ", buf);
                    } else if (text.startsWith("\"") || text.startsWith("'")) {
                        // This can happen if it's an incompleted string
                        addCodeBlock(seq, " " + text.substring(1), buf);
                    } else {
                        // We'll just assume this is useful stuff, it's likely we're
                        // getting an unquoted encapsed string because
                        // the string wasn't terminated yet (the user is still typing)
                        addCodeBlock(seq, text, buf);
                    }
                    break;
                case PHP_HEREDOC_TAG_START:
                case PHP_HEREDOC_TAG_END:
                case PHP_NOWDOC_TAG_START:
                case PHP_NOWDOC_TAG_END:
                    concatenating = false;
                    // fall through intentional
                case PHP_CLOSETAG:
                case WHITESPACE:
                    // These tokens should be ignored, but shouldn't create an "__UNKNOWN__" tag
                    skip(seq);
                    break;
                case UNKNOWN_TOKEN:
                    // probably unfinished statement
                    addCodeBlock(seq, text, buf);
                    break;
                default:
                    switch (seq.token().id()) {
                        case PHP_TOKEN:
                            switch (text) {
                                case "${":
                                case "$":
                                    // this is the beginning of a variable
                                    inVariable = true;
                                    break;
                                case ".":
                                    concatenating = true;
                                    skip(seq);
                                    break outer;
                            }

                            concatenating = false;
                            break;
                        case PHP_CURLY_CLOSE:
                            concatenating = false;
                            inVariable = false;
                            break;
                        default:
                            // no-op
                    }
                    addUnknownCodeBlock(seq, buf);
                    break;
            }
            if (!seq.moveNext() || seq.offset() >= stringEnd) {
                break;
            }
        }
        return buf.toString();
    }

    private boolean lastBlockIsUnknown() {
        CodeBlockData previous = getLastCodeBlock();
        return previous != null && previous.isUnknown;
    }

    private void addCodeBlock(TokenSequence seq, String generatedString, StringBuffer buf) {
        int generatedStart = buf.length();
        int sourceStart = seq.offset();
        int sourceEnd = sourceStart + seq.token().length();
        int generatedEnd = generatedString.length() + generatedStart;
        CodeBlockData data = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd,
                generatedString.equals(SQLCompletion.UNKNOWN_TAG));
        codeBlocks.add(data);
        buf.append(generatedString);
    }

    private void skip(TokenSequence seq) {
        CodeBlockData previous = getLastCodeBlock();
        if (previous != null) {
            previous.sourceEnd = seq.offset() + seq.token().length();
        } else {
            CodeBlockData data = new CodeBlockData(seq.offset(), seq.offset() + seq.token().length(), 0, 0, false);
            codeBlocks.add(data);
        }
    }

    private CodeBlockData getLastCodeBlock() {
        if (codeBlocks.size() > 0) {
            return codeBlocks.get(codeBlocks.size() - 1);
        } else {
            return null;
        }
    }

    private void addUnknownCodeBlock(TokenSequence seq, StringBuffer buf) {
        CodeBlockData codeBlock = getLastCodeBlock();
        if (codeBlock != null && codeBlock.isUnknown) {
            skip(seq);
        } else {
            addCodeBlock(seq, SQLCompletion.UNKNOWN_TAG, buf);
        }

    }

    private CodeBlockData getCodeBlockAtGeneratedOffset(int generatedOffset) {
        for (CodeBlockData codeBlock : codeBlocks) {
            if (codeBlock.generatedStart <= generatedOffset && codeBlock.generatedEnd >= generatedOffset) {
                return codeBlock;
            }
        }
        return null;
    }

    private CodeBlockData getCodeBlockAtSourceOffset(int sourceOffset) {
        for (CodeBlockData codeBlock : codeBlocks) {
            if (codeBlock.sourceStart <= sourceOffset && codeBlock.sourceEnd >= sourceOffset) {
                return codeBlock;
            }
        }
        return null;
    }

    private static class CodeBlockData {
        /** Start of section in PHP file */
        private int sourceStart;
        /** End of section in PHP file */
        private int sourceEnd;
        /** Start of section in generated SQL */
        private int generatedStart;
        /** End of section in generated SQL */
        private int generatedEnd;
        /** Indicates if this block represents an UNKNOWN tag */
        private boolean isUnknown;

        public CodeBlockData(int sourceStart, int sourceEnd, int generatedStart, int generatedEnd, boolean isUnknown) {
            this.sourceStart = sourceStart;
            this.generatedStart = generatedStart;
            this.sourceEnd = sourceEnd;
            this.generatedEnd = generatedEnd;
            this.isUnknown = isUnknown;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CodeBlockData[");
            sb.append("\n  SOURCE(").append(sourceStart).append(",").append(sourceEnd).append(")");
            sb.append(",\n  SQL(").append(generatedStart).append(",").append(generatedEnd).append(")");
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Responsible for finding a complete string in PHP code, based on a supplied offset
     */
    private static class StringFinder {

        private enum StringState {

            STARTING, VARSUB_STRING, CONCATENATING, MAYBE_SUBSTRING_TERM, SUBSTRING_TERM
        }

        private enum StringDirection {

            FORWARD, BACKWARD
        }

        static int findStringBegin(TokenSequence<PHPTokenId> seq, int offset) {
            int startOffset = StringFinder.findStringTerminationOffset(seq, offset, StringDirection.BACKWARD);
            if (startOffset == -1) {
                return -1;
            }

            seq.move(startOffset);
            if (!seq.moveNext()) {
                return -1;
            }

            String text = seq.token().text().toString();
            if (text.equals("\"") || text.equals("\'") || seq.token().id() == PHPTokenId.PHP_HEREDOC_TAG_START) {
                seq.moveNext();
            }

            return seq.offset();
        }

        static int findStringEnd(TokenSequence<PHPTokenId> seq, int offset) {
            int endOffset = StringFinder.findStringTerminationOffset(seq, offset, StringDirection.FORWARD);
            if (endOffset == -1) {
                return -1;
            }
            seq.move(endOffset);
            seq.movePrevious();
            String text = seq.token().text().toString();
            if (text.equals("\"") || text.equals("\'") || seq.token().id() == PHPTokenId.PHP_HEREDOC_TAG_END) {
                seq.movePrevious();
            }
            return seq.offset() + seq.token().length();
        }

        /*
         * Finds the termination offset of a PHP string based on the given offset.
         * It returns a -1 if a string cannot be found in the PHP statement at the
         * given offset.  This includes SQL strings that are a series of concatenations
         * of string literals and PHP variables.
         *
         * See the unit tests for a full set of examples of string formats that we support
         */
        private static int findStringTerminationOffset(TokenSequence<PHPTokenId> seq, int offset, StringDirection direction) {
            int result = -1;
            seq.move(offset);
            if (!seq.moveNext() && !seq.movePrevious()) {
                return result;
            }


            // A string "termination" means the end of the string in the direction we want to go.
            // So if we're going forward, the termination is the end of the string, but *not*
            // beginning, and vice versa.
            StringState state = StringState.STARTING;
            int substringTermOffset = -1;
            outer:
            for (;;) {
                String text = seq.token().text().toString();
                switch (seq.token().id()) {
                    case PHP_CONSTANT_ENCAPSED_STRING:
                        switch (state) {
                            case STARTING:
                                // This can be the termination of the string (in the direction we want to go)
                                // or it could be the the "other" terminator of a variable substitution string.
                                substringTermOffset = getOffset(seq, direction);
                                state = StringState.SUBSTRING_TERM;
                                break;
                            case SUBSTRING_TERM:
                                // OK, we're at a new string without hitting concatenation,
                                // so we're done
                                result = substringTermOffset;
                                break outer;
                            case MAYBE_SUBSTRING_TERM:
                                // We could be at the termination of a variable substitution string
                                // *or*
                                // we could have moved from the termination of one substring to the "other" terminator
                                // of the next one.  So we still are in the same state: maybe at the termination
                                // of a string.  But let's reset the offset...
                                substringTermOffset = getOffset(seq, direction);
                                break;
                            case VARSUB_STRING:
                                // This is the termination of a variable substitution string,
                                // but we may not be done - there may be concatenation operators
                                state = StringState.SUBSTRING_TERM;
                                substringTermOffset = getOffset(seq, direction);
                                break;
                            case CONCATENATING:
                                // We've just hit another substring.  See if it's a literal string,
                                // in which case the token encompasses the full string, or if it's
                                // the "other" terminator for a variable substitution string...
                                if (text.equals("\"") || text.equals("\'")) {
                                    state = StringState.VARSUB_STRING;
                                } else {
                                    state = StringState.SUBSTRING_TERM;
                                    substringTermOffset = getOffset(seq, direction);
                                }
                                break;
                            default:
                                //no-op
                        }
                        break;
                    case PHP_ENCAPSED_AND_WHITESPACE:
                        switch (state) {
                            case STARTING:
                            case SUBSTRING_TERM:
                                // OK, now we *know* we're in a variable substitution string
                                state = StringState.VARSUB_STRING;
                                break;
                            case VARSUB_STRING:
                                // Still in this state...
                                break;
                            case MAYBE_SUBSTRING_TERM:
                            case CONCATENATING:
                            default:
                                // Unexpected.  Let's at least provide the offset for the
                                // last full string we saw, if any.
                                result = substringTermOffset;
                                break outer;
                        }
                        break;
                    case PHP_HEREDOC_TAG_START:
                    case PHP_HEREDOC_TAG_END:
                    case PHP_NOWDOC_TAG_START:
                    case PHP_NOWDOC_TAG_END:
                        switch (state) {
                            case STARTING:
                                // Not inside a string, but possible concatination xDOCs with outer common strings
                            case VARSUB_STRING:
                                // Not done yet, you can concatenate heredocs too, you know...
                                state = StringState.SUBSTRING_TERM;
                                substringTermOffset = getOffset(seq, direction);
                                break;
                            case SUBSTRING_TERM:
                                // This can happen if the heredoc contains a literal with no variables
                                // But we're not done, concatenation can still happen.
                                break;
                            case MAYBE_SUBSTRING_TERM:
                                // That was definitely the termination of a string we saw...
                                state = StringState.SUBSTRING_TERM;
                                break;
                            case CONCATENATING:
                                break;
                            default:
                                //no-op
                        }
                        break;
                    case PHP_SEMICOLON:
                    case PHP_OPENTAG:
                        result = substringTermOffset;
                        break outer;
                    case PHP_TOKEN:
                        switch (state) {
                            case STARTING:
                                // If it's ${, this could be OK, we might be in a var substring
                                // Actually, I think you *only* get this inside a var substring
                                if (!text.equals("${")) {
                                    // NOI18N
                                    // Doesn't look like we're in a string
                                    break outer;
                                }
                                break;
                            case SUBSTRING_TERM:
                            case MAYBE_SUBSTRING_TERM:
                                if (text.equals(".")) {
                                    // NOI18N
                                    state = StringState.CONCATENATING;
                                }
                                // If it's not a concatenation, keep going, there can be other tokens
                                // between the string start and a concatenation token (not sure exactly what,
                                // but it's been known to happen...  We're being lenient, remember? :))
                                break;
                            case VARSUB_STRING:
                            case CONCATENATING:
                                // Keep going...
                                break;
                            default:
                                //no-op
                        }
                        break;
                    case PHP_CURLY_OPEN:
                    case PHP_CURLY_CLOSE:
                    case PHP_VARIABLE:
                    case PHP_VAR:
                    case PHP_STRING:
                        switch (state) {
                            case STARTING:
                            // Could be inside a variable substitution string
                            case VARSUB_STRING:
                            // Keep going, looking for the termination of the string
                            case CONCATENATING:
                            // Keep going...
                            case SUBSTRING_TERM:
                            // We could be inside a variable substitution string, or not.
                            // Let's be lenient, assume we are, and keep going...
                            case MAYBE_SUBSTRING_TERM:
                                // Keep going, looking for that potential concatenation token...
                                break;
                            default:
                                //no-op
                        }
                        break;
                    case UNKNOWN_TOKEN:
                        // probably unfinished statement
                        substringTermOffset = getOffset(seq, direction);
                        state = StringState.SUBSTRING_TERM;
                        break;
                    default:
                        switch (state) {
                            case CONCATENATING:
                            // Skip any tokens besides strings while we're
                            // concatenating
                            case VARSUB_STRING:
                            // Unexpected, but keep going...
                            case SUBSTRING_TERM:
                                // Keep going, looking to see if there's another
                                // concatenation operator within this statement...
                                break;
                            case STARTING:
                                // Not in a string
                                break outer;
                            default:
                                //no-op
                        }
                        break;
                }
                if (direction == StringDirection.FORWARD) {
                    if (!seq.moveNext()) {
                        if (substringTermOffset == -1) {
                            // We may have an unterminated string,
                            // so let's just terminate it at the end...
                            result = getOffset(seq, direction);
                        } else {
                            result = substringTermOffset;
                        }
                        break;
                    }
                } else if (!seq.movePrevious()) {
                    result = substringTermOffset;
                    break;
                }
            }
            return result;
        }

        private static int getOffset(TokenSequence seq, StringDirection direction) {
            if (direction == StringDirection.BACKWARD) {
                return seq.offset();
            } else {
                return seq.offset() + seq.token().length();
            }
        }
    }
}
