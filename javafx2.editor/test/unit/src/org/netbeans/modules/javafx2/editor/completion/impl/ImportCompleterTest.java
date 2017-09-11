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
