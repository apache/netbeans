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
package org.netbeans.modules.javascript2.editor.formatter;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public final class FormatToken {

    private final Kind kind;

    private final int offset;

    private final CharSequence text;

    private final JsTokenId id;

    private FormatToken next;

    private FormatToken previous;

    private FormatToken(Kind kind, int offset, CharSequence text, JsTokenId id) {
        this.kind = kind;
        this.offset = offset;
        this.text = text;
        this.id = id;
    }

    public static FormatToken forText(int offset, CharSequence text, JsTokenId id) {
        assert text != null;
        assert offset >= 0 : offset;
        return new FormatToken(Kind.TEXT, offset, text, id);
    }

    public static FormatToken forFormat(Kind kind) {
        return new FormatToken(kind, -1, null, null);
    }

    public static FormatToken forAny(Kind kind, int offset, CharSequence text, JsTokenId id) {
        assert text != null;
        assert offset >= 0 : offset;
        return new FormatToken(kind, offset, text, id);
    }

    @NonNull
    public Kind getKind() {
        return kind;
    }

    @CheckForNull
    public JsTokenId getId() {
        return id;
    }

    @NonNull
    public CharSequence getText() {
        assert !isVirtual();
        return text != null ? text : ""; // NOI18N
    }

    public int getOffset() {
        return offset;
    }

    @CheckForNull
    public FormatToken next() {
        return next;
    }

    @CheckForNull
    public FormatToken previous() {
        return previous;
    }

    public boolean isVirtual() {
        return offset < 0;
    }

    @Override
    public String toString() {
        return "FormatToken{" + "kind=" + kind + ", offset=" + offset + ", text=" + text + ", id=" + id + '}';
    }

    void setNext(FormatToken next) {
        this.next = next;
    }

    void setPrevious(FormatToken previous) {
        this.previous = previous;
    }

    public static enum Kind {
        SOURCE_START {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        TEXT {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        WHITESPACE {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        EOL {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        LINE_COMMENT {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        DOC_COMMENT {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BLOCK_COMMENT {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        INDENTATION_INC {
            @Override
            public boolean isIndentationMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        ELSE_IF_INDENTATION_INC {
            @Override
            public boolean isIndentationMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        INDENTATION_DEC {
            @Override
            public boolean isIndentationMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        ELSE_IF_INDENTATION_DEC {
            @Override
            public boolean isIndentationMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        AFTER_STATEMENT {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_CASE {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        AFTER_BLOCK_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        ELSE_IF_AFTER_BLOCK_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        // for line wrap after comma separated var
        AFTER_VAR_DECLARATION {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        // for line wrap of parameters
        BEFORE_FUNCTION_DECLARATION_PARAMETER {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        // for line wrap of arguments
        BEFORE_FUNCTION_CALL_ARGUMENT {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        // separate line wrap options
        AFTER_IF_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_ELSE_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_WHILE_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_FOR_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_WITH_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_DO_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_FOR_TEST {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_FOR_MODIFY {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_CHAIN_CALL_DOT {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_CHAIN_CALL_DOT {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_BINARY_OPERATOR_WRAP {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_BINARY_OPERATOR_WRAP {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_ASSIGNMENT_OPERATOR_WRAP {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_ARROW_OPERATOR_WRAP {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_TERNARY_OPERATOR_WRAP {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_TERNARY_OPERATOR_WRAP {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_ARRAY_LITERAL_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_ARRAY_LITERAL_END {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_ARRAY_LITERAL_ITEM {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_OBJECT_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_OBJECT_END {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_CLASS_START {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_CLASS_END {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        BEFORE_CLASS_EXTENDS {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        // a bit special tokens to detect proper continuation
        AFTER_PROPERTY {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_ELEMENT {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_DECORATOR {
            @Override
            public boolean isLineWrapMarker() {
                return true;
            }
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        BEFORE_OBJECT {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        BEFORE_ARRAY {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        // around binary operator
        BEFORE_BINARY_OPERATOR,
        AFTER_BINARY_OPERATOR,

        // around assignment operator
        BEFORE_ASSIGNMENT_OPERATOR,
        AFTER_ASSIGNMENT_OPERATOR,

        // around arrow operator
        BEFORE_ARROW_OPERATOR,
        AFTER_ARROW_OPERATOR,

        // around property
        BEFORE_PROPERTY_OPERATOR,
        AFTER_PROPERTY_OPERATOR,

        // around comma
        BEFORE_COMMA,
        AFTER_COMMA,

        // around dot
        BEFORE_DOT,
        AFTER_DOT,

        // keywords with possible space before parentheses
        AFTER_IF_KEYWORD,
        AFTER_WHILE_KEYWORD,
        AFTER_FOR_KEYWORD,
        AFTER_WITH_KEYWORD,
        AFTER_SWITCH_KEYWORD,
        AFTER_CATCH_KEYWORD,

        // no parenthesis but we should handle that
        AFTER_VAR_KEYWORD,
        AFTER_NEW_KEYWORD,
        AFTER_TYPEOF_KEYWORD,

        // keywords with possible space before
        // or placed on the new line
        BEFORE_WHILE_KEYWORD {
            @Override
            public boolean isAlignmentMarker() {
                return true;
            }
        },

        BEFORE_ELSE_KEYWORD {
            @Override
            public boolean isAlignmentMarker() {
                return true;
            }
        },

        BEFORE_CATCH_KEYWORD {
            @Override
            public boolean isAlignmentMarker() {
                return true;
            }
        },

        BEFORE_FINALLY_KEYWORD {
            @Override
            public boolean isAlignmentMarker() {
                return true;
            }
        },

        BEFORE_SEMICOLON,
        AFTER_SEMICOLON,

        BEFORE_UNARY_OPERATOR,
        AFTER_UNARY_OPERATOR,

        BEFORE_TERNARY_OPERATOR,
        AFTER_TERNARY_OPERATOR,

        // only marks the arrow function start, no corresponding option
        BEFORE_ARROW_FUNCTION_DECLARATION {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        // marks the arrow block
        BEFORE_ARROW_BLOCK {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        // marks the arrow block
        AFTER_ARROW_BLOCK {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        BEFORE_ANONYMOUS_FUNCTION_DECLARATION,

        BEFORE_FUNCTION_DECLARATION,

        BEFORE_FUNCTION_CALL,

        // within parentheses
        AFTER_FUNCTION_DECLARATION_PARENTHESIS,
        BEFORE_FUNCTION_DECLARATION_PARENTHESIS,

        AFTER_FUNCTION_CALL_PARENTHESIS,
        BEFORE_FUNCTION_CALL_PARENTHESIS,

        BEFORE_IF_PARENTHESIS,
        AFTER_IF_PARENTHESIS,

        BEFORE_WHILE_PARENTHESIS,
        AFTER_WHILE_PARENTHESIS,

        BEFORE_FOR_PARENTHESIS,
        AFTER_FOR_PARENTHESIS,

        BEFORE_WITH_PARENTHESIS,
        AFTER_WITH_PARENTHESIS,

        BEFORE_SWITCH_PARENTHESIS,
        AFTER_SWITCH_PARENTHESIS,

        BEFORE_CATCH_PARENTHESIS,
        AFTER_CATCH_PARENTHESIS,

        BEFORE_RIGHT_PARENTHESIS,
        AFTER_LEFT_PARENTHESIS,

        BEFORE_RIGHT_BRACE,
        AFTER_LEFT_BRACE,

        // before braces
        BEFORE_FUNCTION_DECLARATION_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_CLASS_DECLARATION_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_IF_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_ELSE_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_WHILE_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_FOR_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_DO_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_SWITCH_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_TRY_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_CATCH_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_FINALLY_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        BEFORE_WITH_BRACE {
            @Override
            public boolean isBraceMarker() {
                return true;
            }
        },

        // placed after right curly brace to decrease indentation
        AFTER_END_BRACE {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },

        // array literal brackets
        AFTER_ARRAY_LITERAL_BRACKET,
        BEFORE_ARRAY_LITERAL_BRACKET,

        BEFORE_JSX_BLOCK_START {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        },
        AFTER_JSX_BLOCK_END {
            @Override
            public boolean isSpaceMarker() {
                return false;
            }
        };

        public boolean isLineWrapMarker() {
            return false;
        }

        public boolean isIndentationMarker() {
            return false;
        }

        public boolean isSpaceMarker() {
            return true;
        }

        public boolean isBraceMarker() {
            return false;
        }

        public boolean isAlignmentMarker() {
            return false;
        }
    }

}
