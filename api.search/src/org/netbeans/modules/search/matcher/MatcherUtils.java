/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
