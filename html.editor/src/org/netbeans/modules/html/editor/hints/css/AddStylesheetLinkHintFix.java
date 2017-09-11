/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints.css;

import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.html.editor.HtmlSourceUtils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages(
    "description.add.stylesheet.reference=Add reference to containing stylesheet {0}"
)
public class AddStylesheetLinkHintFix implements HintFix {
    private final FileObject externalStylesheet;
    private final FileObject sourceFile;
    private final String path;

    public AddStylesheetLinkHintFix(FileObject sourceFile, FileObject externalStylesheet) {
        this.sourceFile = sourceFile;
        this.externalStylesheet = externalStylesheet;
        
        this.path = WebUtils.getRelativePath(sourceFile, externalStylesheet);
    }

    @Override
    public String getDescription() {
        return Bundle.description_add_stylesheet_reference(path);
    }

    @Override
    public void implement() throws Exception {
        Source source = Source.create(sourceFile);
        final Document doc = source.getDocument(false);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                //html must be top level
                Result result = resultIterator.getParserResult();
                if(!(result instanceof HtmlParserResult)) {
                    return ;
                }
                ModificationResult modification = new ModificationResult();
                if(HtmlSourceUtils.importStyleSheet(modification, (HtmlParserResult)result, externalStylesheet)) {
                    modification.commit();
//                    if(doc != null) {
//                        //refresh the index for the modified file
//                        HtmlSourceUtils.forceReindex(sourceFile);
//                    }
                }
            }
        });
        
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
    
}
