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

import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.html.editor.HtmlSourceUtils;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
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
                if(!(result instanceof HtmlParsingResult)) {
                    return ;
                }
                ModificationResult modification = new ModificationResult();
                if(HtmlSourceUtils.importStyleSheet(modification, (HtmlParsingResult)result, result.getSnapshot(), externalStylesheet)) {
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
