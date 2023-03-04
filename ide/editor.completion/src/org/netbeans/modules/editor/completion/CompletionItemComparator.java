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

package org.netbeans.modules.editor.completion;

import java.util.Comparator;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 * Comparator for completion items either by sort priority or by sort text.
 *
 * @author Dusan Balek, Miloslav Metelka
 */

public class CompletionItemComparator implements Comparator<CompletionItem> {

    public static final Comparator<CompletionItem> BY_PRIORITY = new CompletionItemComparator(true);

    public static final Comparator<CompletionItem> ALPHABETICAL = new CompletionItemComparator(false);
    
    private final boolean byPriority;
    
    private CompletionItemComparator(boolean byPriority) {
        this.byPriority = byPriority;
    }
    
    public static final Comparator<CompletionItem> get(int sortType) {
        if (sortType == CompletionResultSet.PRIORITY_SORT_TYPE)
            return BY_PRIORITY;
        if (sortType == CompletionResultSet.TEXT_SORT_TYPE)
            return ALPHABETICAL;
        throw new IllegalArgumentException();
    }
    
    public int compare(CompletionItem i1, CompletionItem i2) {
        if (i1 == i2)
            return 0;
        if (byPriority) {
            int importanceDiff = compareIntegers(i1.getSortPriority(), i2.getSortPriority());
            if (importanceDiff != 0)
                return importanceDiff;
            int alphabeticalDiff = compareText(i1.getSortText(), i2.getSortText());
            return alphabeticalDiff;
        } else {
            int alphabeticalDiff = compareText(i1.getSortText(), i2.getSortText());
            if (alphabeticalDiff != 0)
                return alphabeticalDiff;
            int importanceDiff = compareIntegers(i1.getSortPriority(), i2.getSortPriority());
            return importanceDiff;
        }
    }
    
    private static int compareIntegers(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
    
    private static int compareText(CharSequence text1, CharSequence text2) {
        if (text1 == null)
            text1 = ""; //NOI18N
        if (text2 == null)
            text2 = ""; //NOI18N
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = text1.charAt(i);
            char ch2 = text2.charAt(i);
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }

}
