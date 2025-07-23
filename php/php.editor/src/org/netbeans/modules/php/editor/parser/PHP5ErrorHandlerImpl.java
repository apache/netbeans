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

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java_cup.runtime.Symbol;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.parser.GSFPHPParser.Context;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl, Ondrej Brejla
 */
public class PHP5ErrorHandlerImpl implements PHP5ErrorHandler {

    private final List<SyntaxError> syntaxErrors;
    private final Context context;
    private volatile boolean handleErrors = true;

    public PHP5ErrorHandlerImpl(Context context) {
        super();
        this.context = context;
        syntaxErrors = new ArrayList<>();
    }

    @Override
    public void handleError(Type type, short[] expectedtokens, Symbol current, Symbol previous) {
        if (handleErrors) {
            if (type == ParserErrorHandler.Type.SYNTAX_ERROR) {
                SyntaxErrorLogger.log(expectedtokens, current, previous);
                SyntaxError.Type syntaxErrorType = SyntaxError.Type.POSSIBLE_ERROR;
                if (syntaxErrors.isEmpty()) {
                    syntaxErrorType = SyntaxError.Type.FIRST_VALID_ERROR;
                }
                syntaxErrors.add(new SyntaxError(expectedtokens, current, previous, syntaxErrorType));
            }
        }
    }

    @Override
    public void disableHandling() {
        handleErrors = false;
    }

    @Override
    public List<Error> displayFatalError() {
        return Arrays.asList((Error) new FatalError(context));
    }

    @Override
    public List<Error> displaySyntaxErrors(Program program) {
        List<Error> errors = new ArrayList<>();
        for (SyntaxError syntaxError : syntaxErrors) {
            errors.add(defaultSyntaxErrorHandling(syntaxError));
        }
        return errors;
    }

    @NbBundle.Messages("SE_Expected=expected")
    private Error defaultSyntaxErrorHandling(SyntaxError syntaxError) {
        StringBuilder message = new StringBuilder();
        Symbol currentToken = syntaxError.getCurrentToken();
        message.append(syntaxError.getMessageHeader());
        message.append(TokenWrapper.create(currentToken).createUnexpectedMessage());
        if (syntaxError.generateExtraInfo()) {
            message.append(TokenWrapper.create(syntaxError.getPreviousToken()).createAfterText());
            List<String> possibleTags = getExpectedTokenNames(syntaxError);
            if (possibleTags.size() > 0) {
                message.append(createExpectedTokensText(possibleTags));
            }
        }
        return new GSFPHPError(
                message.toString(),
                context.getSnapshot().getSource().getFileObject(),
                currentToken.left,
                currentToken.right,
                syntaxError.getSeverity(),
                new Object[]{syntaxError});
    }

    private static List<String> getExpectedTokenNames(SyntaxError syntaxError) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < syntaxError.getExpectedTokens().length; i += 2) {
            String text = TokenWrapper.getTokenTextForm(syntaxError.getExpectedTokens()[i]);
            if (text != null) {
                result.add(text);
            }
        }
        return result;
    }

    private static String createExpectedTokensText(List<String> expectedTokenNames) {
        StringBuilder message = new StringBuilder();
        message.append("\n ").append(Bundle.SE_Expected()); //NOI18N
        message.append(":\t"); //NOI18N
        boolean addOR = false;
        for (String tag : expectedTokenNames) {
            if (addOR) {
                message.append(", "); //NOI18N
            } else {
                addOR = true;
            }
            message.append(tag);
        }
        return message.toString();
    }

    @Override
    public List<SyntaxError> getSyntaxErrors() {
        return syntaxErrors;
    }

    private static class SyntaxErrorLogger {

        private static final Logger LOGGER = Logger.getLogger(SyntaxErrorLogger.class.getName());

        public static void log(short[] expectedtokens, Symbol current, Symbol previous) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Syntax error:"); //NOI18N
                LOGGER.log(
                        Level.FINEST,
                        "Current [{0}, {1}]({2}): {3}", //NOI18N
                        new Object[]{current.left, current.right, Utils.getASTScannerTokenName(current.sym), current.value});
                LOGGER.log(
                        Level.FINEST,
                        "Previous [{0}, {1}] ({2}):{3}", //NOI18N
                        new Object[]{previous.left, previous.right, Utils.getASTScannerTokenName(previous.sym), previous.value});
                StringBuilder message = new StringBuilder();
                message.append("Expected tokens:"); //NOI18N
                for (int i = 0; i < expectedtokens.length; i += 2) {
                    message.append(" ").append(Utils.getASTScannerTokenName(expectedtokens[i])); //NOI18N
                }
                LOGGER.finest(message.toString());
            }
        }

    }

    private static final class TokenWrapper {
        private final Symbol token;

        public static TokenWrapper create(Symbol token) {
            assert token != null;
            return new TokenWrapper(token);
        }

        private TokenWrapper(Symbol token) {
            this.token = token;
        }

        public boolean isCommonToken() {
            return (!(token.value instanceof List) && !(token.value instanceof ASTNode));
        }

        public boolean isNodeToken() {
            return (token.value instanceof ASTNode);
        }

        @NbBundle.Messages({
            "SE_Unexpected=unexpected",
            "SE_EOF=End of File"
        })
        public String createUnexpectedMessage() {
            String result; //NOI18N
            String unexpectedText = null;
            if (token.sym == ASTPHP5Symbols.EOF) {
                unexpectedText = Bundle.SE_EOF();
            } else if (isValuableToken(token)) {
                unexpectedText = getTokenTextForm(token.sym) + " '" + String.valueOf(token.value) + "'";
            } else {
                String currentText = getTokenTextForm(token.sym);
                if (StringUtils.hasText(currentText)) {
                    unexpectedText = currentText.trim();
                }
            }
            if (unexpectedText == null) {
                result = ""; //NOI18N
            } else {
                result = "\n " + Bundle.SE_Unexpected() + ":\t" + unexpectedText; //NOI18N
            }
            return result;
        }

        @NbBundle.Messages("SE_After=after")
        public String createAfterText() {
            String result;
            String afterText = null;
            if (isValuableToken(token)) {
                afterText = getTokenTextForm(token.sym) + " '" + String.valueOf(token.value) + "'";
            } else {
                if (!isNodeToken()) {
                    String previousText = getTokenTextForm(token.sym);
                    if (StringUtils.hasText(previousText)) {
                        afterText = previousText.trim();
                    }
                }
            }
            if (afterText == null) {
                result = ""; //NOI18N
            } else {
                result = "\n " + Bundle.SE_After() + ":\t" + afterText; //NOI18N
            }
            return result;
        }

        private static boolean isValuableToken(Symbol token) {
            return (token.sym == ASTPHP5Symbols.T_STRING || token.sym == ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING
                    || token.sym == ASTPHP5Symbols.T_DNUMBER || token.sym == ASTPHP5Symbols.T_LNUMBER
                    || token.sym == ASTPHP5Symbols.T_VARIABLE) && !(token.value instanceof ASTNode) && !(token.value instanceof List);
        }

        public static String getTokenTextForm(int token) {
            String text = null;
            switch (token) {
                // order: see ASTPHP5Symbols
                case ASTPHP5Symbols.T_BOOLEAN_AND -> text = "&&"; // NOI18N
                case ASTPHP5Symbols.T_INLINE_HTML -> text = "inline html"; // NOI18N
                case ASTPHP5Symbols.T_EMPTY -> text = "empty"; // NOI18N
                case ASTPHP5Symbols.T_PROTECTED -> text = "protected"; // NOI18N
                case ASTPHP5Symbols.T_CLOSE_RECT -> text = "]"; // NOI18N
                case ASTPHP5Symbols.T_TRAIT_C -> text = "__TRAIT__"; // NOI18N
                case ASTPHP5Symbols.T_IS_NOT_EQUAL -> text = "!="; // NOI18N
                case ASTPHP5Symbols.T_INCLUDE -> text = "include"; // NOI18N
                case ASTPHP5Symbols.T_QUATE -> text = "'\"'"; // NOI18N
                case ASTPHP5Symbols.T_GLOBAL -> text = "global"; // NOI18N
                case ASTPHP5Symbols.T_PRINT -> text = "print"; // NOI18N
                case ASTPHP5Symbols.T_ATTRIBUTE -> text = "#["; // NOI18N
                case ASTPHP5Symbols.T_OR_EQUAL -> text = "|="; // NOI18N
                case ASTPHP5Symbols.T_LOGICAL_XOR -> text = "XOR"; // NOI18N
                case ASTPHP5Symbols.T_COALESCE -> text = "??"; // NOI18N
                case ASTPHP5Symbols.T_FUNCTION -> text = "function"; // NOI18N
                case ASTPHP5Symbols.T_STATIC -> text = "static"; // NOI18N
                case ASTPHP5Symbols.T_NEKUDA -> text = "'.'"; // NOI18N
                case ASTPHP5Symbols.T_THROW -> text = "throw"; // NOI18N
                case ASTPHP5Symbols.T_CLASS -> text = "class"; // NOI18N
                case ASTPHP5Symbols.T_ABSTRACT -> text = "abstract"; // NOI18N
                case ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE -> text = "String"; // NOI18N
                case ASTPHP5Symbols.T_MOD_EQUAL -> text = "%="; // NOI18N
                case ASTPHP5Symbols.T_BREAK -> text = "break"; // NOI18N
                case ASTPHP5Symbols.T_WHILE -> text = "while"; // NOI18N
                case ASTPHP5Symbols.T_DO -> text = "do"; // NOI18N
                case ASTPHP5Symbols.T_CONST -> text = "const"; // NOI18N
                case ASTPHP5Symbols.T_CONTINUE -> text = "continue"; // NOI18N
                case ASTPHP5Symbols.T_FUNC_C -> text = "__FUNCTION__"; // NOI18N
                case ASTPHP5Symbols.T_DIV -> text = "/"; // NOI18N
                case ASTPHP5Symbols.T_LOGICAL_OR -> text = "OR"; // NOI18N
                case ASTPHP5Symbols.T_DIR -> text = "__DIR__"; // NOI18N
                case ASTPHP5Symbols.T_OPEN_PARENTHESE -> text = "("; // NOI18N
                case ASTPHP5Symbols.T_REFERENCE -> text = "&"; // NOI18N
                case ASTPHP5Symbols.T_COMMA -> text = "','"; // NOI18N
                case ASTPHP5Symbols.T_FINALLY -> text = "finally"; // NOI18N
                case ASTPHP5Symbols.T_ELSE -> text = "else"; // NOI18N
                case ASTPHP5Symbols.T_IS_EQUAL -> text = "=="; // NOI18N
                case ASTPHP5Symbols.T_LIST -> text = "list"; // NOI18N
                case ASTPHP5Symbols.T_NAMESPACE -> text = "namespace"; // NOI18N
                case ASTPHP5Symbols.T_NS_SEPARATOR -> text = "\\"; // NOI18N
                case ASTPHP5Symbols.T_OR -> text = "|"; // NOI18N
                case ASTPHP5Symbols.T_IS_IDENTICAL -> text = "==="; // NOI18N
                case ASTPHP5Symbols.T_INC -> text = "++"; // NOI18N
                case ASTPHP5Symbols.T_ELSEIF -> text = "elseif"; // NOI18N
                case ASTPHP5Symbols.T_NAME_FULLY_QUALIFIED -> text = "\\...(\\...)"; // NOI18N
                case ASTPHP5Symbols.T_TRY -> text = "try"; // NOI18N
                case ASTPHP5Symbols.T_START_NOWDOC -> text = "<<<'...'"; // NOI18N
                case ASTPHP5Symbols.T_PRIVATE -> text = "private"; // NOI18N
                case ASTPHP5Symbols.T_UNSET_CAST -> text = "(unset)"; // NOI18N
                case ASTPHP5Symbols.T_NAME_QUALIFIED -> text = "...\\..."; // NOI18N
                case ASTPHP5Symbols.T_INCLUDE_ONCE -> text = "include_once"; // NOI18N
                case ASTPHP5Symbols.T_ENDIF -> text = "endif"; // NOI18N
                case ASTPHP5Symbols.T_SR_EQUAL -> text = ">>="; // NOI18N
                case ASTPHP5Symbols.EOF -> text = "EOF"; // NOI18N
                case ASTPHP5Symbols.T_CALLABLE -> text = "callable"; // NOI18N
                case ASTPHP5Symbols.T_PUBLIC -> text = "public"; // NOI18N
                case ASTPHP5Symbols.T_OBJECT_OPERATOR -> text = "->"; // NOI18N
                case ASTPHP5Symbols.T_TILDA -> text = "~"; // NOI18N
                case ASTPHP5Symbols.T_PAAMAYIM_NEKUDOTAYIM -> text = "::"; // NOI18N
                case ASTPHP5Symbols.T_IS_SMALLER_OR_EQUAL -> text = "<="; // NOI18N
                case ASTPHP5Symbols.T_ELLIPSIS -> text = "..."; // NOI18N
                case ASTPHP5Symbols.T_XOR_EQUAL -> text = "^="; // NOI18N
                case ASTPHP5Symbols.T_ENDFOREACH -> text = "endforeach"; // NOI18N
                case ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING -> text = "String"; // NOI18N
                case ASTPHP5Symbols.T_BACKQUATE -> text = "'`'"; // NOI18N
                case ASTPHP5Symbols.T_AT -> text = "@"; // NOI18N
                case ASTPHP5Symbols.T_AS -> text = "as"; // NOI18N
                case ASTPHP5Symbols.T_CURLY_CLOSE -> text = "}"; // NOI18N
                case ASTPHP5Symbols.T_ENDDECLARE -> text = "enddeclare"; // NOI18N
                case ASTPHP5Symbols.T_PROTECTED_SET -> text = "protected(set)"; // NOI18N
                case ASTPHP5Symbols.T_CATCH -> text = "catch"; // NOI18N
                case ASTPHP5Symbols.T_CASE -> text = "case"; // NOI18N
                case ASTPHP5Symbols.T_VARIABLE -> text = "variable"; // NOI18N
                case ASTPHP5Symbols.T_INSTEADOF -> text = "insteadof"; // NOI18N
                case ASTPHP5Symbols.T_NEW -> text = "new"; // NOI18N
                case ASTPHP5Symbols.T_MINUS_EQUAL -> text = "-="; // NOI18N
                case ASTPHP5Symbols.T_PLUS -> text = "+"; // NOI18N
                case ASTPHP5Symbols.T_SL_EQUAL -> text = "<<="; // NOI18N
                case ASTPHP5Symbols.T_ENDWHILE -> text = "endwhile"; // NOI18N
                case ASTPHP5Symbols.T_ENDFOR -> text = "endfor"; // NOI18N
                case ASTPHP5Symbols.T_TRAIT -> text = "trait"; // NOI18N
                case ASTPHP5Symbols.T_CLONE -> text = "clone"; // NOI18N
                case ASTPHP5Symbols.T_BOOLEAN_OR -> text = "||"; // NOI18N
                case ASTPHP5Symbols.T_UNSET -> text = "unset"; // NOI18N
                case ASTPHP5Symbols.T_INTERFACE -> text = "interface"; // NOI18N
                case ASTPHP5Symbols.T_SWITCH -> text = "switch"; // NOI18N
                case ASTPHP5Symbols.T_IS_GREATER_OR_EQUAL -> text = ">="; // NOI18N
                case ASTPHP5Symbols.T_OPEN_RECT -> text = "["; // NOI18N
                case ASTPHP5Symbols.T_CURLY_OPEN_WITH_DOLAR -> text = "{$"; // NOI18N
                case ASTPHP5Symbols.T_FINAL -> text = "final"; // NOI18N
                case ASTPHP5Symbols.T_REQUIRE -> text = "require"; // NOI18N
                case ASTPHP5Symbols.T_PRIVATE_SET -> text = "private(set)"; // NOI18N
                case ASTPHP5Symbols.T_FILE -> text = "__FILE__"; // NOI18N
                case ASTPHP5Symbols.T_DEC -> text = "--"; // NOI18N
                case ASTPHP5Symbols.T_CLOSE_PARENTHESE -> text = ")"; // NOI18N
                case ASTPHP5Symbols.T_CLASS_C -> text = "__CLASS__"; // NOI18N
                case ASTPHP5Symbols.T_EVAL -> text = "eval"; // NOI18N
                case ASTPHP5Symbols.T_MATCH -> text = "match"; // NOI18N
                case ASTPHP5Symbols.T_POW -> text = "**"; // NOI18N
                case ASTPHP5Symbols.T_RGREATER -> text = "<"; // NOI18N
                case ASTPHP5Symbols.T_AMPERSAND_NOT_FOLLOWED_BY_VAR_OR_VARARG -> text = "&"; // NOI18N
                case ASTPHP5Symbols.T_IS_NOT_IDENTICAL -> text = "!=="; // NOI18N
                case ASTPHP5Symbols.T_NOT -> text = "!"; // NOI18N
                case ASTPHP5Symbols.T_REQUIRE_ONCE -> text = "require_once"; // NOI18N
                case ASTPHP5Symbols.T_POW_EQUAL -> text = "**="; // NOI18N
                case ASTPHP5Symbols.T_NS_C -> text = "__NAMESPACE__"; // NOI18N
                case ASTPHP5Symbols.T_NULLSAFE_OBJECT_OPERATOR -> text = "?->"; // NOI18N
                case ASTPHP5Symbols.T_DOLLAR_OPEN_CURLY_BRACES -> text = "${"; // NOI18N
                case ASTPHP5Symbols.T_SPACESHIP -> text = "<=>"; // NOI18N
                case ASTPHP5Symbols.T_VAR -> text = "var"; // NOI18N
                case ASTPHP5Symbols.T_START_HEREDOC -> text = "<<<\"...\""; // NOI18N
                case ASTPHP5Symbols.T_ENDSWITCH -> text = "endswitch"; // NOI18N
                case ASTPHP5Symbols.T_OBJECT_CAST -> text = "(object)"; // NOI18N
                case ASTPHP5Symbols.T_ECHO -> text = "echo"; // NOI18N
                case ASTPHP5Symbols.T_LINE -> text = "__LINE__"; // NOI18N
                case ASTPHP5Symbols.T_FOR -> text = "for"; // NOI18N
                case ASTPHP5Symbols.T_NAME_RELATIVE -> text = "namespace\\..."; // NOI18N
                case ASTPHP5Symbols.T_IMPLEMENTS -> text = "implements"; // NOI18N
                case ASTPHP5Symbols.T_ARRAY_CAST -> text = "(array)"; // NOI18N
                case ASTPHP5Symbols.T_DOLLAR -> text = "$"; // NOI18N
                case ASTPHP5Symbols.T_TIMES -> text = "*"; // NOI18N
                case ASTPHP5Symbols.T_DOUBLE_CAST -> text = "(double)"; // NOI18N
                case ASTPHP5Symbols.T_BOOL_CAST -> text = "(bool)"; // NOI18N
                case ASTPHP5Symbols.T_PRECENT -> text = "%"; // NOI18N
                case ASTPHP5Symbols.T_LNUMBER -> text = "integer"; // NOI18N
                case ASTPHP5Symbols.T_CURLY_OPEN -> text = "{"; // NOI18N
                case ASTPHP5Symbols.T_DEFINE -> text = "define"; // NOI18N
                case ASTPHP5Symbols.T_QUESTION_MARK -> text = "?"; // NOI18N
                case ASTPHP5Symbols.T_END_NOWDOC -> text = "END_NOWDOC"; // NOI18N
                case ASTPHP5Symbols.T_USE -> text = "use"; // NOI18N
                case ASTPHP5Symbols.T_KOVA -> text = "^"; // NOI18N
                case ASTPHP5Symbols.T_IF -> text = "if"; // NOI18N
                case ASTPHP5Symbols.T_MUL_EQUAL -> text = "*="; // NOI18N
                case ASTPHP5Symbols.T_ARRAY -> text = "array"; // NOI18N
                case ASTPHP5Symbols.T_LGREATER -> text = ">"; // NOI18N
                case ASTPHP5Symbols.T_COALESCE_EQUAL -> text = "??="; // NOI18N
                case ASTPHP5Symbols.T_SEMICOLON -> text = ";"; // NOI18N
                case ASTPHP5Symbols.T_NEKUDOTAIM -> text = ":"; // NOI18N
                case ASTPHP5Symbols.T_VAR_COMMENT -> text = "VAR_COMMENT"; // NOI18N
                case ASTPHP5Symbols.T_CONCAT_EQUAL -> text = ".="; // NOI18N
                case ASTPHP5Symbols.T_YIELD -> text = "yield"; // NOI18N
                case ASTPHP5Symbols.T_AND_EQUAL -> text = "&="; // NOI18N
                case ASTPHP5Symbols.T_DNUMBER -> text = "double"; // NOI18N
                case ASTPHP5Symbols.T_MINUS -> text = "-"; // NOI18N
                case ASTPHP5Symbols.T_FOREACH -> text = "foreach"; // NOI18N
                case ASTPHP5Symbols.T_EXIT -> text = "exit"; // NOI18N
                case ASTPHP5Symbols.T_DECLARE -> text = "declare"; // NOI18N
                case ASTPHP5Symbols.T_STRING_VARNAME -> text = "STRING_VARNAME"; // NOI18N
                case ASTPHP5Symbols.T_EXTENDS -> text = "extends"; // NOI18N
                case ASTPHP5Symbols.T_METHOD_C -> text = "__METHOD__"; // NOI18N
                case ASTPHP5Symbols.T_INT_CAST -> text = "(int)"; // NOI18N
                case ASTPHP5Symbols.T_ISSET -> text = "isset"; // NOI18N
                case ASTPHP5Symbols.T_LOGICAL_AND -> text = "&&"; // NOI18N
                case ASTPHP5Symbols.error -> text = null;
                case ASTPHP5Symbols.T_RETURN -> text = "return"; // NOI18N
                case ASTPHP5Symbols.T_DEFAULT -> text = "default"; // NOI18N
                case ASTPHP5Symbols.T_SR -> text = ">>"; // NOI18N
                case ASTPHP5Symbols.T_YIELD_FROM -> text = "yield from"; // NOI18N
                case ASTPHP5Symbols.T_EQUAL -> text = "="; // NOI18N
                case ASTPHP5Symbols.T_SL -> text = "<<"; // NOI18N
                case ASTPHP5Symbols.T_END_HEREDOC -> text = "END_HEREDOC"; // NOI18N
                case ASTPHP5Symbols.T_DOUBLE_ARROW -> text = "=>"; // NOI18N
                case ASTPHP5Symbols.T_PUBLIC_SET -> text = "public(set)"; // NOI18N
                case ASTPHP5Symbols.T_STRING_CAST -> text = "(string)"; // NOI18N
                case ASTPHP5Symbols.T_STRING -> text = "identifier"; // NOI18N
                case ASTPHP5Symbols.T_PLUS_EQUAL -> text = "+="; // NOI18N
                case ASTPHP5Symbols.T_FN -> text = "fn"; // NOI18N
                case ASTPHP5Symbols.T_INSTANCEOF -> text = "instanceof"; // NOI18N
                case ASTPHP5Symbols.T_DIV_EQUAL -> text = "/="; // NOI18N
                case ASTPHP5Symbols.T_NUM_STRING -> text = "NUM_STRING"; // NOI18N
                case ASTPHP5Symbols.T_PROPERTY_C -> text = "__PROPERTY__"; // NOI18N
                case ASTPHP5Symbols.T_HALT_COMPILER -> text = "__halt_compiler"; // NOI18N
                case ASTPHP5Symbols.T_ENUM -> text = "enum"; // NOI18N
                case ASTPHP5Symbols.T_GOTO -> text = "goto"; // NOI18N
                case ASTPHP5Symbols.T_READONLY -> text = "readonly"; // NOI18N PHP 8.1
                default -> {
                    //no-op
                }
            }
            return text;
        }

    }
}
