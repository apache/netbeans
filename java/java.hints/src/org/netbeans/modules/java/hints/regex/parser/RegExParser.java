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
package org.netbeans.modules.java.hints.regex.parser;

import java.util.ArrayList;
import java.util.HashMap;
import org.netbeans.modules.java.hints.regex.parser.RegexConstructs.*;

/**
 *
 * @author sandeemi
 */
public class RegExParser {

    private final String input;
    private int pos;
    private int groupNo;
    private HashMap<Integer, RegEx> groupMap;
    private HashMap<String, RegEx> namedGroupMap;

    private static final char OR = '|';
    private static final char AND = '&';
    private static final char NOT = '^';
    private static final char DASH = '-';
    private static final char DOT = '.';
    private static final char ESCAPE = '\\';
    private static final char COMMA = ',';
    private static final char EQUAL = '=';
    private static final char EXCLM = '!';
    private static final char COLON = ':';

    private static final char OPT = '?';
    private static final char STAR = '*';
    private static final char PLUS = '+';
    private static final char PAR_OP = '(';
    private static final char PAR_CL = ')';
    private static final char CLASS_OP = '[';
    private static final char CLASS_CL = ']';
    private static final char BRAC_OP = '{';
    private static final char BRAC_CL = '}';    
    private static final char NAMED_OP = '<';
    private static final char NAMED_CL = '>';
    private static final char QUOT_OP = 'Q';
    private static final char QUOT_CL = 'E';

    private static final char[] DIGIT = "0123456789".toCharArray();
    private static final char[] BOUNDRY_MATCHERS = "bBAGzZ".toCharArray();
    private static final char[] OPEN_LOOP = new char[]{OPT, STAR, PLUS, BRAC_OP};

    public RegExParser(String input) {
        this.input = input;
        this.pos = 0;
        this.groupNo = 0;
        this.groupMap = new HashMap<>();
        this.namedGroupMap = new HashMap<>();
    }

    public RegEx parse() {
        if (input == null) {
            return null;
        } else if (input.isEmpty()) {
            return new Blank();
        } else {
            return regex();
        }
    }

    /*Recursive descent parsing internals*/
    private char peek() {
        return input.charAt(pos);
    }

    private void eat(char c) {
        if (peek() == c) {
            pos++;
        } else {
            throw new RuntimeException("Expected: " + c + "; got: " + peek());
        }
    }

    private char next() {
        char c = peek();
        eat(c);
        return c;
    }

    private char nextChar() {
        return input.charAt(pos + 1);
    }

    private boolean nextIn(char[] arr) {
        if (more()) {
            char c = peek();
            for (char a : arr) {
                if (c == a) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean more() {
        return pos < input.length();
    }

    /*Regular expression term types*/
    private RegEx regex() {
        RegEx term = term();

        while (more() && peek() == OR) {
            eat('|');
            term = Choice.subChoices(term, term());
        }
        return term;
    }

    private RegEx term() {
        RegEx factor = new Blank();

        while (more() && peek() != PAR_CL && peek() != '|') {
            RegEx nextFactor = factor();
            factor = new Sequence(factor, nextFactor);
        }

        factor = flatten(factor);

        return factor;
    }

    private RegEx factor() {

        RegEx base = base();

        while (more() && nextIn(OPEN_LOOP)) {
            switch (peek()) {
                case PLUS:
                    base = new OneOrMore(base);
                    eat(PLUS);
                    if (more() && peek() == OPT) {
                        eat(OPT);
                    } else if (more() && peek() == PLUS) {
                        eat(PLUS);
                    }
                    break;
                case STAR:
                    base = new Repetition(base);
                    eat(STAR);
                    if (more() && peek() == OPT) {
                        eat(OPT);
                    } else if (more() && peek() == PLUS) {
                        eat(PLUS);
                    }
                    break;
                case OPT:
                    base = new Optional(base);
                    eat(OPT);
                    // TODO: case for z?+Z
                    if (more() && peek() == OPT) {
                        eat(OPT);
                    } else if (more() && peek() == PLUS) {
                        eat(PLUS);
                    }
                    break;
                case BRAC_OP:
                    base = greedyBound(base);
                    break;
            }
        }

        return base;
    }

    private RegEx base() {

        switch (peek()) {
            case CLASS_OP:
                eat(CLASS_OP);
                RegEx c = charClass(false);
                eat(CLASS_CL);
                return c;
            case PAR_OP:
                return handleParenthesis();
            case '\\':
                return handleEscape();
            case DOT:
                eat(DOT);
                return new AnyChar();
            case '^':
                eat('^');
                return new Blank();
            case '$':
                eat('$');
                return new Blank();
            default:
                return new Primitive(next());
        }
    }

    private RegEx handleParenthesis() {
        eat(PAR_OP);
        groupNo++;
        RegEx r = null;
        if (peek() == OPT) {
            next();
            StringBuilder name = new StringBuilder();
            switch (peek()) {
                case NAMED_OP:
                    next();
                    switch (peek()) {
                        case EQUAL:
                            next();
                            r = new SpecialConstructGroup(GroupType.POSITIVE_LOOKBEHIND, regex());
                            break;
                        case EXCLM:
                            next();
                            r = new SpecialConstructGroup(GroupType.NEGATIVE_LOOKBEHIND, regex());
                            break;
                        default:
                            while (peek() != NAMED_CL) {
                                name.append(next());
                            }
                            next();
                            r = new NamedGroup(groupNo, name.toString(), regex(), this);
                            break;
                    }
                    break;
                case EQUAL:
                    next();
                    r = new SpecialConstructGroup(GroupType.POSITIVE_LOOKAHEAD, regex());
                    break;
                case EXCLM:
                    next();
                    r = new SpecialConstructGroup(GroupType.NEGATIVE_LOOKAHEAD, regex());
                    break;
                case COLON: 
                    next();
                    groupNo--;
                    r = new SpecialConstructGroup(GroupType.NON_CAPTURE_GROUP, regex());
                    break;
                
            }
        } else {
            r = new CapturingGroup(groupNo, regex(), this);
        }
        eat(PAR_CL);
        return r;
    }

    private RegEx handleEscape() throws NumberFormatException, RuntimeException {
        eat('\\');
        if (nextIn(BOUNDRY_MATCHERS)) {
            next();
            return new Blank();
        }
        if (nextIn(DIGIT)) {
            String number = "";
            int n = getGroupMap().size();
            while (more() && nextIn(DIGIT)) {
                int parseInt = Integer.parseInt(String.valueOf(number + peek()));
                if (parseInt <= n) {
                    number += next();
                } else {
                    break;
                }
            }
            int num = Integer.parseInt(String.valueOf(number));

            return getGroupMap().get(num);
        }
        char esc = next();
        if (esc == QUOT_OP) {
            return handleQuotations();
        }
        if (esc == 'p' && peek() == BRAC_OP) {
            StringBuilder s = new StringBuilder();
            eat(BRAC_OP);
            while (peek() != BRAC_CL) {
                s.append(next());
            }
            eat(BRAC_CL);
            return handlePosix(s.toString());
        }
        if (esc == 'k' && peek() == NAMED_OP) {
            StringBuilder s = new StringBuilder();
            next();
            while (peek() != NAMED_CL) {
                s.append(next());
            }
            next();
            return getNamedGroupMap().get(s.toString());
        }
        if (SpecialChar.sChars.containsKey(esc)) {
            return SpecialChar.sChars.get(esc);
        }
        return new Primitive(esc);
    }

    private RegEx handleQuotations() throws RuntimeException {
        ArrayList<RegEx> quotations = new ArrayList<>();
        while (!(peek() == ESCAPE && nextChar() == QUOT_CL)) {
            char quot = next();
            quotations.add(new Primitive(quot));
        }
        eat(ESCAPE);
        eat(QUOT_CL);
        if (nextIn(OPEN_LOOP)) {
            throw new RuntimeException(this.input + " token not Quantifiable at " + (pos - 1));
        }
        return new Concat(quotations);
    }

    private RegEx charClass(boolean isIntersection) {
        CharClass charClass;
        if (peek() == NOT) {
            eat(NOT);
            charClass = new CharClass(true, isIntersection);
        } else {
            charClass = new CharClass(false, isIntersection);
        }
        if (peek() == CLASS_CL || peek() == DASH) {
            charClass.addToClass(new Primitive(peek()));
            next();
        }
        while (peek() != CLASS_CL) {
            if (nextChar() == DASH) {
                char from = peek();
                eat(from);
                next();
                if (peek() == CLASS_CL) {
                    charClass.addToClass(new Primitive(DASH));
                } else {
                    char to = peek();
                    eat(to);
                    charClass.addToClass(new Range(from, to));
                }
            } else if (peek() == CLASS_OP) {
                eat(CLASS_OP);
                RegEx c = charClass(false);
                eat(CLASS_CL);
                charClass.addToClass(c);
            } else if (peek() == AND) {
                next();
                if (peek() == AND) {
                    next();
                    if (peek() == CLASS_OP) {
                        eat(CLASS_OP);
                        RegEx c = charClass(true);
                        eat(CLASS_CL);
                        charClass.addToClass(c);
                    }
                } else {
                    charClass.addToClass(new Primitive(AND));
                }
            }else if(peek() == ESCAPE && SpecialChar.sChars.containsKey(nextChar())) {
                next();
                charClass.addToClass(SpecialChar.sChars.get(peek()));
                next();
            }else{
                charClass.addToClass(new Primitive(next()));
            }
        }
        return charClass;
    }

    private RegEx greedyBound(RegEx base) {
        GreedyBound gr = new GreedyBound(base);
        eat(BRAC_OP);
        String number = "";
        while (peek() != BRAC_CL && peek() != COMMA) {
            number += next();
        }
        int min = Integer.parseInt(String.valueOf(number));
        gr.setMin(min);

        int max;
        if (peek() == COMMA) {
            eat(COMMA);
            if (nextIn(DIGIT)) {
                number = "";
                while (peek() != BRAC_CL) {
                    number += next();
                }
                gr.setMaxPresent(true);
                max = Integer.parseInt(String.valueOf(number));
                gr.setMax(max);
            } else {
                gr.setMaxPresent(false);
            }
        } else if (peek() == BRAC_CL) {
            gr.setMaxPresent(true);
            gr.setMax(min);
        }

        eat(BRAC_CL);
        return gr;
    }

    private RegEx flatten(RegEx factor) {
        ArrayList<RegEx> flattenedList = new ArrayList<>();
        Sequence factorCopy = (Sequence) factor;

        while (factorCopy.firstSeq != null && (factorCopy.firstSeq instanceof Sequence)) {
            flattenedList.add(0, factorCopy.secondSeq);
            factorCopy = (Sequence) factorCopy.firstSeq;
        }

        flattenedList.add(0, factorCopy.secondSeq);

        if (flattenedList.size() == 1) {
            return flattenedList.get(0);
        }

        return new Concat(flattenedList);
    }

    private Posix handlePosix(String posix) {
        Posix posixClass;

        posixClass = new Posix(posix, false, false);
        switch (posix) {
            case "Lower":
                posixClass.addToClass(new Range('a', 'z'));
                break;
            case "Upper":
                posixClass.addToClass(new Range('A', 'Z'));
                break;
            case "ASCII":
                posixClass.addToClass(new Range((char) 0x00, (char) 0x7F));
                break;
            case "Alpha":
                posixClass.addToClass(handlePosix("Lower"), handlePosix("Upper"));
                break;
            case "Digit":
                posixClass.addToClass(new Range('0', '9'));
                break;
            case "Alnum":
                posixClass.addToClass(handlePosix("Alpha"), handlePosix("Digit"));
                break;
            case "Punct":
                String punctuations = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
                for (int i = 0; i < punctuations.length(); i++) {
                    posixClass.addToClass(new Primitive(punctuations.charAt(i)));
                }
                break;
            case "Graph":
                posixClass.addToClass(handlePosix("Alnum"), handlePosix("Punct"));
                break;
            case "Print":
                posixClass.addToClass(handlePosix("Graph"), new Primitive((char) 0x20));
                break;
            case "Blank":
                posixClass.addToClass(new Primitive(' '), new Primitive('\t'));
                break;
            case "Cntrl":
                posixClass.addToClass(new Range((char) 0x00, (char) 0x1F), new Primitive((char) 0x7F));
                break;
            case "XDigit":
                posixClass.addToClass(new Range('0', '9'), new Range('a', 'f'), new Range('A', 'F'));
                break;
            case "Space":
                posixClass.addToClass(new Primitive(' '), new Primitive('\t'), new Primitive('\n'), new Primitive((char) 0x0B), new Primitive('\f'), new Primitive('\r'));
                break;
            default:
                break;
        }

        return posixClass;
    }

    public HashMap<String, RegEx> getNamedGroupMap() {
        return namedGroupMap;
    }

    public void setNamedGroupMap(HashMap<String, RegEx> namedGroupMap) {
        this.namedGroupMap = namedGroupMap;
    }

    public HashMap<Integer, RegEx> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(HashMap<Integer, RegEx> groupMap) {
        this.groupMap = groupMap;
    }

}
