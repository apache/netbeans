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
package org.netbeans.modules.html.editor.hints.other;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.hints.HtmlRule;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Christian Lenz
 */
public class AddMissingAltAttributeRule extends HtmlRule {

    private final static AddMissingAltAttributeRule INSTANCE = new AddMissingAltAttributeRule();

    private AddMissingAltAttributeRule() {
    }

    public static AddMissingAltAttributeRule getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    @Override
    protected void run(HtmlRuleContext context, List<Hint> result) {
        try {
            HtmlParserResult parserResult = context.getHtmlParserResult();
            AltAttributeVisitor visitor = new AltAttributeVisitor(this, context, result);

            ElementUtils.visitChildren(parserResult.root(), visitor, ElementType.OPEN_TAG);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
}
