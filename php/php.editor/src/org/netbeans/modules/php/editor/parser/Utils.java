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

/**
 *
 * @author Petr Pisl
 */
public final class Utils {

    private Utils() {
    }

    /**
     * This method is mainly used for debugging purpose.
     *
     * @param id token id
     * @return text representation for the token
     */
    public static String getASTScannerTokenName(int id) {
        String name;
        switch (id) {
            case ASTPHP5Symbols.EOF:
                name = "EOF"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ABSTRACT:
                name = "T_ABSTRACT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_AND_EQUAL:
                name = "T_AND_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ARRAY:
                name = "T_ARRAY"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ARRAY_CAST:
                name = "T_ARRAY_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_AS:
                name = "T_AS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_AT:
                name = "T_AT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_BACKQUATE:
                name = "T_BACKQUATE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_BOOLEAN_AND:
                name = "T_BOOLEAN_AND"; //NOI18N
                break;
            case ASTPHP5Symbols.T_BOOLEAN_OR:
                name = "T_BOOLEAN_OR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_BOOL_CAST:
                name = "T_BOOL_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_BREAK:
                name = "T_BREAK"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CALLABLE:
                name = "T_CALLABLE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CASE:
                name = "T_CASE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CATCH:
                name = "T_CATCH"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CLASS:
                name = "T_CLASS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CLASS_C:
                name = "T_CLASS_C"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CLONE:
                name = "T_CLONE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CLOSE_PARENTHESE:
                name = "T_CLOSE_PARENTHESE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CLOSE_RECT:
                name = "T_CLOSE_RECT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_COMMA:
                name = "T_COMMA"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CONCAT_EQUAL:
                name = "T_CONCAT_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CONST:
                name = "T_CONST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING:
                name = "T_CONSTANT_ENCAPSED_STRING"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CONTINUE:
                name = "T_CONTINUE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CURLY_CLOSE:
                name = "T_CURLY_CLOSE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CURLY_OPEN:
                name = "T_CURLY_OPEN"; //NOI18N
                break;
            case ASTPHP5Symbols.T_CURLY_OPEN_WITH_DOLAR:
                name = "T_CURLY_OPEN_WITH_DOLAR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DEC:
                name = "T_DEC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DECLARE:
                name = "T_DECLARE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DEFAULT:
                name = "T_DEFAULT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DEFINE:
                name = "T_DEFINE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DIV:
                name = "T_DIV"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DIV_EQUAL:
                name = "T_DIV_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DNUMBER:
                name = "T_DNUMBER"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DO:
                name = "T_DO"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DOLLAR:
                name = "T_DOLLAR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DOLLAR_OPEN_CURLY_BRACES:
                name = "T_DOLLAR_OPEN_CURLY_BRACES"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DOUBLE_ARROW:
                name = "T_DOUBLE_ARROW"; //NOI18N
                break;
            case ASTPHP5Symbols.T_DOUBLE_CAST:
                name = "T_DOUBLE_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ECHO:
                name = "T_ECHO"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ELSE:
                name = "T_ELSE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ELSEIF:
                name = "T_ELSEIF"; //NOI18N
                break;
            case ASTPHP5Symbols.T_EMPTY:
                name = "T_EMPTY"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE:
                name = "T_ENCAPSED_AND_WHITESPACE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENDDECLARE:
                name = "T_ENDDECLARE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENDFOR:
                name = "T_ENDFOR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENDFOREACH:
                name = "T_ENDFOREACH"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENDIF:
                name = "T_ENDIF"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENDSWITCH:
                name = "T_ENDSWITCH"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ENDWHILE:
                name = "T_ENDWHILEnejdu."; //NOI18N
                break;
            case ASTPHP5Symbols.T_END_HEREDOC:
                name = "T_END_HEREDOC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_END_NOWDOC:
                name = "T_END_NOWDOC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_EQUAL:
                name = "T_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_EVAL:
                name = "T_EVAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_EXIT:
                name = "T_EXIT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_EXTENDS:
                name = "T_EXTENDS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FILE:
                name = "T_FILE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FINAL:
                name = "T_FINAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FN:
                name = "T_FN"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FOR:
                name = "T_FOR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FOREACH:
                name = "T_FOREACH"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FUNCTION:
                name = "T_FUNCTION"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FUNC_C:
                name = "T_FUNC_C"; //NOI18N
                break;
            case ASTPHP5Symbols.T_GLOBAL:
                name = "T_GLOBAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_HALT_COMPILER:
                name = "T_HALT_COMPILER"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IF:
                name = "T_IF"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IMPLEMENTS:
                name = "T_IMPLEMENTS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INC:
                name = "T_INC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INCLUDE:
                name = "T_INCLUDE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INCLUDE_ONCE:
                name = "T_INCLUDE_ONCE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INLINE_HTML:
                name = "T_INLINE_HTML"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INSTANCEOF:
                name = "T_INSTANCEOF"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INTERFACE:
                name = "T_INTERFACE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INT_CAST:
                name = "T_INT_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ISSET:
                name = "T_ISSET"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IS_EQUAL:
                name = "T_IS_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IS_GREATER_OR_EQUAL:
                name = "T_IS_GREATER_OR_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SPACESHIP:
                name = "T_SPACESHIP"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IS_IDENTICAL:
                name = "T_IS_IDENTICAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IS_NOT_EQUAL:
                name = "T_IS_NOT_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IS_NOT_IDENTICAL:
                name = "T_IS_NOT_IDENTICAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_IS_SMALLER_OR_EQUAL:
                name = "T_IS_SMALLER_OR_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_KOVA:
                name = "T_KOVA"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LGREATER:
                name = "T_LGREATER"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LINE:
                name = "T_LINE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LIST:
                name = "T_LIST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LNUMBER:
                name = "T_LNUMBER"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LOGICAL_AND:
                name = "T_LOGICAL_AND"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LOGICAL_OR:
                name = "T_LOGICAL_OR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_LOGICAL_XOR:
                name = "T_LOGICAL_XOR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_METHOD_C:
                name = "T_METHOD_C"; //NOI18N
                break;
            case ASTPHP5Symbols.T_MINUS:
                name = "T_MINUS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_MINUS_EQUAL:
                name = "T_MINUS_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_MOD_EQUAL:
                name = "T_MOD_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_MUL_EQUAL:
                name = "T_MUL_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NEKUDA:
                name = "T_NEKUDA"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NEKUDOTAIM:
                name = "T_NEKUDOTAIM"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NEW:
                name = "T_NEW"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NOT:
                name = "T_NOT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NUM_STRING:
                name = "T_NUM_STRING"; //NOI18N
                break;
            case ASTPHP5Symbols.T_OBJECT_CAST:
                name = "T_OBJECT_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_OBJECT_OPERATOR:
                name = "T_OBJECT_OPERATOR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_OPEN_PARENTHESE:
                name = "T_OPEN_PARENTHESE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_OPEN_RECT:
                name = "T_OPEN_RECT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_OR:
                name = "T_OR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_OR_EQUAL:
                name = "T_OR_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PAAMAYIM_NEKUDOTAYIM:
                name = "T_PAAMAYIM_NEKUDOTAYIM"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PLUS:
                name = "T_PLUS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PLUS_EQUAL:
                name = "T_PLUS_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PRECENT:
                name = "T_PRECENT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PRINT:
                name = "T_PRINT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PRIVATE:
                name = "T_PRIVATE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PROTECTED:
                name = "T_PROTECTED"; //NOI18N
                break;
            case ASTPHP5Symbols.T_PUBLIC:
                name = "T_PUBLIC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_QUATE:
                name = "T_QUATE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_QUESTION_MARK:
                name = "T_QUESTION_MARK"; //NOI18N
                break;
            case ASTPHP5Symbols.T_REFERENCE:
                name = "T_REFERENCE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_REQUIRE:
                name = "T_REQUIRE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_REQUIRE_ONCE:
                name = "T_REQUIRE_ONCE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_RETURN:
                name = "T_RETURN"; //NOI18N
                break;
            case ASTPHP5Symbols.T_RGREATER:
                name = "T_RGREATER"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SEMICOLON:
                name = "T_SEMICOLON"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SL:
                name = "T_SL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SL_EQUAL:
                name = "T_SL_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SR:
                name = "T_SR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SR_EQUAL:
                name = "T_SR_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_START_HEREDOC:
                name = "T_START_HEREDOC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_START_NOWDOC:
                name = "T_START_NOWDOC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_STATIC:
                name = "T_STATIC"; //NOI18N
                break;
            case ASTPHP5Symbols.T_STRING:
                name = "T_STRING"; //NOI18N
                break;
            case ASTPHP5Symbols.T_STRING_CAST:
                name = "T_STRING_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_STRING_VARNAME:
                name = "T_STRING_VARNAME"; //NOI18N
                break;
            case ASTPHP5Symbols.T_SWITCH:
                name = "T_SWITCH"; //NOI18N
                break;
            case ASTPHP5Symbols.T_THROW:
                name = "T_THROW"; //NOI18N
                break;
            case ASTPHP5Symbols.T_TILDA:
                name = "T_TILDA"; //NOI18N
                break;
            case ASTPHP5Symbols.T_TIMES:
                name = "T_TIMES"; //NOI18N
                break;
            case ASTPHP5Symbols.T_TRY:
                name = "T_TRY"; //NOI18N
                break;
            case ASTPHP5Symbols.T_UNSET:
                name = "T_UNSET"; //NOI18N
                break;
            case ASTPHP5Symbols.T_UNSET_CAST:
                name = "T_UNSET_CAST"; //NOI18N
                break;
            case ASTPHP5Symbols.T_USE:
                name = "T_USE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_VAR:
                name = "T_VAR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_VARIABLE:
                name = "T_VARIABLE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_VAR_COMMENT:
                name = "T_VAR_COMMENT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_WHILE:
                name = "T_WHILE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_XOR_EQUAL:
                name = "T_XOR_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_GOTO:
                name = "T_GOTO"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NAMESPACE:
                name = "T_NAMESPACE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_NS_SEPARATOR:
                name = "T_NS_SEPARATOR"; //NOI18N
                break;
            case ASTPHP5Symbols.T_TRAIT:
                name = "T_TRAIT"; //NOI18N
                break;
            case ASTPHP5Symbols.T_TRAIT_C:
                name = "T_TRAIT_C"; //NOI18N
                break;
            case ASTPHP5Symbols.T_INSTEADOF:
                name = "T_INSTEADOF"; //NOI18N
                break;
            case ASTPHP5Symbols.T_FINALLY:
                name = "T_FINALLY"; //NOI18N
                break;
            case ASTPHP5Symbols.T_POW:
                name = "T_POW"; //NOI18N
                break;
            case ASTPHP5Symbols.T_POW_EQUAL:
                name = "T_POW_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_ELLIPSIS:
                name = "T_ELLIPSIS"; //NOI18N
                break;
            case ASTPHP5Symbols.T_COALESCE:
                name = "T_COALESCE"; //NOI18N
                break;
            case ASTPHP5Symbols.T_COALESCE_EQUAL:
                name = "T_COALESCE_EQUAL"; //NOI18N
                break;
            case ASTPHP5Symbols.T_YIELD:
                name = "T_YIELD"; //NOI18N
                break;
            case ASTPHP5Symbols.T_YIELD_FROM:
                name = "T_YIELD_FROM"; //NOI18N
                break;
            default:
                name = "unknown"; //NOI18N
        }
        return name;
    }

    public static String getSpaces(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String getRepeatingChars(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static int getRowStart(String text, int offset) {
        // Search backwards
        for (int i = offset - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i + 1;
            }
        }
        return 0;
    }

    public static int getRowEnd(String text, int offset) {
        int i = offset - 1;
        if (i < 0) {
            return 0;
        }
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i;
            }
        }
        return i;
    }
}
