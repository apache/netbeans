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
package org.openide.filesystems;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * @author Ales Novak
 */
final class PathElements {
    private static final String DELIMITER = "/"; // NOI18N

    /** Original name */
    private final String name;

    /** tokenizer */
    private StringTokenizer tokenizer;

    /** tokens */
    private final List<String> tokens;

    /** Creates new PathElements */
    public PathElements(String name) {
        this.name = name;
        tokenizer = new StringTokenizer(name, DELIMITER);
        tokens = new ArrayList<String>(10);
    }

    /**
     * @return original name
     */
    public String getOriginalName() {
        return name;
    }

    public Enumeration<String> getEnumeration() {
        return new EnumerationImpl(this);
    }

    synchronized boolean contains(int i) {
        if (tokens.size() <= i) {
            scanUpTo(i);
        }

        return (tokens.size() > i);
    }

    synchronized String get(int i) throws NoSuchElementException {
        if (tokens.size() <= i) {
            scanUpTo(i);
        }

        if (tokens.size() <= i) {
            throw new NoSuchElementException();
        }

        return tokens.get(i);
    }

    private synchronized void scanUpTo(int i) {
        if (tokenizer == null) {
            return;
        }

        if (tokens.size() > i) {
            return;
        }

        for (int k = tokens.size() - 1; (k < i) && tokenizer.hasMoreTokens(); k++) {
            tokens.add(tokenizer.nextToken());
        }

        if (!tokenizer.hasMoreTokens()) {
            tokenizer = null;
        }
    }

    /** Impl of enumeration */
    static final class EnumerationImpl implements Enumeration<String> {
        private PathElements elements;
        private int pos;

        EnumerationImpl(PathElements elements) {
            this.elements = elements;
            this.pos = 0;
        }

        /** From Enumeration */
        @Override
        public boolean hasMoreElements() {
            return elements.contains(pos);
        }

        /** From Enumeration */
        @Override
        public String nextElement() throws NoSuchElementException {
            return elements.get(pos++);
        }
    }
}
