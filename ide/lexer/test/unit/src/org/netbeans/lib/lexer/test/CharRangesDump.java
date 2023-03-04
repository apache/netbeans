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

package org.netbeans.lib.lexer.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Dump of various character ranges.
 *
 * @author Miloslav Metelka
 */
public class CharRangesDump {

    public static void main(String... args) {
        String methodName = (args[0] != null) ? args[0] : "isWhitespace";
        new CharRangesDump(new CharacterMethodAcceptor(methodName)).dump();
    }

    private UnicodeCharAcceptor acceptor;

    private List<Integer> charRanges = new ArrayList<Integer>();
    
    public CharRangesDump(UnicodeCharAcceptor acceptor) {
        if (acceptor == null) {
            throw new IllegalArgumentException("acceptor must be non-null");
        }
        this.acceptor = acceptor;
        initRanges();
    }
    
    private void initRanges() {
        int seqStart = -1;
        for (int i = 0; i <= Character.MAX_CODE_POINT + 1; i++) {
            if ((i <= Character.MAX_CODE_POINT) && acceptor.isAccepted(i)) {
                if (seqStart == -1) {
                    seqStart = i;
                } // sequence already in progress
            } else { // char not accepted
                if (seqStart != -1) { // sequence in progress
                    charRanges.add(seqStart);
                    charRanges.add(i - 1);
                    seqStart = -1;
                }
            }
        }
    }
    
    public List<Integer> charRanges() {
        return charRanges;
    }
    
    public void dump() {
        StringBuilder sb = new StringBuilder(acceptor.toString());
        sb.append('\n');
        for (int i = 0; i < charRanges.size(); i++) {
            Integer start = charRanges.get(i++);
            Integer end = charRanges.get(i);
            if (!start.equals(end)) { // sequence of 2 chars or more
                sb.append("Sequence: " + charToString(start) + " - "
                        + charToString(end));
                int seqLen = end - start;
                if (seqLen >= 2 && seqLen <= 9) {
                    sb.append(" incl. ");
                    for (int c = start + 1; c < end; c++) {
                        if (c != start + 1) {
                            sb.append(", ");
                        }
                        sb.append('\'');
                        sb.append(LexerTestUtilities.debugText(String.valueOf((char)c)));
                        sb.append('\'');
                    }
                }
                sb.append('\n');
            } else { // single char only
                sb.append("Char: " + charToString(start) + '\n');
            }
        }
        
        System.err.println(sb.toString());
    }
    
    public void dumpAsserts() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charRanges.size(); i++) {
            Integer start = charRanges.get(i++);
            Integer end = charRanges.get(i);
            if (start > 0x7F) { // Only ranges up to 0x7F
                break;
            }
            appendAssert(sb, i - 1, start);
            appendAssert(sb, i, end);
        }
        System.err.println(sb.toString());
    }
    
    private static void appendAssert(StringBuilder sb, int index, Integer value) {
        sb.append("TestCase.assertEquals(charRanges.get(" + index + ").intValue(), 0x"
                + Integer.toString(value.intValue(), 16) + ");\n");
    }
    
    private String charToString(int i) {
        return "'" + LexerTestUtilities.debugText(String.valueOf((char)i))
                + "', " + i + "(0x" + Integer.toString(i, 16) + ")";
    }
    
    public interface UnicodeCharAcceptor {

        boolean isAccepted(int c);

    }
    
    public static final class CharacterMethodAcceptor implements UnicodeCharAcceptor {
        
        private final Method characterClassMethod;
        
        public CharacterMethodAcceptor(String methodName) {
            try {
                characterClassMethod = Character.class.getDeclaredMethod(
                        methodName, new Class[] { int.class });
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        public boolean isAccepted(int c) {
            try {
                Object result = characterClassMethod.invoke(null, new Integer(c));
                assert (result instanceof Boolean);
                return (Boolean.TRUE.equals(result));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        
        public String toString() {
            return characterClassMethod.toString();
        }

    }

}
