/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.editor.hints.HintsControllerImpl.CompoundLazyFixList;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;

/**
 *
 * @author Jan Lahoda
 */
public class FixData extends CompoundLazyFixList {

    public FixData(LazyFixList errorFixes, LazyFixList otherFixes) {
        super(Arrays.asList(errorFixes, otherFixes));
    }

    public List<Fix> getSortedFixes() {
        LazyFixList errorFixes = delegates.get(0);
        LazyFixList otherFixes = delegates.get(1);
        List<Fix> result = new LinkedList<Fix>();
        
        result.addAll(sortFixes(new LinkedHashSet<Fix>(errorFixes.getFixes())));
        result.addAll(sortFixes(new LinkedHashSet<Fix>(otherFixes.getFixes())));
        
        return result;
    }

    private List<Fix> sortFixes(Collection<Fix> fixes) {
        List<Fix> result = new ArrayList<Fix>(fixes);

        result.sort(new FixComparator());

        return result;
    }

    private static final String DEFAULT_SORT_TEXT = "\uFFFF";

    private static CharSequence getSortText(Fix f) {
        if (f instanceof EnhancedFix) {
            return ((EnhancedFix) f).getSortText();
        } else {
            return DEFAULT_SORT_TEXT;
        }
    }
    private static final class FixComparator implements Comparator<Fix> {
        public int compare(Fix o1, Fix o2) {
            return compareText(getSortText(o1), getSortText(o2));
        }
    }

    private static int compareText(CharSequence text1, CharSequence text2) {
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
