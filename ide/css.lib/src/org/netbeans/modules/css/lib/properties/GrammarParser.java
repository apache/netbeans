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
package org.netbeans.modules.css.lib.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.TokenAcceptor;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Parser of the semi-grammar expressions taken from the w3c.org css specifications.
 * 
 * @author mfukala@netbeans.org
 */
public class GrammarParser {
    
    /**
     * For tests only.
     * 
     * @param expresssion
     * @return 
     */
    public static GroupGrammarElement parse(String expresssion) {
        return parse(expresssion, null);
    }

    public static GroupGrammarElement parse(String expression, String propertyName) {
        return new GrammarParser(expression, propertyName).parse();
    }
    
    private String propertyName;
    private String expression;

    public GrammarParser(String expression, String propertyName) {
        this.expression = expression;
        this.propertyName = propertyName;
    }
    
    private GroupGrammarElement parse() {
        // Tracks index of all groups inside this parse invocation
        AtomicInteger groupIndex = new AtomicInteger(0);
        // Tracks index of property sub definitions inside their parent
        AtomicInteger inPropertyIndex = new AtomicInteger(0);
        int openedParenthesis = 0;
        GroupGrammarElement root = new GroupGrammarElement(null, groupIndex.getAndIncrement(), propertyName);
        ParserInput input = new ParserInput(expression);

        parseElements(input, root, false, groupIndex, inPropertyIndex, openedParenthesis, new HashMap<>());

        if (openedParenthesis != 0) {
            throw new IllegalStateException(String.format("Property '%s' parsing error - bracket pairs doesn't match: ", propertyName, openedParenthesis));
        }
        return root;
    }

    @SuppressWarnings("fallthrough")
    private void parseElements(
            ParserInput input, GroupGrammarElement parent, boolean ignoreInherits,
            AtomicInteger groupIndex, AtomicInteger inPropertyIndex,
            int openedParenthesis, Map<PropertyGrammarElementRef,
            GroupGrammarElement> knownChilds
    ) {
        GrammarElement last = null;
        for (;;) {
            char c = input.read();
            if (c == Character.MAX_VALUE) {
                return;
            }
            switch (c) {
                case ' ':
                case '\t':
                    //ws, ignore
                    break;
                case '&':
                    char next = input.read();
                    if (next == '&') {
                        //the group is a list
                        parent.setType(GroupGrammarElement.Type.ALL);
                    } else {
                        input.backup(1);
                    }
                    break;
                    
                case '[':
                    openedParenthesis++;
                    //group start
                    last = new GroupGrammarElement(parent, groupIndex.getAndIncrement());
                    parseElements(input, (GroupGrammarElement) last, false, groupIndex, inPropertyIndex, openedParenthesis, knownChilds);
                    parent.addElement(last);
                    break;

                case '|':
                    next = input.read();
                    if (next == '|') {
                        //the group is a list
                        parent.setType(GroupGrammarElement.Type.COLLECTION);
                    } else {
                        input.backup(1);
                        parent.setType(GroupGrammarElement.Type.SET);
                        // else it means OR
                    }
                    break;

                case ']':
                    //group end
                    openedParenthesis--;
                    return; //return from parseElements

                case '<':
                    //reference
                    StringBuilder buf = new StringBuilder();
                    for (;;) {
                        c = input.read();
                        if (c == '>') {
                            break;
                        } else {
                            buf.append(c);
                        }
                    }

                    String referredElementName = buf.toString();
                    PropertyGrammarElementRef currRef = new PropertyGrammarElementRef(parent, inPropertyIndex.incrementAndGet(), referredElementName);
                    if(knownChilds.containsKey(currRef)) {
                        last = knownChilds.get(currRef);
                        parent.addElement(last);
                    } else {
                        PropertyDefinition property = Properties.getPropertyDefinition(referredElementName, true);
                        if (property == null) {
                            throw new IllegalStateException(
                                    String.format("Property '%s' parsing error: No referred element '%s' found. "
                                    + "Read input: %s", propertyName, referredElementName, input.readText())); //NOI18N
                        }

                        ParserInput pinput = new ParserInput(property.getGrammar());
                        String propName = property.getName();
                        last = new GroupGrammarElement(parent, groupIndex.getAndIncrement(), propName);

                        knownChilds.put(currRef, (GroupGrammarElement) last);

                        //ignore inherit tokens in the subtree
                        // Parsing a property sub definition uses its own inPropertyIndex
                        AtomicInteger inPropertyIndexChild = new AtomicInteger(0);
                        parseElements(pinput, (GroupGrammarElement) last, true, groupIndex, inPropertyIndexChild, openedParenthesis, knownChilds);

                        parent.addElement(last);
                    }

                    break;

                case '!':
                    //unit value
                    buf = new StringBuilder();
                    for (;;) {
                        c = input.read();
                        if (c == Character.MAX_VALUE) {
                            break;
                        }
                        if (isEndOfValue(input)) {
                            input.backup(1);
                            break;
                        } else {
                            buf.append(c);
                        }
                    }
                    String unitName = buf.toString();
                    TokenAcceptor acceptor = TokenAcceptor.getAcceptor(unitName);
                    if(acceptor == null) {
                        throw new IllegalStateException(
                                String.format("Property '%s' parsing error - No unit property value acceptor for '%s'. "
                                + "Read input: '%s'",
                                propertyName, unitName, input.readText())); //NOI18N
                    }
                    
                    last = new UnitGrammarElement(parent, acceptor, null);
                    parent.addElement(last);
                    break;

                case '{':
                    //multiplicity range {min,max}
                    StringBuilder text = new StringBuilder();
                    for (;;) {
                        c = input.read();
                        if (c == '}') {
                            break;
                        } else {
                            text.append(c);
                        }
                    }
                    String[] parts =  text.toString().split(",", -1); //NOI18N
                    if(parts.length == 1) {
                        int elements = Integer.parseInt(parts[0]);
                        last.setMinimumOccurances(elements);
                        last.setMaximumOccurances(elements);
                    } else if (parts.length == 2) {
                        int min = 0;
                        int max = Integer.MAX_VALUE;
                        if(! parts[0].trim().isEmpty()) {
                            min = Integer.parseInt(parts[0]);
                        }
                        if(! parts[1].trim().isEmpty()) {
                            max = Integer.parseInt(parts[1]);
                        }
                        last.setMinimumOccurances(min);
                        last.setMaximumOccurances(max);
                    } else {
                        throw new IllegalArgumentException("Invalid multiplicity: " + text.toString());
                    }



                    break;

                case '+':
                    //multiplicity 1-infinity
                    last.setMaximumOccurances(Integer.MAX_VALUE);
                    break;

                case '*':
                    //multiplicity 0-infinity
                    last.setMinimumOccurances(0);
                    last.setMaximumOccurances(Integer.MAX_VALUE);
                    break;

                case '?':
                    //multiplicity 0-1
                    last.setMinimumOccurances(0);
                    last.setMaximumOccurances(1);
                    break;

                case '(':
                    //named elements support; syntax: element($name)
                    char ch = input.read();
                    if(ch == '$') {
                        buf = new StringBuilder();
                        for (;;) {
                            ch = input.read();
                            if (ch == ')') {
                                break;
                            } else {
                                buf.append(ch);
                            }
                        }
                        last.setName(buf.toString());
                        break;
                    }
                    
                    input.backup(1);
                    //intentional fallthrough to the default case!
                    //if the bracket ( is not followed by $ the meaning is 
                    //a simple value.

                default:
                    //values
                    buf = new StringBuilder();
                    boolean quotes = isQuoteChar(c);
                    if(quotes) {
                        c = input.read();
                    }
                    
                    for (;;) {
                        if (c == Character.MAX_VALUE) {
                            break;
                        }
                        if(quotes) {
                            //quoted value - anything except the quote is considered
                            //as the value char
                            if(isQuoteChar(c)) {
                                //closing quote, do not backup
                                break; 
                            }
                        } else {
                            //unqouted value - end the value by various characters
                            if(isEndOfValue(input)) {
                                input.backup(1);
                                break;
                            }
                        }
                        
                        //append the char to the value
                        buf.append(c);
                        
                        c = input.read(); //also include the char from main loop

                    }

                    if (!(ignoreInherits && LexerUtils.equals("inherit", buf, true, true))) { //NOI18N
                        last = new FixedTextGrammarElement(parent, buf, null);
                        parent.addElement(last);
                    }
                    break;

            }
        }

    }

    private static boolean isEndOfValue(ParserInput input) {
        char c = input.LA(0);
        switch(c) {
            case ' ': //ws after the element
            case '+': //multiplicity operator
            case '?': //multiplicity operator
            case '*': //multiplicity operator
            case '&': //first char of the and (&&) operator
            case '{': //multiplicity in curly bracket
            case '[': //following group start
            case ']': //current group end
            case '|': //first char of || operator or | operator itself
                return true;
                
            case '(': //element name start, must be followed by $ to be the termination char
                return input.LA(1) == '$';
                
            default:
                return false;
        }
        
    }

    private static boolean isQuoteChar(char c) {
        return c == '\'' || c == '"';
    }

    private static class ParserInput {

        CharSequence text;
        private int pos = 0;

        private ParserInput(CharSequence text) {
            this.text = text;
        }

        public char read() {
            if (pos == text.length()) {
                return Character.MAX_VALUE;
            } else {
                return text.charAt(pos++);
            }
        }
        
        /**
         * lookahead 
         * 
         * la(0) == last read char
         * la(1) == next char, as read() + backup(1)
         */
        public char LA(int lookahead) {
            //when a char is read, the pointer is moved 
            //to the next position so if we want to la(0) 
            //to return the last read char we need to read 
            //from pos - 1 position
            int dec = pos == 0 ? 0 : 1;
            int la_pos = pos - dec + lookahead;
            if(la_pos >= text.length()) {
                return Character.MAX_VALUE;
            } else {
                return text.charAt(la_pos);
            }
        }

        public void backup(int chars) {
            pos -= chars;
        }

        public CharSequence readText() {
            return text.subSequence(0, pos);
        }
    }

    private static class PropertyGrammarElementRef {
        private final GrammarElement parent;
        private final int indexInsideParent;
        private final String propertyName;

        public PropertyGrammarElementRef(GrammarElement parent, int indexInsideParent, String propertyName) {
            this.parent = parent;
            this.indexInsideParent = indexInsideParent;
            this.propertyName = propertyName;
        }

        // Find the first named parent item (property definition) and return
        // its name. If no names parent is found, null is returned
        private String getParentName() {
            for(GrammarElement p = this.parent; p != null; p = p.parent()) {
               if(p.getName() != null) {
                   return p.getName();
               }
            }
            return null;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.indexInsideParent;
            hash = 29 * hash + Objects.hashCode(this.propertyName);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PropertyGrammarElementRef other = (PropertyGrammarElementRef) obj;
            if (this.indexInsideParent != other.indexInsideParent) {
                return false;
            }
            if (!Objects.equals(this.propertyName, other.propertyName)) {
                return false;
            }
            return Objects.equals(getParentName(), other.getParentName());
        }
    }
}
