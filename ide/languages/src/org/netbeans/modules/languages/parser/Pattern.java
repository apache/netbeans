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

package org.netbeans.modules.languages.parser;

import java.util.Map;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.CharInput;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.languages.TokenType;
import org.netbeans.modules.languages.parser.StringInput;

public class Pattern {
    
    private static final Character STAR = new Character ((char) 0);
    private static NodeFactory<Integer> nodeFactory = new NodeFactory<Integer> () {
        private int counter = 1;
        public Integer createNode () {
            return Integer.valueOf (counter++);
        }
    };
    
    public static Pattern create () {
        return new Pattern ();
    }
    
    public static Pattern create (String input) throws ParseException {
        if (input.length () == 0) throw new ParseException ("Empty pattern.");
        return create (new StringInput (input));
    }
    
    public static Pattern create (CharInput input) throws ParseException {
        Pattern p = createIn (input);
        DG<Integer,Character,Integer,TokenType> ndg = DGUtils.<Integer,Character,Integer,TokenType>reduce (p.dg, nodeFactory);
        return new Pattern (ndg);
    }
    
    private static Pattern createCaseInsensitive (StringBuffer input) throws ParseException {
        int length = input.length();
        Pattern pattern = new Pattern ();
        for (int x = 0; x < length; x++) {
            char c = input.charAt(x);
            char up = Character.toUpperCase(c);
            char down = Character.toLowerCase(c);
            if (up != down) {
                pattern = pattern.append(
                        new Pattern(new Character(up)).merge(new Pattern(new Character(down)))
                );
            } else {
                pattern = pattern.append(new Pattern(new Character(c)));
            }
        }
        return pattern;
    }
    
    private static Pattern createIn (CharInput input) throws ParseException {
        Pattern pattern = new Pattern ();
        Pattern last = null;
        char ch = input.next ();
        while (ch != 0) {
            switch (ch) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    input.read ();
                    break;
                case '*':
                    input.read ();
                    if (last == null) throw new ParseException ("Unexpected character '" + ch + "'.");
                    last = last.star ();
                    break;
                case '?':
                    input.read ();
                    if (last == null) throw new ParseException ("Unexpected character '" + ch + "'.");
                    last = last.question ();
                    break;
                case '+':
                    input.read ();
                    if (last == null) throw new ParseException ("Unexpected character '" + ch + "'.");
                    last = last.plus ();
                    break;
                case '(':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = createIn (input);
                    if (input.next () != ')')
                        throw new ParseException ("Unexpected character '" + input.next () + "'.");
                    input.read ();
                    break;
//                case '<':
//                    input.read ();
//                    if (last != null) pattern = pattern.append (last);
//                    last = new Pattern (readToken (input));
//                    if (input.read () != '>')
//                        throw new ParseException ("> expected: " + input);
//                    break;
                case '\'':
                case '"':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = Pattern.create ();
                    StringBuffer buf = new StringBuffer();
                    ch = input.next ();
                    while (ch != '"' && ch != '\'') {
                        if (ch == 0)
                            throw new ParseException ("Unexpected character '" + ch + "'.");
                        if (ch == '\\') {
                            input.read ();
                            switch (input.next ()) {
                                case '\\':
                                    input.read ();
                                    buf.append('\\');
                                    break;
                                case 'n':
                                    input.read ();
                                    buf.append('\n');
                                    break;
                                case 'r':
                                    input.read ();
                                    buf.append('\r');
                                    break;
                                case 't':
                                    input.read ();
                                    buf.append('\t');
                                    break;
                                case '"':
                                    input.read ();
                                    buf.append('"');
                                    break;
                                case '\'':
                                    input.read ();
                                    buf.append('\'');
                                    break;
                                case 'u':
                                    input.read ();
                                    int ch1 = 0;
                                    for (int i = 16*16*16; i >= 1; i/=16) {
                                        char c = input.next ();
                                        int ii = 0;
                                        if ('0' <= c && c <= '9') {
                                            ii = c - '0';
                                        } else if ('a' <= c && c <= 'f') {
                                            ii = c - 'a' + 10;
                                        } else if ('A' <= c && c <= 'F') {
                                            ii = c - 'A' + 10;
                                        } else {
                                            throw new ParseException ("Unexpected character after \\u:" + c);
                                        }
                                        ch1 += ii * i;
                                        input.read ();
                                    }
                                    buf.append((char) ch1);
                                    break;
                                default:
                                    throw new ParseException ("Unexpected character after \\:" + input.next ());
                            }
                        } else {
                            buf.append(input.read());
                        }
                        ch = input.next ();
                    }
                    input.read ();
                    ch = input.next();
                    if (ch == 'i') {
                        input.read();
                        last = last.append(createCaseInsensitive(buf));
                    } else {
                        int length = buf.length();
                        Pattern pat = new Pattern();
                        for (int x = 0; x < length; x++) {
                            pat = pat.append(new Pattern(new Character(buf.charAt(x))));
                        }
                        last = last.append(pat);
                    }
                    break;
                case '|':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = null;
                    pattern = pattern.merge (Pattern.createIn (input));
                    return pattern;
                case '-':
                    if (last != null) pattern = pattern.append (last);
                    input.read ();
                    skipWhitespaces (input);
                    ch = input.next ();
                    if (ch != '\'' && ch != '"')
                        throw new ParseException ("Unexpected character '" + ch + "'.");
                    input.read ();
                    ch = input.next ();
                    if (ch == '\'' || ch == '"')
                        throw new ParseException ("Unexpected character '" + ch + "'.");
                    Character edge = new Character (input.next ());
                    last = new Pattern (true, Collections.<Character>singleton (edge));
                    last = last.star ().append (new Pattern (edge));
                    input.read ();
                    ch = input.next ();
                    while (ch != '\'' && ch != '"') {
                        if (ch == 0)
                            throw new ParseException ("Unexpected character '" + ch + "'.");
                        last = last.plus ();
                        Integer endN = last.dg.getEnds ().iterator ().next ();
                        Integer newE = last.nodeFactory.createNode ();
                        last.dg.addNode (newE);
                        last.dg.addEdge (endN, newE, new Character (input.next ()));
                        last.dg.setEnds (Collections.singleton (newE));
                        input.read ();
                        ch = input.next ();
                    }
                    input.read ();
                    break;
                case ')':
                    if (last != null) pattern = pattern.append (last);
                    return pattern;
                case '.':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = new Pattern (Pattern.STAR);
                    break;
                case '[':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    boolean not = false;
                    ch = input.next ();
                    if (ch == '^') {
                        input.read ();
                        ch = input.next ();
                        not = true;
                    }
                    Set<Character> set = new HashSet<Character> ();
                    char l = (char) 0;
                    boolean minus = false;
                    ch = input.next ();
                    while (ch != ']' && ch != 0) {
                        switch (ch) {
                            case ' ':
                            case '\t':
                            case '\n':
                            case '\r':
                                input.read ();
                                break;
                            case '\'':
                            case '"':
                                char ol = l;
                                if (l != 0 && !minus) 
                                    set.add (new Character (l));
                                input.read ();
                                ch = input.next ();
                                if (ch == '\\') {
                                    input.read ();
                                    ch = input.next ();
                                    switch (ch) {
                                        case 'n':
                                            l = '\n';
                                            break;
                                        case 't':
                                            l = '\t';
                                            break;
                                        case 'r':
                                            l = '\r';
                                            break;
                                        case '\'':
                                            l = '\'';
                                            break;
                                        case '\\':
                                            l = '\\';
                                            break;
                                        case '"':
                                            l = '"';
                                            break;
                                        case 'u':
                                            l = 0;
                                            for (int i = 16*16*16; i >= 1; i/=16) {
                                                input.read ();
                                                char c = input.next ();
                                                int ii = 0;
                                                if ('0' <= c && c <= '9') {
                                                    ii = c - '0';
                                                } else if ('a' <= c && c <= 'f') {
                                                    ii = c - 'a' + 10;
                                                } else if ('A' <= c && c <= 'F') {
                                                    ii = c - 'A' + 10;
                                                } else {
                                                    throw new ParseException ("Unexpected character after \\u:" + c);
                                                }
                                                l += ii * i;
                                            }
                                            break;
                                        default:
                                            throw new ParseException ("Unexpected character '" + ch + "'.");
                                    } // switch
                                    input.read ();
                                } else // if '\\'
                                    l = input.read ();
                                ch = input.next ();
                                if (ch != '"' && ch != '\'')
                                    throw new ParseException ("Unexpected character '" + ch + "'.");
                                input.read ();
                                if (minus) {
                                    addInterval (set, ol, l);
                                    l = 0;
                                }
                                minus = false;
                                break; // case '"'
                            case '-':
                                input.read ();
                                if (l == 0) throw new ParseException ("Unexpected character '-'.");
                                minus = true;
                                break;
//                            case '<':
//                                input.read ();
//                                if (minus) throw new ParseException (input.toString ());
//                                if (l != 0) 
//                                    set.add (new Character (l));
//                                set.add (readToken (input));
//                                if (input.read () != '>')
//                                    throw new ParseException ("> expected: " + input);
//                                break;
                            default:
                                throw new ParseException ("Unexpected character '" + ch + "'.");
                        } // switch
                        ch = input.next ();
                    } // while
                    if (minus) throw new ParseException ("Unexpected character '" + ch + "'.");
                    if (l != 0) 
                        set.add (new Character (l));
                    input.read ();
                    last = new Pattern (not, set);
                    break;
                default:
                    throw new ParseException ("Unexpected character '" + ch + "'.");
//                    input.read ();
//                    if (last != null) pattern = pattern.append (last);
//                    last = new Pattern (new Character (ch));
            } // switch
            ch = input.next ();
        } // while
        if (last != null) pattern = pattern.append (last);
        return pattern;
    }

//    private static ASTToken readToken (CharInput input) throws ParseException {
//        StringBuilder sb = new StringBuilder ();
//        char ch = input.next ();
//        while (ch != ',' && ch != '>') {
//            if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
//            sb.append (ch);
//            input.read ();
//            ch = input.next ();
//        }
//        ch = input.next ();
//        String type = sb.toString ().trim ();
//        if (ch == '>') return ASTToken.create (type, null);
//        input.read ();
//        skipWhitespaces (input);
//        sb = new StringBuilder ();
//        ch = input.next ();
//        boolean read = ch != '"' && ch != '\'';
//        if (!read) {
//            input.read ();
//            ch = input.next ();
//        }
//        while (ch != '>' && ch != '"' && ch != '\'' && ch != ',') {
//            if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
//            sb.append (ch);
//            input.read ();
//            ch = input.next ();
//        }
//        if (read && (ch == '"' || ch == '\'')) throw new ParseException ("Unexpected \":" + input.toString ());
//        if (!read) input.read ();
//        String identifier = null;
//        String name = null;
//        if (read) name = sb.toString ();
//        else identifier = sb.toString ();
//        if (!read && ch == ',') {
//            ch = input.next ();
//            sb = new StringBuilder ();
//            while (ch != '>') {
//                if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
//                sb.append (ch);
//                input.read ();
//                ch = input.next ();
//            }
//            name = sb.toString ();
//        }
//        return ASTToken.create (type, identifier);
//    }
    
    private static Set<Character> whitespace = new HashSet<Character> ();
    static {
        whitespace.add (new Character (' '));
        whitespace.add (new Character ('\n'));
        whitespace.add (new Character ('\r'));
        whitespace.add (new Character ('\t'));
    }
    
    private static void skipWhitespaces (CharInput input) {
        while (whitespace.contains (new Character (input.next ())))
            input.read ();
    }
    
    private static void addInterval (Set<Character> set, char from, char to) 
    throws ParseException {
        if (from > to) throw new ParseException ("Invalid interval (" + from + ">" + to + ").");
        do {
            set.add (new Character (from));
            from++;
        } while (from <= to);
    }
    
    private DG<Integer,Character,Integer,TokenType> dg;// = DG.<Integer,Character,K,TokenType>createDG ();
    
    private Pattern (DG<Integer,Character,Integer,TokenType> dg) {
        this.dg = dg;
    }
    
    private Pattern () {
        dg = DG.<Integer,Character,Integer,TokenType>createDG (nodeFactory.createNode ());
//        Integer start = nodeFactory.createNode ();
//        dg.addNode (start);
//        dg.setStart (start);
//        dg.addEnd (start);
    }

    private Pattern (Pattern p) {
        dg = DGUtils.<Integer,Character,Integer,TokenType>cloneDG (p.dg, false, nodeFactory);
    }
    
    private Pattern (Character edge) {
        Integer start = nodeFactory.createNode ();
        dg = DG.<Integer,Character,Integer,TokenType>createDG (start);
        Integer end = nodeFactory.createNode ();
        dg.addNode (end);
        dg.addEdge (start, end, edge);
        dg.setEnds (Collections.<Integer>singleton (end));
    }

    private Pattern (boolean not, Set<Character> edges) {
        Integer start = nodeFactory.createNode ();
        dg = DG.<Integer,Character,Integer,TokenType>createDG (start);
        Integer end = nodeFactory.createNode ();
        dg.addNode (end);
        dg.setStart (start);
        dg.setEnds (Collections.<Integer>emptySet ());
        Iterator<Character> it = edges.iterator ();
        while (it.hasNext ()) {
            Character edge = it.next ();
            dg.addEdge (start, end, edge);
        }
        if (not) {
            Integer failedState = nodeFactory.createNode ();
            dg.addNode (failedState);
            dg.addEdge (start, failedState, Pattern.STAR);
            dg.addEnd (failedState);
        } else 
            dg.addEnd (end);
    }
    
    public Pattern clonePattern () {
        return new Pattern (this);
    }

    public Pattern star () {
        DG<Integer,Character,Integer,TokenType> ndg = DGUtils.<Integer,Character,Integer,TokenType>plus (dg, STAR, nodeFactory);
        ndg = DGUtils.<Integer,Character,Integer,TokenType>merge (DG.<Integer,Character,Integer,TokenType>createDG (nodeFactory.createNode ()), ndg, STAR, nodeFactory);
        Pattern p = new Pattern (ndg);
        return p;
    }

    public Pattern plus () {
        DG<Integer,Character,Integer,TokenType> ndg = DGUtils.<Integer,Character,Integer,TokenType>plus (dg, STAR, nodeFactory);
        Pattern p = new Pattern (ndg);
        return p;
    }

    public Pattern question () {
        DG<Integer,Character,Integer,TokenType> ndg = DGUtils.<Integer,Character,Integer,TokenType>cloneDG (dg, true, nodeFactory);
        ndg.addEnd (ndg.getStartNode ());
        Pattern p = new Pattern (ndg);
        return p;
    }

    public Pattern merge (Pattern parser) {
        DG<Integer,Character,Integer,TokenType> ndg = DGUtils.<Integer,Character,Integer,TokenType>merge (dg, parser.dg, STAR, nodeFactory);
        Pattern p = new Pattern (ndg);
        return p;
    }

    public Pattern append (Pattern parser) {
        DG<Integer,Character,Integer,TokenType> ndg = DGUtils.<Integer,Character,Integer,TokenType>append (dg, parser.dg, STAR, nodeFactory);
        Pattern p = new Pattern (ndg);
        return p;
    }

    public boolean matches (String text) {
        int i = 0;
        Integer state = dg.getStartNode ();
        while (i < text.length ()) {
            state = dg.getNode (state, new Character (text.charAt (i++)));
            if (state == null) return false;
        }
        return dg.getEnds ().contains (state);
    }

    public Integer next (CharInput input) {
        return next (dg.getStartNode (), input);
    }
    
    public Integer next (Integer state, CharInput input) {
        int lastIndex = input.getIndex ();
        Integer lastState = null;
        while (state != null) {
            if (dg.getEnds ().contains (state)) {
                lastState = state;
                lastIndex = input.getIndex ();
            }
            if (input.eof ()) break;
            Integer newState = dg.getNode (state, new Character (input.next ()));
            if (newState != null)
                state = newState;
            else
                state = dg.getNode (state, STAR);
            if (state != null) input.read ();
        }
        input.setIndex (lastIndex);
        return lastState;
    }

    public String toString () {
        return dg.toString ();
    }
    
//    public Object getValue (Object state, Object key) {
//        return dg.getProperty (state, key);
//    }
    
//    DG<Integer,Character,K,TokenType> getDG () {
//        return dg;
//    }
    
    
    public Object read (CharInput input) {
        if (input.eof ()) return null;
        int originalIndex = input.getIndex ();
        int lastIndex = -1;
        TokenType    lastTT = null;
        Integer node = dg.getStartNode ();
        while (!input.eof ()) {
            Character edge = new Character (input.next ());
            Integer nnode = dg.getNode (node, edge);
            if (nnode == null) {
                edge = Pattern.STAR;
                nnode = dg.getNode (node, edge);
            }
            
            if (input.getIndex () > originalIndex) {
                TokenType bestTT = getBestTT (node);
                if (bestTT != null) {
                    lastTT = bestTT;
                    lastIndex = input.getIndex ();
                }
            }
            
            if (nnode == null ||
                ( dg.getEdges (nnode).isEmpty () &&
                  dg.getProperties (nnode).isEmpty ()
                )
            ) {
                if (lastTT == null) {
                    // error => reset position in CURRENT pattern (state)
                    return null;
                }
                input.setIndex (lastIndex);
                return lastTT;
            }
            
            input.read ();
            node = nnode;
        }
        
        TokenType bestTT = getBestTT (node);
        if (bestTT != null) {
            lastTT = bestTT;
            lastIndex = input.getIndex ();
        }
        
        if (lastTT == null) return null;
        return lastTT;
    }
    
    private TokenType getBestTT (Integer node) {
        Map tts = dg.getProperties (node);
        TokenType best = null;
        Iterator it = tts.keySet ().iterator ();
        while (it.hasNext ()) {
            Integer i = (Integer) it.next ();
            TokenType tt = (TokenType) tts.get (i);
            if (best == null || best.getPriority () > tt.getPriority ())
                best = tt;
        }
        return best;
    }
    
    void mark (int priority, TokenType r) {
        Iterator<Integer> it = dg.getEnds ().iterator ();
        while (it.hasNext ()) {
            Integer s = it.next ();
            dg.setProperty (
                s, 
                priority, 
                r
            );
        }
    }
}
