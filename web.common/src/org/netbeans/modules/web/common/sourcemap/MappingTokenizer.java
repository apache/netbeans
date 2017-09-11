/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.sourcemap;

import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Parser of mappings of a source map.
 *
 * @author Jan Stola
 */
class MappingTokenizer implements Iterable<Mapping> {
    /** Text representation of the mappings. */
    private final String mappings;

    /**
     * Creates a new {@code MappingTokenizer}.
     * 
     * @param mappings text representation of the mappings.
     */
    MappingTokenizer(String mappings) {
        this.mappings = mappings;
    }

    @Override
    public Iterator<Mapping> iterator() {
        return new MappingIterator(mappings);
    }

    /**
     * Iterator over {@code Mapping}s.
     */
    static class MappingIterator implements Iterator<Mapping> {
        private final StringTokenizer st;
        /** Column of the last mapping. */
        private int lastColumn = 0;
        /** Source index of the last mapping. */
        private int lastSourceIndex = 0;
        /** Original line of the last mapping. */
        private int lastOriginalLine = 0;
        /** Original column of the last mapping. */
        private int lastOriginalColumn = 0;
        /** Name index of the last mapping. */
        private int lastNameIndex = 0;

        /**
         * Creates a new {@code MappingIterator}.
         * 
         * @param mappings text representation of the mappings.
         */
        MappingIterator(String mappings) {
             st = new StringTokenizer(mappings, ";,", true); // NOI18N
        }

        @Override
        public boolean hasNext() {
            return st.hasMoreTokens();
        }

        @Override
        public Mapping next() {
            Mapping mapping;
            String token = st.nextToken();
            if (",".equals(token)) { // NOI18N
                token = st.nextToken();
            }
            if (";".equals(token)) { // NOI18N
                lastColumn = 0;
                mapping = Mapping.NEW_LINE;
            } else {
                MappingParser parser = new MappingParser(token);
                mapping = new Mapping();
                lastColumn += parser.next();
                mapping.setColumn(lastColumn);
                if (parser.hasNext()) {
                    lastSourceIndex += parser.next();
                    mapping.setSourceIndex(lastSourceIndex);
                    lastOriginalLine += parser.next();
                    mapping.setOriginalLine(lastOriginalLine);
                    lastOriginalColumn += parser.next();
                    mapping.setOriginalColumn(lastOriginalColumn);
                    if (parser.hasNext()) {
                        lastNameIndex += parser.next();
                        mapping.setNameIndex(lastNameIndex);
                    }
                } else {
                    // ignore mapping of length 1:
                    return next();
                }
            }
            return mapping;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Parser of a mapping.
     */
    static class MappingParser {
        /** Continuation mask/bit. */
        private static final int CONTINUATION_MASK = 32;
        /** Data mask. */
        private static final int DATA_MASK = 31;
        /** Text representation of the mapping. */
        private final String mapping;
        /** Current position. */
        private int index = 0;

        /**
         * Creates a new {@code MappingParser}.
         * 
         * @param mapping text representation of the mapping.
         */
        MappingParser(String mapping) {
            this.mapping = mapping;
        }

        /**
         * Determines whether there is a next section in this mapping.
         * 
         * @return {@code true} when there is a next section in this mapping,
         * returns {@code false} otherwise.
         */
        boolean hasNext() {
            return mapping.length() > index;
        }

        /**
         * Returns the next section in this mapping.
         * 
         * @return next section in this mapping.
         */
        int next() {
            int result = 0;
            int shift = 0;
            boolean continuation = true;
            while (continuation) {
                char c = mapping.charAt(index);
                int data = decode(c);
                continuation = ((data & CONTINUATION_MASK) != 0);
                data &= DATA_MASK;
                result += (data << shift);
                shift += 5;
                index++;
            }
            boolean negative = (result%2) == 1;
            result >>= 1;
            return negative ? -result : result;
        }

        /**
         * Decodes the given character (from BASE64 encoding).
         * 
         * @param c character to decode.
         * @return number represented by the character.
         */
        private int decode(char c) {
            if (c >= 'A' && c <= 'Z') {
                return c - 65;
            } else if (c >= 'a' && c <= 'z') {
                return c - 71;
            } else if (c >= '0' && c <= '9') {
                return c + 4;
            } else {
                switch (c) {
                    case '+': return 62;
                    case '/': return 63;
                    case '=': return 0;
                    default: throw new IllegalArgumentException("Illegal character: " + c); // NOI18N
                }
            }
        }
    }

}
