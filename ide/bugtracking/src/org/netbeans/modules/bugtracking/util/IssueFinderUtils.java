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

package org.netbeans.modules.bugtracking.util;

import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.IssueFinder;

/**
 *
 * @author Marian Petras
 */
public class IssueFinderUtils {

    private IssueFinderUtils() {}
        
    public static Collection<IssueFinder> getIssueFinders() {
        List<IssueFinder> ret = new LinkedList<IssueFinder>();
        if(RepositoryRegistry.getInstance().isInitializing()) {
            return Collections.EMPTY_LIST;
        }
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getKnownRepositories(false);
        for (RepositoryImpl r : repos) {
            IssueFinder issueFinder = r.getIssueFinder();
            if(issueFinder != null) {
                ret.add(issueFinder);
            }
        }
        return ret;
    }

    public static int[] getIssueSpans(String text) {
        Collection<IssueFinder> issueFinders = getIssueFinders();

        if (issueFinders.isEmpty()) {
            return new int[0];
        }

        int[] result = null;

        int issueFindersWithResults = 0;
        for (IssueFinder issueFinder : issueFinders) {
            int[] subresult = issueFinder.getIssueSpans(text);
            boolean resultsValid = checkIssueSpans(subresult,
                                                   text.length(),
                                                   issueFinder);
            if (!resultsValid || (subresult.length == 0)) {
                continue;
            }

            if (++issueFindersWithResults == 1) {
                result = subresult;
            }
//            else {
//                assert result != null;   
//                result = append(result, subresult);
//            }
            if(result != null && result.length >= 2) {
                // TODO first catch wins. we could be smarter here and try to 
                // evaluate file ownership - see RepositoryQuery
                break;
            }
        }
        //PENDING: check for overlaping intervals if issueFindersWithResults > 1
        return (result != null) ? result : new int[0];
    }

    public static List<HyperlinkSpanInfo> getIssueSpansExt(String text) {
        Collection<IssueFinder> issueFinders = getIssueFinders();

        if (issueFinders.isEmpty()) {
            return Collections.emptyList();
        }

        List<HyperlinkSpanInfo> result = new ArrayList<HyperlinkSpanInfo>(4);

        int issueFindersWithResults = 0;
        for (IssueFinder issueFinder : issueFinders) {
            int[] subresult = issueFinder.getIssueSpans(text);
            boolean resultsValid = checkIssueSpans(subresult,
                                                   text.length(),
                                                   issueFinder);
            if (!resultsValid || (subresult.length == 0)) {
                continue;
            }

            issueFindersWithResults++;

            for (int i = 0; i < subresult.length; i += 2) {
                result.add(new HyperlinkSpanInfo(issueFinder, subresult[i],
                                                              subresult[i + 1]));
            }
        }
        //PENDING: check for overlaping intervals if issueFindersWithResults > 1
        return result;

    }

    public static String getIssueId(String issueHyperlinkText) {        
        IssueFinder issueFinder = determineIssueFinder(issueHyperlinkText, 0, issueHyperlinkText.length());
        if (issueFinder == null) {
            return null;
        }
        return issueFinder.getIssueId(issueHyperlinkText);
    }  
    
    public static IssueFinder determineIssueFinder(String text, int startOffset,
                                                                int endOffset) {
        Collection<IssueFinder> issueFinders = getIssueFinders();

        for (IssueFinder issueFinder : issueFinders) {
            int[] spans = issueFinder.getIssueSpans(text);
            if (checkSpansContainsPair(spans, startOffset, endOffset)) {
                return issueFinder;
            }
        }
        return null;
    }

    /**
     * Checks whether the given set of spans contains a given span.
     * @param  spans  set of span boundaries to be checked
     * @param  startOffset  start offset of the given span
     * @param  endOffset  end offset of the given span
     * @return   {@code true} if the given span was found,
     *           {@code false} otherwise
     */
    private static boolean checkSpansContainsPair(int[] spans, int startOffset,
                                                               int endOffset) {
        for (int i = 0; i < spans.length; i += 2) {
            if ((spans[i] == startOffset) && (spans[i + 1] == endOffset)) {
                return true;
            }
        }
        return false;
    }

    private static int[] append(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];

        System.arraycopy(arr1, 0, result,           0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);

        return result;
    }

    private static boolean checkIssueSpans(int[] spans,
                                           int textLength,
                                           IssueFinder issueFinder) {
        if (spans == null) {
            BugtrackingManager.LOG.log(
                    Level.WARNING, "Issue finder {0} returned <null> from getIssueSpans(...).", issueFinder.getClass().getName());     //NOI18N
            return false;
        }
        if ((spans.length % 2) != 0) {
            BugtrackingManager.LOG.log(
                    Level.WARNING,"{0}"                                     //NOI18N
                    + "Issue finder "
                    + " returned array containing odd number of "       //NOI18N
                    + " elements from method getIssueSpans().", issueFinder.getClass().getName());        //NOI18N
            return false;
        }
        for (int index = 0; index < spans.length; ) {
            int low = spans[index++];
            int high = spans[index++];
            if ((low < 0) || (high < 0)) {
                BugtrackingManager.LOG.log(
                        Level.WARNING,"Issue finder {0}"
                        + " returned invalid data from method"          //NOI18N
                        + " getIssueSpans() (negative index).", issueFinder.getClass().getName());        //NOI18N
                return false;
            }
            if (low >= high) {
                BugtrackingManager.LOG.log(
                        Level.WARNING,"Issue finder {0}"
                        + " returned invalid data from method"          //NOI18N
                        + " getIssueSpans() (start >= end).", issueFinder.getClass().getName());          //NOI18N
                return false;
            }
            if (high > textLength) {
                BugtrackingManager.LOG.log(
                        Level.WARNING,"Issue finder {0}"
                        + " returned invalid data from method"          //NOI18N
                        + " getIssueSpans() (index > text length).", issueFinder.getClass().getName());   //NOI18N
                return false;
            }
            //PENDING - check for overlaping intervals
        }
        return true;
    }

    public static class HyperlinkSpanInfo {
        public final IssueFinder issueFinder;
        public final int startOffset;
        public final int endOffset;
        public HyperlinkSpanInfo(IssueFinder finder, int start, int end) {
            this.issueFinder = finder;
            this.startOffset = start;
            this.endOffset = end;
        }
    }

}
