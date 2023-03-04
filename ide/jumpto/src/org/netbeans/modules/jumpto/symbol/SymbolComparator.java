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

package org.netbeans.modules.jumpto.symbol;

import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.common.DescriptorAccessor;
import org.netbeans.modules.jumpto.common.StateFullComparator;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.util.ChangeSupport;

/**
 * The {@code SymbolComparator} establishes the sort order of the symbols.
 * It is used for ordering a list that will be displayed in:
 * <ul>
 *   <li>the field "Symbols Found" of the dialog "Go to Symbol"
 *      (Ctrl+Alt+Shift+O)</li>
 * </ul>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public abstract class SymbolComparator extends EntityComparator<SymbolDescriptor> implements StateFullComparator<SymbolDescriptor> {

    private final ChangeSupport support;
    protected final boolean caseSensitive;
    protected final boolean preferOpPrjs;

    private SymbolComparator(
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

    void fireChange() {
        support.fireChange();
    }

    abstract void setText(@NonNull final String text);

    @Override
    public abstract int compare(SymbolDescriptor e1, SymbolDescriptor e2);


    private static final class Alphabet extends SymbolComparator {

        Alphabet(
                final boolean caseSensitive,
                final boolean preferOpPrjs) {
            super(caseSensitive, preferOpPrjs);
        }
        /**
         * Compares its two {@code SymbolDescriptor}s for order.
         * <p>
         * This method establishes the following groups for order
         * (from lowest to highest):
         * <ul>
         * <li>Symbols being defined in the main project (if any)</li>
         * <li>Symbols being defined in the projects that are opened in the
         *     IDE's GUI</li>
         * <li>Symbols being defined in other accessible projects.</li>
         * </ul>
         * </p>
         * The alphabetical order of the symbol names is established inside each
         * group.<br/>
         * If the symbols names are the same then the alphabetical order of
         * the owner names of the symbols is used.<br/>
         *
         * @param e1 the first {@code SymbolDescriptor} to be compared.
         * @param e2 the second {@code SymbolDescriptor} to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * 	   first argument is less than, equal to, or greater than the
         *	   second.
         */
        @Override
        public int compare(SymbolDescriptor e1, SymbolDescriptor e2) {
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
            result = compare(getSortName(e1), getSortName(e2), caseSensitive);
            if ( result != 0 ) {
               return result;
            }
            // here: e1Name equals to e2Name
            return compare(e1.getOwnerName(), e2.getOwnerName(), caseSensitive);
        }

        @Override
        void setText(String text) {
        }
    }

    private static final class Levenshtein extends SymbolComparator {

        private static final String ATTR_PATTERN = "Pattern";       //NOI18N
        private static final String ATTR_LS_DIST = "LevenshteinDistance";   //NOI18N
        private static final String ATTR_LS_TAIL = "LevenshteinTail";   //NOI18N


        private String text;

        Levenshtein(
                @NonNull final String text,
                final boolean caseSensitive,
                final boolean preferOpPrjs) {
            super(caseSensitive, preferOpPrjs);
            this.text = text;
        }

        @Override
        public int compare(SymbolDescriptor e1, SymbolDescriptor e2) {
            int result;
            if (preferOpPrjs) {
                String e1projectName = e1.getProjectName();
                String e2projectName = e2.getProjectName();
                result = compareProjects(e1projectName, e2projectName);
                if(result != 0) {
                    return result; // e1projectName NOT equals to e2projectName
                }
            }
            final String name1 = getSortName(e1);
            final String name2 = getSortName(e2);

            int l1, l2, t1, t2;
            Object d = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_LS_DIST);
            Object t = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_LS_TAIL);
            Object p = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_PATTERN);
            if (d instanceof Integer && t instanceof Integer && text.equals(p)) {
                l1 = (Integer) d;
                t1 = (Integer) t;
            } else {
                final String prefix = levenshteinPrefix(name1, text, caseSensitive);
                l1 = levenshteinDistance(prefix, text, caseSensitive);
                t1 = name1.length() - prefix.length();
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_LS_DIST, l1);
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_LS_TAIL, t1);
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_PATTERN, text);
            }
            d = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_LS_DIST);
            t = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_LS_TAIL);
            p = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_PATTERN);
            if (d instanceof Integer && t instanceof Integer && text.equals(p)) {
                l2 = (Integer) d;
                t2 = (Integer) t;
            } else {
                final String prefix = levenshteinPrefix(name2, text, caseSensitive);
                l2 = levenshteinDistance(prefix, text, caseSensitive);
                t2 = name2.length() - prefix.length();
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
            result = compare(name1, name2, caseSensitive);
            if ( result != 0 ) {
               return result;
            }
            // here: e1Name equals to e2Name
            return compare(e1.getOwnerName(), e2.getOwnerName(), caseSensitive);
        }

        @Override
        void setText(String text) {
            final boolean fire = !Objects.equals(this.text, text);
            this.text = text;
            if (fire) {
                fireChange();
            }
        }
    }

    @NonNull
    public static SymbolComparator create(
            @NonNull final GoToSettings.SortingType kind,
            @NonNull final String text,
            final boolean caseSensitive,
            final boolean preferOpPrjs) {
        switch (kind) {
            case LEXICOGRAPHIC:
                return new Alphabet(caseSensitive, preferOpPrjs);
            case LEVENSHTEIN:
                return new Levenshtein(text, caseSensitive, preferOpPrjs);
            default:
                throw new IllegalArgumentException(String.valueOf(kind));
        }
    }

    @NonNull
    private static String getSortName(@NonNull final SymbolDescriptor d) {
        String res = d.getSimpleName();
        if (res == null) {
            res = d.getSymbolName();
        }
        return res;
    }
}
