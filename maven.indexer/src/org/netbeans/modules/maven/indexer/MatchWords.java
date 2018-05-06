/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.maven.indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fast test for matching any of a set of prefixes, iterating the characters of
 * the passed string only once. Borrowed from
 * https://github.com/timboudreau/bunyan-java/blob/master/bunyan-parse/src/main/java/com/mastfrog/bunyan/parse/MatchWords.java
 * (MIT license)
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
        }
        return result;
    }

    @Override
    public boolean test(String t) {
        char[] c = t.toCharArray();
        MatchState[] mtchrs = matchers();
        for (MatchState mtchr : mtchrs) {
            mtchr.reset();
        }
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < mtchrs.length; j++) {
                mtchrs[j].check(c[i]);
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
        }

        boolean isMatched() {
            return matched >= what.length - 1;
        }

        void check(char c) {
            if (isMatched()) {
                return;
            }
            if (what[matched] == c) {
                matched++;
            }
        }
    }
}
