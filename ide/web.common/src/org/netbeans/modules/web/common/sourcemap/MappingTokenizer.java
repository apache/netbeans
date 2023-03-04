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
