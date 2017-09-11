/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
                sb.append(displayText.substring(lastIdx, displayText.length()));
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
