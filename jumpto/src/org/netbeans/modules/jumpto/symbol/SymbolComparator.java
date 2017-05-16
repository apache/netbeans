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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
