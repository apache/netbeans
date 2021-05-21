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
package org.netbeans.modules.maven.indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Given multiple strings, this {@link Predicate} tests positive, if one or
 * multiple of the strings are prefixes of the string, that is tested.
 *
 * @author Tim Boudreau
 */
final class MatchWords implements Predicate<String> {

    private final List<MatchState> matchers = new ArrayList<>();
    private ThreadLocal<MatchState[]> local = new ThreadLocal<>();

    MatchWords(String[] strings) {
        for (String s : strings) {
            matchers.add(new MatchState(s));
        }
    }

    private MatchState[] matchers() {
        MatchState[] result = local.get();
        if (result == null) {
            result = new MatchState[matchers.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = matchers.get(i).copy();
            }
            local.set(result);
        }
        return result;
    }

    @Override
    public boolean test(String t) {
        int max = t.length();
        MatchState[] mtchrs = matchers();
        for (MatchState mtchr : mtchrs) {
            mtchr.reset();
        }
        for (int i = 0; i < max; i++) {
            char c = t.charAt(i);
            for (int j = 0; j < mtchrs.length; j++) {
                mtchrs[j].check(c);
                if (mtchrs[j].isMatched()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class MatchState {

        private final char[] what;
        private int matched = 0;
        private boolean failed = false;

        MatchState(String what) {
            this.what = what.toCharArray();
        }

        MatchState(char[] what) {
            this.what = what;
        }

        public MatchState copy() {
            return new MatchState(what);
        }

        private void reset() {
            matched = 0;
            failed = false;
        }

        boolean isMatched() {
            return matched >= what.length;
        }

        void check(char c) {
            if (failed || isMatched()) {
                return;
            }
            if (what[matched] == c) {
                matched++;
            } else {
                failed = true;
            }
        }
    }
}
