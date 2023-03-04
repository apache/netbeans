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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.javafx2.editor.FXMLCompletionTestBase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.CompletionItem;

import org.netbeans.modules.javafx2.editor.FXMLCompletion2;

/**
 * Checks function of the import completer
 * @author sdedic
 */
public class ImportCompleterTest extends FXMLCompletionTestBase {

    public ImportCompleterTest(String testName) {
        super(testName);
    }
    
    public void testFirstImport() throws Exception {
        performTest("ImportCompleterTest/empty", 0, "", "firstImport.pass", 
                "\\<\\?import", "firstImport2.pass");
    }
    
    public void testFirstImportStartInstruction() throws Exception {
        performTest("ImportCompleterTest/empty", 0, "<?", "firstImportStartInstruction.pass",
                "\\<\\?import", "firstImport2.pass");
    }

    public void testImportPartialTarget() throws Exception {
        performTest("ImportCompleterTest/empty", 0, "<?imp", "partialImport.pass",
                "import", "firstImport2.pass");
    }
    
    public void testImportTLPackages() throws Exception {
        performTest("ImportCompleterTest/empty", 0, "<?import ", "tlPackages.pass",
                "^com$", "tlPackages2.pass");
    }

    public void testImportJavaSubpackages() throws Exception {
        performTest("ImportCompleterTest/empty", 0, "<?import java.", "importJavaSubpackages.pass",
                "^util$", "importJavaSubpackages2.pass");
    }
    
    public void testImportInMiddleOfInstructions() throws Exception {
        performTest("ImportCompleterTest/imports", 76, "", "importInMiddleOfInstructions.pass");
    }
    
    public void testImportBeforeRoot() throws Exception {
        performTest("ImportCompleterTest/importsAndRoot", 205, "", "importInMiddleOfInstructions.pass");
    }
    
    public void testNoImportInsideRoot() throws Exception {
        performTest("ImportCompleterTest/importsAndRoot", 219, "", "importInsideRoot.pass");
    }
    
    public void testImportAfterRoot() throws Exception {
        performTest("ImportCompleterTest/importsAndRoot", 239, "", "importAfterRoot.pass");
    }

    @Override
    protected List<? extends CompletionItem> performQuery(Source source, int queryType, int offset, int substitutionOffset, Document doc) throws Exception {
        return FXMLCompletion2.testQuery(source, doc, queryType, offset);
    }
    
    
}
