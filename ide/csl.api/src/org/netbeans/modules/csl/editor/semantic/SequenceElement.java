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

package org.netbeans.modules.csl.editor.semantic;

import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Each SequeneceElement represents a OffsetRange/Coloring/Language tuple that
 * is managed for semantic highlighting purposes. They are comparable since they
 * are maintained in a TreeSet (sorted by the OffsetRanges). This sorted treeset
 * is used to manage the various subsequences etc. needed by the highlight sequences.
 * There is a special subclass of ElementSequence, ComparisonItem, which is used
 * as comparison bounds (keys) passed into the TreeSet when generating subsequences.
 *
 * @author Tor Norbye
 */
class SequenceElement implements Comparable<SequenceElement> {
    public final Language language;
    public OffsetRange range;
    public final Coloring coloring;
    
    private SequenceElement() {
        this(null, null, null);
    }

    public SequenceElement(Language language, OffsetRange range, Coloring coloring) {
        this.language = language;
        this.range = range;
        this.coloring = coloring;
    }
    
    public int compareTo(SequenceElement o) {
        if (o instanceof ComparisonItem) {
            return -1 * ((ComparisonItem) o).compareTo(this);
        } else {
            assert o.range != null;
            return range.compareTo(o.range);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SequenceElement)) {
            return false;
        }
        SequenceElement other = (SequenceElement)obj;

        return range.equals(other.range);
    }

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    @Override
    public String toString() {
        return "SequenceElement(range=" + range + ", lang=" + language + ", color=" + coloring + ")"; //NOI18N
    }
    
    // This class is used only for key comparison when creating subsets
    static class ComparisonItem extends SequenceElement {
        private int offset;
        
        ComparisonItem(int offset) {
            this.offset = offset;
        }

        @Override
        public int compareTo(SequenceElement o) {
            if (o instanceof ComparisonItem) {
                return offset - ((ComparisonItem)o).offset;
            } else {
                if (offset < o.range.getStart()) {
                    return -1;
                } else if (offset >= o.range.getEnd()) { // forward biased
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
