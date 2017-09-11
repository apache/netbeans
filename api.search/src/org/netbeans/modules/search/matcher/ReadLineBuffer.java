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
        List<Line> l = new ArrayList<Line>(size);
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
