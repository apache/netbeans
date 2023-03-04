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
package org.netbeans.modules.css.prep.editor.less;

import javax.swing.text.Document;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;

/**
 * Testing of plain css stuff in less files.
 * 
 * @author marekfukala
 */
public class LessCompletionTest2 extends CssModuleTestBase {

    public LessCompletionTest2(String name) {
        super(name);
    }
    @Override
    protected String getCompletionItemText(CompletionProposal cp) {
        return cp.getInsertPrefix();
    }

     public void testImportCompletion() throws ParseException {
        FileObject cssFile = getTestFile("testProject/public_html/test.less");
        Document document = getDocumentForFileObject(cssFile);
        
        setDocumentContent(document, "@import |");
        assertCompletion(document, Match.CONTAINS, "\"test1.scss\";");
        
        setDocumentContent(document, "@import | ");
        assertCompletion(document, Match.CONTAINS, "\"test1.scss\";");
        
    }
}
