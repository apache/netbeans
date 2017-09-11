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
