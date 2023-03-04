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
public class PropertyCompleterTest extends FXMLCompletionTestBase {

    public PropertyCompleterTest(String testName) {
        super(testName);
    }

    /**
     * Attribute immediately following tag name (+ whistespace)
     * @throws Exception 
     */
    public void testNewAttributeAfterTag() throws Exception {
        performTest("PropertyCompleterTest/sample1", 257, "", 
                "newAttributeAfterTag.pass", 
                "disable", "newAttributeAfterTag2.pass");
    }
    
    /**
     * Checks that a new attribute + whitespace after is created if
     * caret is positioned a the 1st character of an attribute
     * @throws Exception 
     */
    public void testNewAttributeBeforeValue() throws Exception {
        performTest("PropertyCompleterTest/sample1", 263, "", 
                "newAttributeAfterTag.pass", 
                "cacheHint", "newAttributeBeforeValue2.pass");
    }
    
    /**
     * Checks that if caret is positioned within property, the attribute
     * name is replaced, but the value remains untouched
     * @throws Exception 
     */
    public void testReplaceAttribute() throws Exception{
        performTest("PropertyCompleterTest/sample1", 278, "", 
                "replaceAttribute.pass", 
                "prefWidth", "replaceAttribute2.pass");
    }
    
    public void testAddPropertySuffix() throws Exception {
        performTest("PropertyCompleterTest/sample1", 257, "pref", 
                "addPropertySuffix.pass", 
                "prefWidth", "addPropertySuffix2.pass");
    }
    
    public void testAddPropertyAfterAttribute() throws Exception {
        performTest("PropertyCompleterTest/sample1", 275, "", 
                "newAttributeAfterTag.pass", 
                "disable", "addPropertyAfterAttribute2.pass");
    }
    
    public void testAddPropertyBeforeBrace() throws Exception {
        performTest("PropertyCompleterTest/sample1", 328, "", 
                "newAttributeAfterTag.pass", 
                "disable", "addPropertyBeforeBrace2.pass");
    }
    
    public void testAddFirstChildElementProperty() throws Exception {
        performTest("PropertyCompleterTest/sample2", 334, "", 
                "addFirstChildElementProperty.pass", 
                "snapToPixel", "addFirstChildElementProperty2.pass");
    }
    
    public void testAddElementChildPropertyBrace() throws Exception {
        performTest("PropertyCompleterTest/sample2", 334, "<", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "addElementChildPropertyBrace.pass", 
                "disable", "addElementChildPropertyBrace2.pass");
    }

    public void testAddFirstElementProperty() throws Exception {
        performTest("PropertyCompleterTest/sample1", 329, "", 
                "addFirstElementProperty.pass", 
                "snapToPixel", "addFirstElementProperty2.pass");
    }
    
    public void testAddElementPropertyBrace() throws Exception {
        performTest("PropertyCompleterTest/sample1", 329, "<", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "elementFollowingBrace.pass", 
                "disable", "elementPropertyBrace2.pass");
    }

    public void testReplaceElementProperty() throws Exception {
        performTest("PropertyCompleterTest/sample1", 340, "", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "replaceElementProperty.pass", 
                "disable", "replaceElementProperty2.pass");
    }
    
    public void testElementSuffix() throws Exception {
        performTest("PropertyCompleterTest/sample1", 344, "", 
                "elementSuffix.pass", 
                "childrenUnmodifiable", "elementSuffix2.pass");
    }
    
    public void testElementFollowingBrace() throws Exception {
        performTest("PropertyCompleterTest/sample1", 329, "", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "elementFollowingBrace.pass", 
                "disable", "elementFollowingBrace2.pass");
    }
    
    public void testElementPrecedingBrace() throws Exception {
        performTest("PropertyCompleterTest/sample1", 339, "", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "elementFollowingBrace.pass", 
                "disable", "elementPrecedingBrace2.pass");
    }
    
    public void testMapPropertyElement() throws Exception {
        performTest("PropertyCompleterTest/sample1", 329, "", 
                CompletionProvider.COMPLETION_ALL_QUERY_TYPE,
                "elementFollowingBrace.pass", 
                "properties", "mapPropertyElement2.pass");
    }
    
    @Override
    protected List<? extends CompletionItem> performQuery(Source source, int queryType, int offset, int substitutionOffset, Document doc) throws Exception {
        return FXMLCompletion2.testQuery(source, doc, queryType, offset);
    }
}
