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
package org.netbeans.modules.diff.tree;

import java.util.regex.Pattern;

public class ExclusionPattern {
    public enum ExclusionType {
        WILDCARD
    }

    private ExclusionType type = ExclusionType.WILDCARD;
    private String pattern;

    public ExclusionType getType() {
        return type;
    }

    public void setType(ExclusionType type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("(/|\\\\|[*]{1,2})");
    public Pattern asPattern() {
        if (type == ExclusionType.WILDCARD) {
            String convertedPattern
                    = "\\Q"
                    + WILDCARD_PATTERN.matcher(pattern).replaceAll(mr -> {
                        String matchedText = mr.group();
                        return switch (matchedText) {
                            case "/", "\\" ->
                                "\\E[/\\\\]\\Q".replace("\\", "\\\\");
                            case "*" ->
                                "\\E[^/\\\\]+\\Q".replace("\\", "\\\\");
                            case "**" ->
                                "\\\\E.+\\\\Q";
                            default ->
                                matchedText;
                        };
                    })
                    + "\\E";
            return Pattern.compile(convertedPattern);
        }
        throw new IllegalStateException("Type: " + type + " is not known");
    }
}
