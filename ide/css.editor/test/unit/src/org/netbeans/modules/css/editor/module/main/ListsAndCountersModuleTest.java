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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ListsAndCountersModuleTest extends CssModuleTestBase {

    public ListsAndCountersModuleTest(String testName) {
        super(testName);
    }

    public void testListStyle() {
        assertPropertyDeclaration("list-style: circle outside");
        assertPropertyDeclaration("list-style: lower-alpha");
        assertPropertyDeclaration("list-style: upper-roman inside ");
        assertPropertyDeclaration("list-style: symbols(\"*\" \"\\2020\" \"\\2021\" \"\\A7\");");
    }
    
    public void testListStyleCompletion() {
        PropertyDefinition p = Properties.getPropertyDefinition( "list-style");
        assertAlternatives(p.getGrammarElement(null), "",
                "repeating-linear-gradient","lower-latin","lower-greek",
                "repeating-radial-gradient","disc","lower-alpha","lower-roman",
                "!identifier","georgian","element","upper-alpha","armenian",
                "upper-latin","linear-gradient","!string","image","decimal",
                "upper-roman","!uri","cross-fade","radial-gradient","inside",
                "decimal-leading-zero","square", "circle","none","symbols",
                "outside", "initial", "inherit", "unset", "var");
    }
    
    public void testListStyleType() {
        assertPropertyDeclaration("list-style-type: circle");
        assertPropertyDeclaration("list-style-type: none");
        assertPropertyDeclaration("list-style-type: \"hello\"");
        assertPropertyDeclaration("list-style-type: someident");
        assertPropertyDeclaration("list-style-type: symbols(\"*\" \"\\2020\" \"\\2021\" \"\\A7\");");
    }
    
    public void testListStyleTypeCompletion() {
        PropertyDefinition p = Properties.getPropertyDefinition( "list-style-type");
        assertAlternatives(p.getGrammarElement(null), "",
            "georgian", "armenian", "upper-alpha", "upper-latin", "!string",
            "lower-latin", "circle", "lower-greek", "decimal", "upper-roman",
            "disc", "lower-alpha", "symbols", "lower-roman", "none",
            "decimal-leading-zero", "square", "!identifier", "initial",
            "inherit", "unset", "var");
        
        assertAlternatives(p.getGrammarElement(null), "symbols", "(");
        assertAlternatives(p.getGrammarElement(null), "symbols(",
            "repeating-linear-gradient", "element", "numeric", "linear-gradient",
            "!string", "alphabetic", "image", "symbolic", "repeating-radial-gradient",
            "!uri", "repeating", "cross-fade", "non-repeating", "radial-gradient",
            "var");
    }
    
    public void testListStyleImage() {
        assertPropertyDeclaration("list-style-image: none");        
        assertPropertyDeclaration("list-style-image: url(\"http://www.example.com/ellipse.png\")");        

    }
    public void testMarkerAttachmement() {
        assertPropertyDeclaration("marker-attachment: list-container");
        assertPropertyDeclaration("marker-attachment: list-item");
    }
    
    public void testListStylePosition() {
        assertPropertyDeclaration("list-style-position: inside");
        assertPropertyDeclaration("list-style-position: outside");
    }

    public void testMarkerPseudoElementCompletion() throws ParseException {
        checkCC("div::| ", arr("marker"), Match.CONTAINS);
        checkCC("li::mar| ", arr("marker"), Match.CONTAINS);
    }
}
