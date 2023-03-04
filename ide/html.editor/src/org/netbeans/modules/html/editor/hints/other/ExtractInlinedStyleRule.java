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
package org.netbeans.modules.html.editor.hints.other;

import javax.swing.text.Document;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.refactoring.HtmlSpecificRefactoringsProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
@NbBundle.Messages("ExtractInlinedStyleRuleName=Extract Inline Style")
public class ExtractInlinedStyleRule implements Rule {

    public static ExtractInlinedStyleRule SINGLETON = new ExtractInlinedStyleRule();

    @Override
    public boolean appliesTo(RuleContext context) {
        HtmlSpecificRefactoringsProvider provider = Lookup.getDefault().lookup(HtmlSpecificRefactoringsProvider.class);
        if (provider == null) {
            return false;
        }

        Document doc = context.parserResult.getSnapshot().getSource().getDocument(true);
        if (doc == null) {
            return false;
        }

        return provider.canExtractInlineStyle(doc, new OffsetRange(context.caretOffset, context.caretOffset));

    }

    @Override
    public String getDisplayName() {
        return Bundle.ExtractInlinedStyleRuleName();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

}
