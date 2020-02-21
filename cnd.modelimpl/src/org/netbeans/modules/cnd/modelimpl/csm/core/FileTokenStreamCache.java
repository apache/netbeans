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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.antlr.TokenStream;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
/*package*/ final class FileTokenStreamCache {
    private final List<TSData> cacheData;
    public FileTokenStreamCache() {
        this.cacheData = new CopyOnWriteArrayList<>();
    }

    public final TokenStream getTokenStreamInActiveBlock(boolean filtered, int start, int end, int/*CPPTokenTypes*/ firstTokenIDIfExpandMacros) {
        if (cacheData.size() > 1) {
            int bestCoverage = -1;
            TSData bestData = null;
            for (TSData pair : cacheData) {
                if (pair.pcState != null) {
                    int coverage = pair.pcState.getActiveCoverage(start, end);
                    if (coverage > bestCoverage) {
                        bestCoverage = coverage;
                        bestData = pair;
                        if (coverage == (end - start)) {
                            // max coverage is found
                            break;
                        }
                    }
                }
            }
            if (bestData != null) {
                return bestData.getTS(filtered, start, end, firstTokenIDIfExpandMacros);
            }
        } else if (!cacheData.isEmpty()) {
            TSData data = cacheData.get(0);
            if (data.pcState != null && data.pcState.isInActiveBlock(start, end)) {
                return data.getTS(filtered, start, end, firstTokenIDIfExpandMacros);
            }
        }
        return null;
    }

    public final void cacheTokens(FilePreprocessorConditionState pcState, List<APTToken> tokens, APTLanguageFilter lang) {
        TSData newData = new TSData(pcState, tokens, lang);
        cacheData.add(newData);
    }

    public final TokenStream cacheTokensAndReturnFiltered(FilePreprocessorConditionState pcState, List<APTToken> tokens, APTLanguageFilter lang) {
        TSData newData = new TSData(pcState, tokens, lang);
        cacheData.add(newData);
        return newData.getTS(true, 0, Integer.MAX_VALUE, 0);
    }

    private static final class TSData {
        private final FilePreprocessorConditionState pcState;
        private final List<APTToken> tokens;
        private final APTLanguageFilter lang;
        private final TreeMap<Integer, Integer> knownIndices = new TreeMap<>();
        private TSData(FilePreprocessorConditionState pcState, List<APTToken> tokens, APTLanguageFilter lang) {
            this.pcState = pcState;
            this.tokens = tokens;
            this.lang = lang;
            rememberIndex(0, 0);
        }

        private TokenStream getTS(boolean filtered, int start, int endOffset, int/*CPPTokenTypes*/ firstTokenIDIfExpandMacros) {
            int iteratorIndex = checkKnownIndex(start);
            // create wrapper
            ListIterator<APTToken> iterator = tokens.listIterator(iteratorIndex);
            while (iterator.hasNext()) {
                APTToken next = iterator.next();
                int currOffset = next.getOffset();
                if (currOffset >= start) {
                    if ((firstTokenIDIfExpandMacros == 0) || (next.getType() == firstTokenIDIfExpandMacros) || !APTUtils.isMacroExpandedToken(next)) {
                        iterator.previous();
                        break;
                    }
                }
            }
            TokenStream ts = new IteratorBasedTS(tokens, iterator.nextIndex(), endOffset, this);
            rememberIndex(start, iterator.nextIndex());
            return filtered ? lang.getFilteredStream(new APTCommentsFilter(ts)) : ts;
        }

        private synchronized void rememberIndex(int startOffset, int listIndex) {
            knownIndices.put(Integer.valueOf(startOffset), Integer.valueOf(listIndex));
        }

        private synchronized int checkKnownIndex(int startOffset) {
            SortedMap<Integer, Integer> tailMap = knownIndices.tailMap(startOffset);
            Integer knownOffset;
            if (tailMap.isEmpty() || (tailMap.firstKey() > startOffset)) {
                knownOffset = knownIndices.headMap(startOffset).lastKey();
            } else {
                knownOffset = tailMap.firstKey();
            }
            return knownIndices.get(knownOffset);
        }
    }

    private final static class IteratorBasedTS implements TokenStream, APTTokenStream {
        private final ListIterator<APTToken> position;
        private final List<APTToken> debugTokens; // for debug only
        private final int debugStartIndex; // for debug only
        private final int endOffset;
        private final TSData callback;
        /** Creates a new instance of ListBasedTokenStream */
        public IteratorBasedTS(List<APTToken> tokens, int startIndex, int endOffset, TSData callback) {
            this.position = tokens.listIterator(startIndex);
            this.debugTokens = tokens;
            this.debugStartIndex = startIndex;
            this.endOffset = endOffset;
            this.callback = callback;
        }

        @Override
        public APTToken nextToken() {
            if (position.hasNext()) {
                APTToken out = position.next();
                assert (out != null);
                assert (!APTUtils.isEOF(out));
                int offset = out.getOffset();
                if (offset > endOffset) {
                    out = APTUtils.EOF_TOKEN;
                    if (callback != null) {
                        callback.rememberIndex(offset, position.previousIndex());
                    }
                }
                return out;
            } else {
                return APTUtils.EOF_TOKEN;
            }
        }

        @Override
        public String toString() {
            return APTUtils.debugString(new IteratorBasedTS(debugTokens, debugStartIndex, endOffset, null)).toString();
        }
    }
}
