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

package org.netbeans.modules.bugtracking.issuetable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * Shouldn't be used by clients
 *
 * @author Tomas Stupka
 */
final class SummaryTextFilter extends Filter {
    private boolean highlighting;
    private Pattern pattern;

    public SummaryTextFilter() {
        pattern = Pattern.compile("$^"); // NOI18N
    }

    public void setText(String text, boolean regular, boolean wholeWords, boolean matchCase) {
        if (!regular) {
            text = Pattern.quote(text);
            if (wholeWords) {
                text ="\\b"+ text +"\\b"; // NOI18N
            }
        }
        int flags = 0;
        if (!matchCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        try {
            pattern = Pattern.compile(text, flags);
        } catch (PatternSyntaxException psex) {
            String message = NbBundle.getMessage(SummaryTextFilter.class, "FindInQueryBar.invalidExpression"); // NOI18N
            StatusDisplayer.getDefault().setStatusText(message, StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        }
    }

    public void setHighlighting(boolean on) {
        highlighting = on;
    }

    @Override
    public String getDisplayName() {
        throw new IllegalStateException();
    }

    @Override
    public boolean accept(IssueNode node) {
        return pattern.matcher(node.getSummary()).find();
    }

    boolean isHighLightingOn() {
        return highlighting;
    }

    Pattern getPattern() {
        return pattern;
    }
}
