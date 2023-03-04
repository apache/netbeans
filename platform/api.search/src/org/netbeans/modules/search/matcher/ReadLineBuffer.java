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
package org.netbeans.modules.search.matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores n last lines.
 *
 * @author jhavlin
 */
class ReadLineBuffer {

    private int currentIndex = 0;
    private int size;
    private final int capacity;
    private final Line[] lines;

    /**
     * Create buffer with defined capacity.
     */
    ReadLineBuffer(int capacity) {
        this.capacity = capacity;
        lines = new Line[capacity];
    }

    /**
     * Add a line to the buffer.
     */
    void addLine(int number, String text) {
        lines[currentIndex] = new Line(number, text);
        currentIndex = (currentIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    /**
     * Get buffered lines, in order of their addition to the buffer.
     */
    List<Line> getLines() {
        List<Line> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(lines[(currentIndex - size + i + capacity) % capacity]);
        }
        return l;
    }

    /**
     * Line read from a file.
     */
    static class Line {

        private final int number;
        private final String text;

        /**
         * Create a new line.
         *
         * @param number Line number. Starting from 1.
         * @param text Line text.
         */
        Line(int number, String text) {
            this.number = number;
            this.text = text;
        }

        /**
         * Get line number. First line has number 1, not 0.
         *
         * @return Line number, 1 or more.
         */
        int getNumber() {
            return number;
        }

        /**
         * Get line text.
         *
         * @return Line text, can be empty, not null.
         */
        String getText() {
            return text;
        }
    }
}
