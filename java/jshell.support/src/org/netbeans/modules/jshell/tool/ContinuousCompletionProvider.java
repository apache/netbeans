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
