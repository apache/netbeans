/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
