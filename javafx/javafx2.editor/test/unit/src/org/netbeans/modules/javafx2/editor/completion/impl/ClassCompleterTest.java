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
