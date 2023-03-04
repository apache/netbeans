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

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.modules.search.TextDetail;
import org.openide.loaders.DataObject;

/**
 * Various utility method for file matchers.
 *
 * @author jhavlin
 */
final class MatcherUtils {

    private static final Pattern patternCR = Pattern.compile("\r");     //NOI18N

    /**
     * Decide whether passed search pattern is trivial - i.e. no matching is
     * needed at all.
     */
    static boolean isTrivialPattern(SearchPattern sp) {
        return sp == null || sp.getSearchExpression() == null
                || sp.getSearchExpression().isEmpty();
    }

    /**
     * Create new text detail object for current match in the matcher.
     *
     * @param multiline True for multi-line mode (single shared matcher for all
     * lines), false for single-line mode (each line has dedicated matcher).
     * @param offset If multi-line, the value represents column in the current
     * line where the match starts. If single-line, it represent offset of the
     * matcher (= position of start of current line) in the whole file.
     */
    static TextDetail createTextDetail(boolean multiline, Matcher matcher,
            DataObject dataObject, int lineNumber, String lineText,
            int offset, SearchPattern searchPattern) {

        String group = matcher.group();
        int start = matcher.start();
        int end = matcher.end();
        int countCR = countCR(group);
        int markLength = end - start - countCR;
        int matcherOffset;
        int column;
        if (multiline) {
            matcherOffset = 0;
            column = offset;
        } else /* single-line mode */ {
            matcherOffset = offset;
            column = start + 1;
        }
        assert dataObject != null;
        TextDetail det = new TextDetail(dataObject, searchPattern);
        det.setMatchedText(group);
        det.setStartOffset(start + matcherOffset);
        det.setEndOffset(end + matcherOffset);
        det.setMarkLength(markLength);
        det.setLine(lineNumber);
        det.setColumn(column);
        det.setLineText(lineText);
        return det;
    }

    /**
     * Counts up a number of CRs in the specified string.
     *
     * @param s the string.
     * @return a number of CRs.
     */
    private static int countCR(String s) {
        Matcher matcherCR = patternCR.matcher(s);
        int countCR = 0;
        while (matcherCR.find()) {
            countCR++;
        }
        return countCR;
    }

    /**
     * Unmap mapped buffer.
     */
    public static void unmap(MappedByteBuffer buffer) {
        try {
            Method getCleanerMethod = buffer.getClass().getMethod(
                    "cleaner");                                         //NOI18N
            getCleanerMethod.setAccessible(true);
            // sun.misc.Cleaner
            Object cleaner = getCleanerMethod.invoke(buffer);
            cleaner.getClass().getMethod("clean").invoke(cleaner);
        } catch (Exception e) {
        }
    }
}
