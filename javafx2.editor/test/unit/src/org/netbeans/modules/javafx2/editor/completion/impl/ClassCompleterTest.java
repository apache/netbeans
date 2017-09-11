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
import org.netbeans.modules.javafx2.editor.FXMLCompletion2;
import org.netbeans.modules.javafx2.editor.FXMLCompletionTestBase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author sdedic
 */
public class ClassCompleterTest extends FXMLCompletionTestBase {

    public ClassCompleterTest(String testName) {
        super(testName);
    }
    
    /**
     * Only certain classes from imported packages should appear
     */
    public void testRootEmptyCompletionShallow() throws Exception {
        performTest("ClassCompleterTest/empty", 164, "", "rootEmptyCompletionShallow.pass",
                "AnchorPane", "rootEmptyCompletionShallow2.pass");
    }
    
    
    /**
     * All relevant classes (inheriting from Node), replacement with 
     * FQN (outside of present imports)
     */
    public void testRootEmptyCompletionDeep() throws Exception {
        performTest("ClassCompleterTest/empty", 164, "", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "rootEmptyCompletionDeep.pass", 
                "Arc", "rootEmptyCompletionDeep2.pass");
    }
    
    /**
     * Completes a qualified class
     */
    public void testQualifiedClassNoImports() throws Exception {
        performTest("ClassCompleterTest/noImports", 39, "<A", 
                CompletionProvider.COMPLETION_QUERY_TYPE,
                "qualifiedClassNoImports.pass", 
                "AnchorPane", "qualifiedClassNoImports2.pass");
    }
    
    /*
     * Replaces an existing class name with another
     */
    public void testReplaceClassTag() throws Exception {
        performTest("ClassCompleterTest/sample1", 400, "", 
                "replaceClassTag.pass", 
                "AnchorPane", "replaceClassTag2.pass");
    }
    
    public void testReplaceSelfClosedTag() throws Exception {
        performTest("ClassCompleterTest/sample1", 505, "", 
                "replaceClassTag.pass", 
                "TextField", "replaceSelfClosedTag2.pass");
    }
    /*
     * Replaces an existing class name with another, preserving attributes
     */
    public void testReplaceClassTagWithAttributes() throws Exception {
        performTest("ClassCompleterTest/sample1", 431, "", 
                "replaceClassTag.pass", 
                "AnchorPane", "replaceClassTagWithAttributes2.pass");
    }

    /**
     * Checks completion after opening &lt; brace
     * @throws Exception 
     */
    public void testCompleteOpeningBrace() throws Exception {
        performTest("ClassCompleterTest/sample1", 394, "<", 
                "completeOpeningBrace.pass", 
                "Label", "completeOpeningBrace2.pass");
    }
    
    /**
     * Complete partially written &lt;name
     * @throws Exception 
     */
    public void testCompletePartialName() throws Exception {
        performTest("ClassCompleterTest/sample1", 394, "<Te", 
                "completePartialName.pass", 
                "TextField", "completePartialName2.pass");
    }
    
    /**
     * Unfinished opening tag with incomplete name and attributes
     * @throws Exception 
     */
    public void testCompletePartialNameWithAttributes() throws Exception {
        performTest("ClassCompleterTest/sample1", 523, 4, -1, "", 
                CompletionProvider.COMPLETION_QUERY_TYPE,
                "completePartialNameWithAttributes.pass", 
                "ListView", "completePartialNameWithAttributes2.pass");
    }

    /**
     * Unfinished opening tag with incomplete name,
     * but caret is positioned in the middle of the partial name
     * @throws Exception 
     */
    public void testCompletePartialNameMiddle() throws Exception {
        performTest("ClassCompleterTest/sample1", 508, 2, 506, "", 
                CompletionProvider.COMPLETION_QUERY_TYPE,
                "completePartialMiddle.pass", 
                "ListView", "completePartialMiddle2.pass");
    }
    
    /**
     * Unfinished opening tag with incomplete name and attributes,
     * but caret is positioned in the middle of the partial name
     * @throws Exception 
     */
    public void testCompletePartialNameMiddleWithAttributes() throws Exception {
        performTest("ClassCompleterTest/sample1", 525, 2, 523, "", 
                CompletionProvider.COMPLETION_QUERY_TYPE,
                "completePartialMiddle.pass", 
                "ListView", "completePartialMiddleWithAttributes2.pass");
    }
    
    /**
     * Completes specific subclass
     * @throws Exception 
     */
    public void testCompleteSubclassShallow() throws Exception {
        performTest("ClassCompleterTest/sample1", 850, 0, -1, "<", 
                CompletionProvider.COMPLETION_QUERY_TYPE,
                "completeSubclassShallow.pass", 
                "Rotate", "completeSubclassShallow2.pass");
    }
    
    /**
     * Attempts to add class Element into the EMPTY content of a bean with
     * @DefaultProperty-annotated property. In that case, CompletionContext should
     * initialize for CHILD_ELEMENT completion.
     * 
     * @throws Exception 
     */
    public void testStartInDefaultProperty() throws Exception {
        performTest("ClassCompleterTest/sample1", 409, 0, -1, "<", 
                CompletionProvider.COMPLETION_QUERY_TYPE,
                "startInDefaultProperty.pass", 
                "TextField", "startInDefaultProperty2.pass");
    }
    
    @Override
    protected List<? extends CompletionItem> performQuery(Source source, int queryType, int offset, int substitutionOffset, Document doc) throws Exception {
        return FXMLCompletion2.testQuery(source, doc, queryType, offset);
    }
}
