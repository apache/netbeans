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
package org.netbeans.modules.html.editor.hints.css;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.hints.HtmlRule;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Missing CSS class hint.
 * 
 * Check whether the class reference in an html element is valid.
 * 
 * Hint fixes rules:
 * 0) if the class is found in a lined stylesheet, no hint, not fixes
 * 
 * 1) if the class is found in one of the stylesheets in the project:
 *      * add "Import stylesheet" hintfix
 * 
 * 2) if the class is not found in any stylesheet from the project:
 *      * add "create in xxx stylesheet" - for all available stylesheets. 
 *      The fix will add the stylesheet reference and create the rule there
 * 
 * 3) if there's no stylesheet in the project
 *      * create in new stylesheet 
 *      Creates stylesheet 'styles.css' in the same folder, add ref to it and put the rule inside.
 * 
 * @author marekfukala
 */
public class MissingClassRule extends HtmlRule {

    public MissingClassRule() {
    }

    @Override
    protected void run(HtmlRuleContext context, List<Hint> result) {
        try {
            HtmlParserResult parserResult = context.getHtmlParserResult();
            if(context.getCssIndex() != null) { //test if we can get the index, if not we can check nothing (typically css out of a project)
                CssClassesVisitor visitor = new CssClassesVisitor(this, context, result);
                ElementUtils.visitChildren(parserResult.root(), visitor, ElementType.OPEN_TAG);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        FileObject file = result.getSnapshot().getSource().getFileObject();
        if (file == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return false;
        }
        //do not apply this missing class rule on js-html web application files 
        //as these typically uses external stylesheets binding (there's no static
        //link in the application html files).
        //
        //there's another MissingClassRuleInApp rule which applies to application pieces
        //and uses HintSeverity.CURRENT_LINE_WARNING for them.
        return HtmlExtensions.isApplicationPiece(result) == appliesToApplicationPieceOnly();
    }
    
    protected boolean appliesToApplicationPieceOnly() {
        return false;
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @NbBundle.Messages("MissingCssClassRuleName=Missing CSS Class")
    @Override
    public String getDisplayName() {
        return  Bundle.MissingCssClassRuleName();
    }

    @NbBundle.Messages("MissingCssClassRuleDescription=Checks if referred css class rule exists.")
    @Override
    public String getDescription() {
        return Bundle.MissingCssClassRuleDescription();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
}
