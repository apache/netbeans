/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.tool;

import java.util.List;
import static java.util.Comparator.comparing;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.netbeans.modules.jshell.tool.JShellTool.CompletionProvider;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

class ContinuousCompletionProvider implements CompletionProvider {

    static final BiPredicate<String, String> STARTSWITH_MATCHER =
            (word, input) -> word.startsWith(input);
    static final BiPredicate<String, String> PERFECT_MATCHER =
            (word, input) -> word.equals(input);

    private final Supplier<Map<String, CompletionProvider>> wordCompletionProviderSupplier;
    private final BiPredicate<String, String> matcher;

    ContinuousCompletionProvider(
            Map<String, CompletionProvider> wordCompletionProvider,
            BiPredicate<String, String> matcher) {
        this(() -> wordCompletionProvider, matcher);
    }

    ContinuousCompletionProvider(
            Supplier<Map<String, CompletionProvider>> wordCompletionProviderSupplier,
            BiPredicate<String, String> matcher) {
        this.wordCompletionProviderSupplier = wordCompletionProviderSupplier;
        this.matcher = matcher;
    }

    @Override
    public List<Suggestion> completionSuggestions(String input, int cursor, int[] anchor) {
        String prefix = input.substring(0, cursor);
        int space = prefix.indexOf(' ');

        Stream<SourceCodeAnalysis.Suggestion> result;

        Map<String, CompletionProvider> wordCompletionProvider = wordCompletionProviderSupplier.get();

        if (space == (-1)) {
            result = wordCompletionProvider.keySet().stream()
                    .distinct()
                    .filter(key -> key.startsWith(prefix))
                    .map(key -> new JShellTool.ArgSuggestion(key + " "));
            anchor[0] = 0;
        } else {
            String rest = prefix.substring(space + 1);
            String word = prefix.substring(0, space);

            List<CompletionProvider> candidates = wordCompletionProvider.entrySet().stream()
                    .filter(e -> matcher.test(e.getKey(), word))
                    .map(Map.Entry::getValue)
                    .collect(toList());
            if (candidates.size() == 1) {
                result = candidates.get(0).completionSuggestions(rest, cursor - space - 1, anchor).stream();
            } else {
                result = Stream.empty();
            }
            anchor[0] += space + 1;
        }

        return result.sorted(comparing(Suggestion::continuation))
                     .collect(toList());
    }

}
