/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        if (obj == null || !(obj instanceof SequenceElement)) {
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
