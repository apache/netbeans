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


package org.netbeans.modules.i18n.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Parser of regular expressions.
 *
 * @author  Marian Petras
 */
public class Parser {

    /** regular expression being parsed by this parser */
    private String regexp;

    /**
     * names of string tokens <tt>{</tt>token-name<tt>}</tt> to be recognized
     * by this parser.
     * The array contains just the names (without
     * <code>'{'</code> and <code>'}'</code>).
     */
    private String[] tokenNames;

    /**
     * length of the longest string token name
     *
     * @see  #tokenNames
     */
    private int maxTokenLength;

    /**
     * Parses the given regular expression.
     *
     * @param  regexp  regular expression to parse
     * @return  root of a syntax tree of the regular expression
     * @exception  java.lang.IllegalArgumentException
     *             if the regular expression is <code>null</code>
     * @exception  ParseException
     *             if the given expression contained a syntax error
     */
    public static TreeNodeRoot parse(String regexp)
            throws IllegalArgumentException, ParseException {
        return parse(regexp, null);
    }

    /**
     * Parses the given regular expression with tokens enclosed
     * between <code>{</code> and <code>}</code>.
     *
     * @param  regexp  regular expression to parse
     * @param  tokenNames  names of a tokens to be recognized;
     *                     or <code>null</code> if no tokens should be
     *                     recognized
     * @return  root of a syntax tree of the regular expression
     * @exception  java.lang.IllegalArgumentException
     *             if the regular expression is <code>null</code>
     * @exception  ParseException
     *             if the given expression contained a syntax error
     */
    public static TreeNodeRoot parse(String regexp, String tokenNames[])
            throws IllegalArgumentException, ParseException {
        Parser parser = new Parser(regexp);
        if (tokenNames != null && tokenNames.length != 0) {
            parser.setTokenNames(tokenNames);
        }
        return parser.parse();
    }

    /**
     * Constructs a parser for parsing a given regular expression.
     *
     * @param  regexp  regular expression to parse
     * @exception  java.lang.IllegalArgumentException
     *             if the argument is <code>null</code>
     */
    Parser(String regexp) {
        if (regexp == null) {
            throw new IllegalArgumentException();
        }
        this.regexp = regexp;
    }

    /**
     */
    private void setTokenNames(String[] tokenNames) {
        if (tokenNames != null && tokenNames.length != 0) {
            this.tokenNames = tokenNames;
            maxTokenLength = tokenNames[0].length();
            for (int i = 1; i < tokenNames.length; i++) {
                if (tokenNames[i].length() > maxTokenLength) {
                    maxTokenLength = tokenNames[i].length();
                }
            }
        } else {
            this.tokenNames = null;
            maxTokenLength = 0;
        }
    }

    /**
     * Performs parsing of the regular expression.
     *
     * @return  root of a syntax tree of the regular expression
     * @exception  ParseException
     *             if the expression contained a syntax error
     */
    TreeNodeRoot parse() throws ParseException {

        TreeNodeRoot result;
        TreeNode multiRegexpNode = null;

        int begin = 0;
        int end = regexp.length();
        boolean initialPart = false;
        boolean finalPart = false;

        if (begin == end) {
            return null;
        }

        /* Handle regular expressions "^", "$" and "^$": */
        if (regexp.charAt(0) == '^') {
            initialPart = true;
            begin++;
        }
        if ((end == begin + 1) && (regexp.charAt(begin) == '$')) {
            finalPart = true;
            end--;
        }

        /*
         * The following special regular expressions are now handled:
         *
         *   <empty> ...  returned <null>
         *   ^   .......  begin=1, end=1, initialpart=true,  finalPart=false
         *   ^$  .......  begin=1, end=1, initialpart=true,  finalPart=true
         *   $   .......  begin=0, end=0, initialPart=false, finalPart=true
         *
         * In all the cases except for the empty regular expression,
         * it is true that (begin == end). So we know that if (begin != end),
         * there must be something between the optional characters '^' and '$'.
         * If nothing is found, it singals a syntax error.
         */

        if (begin != end) {
            multiRegexpNode = parseMultiRegexp(begin, end);

            /*
             * If nothing was found between the (optional) initial '^'
             * and the (optional) final '$', it is a syntax error:
             */
            if (multiRegexpNode == null) {
                throwParseException(begin);
            }

            /*
             * If there is a single character pending after the recognized
             * regular expression and it is '$', it is the (optional) final '$':
             */
            if ((multiRegexpNode.end == end - 1)
                    && (regexp.charAt(end - 1) == '$')) {
                finalPart = true;
                end--;
            }

            /*
             * If some characters between the (optional) initial '^'
             * and the (optional) final '$' have been left unrecognized,
             * it is a syntax error:
             */
            if (multiRegexpNode.end != end) {
                throwParseException(begin);
            }
        }

        String attribs = null;
        if (initialPart || finalPart) {
            StringBuilder buf = new StringBuilder(2);
            if (initialPart) {
                buf.append('^');
            }
            if (finalPart) {
                buf.append('$');
            }
            attribs = buf.toString();
        }

        result = new TreeNodeRoot(regexp, attribs);
        if (multiRegexpNode != null) {
            result.add(multiRegexpNode);
        }
        return result;
    }


    /** */
    private void throwParseException(int position) throws ParseException {
        throw new ParseException(regexp, position);
    }


    /** */
    private TreeNode parseMultiRegexp(int start, int end)
            throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode regexpSequenceNode = parseRegexpSequence(start, end);
        if (regexpSequenceNode == null) {
            return null;
        }

        List<TreeNode> alternatives = new ArrayList<TreeNode>(4);
        alternatives.add(regexpSequenceNode);

        while (regexpSequenceNode.end != end
                && regexp.charAt(regexpSequenceNode.end) == '|') {
            int from = regexpSequenceNode.end + 1;
            regexpSequenceNode = parseRegexpSequence(from, end);

            if (regexpSequenceNode == null) {

                /* expected: regexp sequence */
                throwParseException(from);
            }

            alternatives.add(regexpSequenceNode);
        }

        TreeNode result = new TreeNode(TreeNode.MULTI_REGEXP,
                                       start,
                                       regexpSequenceNode.end);
        for (TreeNode alt : alternatives) {
            result.add(alt);
        }
        return result;
    }

    
    /** */
    private TreeNode parseRegexpSequence(int start, int end)
            throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode result;
        List<TreeNode> sequence = null;
        TreeNode lastChildNode = null;

        int from = start;
        while (true) {
            TreeNode qRegexpNode = parseQRegexp(from, end);

            if (qRegexpNode == null) {
                break;
            }

            if (sequence == null) {
                sequence = new ArrayList<TreeNode>(4);
            }
            sequence.add(qRegexpNode);

            /* remember the last added regexp: */
            lastChildNode = qRegexpNode;

            /* test - is parsing finished? */
            if (qRegexpNode.end == end) {
                break;
            }

            from = qRegexpNode.end;
        }

        if (sequence == null) {
            return null;
        }

        result = new TreeNode(TreeNode.SIMPLE_REGEXP, start, lastChildNode.end);
        for (TreeNode seqPart : sequence) {
            result.add(seqPart);
        }
        return result;
    }


    /** */
    private TreeNode parseQRegexp(int start, int end) throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode result;

        TreeNode singleRegexpNode = parseSingleRegexp(start, end);
        if (singleRegexpNode == null) {
            return null;
        }

        /* test - is parsing finished? */
        if (singleRegexpNode.end == end) {
            result = new TreeNode(TreeNode.Q_REGEXP,
                                  start,
                                  singleRegexpNode.end);
            result.add(singleRegexpNode);
            return result;
        }

        TreeNode quantifierNode = parseQuantifier(singleRegexpNode.end, end);
        if (quantifierNode == null) {
            result = new TreeNode(TreeNode.Q_REGEXP,
                                  start,
                                  singleRegexpNode.end);
            result.add(singleRegexpNode);
        } else {
            result = new TreeNode(TreeNode.Q_REGEXP,
                                  start,
                                  quantifierNode.end);
            result.add(singleRegexpNode);
            result.add(quantifierNode);
        }
        return result;
    }


    /** */
    private TreeNode parseSingleRegexp(int start, int end)
            throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode result;
        char ch = regexp.charAt(start);
        switch (ch) {
            case '.':
                result = new TreeNode(TreeNode.METACHAR,
                                      start,
                                      start + 1, ch);
                break;

            case '[':
                TreeNode setNode = parseSet(start, end);
                assert setNode != null;
                return setNode;

            case '(':
                TreeNode subexprNode = parseSubexpr(start, end);
                assert subexprNode != null;
                return subexprNode;

            case '\\':
                if (end == start + 1) {
                    
                    /* unexpected end of regexp */
                    throwParseException(end);
                }
                char ch2 = regexp.charAt(start + 1);
                switch (ch2) {
                    case 'b':
                    case 'B':
                        result = new TreeNode(TreeNode.METACHAR,
                                              start,
                                              start + 2, ch2);
                        break;

                    case 'u':
                        Integer unicode = parseUnicode(start + 2, end);
                        if (unicode == null) {

                            /* expected: 4-digit hexadecimal number */
                            throwParseException(start + 2);
                        }
                        result = new TreeNode(TreeNode.UNICODE_CHAR,
                                              start,
                                              start + 6,
                                              unicode);
                        break;

                    default:
                        char parsedChar;
                        switch (ch2) {
                            case 't':
                                parsedChar = '\t';
                                break;

                            case 'n':
                                parsedChar = '\n';
                                break;

                            case 'r':
                                parsedChar = '\r';
                                break;

                            case 'f':
                                parsedChar = '\f';
                                break;

                            default:
                                parsedChar = ch2;
                                break;
                        }
                        result = new TreeNode(TreeNode.CHAR,
                                              start,
                                              start + 2, parsedChar);
                        break;
                }
                break;

            case '{':
                String tokenName = getTokenName(start, end);
                if (tokenName != null) {
                    result = new TreeNode(TreeNode.TOKEN,
                                          start,
                                          start + tokenName.length() + 2,
                                          tokenName);
                    break;
                }
                /* falls through */

            default:
                if ("^$|*+?)]{}".indexOf(ch) != -1) {                //NOI18N
                    return null;
                }
                result = new TreeNode(TreeNode.CHAR,
                                      start,
                                      start + 1, ch);
                break;
        }
        return result;
    }


    /** */
    private TreeNode parseQuantifier(int start, int end)
            throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode result = null;
        char ch = regexp.charAt(start);
        switch (ch) {
            case '*':
            case '+':
            case '?':
                result = new TreeNode(TreeNode.QUANTIFIER,
                                      start,
                                      start + 1, ch);
                return result;
            case '{':
                break;
            default:
                return null;
        }

        if (end - start == 1) {
            
            /* expected: number or token */
            throwParseException(start + 1);
        }

        TreeNode numberNode1 = parseNumber(start + 1, end);
        if (numberNode1 == null) {

            /* it is not a number - maybe it is a token: */
            if (getTokenName(start, end) != null) {

                /* if it is a token, it is not a quantifier: */
                return null;
            }

            /* expected: number */
            throwParseException(start + 1);
        }
        if (numberNode1.end == end) {

            /* expected: '}', ',' */
            throwParseException(numberNode1.end);
        }

        switch (regexp.charAt(numberNode1.end)) {
            case '}':
                result = new TreeNode(TreeNode.QUANTIFIER,
                                      start,
                                      numberNode1.end + 1,
                                      "{n}");                           //NOI18N
                result.add(numberNode1);
                return result;
            case ',':
                break;
            default:

                /* expected: '}' or ',' */
                throwParseException(numberNode1.end);
        }

        if (numberNode1.end + 1 == end) {
            
            /* expected: number or '}' */
            throwParseException(numberNode1.end + 1);
        }

        if (regexp.charAt(numberNode1.end + 1) == '}') {
                result = new TreeNode(TreeNode.QUANTIFIER,
                                      start,
                                      numberNode1.end + 2,
                                      "{n,}");                          //NOI18N
                result.add(numberNode1);
                return result;
        }

        TreeNode numberNode2 = parseNumber(numberNode1.end + 1, end);
        if (numberNode2 == null) {

            /* expected: number */
            throwParseException(numberNode1.end + 1);
        }
        if (numberNode2.end == end
                || regexp.charAt(numberNode2.end) != '}') {

            /* expected: '}' */
            throwParseException(numberNode2.end);
        }

        int num1 = ((Integer) numberNode1.getAttribs()).intValue();
        int num2 = ((Integer) numberNode2.getAttribs()).intValue();
        if (num2 < num1) {
            throwParseException(numberNode2.start);
        }

        result = new TreeNode(TreeNode.QUANTIFIER,
                              start,
                              numberNode2.end + 1,
                              "{n,n}");                                 //NOI18N
        result.add(numberNode1);
        result.add(numberNode2);
        return result;
    }
    

    /** */
    private TreeNode parseNumber(int start, int end) throws ParseException {
        if (start == end) {
            return null;
        }

        char[] chars = regexp.substring(start, end).toCharArray();
        int endIndex = chars.length;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                endIndex = i;
                break;
            }
        }

        if (endIndex == 0) {
            return null;
        } else if (endIndex > 3) {

            /* max 3 digits */
            throwParseException(start);
        }

        int number;
        if (endIndex == 1) {
            number = chars[0] - '0';
        } else {
            try {
                number = Integer.parseInt(regexp.substring(start,
                                                           start + endIndex));
            } catch (NumberFormatException ex) {
                throw new AssertionError();                  //should not happen
            }
        }

        TreeNode result = new TreeNode(TreeNode.NUMBER,
                                       start,
                                       start + endIndex,
                                       number);
        return result;
    }


    /** */
    private String getTokenName(int start, int end) {
        if (tokenNames == null) {
            return null;
        }

        int checkAreaLength = Math.min(end - start, maxTokenLength + 2);
        String substring = regexp.substring(start, start + checkAreaLength);
        if (substring.charAt(0) != '{') {
            return null;
        }
        int rightBoundaryIndex = substring.indexOf('}', 1);
        if (rightBoundaryIndex == -1) {
            return null;
        }
        String tokenName = substring.substring(1, rightBoundaryIndex);
        for (int i = 0; i < tokenNames.length; i++) {
            if (tokenName.equals(tokenNames[i])) {
                return tokenName;
            }
        }
        return null;
    }


    /** */
    private Integer parseUnicode(int start, int end) throws ParseException {
        if (start == end) {
            return null;
        }

        if (end - start < 4) {

            /* expected: 4-digit hexadecimal number */
            throwParseException(start);
        }

        char[] chars = regexp.substring(start, start + 4).toCharArray();
        for (int i = 0; i < 4; i++) {
            char ch = chars[i];
            if ("01234567890abcdefABCDEF".indexOf(ch) == -1) {          //NOI18N
                if (i == 0) {
                    return null;
                } else {
                    throwParseException(start);
                }
            }
        }

        Integer integer;
        try {
            integer = Integer.valueOf(regexp.substring(start, start + 4), 16);
        } catch (NumberFormatException ex) {
            throw new AssertionError();         //should not happen
        }
        return integer;
    }


    /** */
    private TreeNode parseSubexpr(int start, int end) throws ParseException {
        if (start == end) {
            return null;
        }

        if (regexp.charAt(start) != '(') {
            return null;
        }
        if (end == start + 1) {
            throwParseException(start + 1);
        }

        TreeNode result;
        TreeNode multiRegexpNode = parseMultiRegexp(start + 1, end);
        if (multiRegexpNode == null) {

            /* expected: regular subexpression */
            throwParseException(start + 1);
        }
        if (multiRegexpNode.end == end
                || regexp.charAt(multiRegexpNode.end) != ')') {
            throwParseException(multiRegexpNode.end);
        }
        result = new TreeNode(TreeNode.SUBEXPR, start, multiRegexpNode.end + 1);
        result.add(multiRegexpNode);
        return result;
    }


    /** */
    private TreeNode parseSet(int start, int end) throws ParseException {
        if (start == end) {
            return null;
        }

        if (regexp.charAt(start) != '[') {
            return null;
        }
        if (end == start + 1) {

            /* unexpected end of regexp: */
            throwParseException(start + 1);
        }

        /*
         * Test which of the three chars that may occur only in the beginning
         * of the set ('^', ']', '-') are present:
         */
        String setString = regexp.substring(start, end);
        String specials = getSpecials(setString);

        /* Find indices of the bounding square brackets: */
        int endIndex = setString.indexOf(']', 1 + specials.length());
        if (endIndex == -1) {

            /* matching bracket (']') not found: */
            throwParseException(start);
        } else {
            endIndex++;                 //index of the first character after ']'
        }
        endIndex += start;              //from the beginning of 'regexp'

        setString = regexp.substring(start, endIndex);
        int setLength = setString.length();

        TreeNode result;

        /* Test whether the set is a named character set: */
        if (setLength >= 5
                && setString.charAt(1) == ':'
                && setString.charAt(setLength - 2) == ':') {
            String charClassName = setString.substring(2, setLength - 2);
            if (isPosixCharClass(charClassName)) {
                result = new TreeNode(TreeNode.POSIX_SET,
                                               start,
                                               endIndex,
                                               charClassName);
                return result;
            } else {
                throwParseException(start + 2);
            }
        }

        result = new TreeNode(TreeNode.SET,
                              start,
                              endIndex,
                              specials);

        int from = start + 1 + specials.length();
        int to = endIndex - 1;

        while (from != to) {
            TreeNode rangeNode = parseRangeOrChar(from, to);
            if (rangeNode == null) {

                /* expected: character or range of characters */
                throwParseException(from);
            }
            result.add(rangeNode);
            from = rangeNode.end;
        }

        return result;
    }


    /** */
    private TreeNode parseRangeOrChar(int start, int end)
            throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode rangeCharNode1 = parseRangeChar(start, end);
        if (rangeCharNode1 == null) {
            return null;
        }

        if (rangeCharNode1.end == end
                || regexp.charAt(rangeCharNode1.end) != '-') {
            return rangeCharNode1;
        }

        TreeNode rangeCharNode2 = parseRangeChar(rangeCharNode1.end + 1, end);
        if (rangeCharNode2 == null) {

            /* expected: range character */
            throwParseException(rangeCharNode1.end + 1);
        }

        Object charObject;

        charObject = rangeCharNode1.getAttribs();
        int char1 = charObject instanceof Character
                     ? Character.getNumericValue(
                            ((Character) charObject).charValue())
                     : ((Integer) charObject).intValue();
        charObject = rangeCharNode2.getAttribs();
        int char2 = charObject instanceof Character
                     ? Character.getNumericValue(
                            ((Character) charObject).charValue())
                     : ((Integer) charObject).intValue();

        if (!(char1 < char2)) {

            /* expected: range character */
            throwParseException(rangeCharNode1.end + 1);
        }

        TreeNode result = new TreeNode(TreeNode.RANGE,
                                       start,
                                       rangeCharNode2.end);
        result.add(rangeCharNode1);
        result.add(rangeCharNode2);
        return result;
    }

    
    /** */
    private TreeNode parseRangeChar(int start, int end) throws ParseException {
        if (start == end) {
            return null;
        }

        TreeNode result;

        char ch = regexp.charAt(start);
        switch (ch) {
            case ']':
            case '-':
                return null;

            case '\\':
                if (end == start + 1) {

                    /* expected: any character except ']', '-' */
                    throwParseException(start + 1);
                }
                char ch2 = regexp.charAt(start + 1);
                char parsedChar;
                switch (ch2) {
                    case 'u':
                        Integer unicode = parseUnicode(start + 2, end);
                        if (unicode == null) {
                            
                            /* expected: 4-digit hexadecimal number */
                            throwParseException(start + 2);
                        }
                        int codeValue = unicode.intValue();
                        assert codeValue >= 0;
                        if (codeValue <= 0x007f) {

                            /* expected: unicode value >= 0080h */
                            throwParseException(start + 2);
                        }
                        return new TreeNode(TreeNode.UNICODE_CHAR,
                                              start,
                                              start + 6,
                                              unicode);

                    case ']':
                    case '-':

                        /*
                         * characters ']' and '-' must be in the beginning
                         * of the definition of the set:
                         */
                        throwParseException(start + 2);

                    case 't':
                        parsedChar = '\t';
                        break;

                    case 'n':
                        parsedChar = '\n';
                        break;

                    case 'r':
                        parsedChar = '\r';
                        break;

                    case 'f':
                        parsedChar = '\f';
                        break;

                    default:
                        parsedChar = ch2;
                        break;
                }
                result = new TreeNode(TreeNode.CHAR,
                                      start,
                                      start + 2, parsedChar);
                break;
            default:
                result = new TreeNode(TreeNode.CHAR,
                                      start,
                                      start + 1, ch);
                break;
        }
        return result;
    }


    /** */
    private String getSpecials(String setRegexp) {
        int index = 1;
        int maxIndex = 3;
        if (setRegexp.length() < 5) {
            maxIndex = setRegexp.length() - 2;
        }
        StringBuilder buf = new StringBuilder(maxIndex - index + 1);
        char ch = setRegexp.charAt(index);
        if (ch == '^') {
            buf.append(ch);
            if (index == maxIndex) {
                return buf.toString();
            }
            ch = setRegexp.charAt(++index);
        }
        if (ch == ']') {
            buf.append(ch);
            if (index == maxIndex) {
                return buf.toString();
            }
            ch = setRegexp.charAt(++index);
        }
        if (ch == '-') {
            buf.append(ch);
        }
        return buf.toString();
    }


    /** */
    private boolean isPosixCharClass(String name) {
        /* "xdigit" is the only class name not having exactly 5 characters: */
        if (name.equals("xdigit")) {                                    //NOI18N
            return true;
        }
        if (name.length() != 5) {
            return false;
        }

        String classNames = "alnum alpha blank cntrl digit graph "      //NOI18N
                            + "lower print punct space upper";          //NOI18N
        StringTokenizer tokenizer
                = new StringTokenizer(classNames, " ");                 //NOI18N
        while (tokenizer.hasMoreTokens()) {
            if (name.equals(tokenizer.nextToken())) {
                return true;
            }
        }
        return false;
    }

}
