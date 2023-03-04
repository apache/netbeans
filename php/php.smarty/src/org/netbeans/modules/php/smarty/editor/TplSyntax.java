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
package org.netbeans.modules.php.smarty.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.util.Parameters;

/**
 * Holds information about syntax and block of the Smarty templating engine. These information are used on various
 * places like indenter, parser etc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplSyntax {

    /**
     * List of all tags which introduce block of code.
     */
    public static final Set<String> BLOCK_TAGS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "block", //NOI18N
            "capture", //NOI18N
            "for", //NOI18N
            "foreach", //NOI18N
            "function", //NOI18N
            "if", //NOI18N
            "literal", //NOI18N
            "nocache", //NOI18N
            "php", //NOI18N
            "section", //NOI18N
            "setfilter", //NOI18N
            "strip", //NOI18N
            "while"))); //NOI18N
    /**
     * List of all tags which are else-typed.
     */
    public static final Set<String> ELSE_TAGS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "foreachelse", //NOI18N
            "elseif", //NOI18N
            "else", //NOI18N
            "sectionelse"))); //NOI18N
    /**
     * Mapping of non-else tag to else-like tags. To every else tag must correspond at least one normal tag.
     */
    public static final Map<String, Set<String>> RELATED_TAGS = Collections.unmodifiableMap(new HashMap<String, Set<String>>() {
        {
            put("if", new HashSet<String>(Arrays.asList("else", "elseif"))); //NOI18N
            put("foreach", new HashSet<String>(Arrays.asList("foreachelse"))); //NOI18N
            put("section", new HashSet<String>(Arrays.asList("sectionelse"))); //NOI18N
        }
    });

    /**
     * Gets information whether the given command is block command.
     *
     * @param tag examined tag
     * @return {@code true} when the given tag is block tag, {@code false} otherwise
     */
    public static boolean isBlockCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        String tokenText = tag.toLowerCase();
        return !tag.isEmpty() && (BLOCK_TAGS.contains(tokenText)
                || BLOCK_TAGS.contains(tokenText.substring(1))
                || ELSE_TAGS.contains(tokenText));
    }

    /**
     * Gets information whether the given command is "else-like" command.
     *
     * @param tag examined tag
     * @return {@code true} when the given tag is "else-like" tag, {@code false} otherwise
     */
    public static boolean isElseSmartyCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        String tokenText = tag.toLowerCase();
        return !tag.isEmpty() && ELSE_TAGS.contains(tokenText);
    }

    /**
     * Gets information whether the actual command is in relation to comparing command.
     *
     * @param actualTag examined tag
     * @param relatedToTag tag which is suspicious for relation
     * @return {@code true} when the tag is "else-like" or ending tag to the relatedToTag, {@code false} otherwise
     */
    public static boolean isInRelatedCommand(String actualTag, String relatedToTag) {
        Parameters.notNull("actualTag", actualTag); //NOI18N
        Parameters.notNull("relatedToTag", relatedToTag); //NOI18N
        return actualTag.substring(1).equals(relatedToTag)
                || (RELATED_TAGS.get(relatedToTag) != null && RELATED_TAGS.get(relatedToTag).contains(actualTag));
    }

    /**
     * Gets related tag to given else-like or ending tag.
     *
     * @param tag examined else-like or ending tag
     * @return related tag to the given one, {@code null} when no such tag exists
     */
    public static String getRelatedBaseCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        if (isEndingSmartyCommand(tag)) {
            String startTag = tag.substring(1);
            if (BLOCK_TAGS.contains(startTag)) {
                return startTag;
            }
        } else if (isElseSmartyCommand(tag)) {
            for (Map.Entry<String, Set<String>> entry : RELATED_TAGS.entrySet()) {
                if (entry.getValue().contains(tag)) {
                    return entry.getKey();
                }
            }
        } else {
            if (BLOCK_TAGS.contains(tag)) {
                return tag;
            }
        }
        return null;
    }

    /**
     * Says whether the given tag is ending tag or not.
     *
     * @param tag examined tag
     * @return {@code true} when the given tag is ending tag, {@code false} otherwise
     */
    public static boolean isEndingSmartyCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        if (!tag.isEmpty() && tag.startsWith("/")) { //NOI18N
            String startTag = tag.substring(1);
            return BLOCK_TAGS.contains(startTag);
        }
        return false;
    }

    /**
     * Return ending tag to given tag.
     *
     * @param tag tag
     * @return ending tag
     */
    public static String getEndingCommand(String tag) {
        Parameters.notNull("tag", tag); //NOI18N
        String command = getRelatedBaseCommand(tag);
        return "/" + command; //NOI18N
    }
}
