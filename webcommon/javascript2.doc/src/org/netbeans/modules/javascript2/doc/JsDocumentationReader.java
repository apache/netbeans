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
package org.netbeans.modules.javascript2.doc;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Reads the documantation comments from the file and returns list of comment tags (like @private, @class, ...).
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationReader {

    public static Set<String> getAllTags(Snapshot snapshot) {
        Set<String> tags = new HashSet<String>();

//        TokenSequence tokenSequence = snapshot.getTokenHierarchy().tokenSequence(JsTokenId.javascriptLanguage());
//        if (tokenSequence == null) {
//            return tags;
//        }
//
//        while (tokenSequence.moveNext()) {
//            if (tokenSequence.token().id() == JsTokenId.DOC_COMMENT) {
//                tags.addAll(getCommentTags(tokenSequence.token().text()));
//                continue;
//            }
//        }
        tags.addAll(getCommentTags(snapshot.getText()));

        return tags;
    }

    protected static Set<String> getCommentTags(CharSequence commentText) {
        Set<String> tags = new HashSet<String>();
        String comment = commentText.toString();
        // XXX - could be rewrite to lexer
        Pattern pattern = Pattern.compile("[@][a-zA-Z]+"); //NOI18N
        Matcher matcher = pattern.matcher(comment);
        while (matcher.find()) {
			tags.add(matcher.group());
		}
        return tags;
    }
}
