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

package org.netbeans.modules.bugzilla.issue;

import java.util.Collection;

/**
 * Determines boundaries of text that should be rendered as a hyperlink
 * to an issue attachment (screenshot, full thread-dump, app. output etc.).
 *
 * @author Marian Petras
 */
class AttachmentHyperlinkSupport {

    private static final String PREFIX = "Created an attachment (id=";  //NOI18N
    private static final String PREFIX2 = "Created attachment ";  //NOI18N
    private static final int EQUAL_SIGN_POSITION = PREFIX.lastIndexOf('=');
    private static final int ATTACHMENT_HYPERLINK_START_FALLBACK  = PREFIX.indexOf("attachment");            //NOI18N
    private static final int ATTACHMENT_HYPERLINK_START_FALLBACK2  = PREFIX2.indexOf("attachment");            //NOI18N
    //private static final Pattern pattern = Pattern.compile(
    //                 "([0-9]++)\\)[^\\r\\n]*+(?:[\\r\\n]++(.*+))?+"); //NOI18N

    private static final char CR = '\r';
    private static final char LF = '\n';

    static Attachement findAttachment(String text, Collection<String> knownIds) {
        int[] boundaries = findAttachment1(text, knownIds);
        if(boundaries == null) {
            return findAttachment2(text, knownIds);
        } else {
            Attachement b = new Attachement();
            b.idx1 = boundaries[0];
            b.idx2 = boundaries[1];
            b.id = getAttachmentId(text);
            return b;
        }
    }
    
    private static int[] findAttachment1(String text, Collection<String> knownIds) {
        if ((knownIds != null) && knownIds.isEmpty()) {
            return null;
        }

        final int length = text.length();
        if ((length >= EQUAL_SIGN_POSITION + 3)
                && (text.charAt(EQUAL_SIGN_POSITION) == '=')
                && text.startsWith(PREFIX)) {
            int idStartIndex = EQUAL_SIGN_POSITION + 1;
            if (isValidIdChar(text.charAt(idStartIndex))) {
                int index = idStartIndex + 1;
                while ((index < length) && isValidIdChar(text.charAt(index))) {
                    index++;
                }
                if ((index < length) && (text.charAt(index) == ')')) {
                    int idEndIndex = index;
                    if (isKnownId(text.substring(idStartIndex, idEndIndex),
                                  knownIds)) {
                        return findBoundaries(index, text, idEndIndex + 1, ATTACHMENT_HYPERLINK_START_FALLBACK);
                    }
                }
            }
        }
        return null;
    }

    private static Attachement findAttachment2(String text, Collection<String> knownIds) {
        if (knownIds == null || knownIds.isEmpty()) {
            return null;
        }

        for (String id : knownIds) {
            String prefixId = PREFIX2 + id;
            if(!text.startsWith(prefixId)) {
                continue;
            }
            
            int[] boundaries = findBoundaries(prefixId.length() - 1, text, prefixId.length(), ATTACHMENT_HYPERLINK_START_FALLBACK2);
            if(boundaries != null) {
                Attachement b = new Attachement();
                b.idx1 = boundaries[0];
                b.idx2 = boundaries[1];
                b.id = id;
                return b;
            }
        }
        return null;
    }    
    
    private static int[] findBoundaries(int index, String text, int idEndIndex, int fallback) {
        final int length = text.length();
        do {
            index++;
        } while ((index < length) && isNotNewline(text.charAt(index)));
        if (index < length) {       //at newline

            /* skip just one newline */
            if (text.charAt(index) == CR) {
                index++;
            }
            if ((index < length) && (text.charAt(index) == LF)) {
                index++;
            }
            while ((index < length) && isSpace(text.charAt(index))) {
                index++;
            }
            if ((index < length) && isNotNewline(text.charAt(index))) {   //at printable
                int descriptionStart = index;
                do {
                    index++;
                } while ((index < length) && isNotNewline(text.charAt(index)));
                return new int[] {descriptionStart, index};
            }
        }
        return new int[] {fallback, idEndIndex};
    }

    private static String getAttachmentId(String commentText) {
        int closingBracketPos = commentText.indexOf(')', PREFIX.length() + 1);
        assert closingBracketPos != -1;
        return new String(commentText.substring(PREFIX.length(), closingBracketPos));
    }

    static class Attachement {
        int idx1;
        int idx2;
        String id;
    }
    
    private static boolean isKnownId(String id,
                                     Collection<String> knownIds) {
        if (knownIds == null) {
            return true;
        }

        for (String validId : knownIds) {
            if (id.equals(validId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidIdChar(char c) {
        return (c >= '0') && (c <= '9');
    }

    private static boolean isNotNewline(char c) {
        return (c != '\r') && (c != '\n');
    }

    private static boolean isSpace(char c) {
        return (c == ' ') || (c == '\t');
    }

}
