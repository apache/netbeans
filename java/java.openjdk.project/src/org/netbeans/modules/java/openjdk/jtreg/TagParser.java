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
package org.netbeans.modules.java.openjdk.jtreg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author lahvac
 */
public class TagParser {

    public static final List<String> RECOMMENDED_TAGS_ORDER = Arrays.asList(
            "test", "bug", "summary", "library", "author", "modules", "requires", "key", "library", "modules"
    );

    private static final Pattern TAG_PATTERN = Pattern.compile("@([a-zA-Z]+)(\\s+|/|$)");

    public static Result parseTags(CompilationInfo info) {
        return parseTags(info.getTokenHierarchy().tokenSequence(JavaTokenId.language()));
    }

    public static Result parseTags(Document doc) {
        return parseTags(TokenHierarchy.get(doc).tokenSequence(JavaTokenId.language()));
    }

    private static Result parseTags(TokenSequence<JavaTokenId> ts) {
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.BLOCK_COMMENT || ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                String text = ts.token().text().toString();

                if (text.contains("@test")) {
                    List<Tag> tags = new ArrayList<>();
                    int start = -1;
                    int end = -1;
                    int tagStart = -1;
                    int tagEnd = -1;

                    text = text.substring(0, text.length() - 2);

                    String tagName = null;
                    StringBuilder tagText = new StringBuilder();
                    int prefix = ts.token().id() == JavaTokenId.BLOCK_COMMENT ? 2 : 3;
                    String[] lines = text.substring(prefix).split("\n");
                    int pos = ts.offset() + prefix;

                    for (String line : lines) {
                        if (line.replaceAll("[*\\s]+", "").isEmpty()) {
                            pos += line.length() + 1;
                            continue;
                        }
                        Matcher m = TAG_PATTERN.matcher(line);
                        if (m.find()) {
                            if (tagName != null) {
                                tags.add(new Tag(start, pos, tagStart, tagEnd, tagName, tagText.toString()));
                                tagText.delete(0, tagText.length());
                            }

                            tagName = m.group(1);

                            start = pos;
                            tagStart = pos + m.start();
                            tagEnd = pos + m.end(1);
                            tagText.append(line.substring(m.end(1)));
                        } else if (tagName != null) {
                            int asterisk = line.indexOf('*');
                            tagText.append(line.substring(asterisk + 1));
                        }

                        pos += line.length() + 1;

                        if (tagName != null) {
                            end = pos;
                        }
                    }

                    if (tagName != null) {
                        tags.add(new Tag(start, end, tagStart, tagEnd, tagName, tagText.toString()));
                    }

                    Map<String, List<Tag>> result = new HashMap<>();

                    for (Tag tag : tags) {
                        List<Tag> innerTags = result.get(tag.getName());

                        if (innerTags == null) {
                            result.put(tag.getName(), innerTags = new ArrayList<>());
                        }

                        innerTags.add(tag);
                    }

                    return new Result(tags, result);
                }
            }
        }

        return new Result(Collections.<Tag>emptyList(), Collections.<String, List<Tag>>emptyMap());
    }

    public static final class Result {
        private final List<Tag> tags;
        private final Map<String, List<Tag>> name2Tag;

        public Result(List<Tag> tags, Map<String, List<Tag>> name2Tag) {
            this.tags = tags;
            this.name2Tag = name2Tag;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public Map<String, List<Tag>> getName2Tag() {
            return name2Tag;
        }
        
    }
}
