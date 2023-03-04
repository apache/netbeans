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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class MissingCssElement extends Hint {

    private final HintContext hintContext;
    private List<HintFix> computedFixes;
    
    public MissingCssElement(Rule rule, String msg, HtmlRuleContext context, OffsetRange range, HintContext hintContext) {
        super(rule,
                msg,
                context.getFile(),
                range,
                Collections.<HintFix>emptyList(),
                10);
        this.hintContext = hintContext;
    }

    @Override
    public synchronized List<HintFix> getFixes() {
        if(computedFixes == null) {
            computedFixes = createFixes();
        }
        return Collections.unmodifiableList(computedFixes);
    }
    
    private List<HintFix> createFixes() {
        List<HintFix> fixes = new ArrayList<>();
        FileObject sourceFile = getFile();

        if (hintContext.getElement2files().get(hintContext.getPureElementName()) != null) {
            //1) if the class is found in one of the stylesheets in the project:
            //      * add "Import stylesheet" hintfix
            for (FileObject file : hintContext.getElement2files().get(hintContext.getPureElementName())) {
                fixes.add(new AddStylesheetLinkHintFix(sourceFile, file));
            }
        } else {
            if(!hintContext.getAllStylesheets().isEmpty()) {
                //2) if the class is not found in any stylesheet from the project:
                //      * add "create in xxx stylesheet" - for all available stylesheets. 
                //      The fix will add the stylesheet reference and create the rule there
                for (FileObject stylesheet : hintContext.getAllStylesheets()) {
                    fixes.add(new CreateRuleInStylesheet(
                            sourceFile, 
                            stylesheet, 
                            hintContext.getElementName(),
                            !hintContext.getReferredFiles().contains(stylesheet),
                            false));
                }
            } else {
                //* 3) if there's no stylesheet in the project
                //      * create in new stylesheet 
                //      Creates stylesheet 'styles.css' in the same folder, add ref to it and put the rule inside.
                    fixes.add(new CreateRuleInStylesheet(
                            sourceFile, 
                            null, 
                            hintContext.getElementName(),
                            true,
                            true));
            }
        }

        return fixes;
    }
}
