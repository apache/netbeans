/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
