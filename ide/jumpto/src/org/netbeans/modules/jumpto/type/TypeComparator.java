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

package org.netbeans.modules.jumpto.type;

import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.common.DescriptorAccessor;
import org.netbeans.modules.jumpto.common.StateFullComparator;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.util.ChangeSupport;

/**
 * The {@code TypeComparator} establishes the sort order of the types.
 * It is used for ordering a list that will be displayed in:
 * <ul>
 *   <li>the field "Types Found" of the dialog "Go to Type" (Ctrl+O)</li>
 *   <li>the results of the quick Search (Ctrl+I) in category "Go To Type"</li>
 * </ul>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public abstract class TypeComparator extends EntityComparator<TypeDescriptor> implements StateFullComparator<TypeDescriptor> {

    private final ChangeSupport support;
    protected final boolean caseSensitive;
    protected final boolean preferOpPrjs;

    private TypeComparator(
            final boolean caseSensitive,
            final boolean preferOpPrjs) {
        this.caseSensitive = caseSensitive;
        this.preferOpPrjs = preferOpPrjs;
        this.support = new ChangeSupport(this);
    }

    @Override
    public void addChangeListener(@NonNull final ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(@NonNull final ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    @Override
    public abstract int compare(TypeDescriptor e1, TypeDescriptor e2);

    abstract void setText(@NonNull final String text);

    protected void fireChange() {
        support.fireChange();
    }

    private static final class Levenshtein extends TypeComparator {

        private static final String ATTR_PATTERN = "Pattern";       //NOI18N
        private static final String ATTR_LS_DIST = "LevenshteinDistance";   //NOI18N
        private static final String ATTR_LS_TAIL = "LevenshteinTail";   //NOI18N

        protected String text;

        Levenshtein(
                @NonNull final String text,
                final boolean caseSensitive,
                final boolean preferOpPrjs) {
            super(caseSensitive, preferOpPrjs);
            this.text = text;
        }

        @Override
        void setText(@NonNull final String text) {
            final boolean fire = !Objects.equals(this.text, text);
            this.text = text;
            if (fire) {
                fireChange();
            }
        }

        @Override
        public int compare(TypeDescriptor e1, TypeDescriptor e2) {
            int result;
            if (preferOpPrjs) {
                String e1projectName = e1.getProjectName();
                String e2projectName = e2.getProjectName();
                result = compareProjects(e1projectName, e2projectName);
                if(result != 0) {
                    return result; // e1projectName NOT equals to e2projectName
                }
            }
            int l1, l2, t1, t2;
            Object o = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_LS_DIST);
            Object t = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_LS_TAIL);
            Object p = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_PATTERN);
            if (o instanceof Integer && t instanceof Integer && text.equals(p)) {
                l1 = (Integer) o;
                t1 = (Integer) t;
            } else {
                final String tn = e1.getSimpleName();
                final String prefix = levenshteinPrefix(tn,text, caseSensitive);
                l1 = levenshteinDistance(prefix, text, caseSensitive);
                t1 = tn.length() - prefix.length();
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_LS_DIST, l1);
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_LS_TAIL, t1);
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_PATTERN, text);
            }
            o = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_LS_DIST);
            t = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_LS_TAIL);
            p = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_PATTERN);
            if (o instanceof Integer && t instanceof Integer && text.equals(p)) {
                l2 = (Integer) o;
                t2 = (Integer) t;
            } else {
                final String tn = e2.getSimpleName();
                final String prefix = levenshteinPrefix(tn, text, caseSensitive);
                l2 = levenshteinDistance(prefix, text, caseSensitive);
                t2 = tn.length() - prefix.length();
                DescriptorAccessor.getInstance().setAttribute(e2, ATTR_LS_DIST, l2);
                DescriptorAccessor.getInstance().setAttribute(e2, ATTR_LS_TAIL, t2);
                DescriptorAccessor.getInstance().setAttribute(e2, ATTR_PATTERN, text);
            }

            result = l1 - l2;
            if (result != 0) {
                return result;
            }
            result = t1 - t2;
            if (result != 0) {
                return result;
            }

            // here: e1projectName equals to e2projectName
            result = compare(e1.getTypeName(), e2.getTypeName(), caseSensitive);
            if ( result != 0 ) {
               return result;
            }
            // here: e1Name equals to e2Name
            result = compare(e1.getOuterName(), e2.getOuterName());
            if ( result != 0 ) {
               return result;
            }
            // here: e1OuterName equals to e2OuterName
            return compare(e1.getContextName(), e2.getContextName());
        }
    }

    private static final class Alphabet extends TypeComparator {
        Alphabet(
                final boolean caseSensitive,
                final boolean preferOpPrjs) {
            super(caseSensitive, preferOpPrjs);
        }

        @Override
        void setText(@NonNull final String text) {
        }

        /**
         * Compares its two {@code TypeDescriptor}s for order.
         * <p>
         * This method establishes the following groups for order
         * (from lowest to highest):
         * <ul>
         * <li>Types being defined in the main project (if any)</li>
         * <li>Types being defined in the projects that are opened in the
         *     IDE's GUI</li>
         * <li>Types being defined in other accessible projects.</li>
         * </ul>
         * </p>
         * The alphabetical order of the type names is established inside each
         * group.<br/>
         * If the type names are the same then the alphabetical order of
         * the outer names of the types is used.<br/>
         * If the outer names are the same then alphabetical order of
         * the context names of the types is used.
         *
         * @param e1 the first {@code TypeDescriptor} to be compared.
         * @param e2 the second {@code TypeDescriptor} to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * 	   first argument is less than, equal to, or greater than the
         *	   second.
         */
        @Override
        public int compare(TypeDescriptor e1, TypeDescriptor e2) {
            int result;
            if (preferOpPrjs) {
                String e1projectName = e1.getProjectName();
                String e2projectName = e2.getProjectName();
                result = compareProjects(e1projectName, e2projectName);
                if(result != 0) {
                    return result; // e1projectName NOT equals to e2projectName
                }
            }
            // here: e1projectName equals to e2projectName
            result = compare(e1.getTypeName(), e2.getTypeName(), caseSensitive);
            if ( result != 0 ) {
               return result;
            }
            // here: e1Name equals to e2Name
            result = compare(e1.getOuterName(), e2.getOuterName());
            if ( result != 0 ) {
               return result;
            }
            // here: e1OuterName equals to e2OuterName
            return compare(e1.getContextName(), e2.getContextName());
        }
    }

    @NonNull
    public static TypeComparator create(
            @NonNull final GoToSettings.SortingType kind,
            @NonNull final String text,
            final boolean caseSensitive,
            final boolean preferOpPrjs) {
        return switch (kind) {
            case LEVENSHTEIN -> new Levenshtein(text, caseSensitive, preferOpPrjs);
            case LEXICOGRAPHIC -> new Alphabet(caseSensitive, preferOpPrjs);
        };
    }
}
