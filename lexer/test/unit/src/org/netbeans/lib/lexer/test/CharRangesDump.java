/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
