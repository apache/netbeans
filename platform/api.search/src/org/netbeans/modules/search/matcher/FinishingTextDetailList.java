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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.search.TextDetail;

/**
 * Data structure that contains text details that are waiting for their
 * surrounding lines..
 *
 * @author jhavlin
 */
class FinishingTextDetailList {

    private final int linesAfterMatch;
    List<TextDetail> waitList = new LinkedList<>();

    FinishingTextDetailList(int linesAfterMatch) {
        this.linesAfterMatch = linesAfterMatch;
    }
    Map<Integer, List<TextDetail>> detailMap =
            new HashMap<>();

    /**
     * Add a text detail that may need to add surrounding lines.
     */
    void addTextDetail(TextDetail textDetail) {
        waitList.add(textDetail);
    }

    /**
     * A new line was read from the file. Update text details, add surrounding
     * lines to them, and remove those text details that have all required
     * surrounding lines.
     */
    void nextLineRead(int number, String line) {
        Iterator<TextDetail> iterator = waitList.iterator();
        while (iterator.hasNext()) {
            TextDetail next = iterator.next();
            if (number > next.getLine()) {
                next.addSurroundingLine(number, line);
            }
            if (next.getLine() <= number - linesAfterMatch) {
                iterator.remove();
            }
        }
    }
}