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

package org.netbeans.modules.bugtracking.ui.search;

import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.commons.TextUtils;

/**
 *
 * @author Tomas Stupka
 */
abstract class PopupItem {
    
    abstract void invoke();
    abstract String getDisplayText();

    static class IssueItem extends PopupItem {
        private IssueImpl issue;

        public IssueItem(IssueImpl issue) {
            this.issue = issue;
        }

        @Override
        void invoke() {
            // do nothing
        }

        @Override
        String getDisplayText() {
            return getIssueDescription(issue);
        }

        public String highlite(String text, String displayText) {
            if(text == null || text.trim().equals("")) {
                return displayText;
            }      // NOI18N
            StringBuilder sb = new StringBuilder();

            text = TextUtils.escapeForHTMLLabel(text);
            displayText = TextUtils.escapeForHTMLLabel(displayText);

            String displayTextLower = displayText.toLowerCase();
            String textLower = text.toLowerCase();
            int idx = displayTextLower.indexOf(textLower);
            if(idx < 0) {
                return displayText;
            }
            int lastIdx = 0;
            sb.append("<html><table width=10000>");                             // NOI18N
            while(idx > -1) {
                sb.append(displayText.substring(lastIdx, idx));
                lastIdx = idx + textLower.length();
                if(idx > -1) {
                    sb.append("<b>");                                           // NOI18N
                    sb.append(displayText.substring(idx, lastIdx));
                    sb.append("</b>");                                          // NOI18N
                }
                lastIdx = idx + textLower.length();
                idx = displayTextLower.indexOf(displayTextLower, lastIdx);
            }
            if(lastIdx < displayText.length()) {
                sb.append(displayText.substring(lastIdx));
            } 
            sb.append("</table></html>");                                       // NOI18N
            return sb.toString();
        }

        public IssueImpl getIssue() {
            return issue;
        }

        public static String getIssueDescription(IssueImpl issue) {
            return issue.getID() + " - " + issue.getSummary();                  // NOI18N
        }

    }

}
