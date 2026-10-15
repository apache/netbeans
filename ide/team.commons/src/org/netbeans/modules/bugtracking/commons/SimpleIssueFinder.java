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

package org.netbeans.modules.bugtracking.commons;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.openide.ErrorManager;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class SimpleIssueFinder {
    
    private static SimpleIssueFinder instance;
    
    private static final int[] EMPTY_INT_ARR = new int[0];

    private SimpleIssueFinder() {}
    
    public static synchronized SimpleIssueFinder getInstance() {
        if(instance == null) {
            instance = new SimpleIssueFinder();
        }
        return instance;
    }

    public int[] getIssueSpans(CharSequence text) {
        int[] result = findBoundaries(text);
        return (result != null) ? result : EMPTY_INT_ARR;
    }

    public String getIssueId(String issueHyperlinkText) {
        int pos = issueHyperlinkText.length() - 1;
        while ((pos >= 0) && Impl.isDigit(issueHyperlinkText.charAt(pos))) {
            pos--;
        }
        return issueHyperlinkText.substring(pos + 1);
    }

    private static int[] findBoundaries(CharSequence str) {
        try {
            return getImpl().findBoundaries(str);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return null;
        }
    }

    private static Impl getImpl() {
        return new Impl();
    }

    static SimpleIssueFinder getTestInstance() {
        return new SimpleIssueFinder();
    }

    //--------------------------------------------------------------------------

    private static final class Impl {

        /*
         * This implementation is quite simple because of two preconditions:
         *
         * #1 - all defined bug-words ("bug", "issue") and all words of the
         *      bug number prefix ("duplicate of") consist of lowercase
         *      characters from the basic latin alphabet (a-z), no spaces
         * #2 - all words that make a defined bug number prefix ("duplicate of")
         *      are unique
         *
         * This implementation relies on these preconditions and will may not
         * work correctly if one or more of the preconditions are not met.
         *
         * 
         * Note that although all the bug words and the bug number prefix
         * must be defined as lowercase, the implementation ignores case
         * of characters that are passed to input of the finder, as long as
         * these are from the basic latin alphabet. All letters that do not
         * belong to the basic latin alphabet are considered as garbage,
         * no matter what is their case.
         */

        private static final String[] BUGWORDS = new String[] {"bug", "issue", // NOI18N
                                                               // some people prefer to refer to issues by their type
                                                               "defect", "feature", "task", "enhancement", "task-id", // NOI18N
                                                               // to typo is human
                                                               "isseu" }; // NOI18N
        private static final String BUG_NUMBER_PREFIX = "duplicate of"; //NOI18N
        private static final String[] BUGNUM_PREFIX_PARTS;

        private static final String PUNCT_CHARS = ".,:;()[]{}/*";         //NOI18N

        private static final int LOWER_A = 'a';     //automatic conversion to int
        private static final int LOWER_Z = 'z';     //automatic conversion to int

        private static final int INIT       = 0;
        private static final int CHARS      = 1;
        private static final int HASH       = 2;
        private static final int HASH_SPC   = 3;
        private static final int NUM        = 4;
        private static final int BUGWORD    = 5;
        private static final int BUGWORD_NL = 6;
        private static final int STAR       = 7;
        private static final int GARBAGE    = 8;

        private CharSequence str;
        private int pos;
        private int state;

        static {
            BUGNUM_PREFIX_PARTS = BUG_NUMBER_PREFIX.split(" ");         //NOI18N

            boolean asserts = false;
            assert asserts = true;
            if (asserts) {
                /*
                 * Checks that precondition #1 is met
                 * - all bugwords the bug number prefix are lowercase:
                 */
                for (int i = 0; i < BUGWORDS.length; i++) {
                    assert BUGWORDS[i].equalsIgnoreCase(BUGWORDS[i]);
                }
                for (int i = 0; i < BUGNUM_PREFIX_PARTS.length; i++) {
                    assert BUGNUM_PREFIX_PARTS[i].equalsIgnoreCase(BUGNUM_PREFIX_PARTS[i]);
                }

                /*
                 * Checks that precondition #2 is met
                 * - all elements of BUGNUM_PREFIX_PARTS are unique:
                 */
                Set<String> bugnumPrefixPartsSet = new HashSet<String>(7);
                bugnumPrefixPartsSet.addAll(Arrays.asList(BUGNUM_PREFIX_PARTS));
                assert bugnumPrefixPartsSet.size() == BUGNUM_PREFIX_PARTS.length;
            }
        }

        /**
         * how many parts of the bugnum prefix ({@code "duplicate of"})
         * have been already parsed
         */
        private int bugnumPrefixPartsProcessed;

        int startOfWord;
        int start;
        int end;
        int[] result;

        private Impl() { }

        private int[] findBoundaries(CharSequence str) {
            reset();

            this.str = str;

            for (pos = 0; pos < str.length(); pos++) {
                handleChar(str.charAt(pos));
            }
            if (state == NUM) {
                storeResult(start, pos);
            }
            return result;
        }

        private void reset() {
            str = null;
            pos = 0;
            state = INIT;

            bugnumPrefixPartsProcessed = 0;

            startOfWord = -1;
            start = -1;
            end = -1;

            result = null;
        }

        private void handleChar(int c) {
            int newState;
            boolean keepCountingBugwords = false;
            switch (state) {
                case INIT:
                    if (c == '#') {
                        rememberIsStart();
                        newState = HASH;
                    } else if (isLetter(c)) {
                        rememberIsStart();
                        newState = CHARS;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case CHARS:
                    if (isLetter(c) || c == '-') {
                        newState = CHARS;
                        keepCountingBugwords = true;
                    } else if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n') || (c == ':')) {
                        if ((bugnumPrefixPartsProcessed == 0) && isBugword()
                                || tryHandleBugnumPrefixPart()) {
                            newState = ((c == ' ') || (c == '\t')) ? BUGWORD
                                                                   : BUGWORD_NL;
                            keepCountingBugwords = true;
                        } else {
                            newState = getInitialState(c);
                        }
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH_SPC:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case NUM:
                    if (isDigit(c)) {
                        newState = NUM;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD:
                case BUGWORD_NL:
                    if ((state == BUGWORD_NL) && (c == '*')) {
                        keepCountingBugwords = true;
                        newState = STAR;
                    } else if ((c == ' ') || (c == '\t')) {
                        keepCountingBugwords = true;
                        newState = state;
                    } else if ((c == '\r') || (c == '\n')) {
                        keepCountingBugwords = true;
                        newState = BUGWORD_NL;
                    } else if (c == '#') {
                        newState = HASH;
                        if (isBugnumPrefix()) {
                            start = pos;        //exclude "duplicate of"
                        }
                    } else if (isDigit(c)) {
                        if (isPartialBugnumPrefix()) {
                            newState = getInitialState(c);
                        } else {
                            newState = NUM;
                            if (isFullBugnumPrefix()) {
                                start = pos;    //exclude "duplicate of"
                            }
                        }
                    } else if (isLetter(c)) {
                        newState = CHARS;
                        if (isPartialBugnumPrefix()) {
                            keepCountingBugwords = true;
                            startOfWord = pos;
                        } else {
                            /* relies on precondition #2 (see top of the class) */
                            rememberIsStart();
                        }
                    } else {
                        newState = getInitialState(c);
                    }

                    break;
                case STAR:
                    if ((c == ' ') || (c == '\t')) {
                        keepCountingBugwords = true;
                        newState = BUGWORD;
                    } else if ((c == '\r') || (c == '\n')) {
                        keepCountingBugwords = true;
                        newState = BUGWORD_NL;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case GARBAGE:
                    newState = getInitialState(c);
                    break;
                default:
                    assert false;
                    newState = getInitialState(c);
                    break;
            }
            if ((state == NUM) && (newState != NUM)) {
                if (isSpaceOrPunct(c)) {
                    storeResult(start, pos);
                }
            }
            if ((newState == INIT) || (newState == GARBAGE)) {
                start = -1;
            }
            if (!keepCountingBugwords) {
                bugnumPrefixPartsProcessed = 0;
            }
            state = newState;
        }

        private int getInitialState(int c) {
            return isSpaceOrPunct(c) ? INIT : GARBAGE;
        }

        private void rememberIsStart() {
            start = pos;
            startOfWord = pos;
        }

        private void storeResult(int start, int end) {
            assert (start != -1);
            if (result == null) {
                result = new int[] {start, end};
            } else {
                int[] newResult = new int[result.length + 2];
                System.arraycopy(result, 0, newResult, 0, result.length);
                newResult[result.length    ] = start;
                newResult[result.length + 1] = end;
                result = newResult;
            }
        }

        private static boolean isLetter(int c) {
            /* relies on precondition #1 (see the top of the class) */
            c |= 0x20;
            return ((c >= LOWER_A) && (c <= LOWER_Z));
        }

        private static boolean isDigit(int c) {
            return ((c >= '0') && (c <= '9'));
        }

        private static boolean isSpaceOrPunct(int c) {
            return (c == '\r') || (c == '\n')
                   || Character.isSpaceChar(c) || isPunct(c);
        }

        private static boolean isPunct(int c) {
            return PUNCT_CHARS.indexOf(c) != -1;
        }

        private boolean isBugword() {
            /* relies on precondition #1 (see the top of the class) */
            CharSequence word = str.subSequence(start, pos);
            for (int i = 0; i < BUGWORDS.length; i++) {
                if (equalsIgnoreCase(BUGWORDS[i], word)) {
                    return true;
                }
            }
            return false;
        }

        private boolean tryHandleBugnumPrefixPart() {
            CharSequence word = str.subSequence(startOfWord, pos);
            if ((bugnumPrefixPartsProcessed < BUGNUM_PREFIX_PARTS.length)
                    && equalsIgnoreCase(BUGNUM_PREFIX_PARTS[bugnumPrefixPartsProcessed], word)) {
                bugnumPrefixPartsProcessed++;
                return true;
            } else if ((bugnumPrefixPartsProcessed != 0)
                    && equalsIgnoreCase(BUGNUM_PREFIX_PARTS[0], word)) {
                /* handles strings such as "duplicate duplicate of" */
                bugnumPrefixPartsProcessed = 1;
                start = startOfWord;
                return true;
            } else {
                return false;
            }
        }

        private boolean isBugnumPrefix() {
            return (bugnumPrefixPartsProcessed != 0);
        }

        private boolean isPartialBugnumPrefix() {
            return (bugnumPrefixPartsProcessed > 0)
                   && (bugnumPrefixPartsProcessed < BUGNUM_PREFIX_PARTS.length);
        }

        private boolean isFullBugnumPrefix() {
            return bugnumPrefixPartsProcessed == BUGNUM_PREFIX_PARTS.length;
        }

    }

    private static boolean equalsIgnoreCase(CharSequence pattern, CharSequence str) {
        final int patternLength = pattern.length();

        if (str.length() != patternLength) {
            return false;
        }

        /* relies on precondition #1 (see the top of the class) */
        for (int i = 0; i < patternLength; i++) {
            if ((str.charAt(i) | 0x20) != pattern.charAt(i)) {
                return false;
            }
        }

        return true;
    }

}
