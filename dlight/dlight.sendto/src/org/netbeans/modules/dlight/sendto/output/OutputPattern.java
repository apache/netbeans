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
package org.netbeans.modules.dlight.sendto.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public final class OutputPattern {

    public final Pattern pattern;
    public final Order order;
    public final String name;

    public OutputPattern(String name, Pattern pattern, Order order) {
        this.name = name;
        this.pattern = pattern;
        this.order = order;
    }

    public boolean match(String line) {
        Matcher m = pattern.matcher(line);
        return (m.matches() && m.groupCount() == 2);
    }

    public MatchResult process(String line) {
        Matcher m = pattern.matcher(line);
        if (m.matches() && m.groupCount() == 2) {
            try {
                int l = Integer.parseInt(m.group(order == Order.FILE_LINE ? 2 : 1));
                String file = m.group(order == Order.FILE_LINE ? 1 : 2);
                return new MatchResult(file, l);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public static class MatchResult {

        public final String filePath;
        public final int line;

        public MatchResult(String filePath, int line) {
            this.filePath = filePath;
            this.line = line;
        }
    }

    public static enum Order {

        FILE_LINE,
        LINE_FILE
    }
}
