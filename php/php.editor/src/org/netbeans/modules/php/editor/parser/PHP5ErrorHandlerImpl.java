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
                case ASTPHP5Symbols.T_BOOLEAN_AND : text = "&&"; break; //NOI18N
                case ASTPHP5Symbols.T_INLINE_HTML : text = "inline html"; break; //NOI18N
                case ASTPHP5Symbols.T_EMPTY : text = "empty"; break; //NOI18N
                case ASTPHP5Symbols.T_PROTECTED : text = "protected"; break; //NOI18N
                case ASTPHP5Symbols.T_CLOSE_RECT : text = "]"; break; //NOI18N
                case ASTPHP5Symbols.T_TRAIT_C : text = "__TRAIT__"; break; //NOI18N
                case ASTPHP5Symbols.T_IS_NOT_EQUAL : text = "!="; break; //NOI18N
                case ASTPHP5Symbols.T_INCLUDE : text = "include"; break; //NOI18N
                case ASTPHP5Symbols.T_QUATE : text = "'\"'"; break; //NOI18N
                case ASTPHP5Symbols.T_GLOBAL : text = "global"; break; //NOI18N
                case ASTPHP5Symbols.T_PRINT : text = "print"; break; //NOI18N
                case ASTPHP5Symbols.T_OR_EQUAL : text = "|="; break; //NOI18N
                case ASTPHP5Symbols.T_LOGICAL_XOR : text = "XOR"; break; //NOI18N
                case ASTPHP5Symbols.T_FUNCTION : text = "function"; break; //NOI18N
                case ASTPHP5Symbols.T_FN : text = "fn"; break; //NOI18N
                case ASTPHP5Symbols.T_STATIC : text = "static"; break; //NOI18N
                case ASTPHP5Symbols.T_NEKUDA : text = "'.'"; break; //NOI18N
                case ASTPHP5Symbols.T_THROW : text = "throw"; break; //NOI18N
                case ASTPHP5Symbols.T_CLASS : text = "class"; break; //NOI18N
                case ASTPHP5Symbols.T_ABSTRACT : text = "abstract"; break; //NOI18N
                case ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE : text = "String"; break; //NOI18N
                case ASTPHP5Symbols.T_MOD_EQUAL : text = "%="; break; //NOI18N
                case ASTPHP5Symbols.T_BREAK : text = "break"; break; //NOI18N
                case ASTPHP5Symbols.T_WHILE : text = "while"; break; //NOI18N
                case ASTPHP5Symbols.T_DO : text = "do"; break; //NOI18N
                case ASTPHP5Symbols.T_CONST : text = "const"; break; //NOI18N
                case ASTPHP5Symbols.T_CONTINUE : text = "continue"; break; //NOI18N
                case ASTPHP5Symbols.T_FUNC_C : text = "__FUNCTION__"; break; //NOI18N
                case ASTPHP5Symbols.T_DIV : text = "/"; break; //NOI18N
                case ASTPHP5Symbols.T_LOGICAL_OR : text = "OR"; break; //NOI18N
                case ASTPHP5Symbols.T_DIR : text = "__DIR__"; break; //NOI18N
                case ASTPHP5Symbols.T_OPEN_PARENTHESE : text = "("; break; //NOI18N
                case ASTPHP5Symbols.T_REFERENCE : text = "&"; break; //NOI18N
                case ASTPHP5Symbols.T_COMMA : text = "','"; break; //NOI18N
                case ASTPHP5Symbols.T_ELSE : text = "else"; break; //NOI18N
                case ASTPHP5Symbols.T_IS_EQUAL : text = "=="; break; //NOI18N
                case ASTPHP5Symbols.T_LIST : text = "list"; break; //NOI18N
                case ASTPHP5Symbols.T_NAMESPACE : text = "namespace"; break; //NOI18N
                case ASTPHP5Symbols.T_NS_SEPARATOR : text = "\\"; break; //NOI18N
                case ASTPHP5Symbols.T_OR : text = "|"; break; //NOI18N
                case ASTPHP5Symbols.T_IS_IDENTICAL : text = "==="; break; //NOI18N
                case ASTPHP5Symbols.T_INC : text = "++"; break; //NOI18N
                case ASTPHP5Symbols.T_ELSEIF : text = "elseif"; break; //NOI18N
                case ASTPHP5Symbols.T_TRY : text = "try"; break; //NOI18N
                case ASTPHP5Symbols.T_START_NOWDOC : text = "<<<'...'"; break; //NOI18N
                case ASTPHP5Symbols.T_PRIVATE : text = "private"; break; //NOI18N
                case ASTPHP5Symbols.T_UNSET_CAST : text = "(unset)"; break; //NOI18N
                case ASTPHP5Symbols.T_INCLUDE_ONCE : text = "include_once"; break; //NOI18N
                case ASTPHP5Symbols.T_ENDIF : text = "endif"; break; //NOI18N
                case ASTPHP5Symbols.T_SR_EQUAL : text = ">>="; break; //NOI18N
                case ASTPHP5Symbols.T_CALLABLE : text = "callable"; break; //NOI18N
                case ASTPHP5Symbols.T_PUBLIC : text = "public"; break; //NOI18N
                case ASTPHP5Symbols.T_OBJECT_OPERATOR : text = "->"; break; //NOI18N
                case ASTPHP5Symbols.T_NULLSAFE_OBJECT_OPERATOR : text = "?->"; break; //NOI18N
                case ASTPHP5Symbols.T_TILDA : text = "~"; break; //NOI18N
                case ASTPHP5Symbols.T_PAAMAYIM_NEKUDOTAYIM : text = "::"; break; //NOI18N
                case ASTPHP5Symbols.T_IS_SMALLER_OR_EQUAL : text = "<="; break; //NOI18N
                case ASTPHP5Symbols.T_XOR_EQUAL : text = "^="; break; //NOI18N
                case ASTPHP5Symbols.T_ENDFOREACH : text = "endforeach"; break; //NOI18N
                case ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING : text = "String"; break; //NOI18N
                case ASTPHP5Symbols.T_BACKQUATE : text = "'`'"; break; //NOI18N
                case ASTPHP5Symbols.T_AT : text = "@"; break; //NOI18N
                case ASTPHP5Symbols.T_AS : text = "as"; break; //NOI18N
                case ASTPHP5Symbols.T_CURLY_CLOSE : text = "}"; break; //NOI18N
                case ASTPHP5Symbols.T_ENDDECLARE : text = "enddeclare"; break; //NOI18N
                case ASTPHP5Symbols.T_CATCH : text = "catch"; break; //NOI18N
                case ASTPHP5Symbols.T_CASE : text = "case"; break; //NOI18N
                case ASTPHP5Symbols.T_VARIABLE : text = "variable"; break; //NOI18N
                case ASTPHP5Symbols.T_INSTEADOF : text = "insteadof"; break; //NOI18N
                case ASTPHP5Symbols.T_NEW : text = "new"; break; //NOI18N
                case ASTPHP5Symbols.T_MINUS_EQUAL : text = "-="; break; //NOI18N
                case ASTPHP5Symbols.T_PLUS : text = "+"; break; //NOI18N
                case ASTPHP5Symbols.T_SL_EQUAL : text = "<<="; break; //NOI18N
                case ASTPHP5Symbols.T_ENDWHILE : text = "endwhile"; break; //NOI18N
                case ASTPHP5Symbols.T_ENDFOR : text = "endfor"; break; //NOI18N
                case ASTPHP5Symbols.T_TRAIT : text = "trait"; break; //NOI18N
                case ASTPHP5Symbols.T_CLONE : text = "clone"; break; //NOI18N
                case ASTPHP5Symbols.T_BOOLEAN_OR : text = "||"; break; //NOI18N
                case ASTPHP5Symbols.T_UNSET : text = "unset"; break; //NOI18N
                case ASTPHP5Symbols.T_INTERFACE : text = "interface"; break; //NOI18N
                case ASTPHP5Symbols.T_SWITCH : text = "switch"; break; //NOI18N
                case ASTPHP5Symbols.T_MATCH : text = "match"; break; //NOI18N
                case ASTPHP5Symbols.T_IS_GREATER_OR_EQUAL : text = ">="; break; //NOI18N
                case ASTPHP5Symbols.T_SPACESHIP : text = "<=>"; break; //NOI18N
                case ASTPHP5Symbols.T_OPEN_RECT : text = "["; break; //NOI18N
                case ASTPHP5Symbols.T_CURLY_OPEN_WITH_DOLAR : text = "{$"; break; //NOI18N
                case ASTPHP5Symbols.T_FINAL : text = "final"; break; //NOI18N
                case ASTPHP5Symbols.T_REQUIRE : text = "require"; break; //NOI18N
                case ASTPHP5Symbols.T_FILE : text = "__FILE__"; break; //NOI18N
                case ASTPHP5Symbols.T_DEC : text = "--"; break; //NOI18N
                case ASTPHP5Symbols.T_CLOSE_PARENTHESE : text = ")"; break; //NOI18N
                case ASTPHP5Symbols.T_CLASS_C : text = "__CLASS__"; break; //NOI18N
                case ASTPHP5Symbols.T_EVAL : text = "eval"; break; //NOI18N
                case ASTPHP5Symbols.T_RGREATER : text = "<"; break; //NOI18N
                case ASTPHP5Symbols.T_IS_NOT_IDENTICAL : text = "!=="; break; //NOI18N
                case ASTPHP5Symbols.T_NOT : text = "!"; break; //NOI18N
                case ASTPHP5Symbols.T_REQUIRE_ONCE : text = "require_once"; break; //NOI18N
                case ASTPHP5Symbols.T_NS_C : text = "__NAMESPACE__"; break; //NOI18N
                case ASTPHP5Symbols.T_DOLLAR_OPEN_CURLY_BRACES : text = "${"; break; //NOI18N
                case ASTPHP5Symbols.T_VAR : text = "var"; break; //NOI18N
                case ASTPHP5Symbols.T_START_HEREDOC : text = "<<<\"...\""; break; //NOI18N
                case ASTPHP5Symbols.T_ENDSWITCH : text = "endswitch"; break; //NOI18N
                case ASTPHP5Symbols.T_OBJECT_CAST : text = "(object)"; break; //NOI18N
                case ASTPHP5Symbols.T_ECHO : text = "echo"; break; //NOI18N
                case ASTPHP5Symbols.T_LINE : text = "__LINE__"; break; //NOI18N
                case ASTPHP5Symbols.T_FOR : text = "for"; break; //NOI18N
                case ASTPHP5Symbols.T_IMPLEMENTS : text = "implements"; break; //NOI18N
                case ASTPHP5Symbols.T_ARRAY_CAST : text = "(array)"; break; //NOI18N
                case ASTPHP5Symbols.T_DOLLAR : text = "$"; break; //NOI18N
                case ASTPHP5Symbols.T_TIMES : text = "*"; break; //NOI18N
                case ASTPHP5Symbols.T_DOUBLE_CAST : text = "(double)"; break; //NOI18N
                case ASTPHP5Symbols.T_BOOL_CAST : text = "(bool)"; break; //NOI18N
                case ASTPHP5Symbols.T_PRECENT : text = "%"; break; //NOI18N
                case ASTPHP5Symbols.T_LNUMBER : text = "integer"; break; //NOI18N
                case ASTPHP5Symbols.T_CURLY_OPEN : text = "{"; break; //NOI18N
                case ASTPHP5Symbols.T_DEFINE : text = "define"; break; //NOI18N
                case ASTPHP5Symbols.T_QUESTION_MARK : text = "?"; break; //NOI18N
                case ASTPHP5Symbols.T_COALESCE : text = "??"; break; //NOI18N
                case ASTPHP5Symbols.T_COALESCE_EQUAL : text = "??="; break; //NOI18N
                case ASTPHP5Symbols.T_END_NOWDOC : text = "END_NOWDOC"; break; //NOI18N
                case ASTPHP5Symbols.T_USE : text = "use"; break; //NOI18N
                case ASTPHP5Symbols.T_KOVA : text = "^"; break; //NOI18N
                case ASTPHP5Symbols.T_IF : text = "if"; break; //NOI18N
                case ASTPHP5Symbols.T_MUL_EQUAL : text = "*="; break; //NOI18N
                case ASTPHP5Symbols.T_ARRAY : text = "array"; break; //NOI18N
                case ASTPHP5Symbols.T_LGREATER : text = ">"; break; //NOI18N
                case ASTPHP5Symbols.T_SEMICOLON : text = ";"; break; //NOI18N
                case ASTPHP5Symbols.T_NEKUDOTAIM : text = ":"; break; //NOI18N
                case ASTPHP5Symbols.T_VAR_COMMENT : text = "VAR_COMMENT"; break; //NOI18N
                case ASTPHP5Symbols.T_CONCAT_EQUAL : text = ".="; break; //NOI18N
                case ASTPHP5Symbols.T_AND_EQUAL : text = "&="; break; //NOI18N
                case ASTPHP5Symbols.T_DNUMBER : text = "double"; break; //NOI18N
                case ASTPHP5Symbols.T_MINUS : text = "-"; break; //NOI18N
                case ASTPHP5Symbols.T_FOREACH : text = "foreach"; break; //NOI18N
                case ASTPHP5Symbols.T_EXIT : text = "exit"; break; //NOI18N
                case ASTPHP5Symbols.T_DECLARE : text = "declare"; break; //NOI18N
                case ASTPHP5Symbols.T_STRING_VARNAME : text = "STRING_VARNAME"; break; //NOI18N
                case ASTPHP5Symbols.T_EXTENDS : text = "extends"; break; //NOI18N
                case ASTPHP5Symbols.T_METHOD_C : text = "__METHOD__"; break; //NOI18N
                case ASTPHP5Symbols.T_INT_CAST : text = "(int)"; break; //NOI18N
                case ASTPHP5Symbols.T_ISSET : text = "isset"; break; //NOI18N
                case ASTPHP5Symbols.T_LOGICAL_AND : text = "&&"; break; //NOI18N
                case ASTPHP5Symbols.T_RETURN : text = "return"; break; //NOI18N
                case ASTPHP5Symbols.T_DEFAULT : text = "default"; break; //NOI18N
                case ASTPHP5Symbols.T_SR : text = ">>"; break; //NOI18N
                case ASTPHP5Symbols.T_EQUAL : text = "="; break; //NOI18N
                case ASTPHP5Symbols.T_SL : text = "<<"; break; //NOI18N
                case ASTPHP5Symbols.T_END_HEREDOC : text = "END_HEREDOC"; break; //NOI18N
                case ASTPHP5Symbols.T_DOUBLE_ARROW : text = "=>"; break; //NOI18N
                case ASTPHP5Symbols.T_STRING_CAST : text = "(string)"; break; //NOI18N
                case ASTPHP5Symbols.T_STRING : text = "identifier"; break; //NOI18N
                case ASTPHP5Symbols.T_PLUS_EQUAL : text = "+="; break; //NOI18N
                case ASTPHP5Symbols.T_INSTANCEOF : text = "instanceof"; break; //NOI18N
                case ASTPHP5Symbols.T_DIV_EQUAL : text = "/="; break; //NOI18N
                case ASTPHP5Symbols.T_NUM_STRING : text = "NUM_STRING"; break; //NOI18N
                case ASTPHP5Symbols.T_HALT_COMPILER : text = "__halt_compiler"; break; //NOI18N
                case ASTPHP5Symbols.T_GOTO : text = "goto"; break; //NOI18N
                case ASTPHP5Symbols.T_YIELD : text = "yield"; break; //NOI18N
                case ASTPHP5Symbols.T_YIELD_FROM : text = "yield from"; break; //NOI18N
                case ASTPHP5Symbols.T_READONLY : text = "readonly"; break; //NOI18N PHP 8.1
                default:
                    //no-op
            }
            return text;
        }

    }
}
