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
package org.netbeans.modules.php.composer.output.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.php.composer.output.model.SearchResult;

/**
 * Parsers for standard CLI output.
 */
class CliParser implements Parser {

    CliParser() {
    }

    @Override
    public List<SearchResult> parseSearch(String chunk) {
        String[] lines = chunk.split("\n"); // NOI18N
        if (lines.length == 0) {
            return Collections.emptyList();
        }
        List<SearchResult> result = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (!Composer.isValidOutput(line)) {
                // ignore warnings
                continue;
            }
            // legacy
            String[] split = line.split(":", 2); // NOI18N
            if (split.length == 2) {
                String name = split[0].trim();
                // verify name
                if (name.indexOf(' ') == -1) { // NOI18N
                    result.add(new SearchResult(name, split[1].trim()));
                    continue;
                }
            }
            // current
            split = line.split(" ", 2); // NOI18N
            String description = split.length == 2 ? split[1].trim() : ""; // NOI18N
            result.add(new SearchResult(split[0].trim(), description));
        }
        return result;
    }

}
