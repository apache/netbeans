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

import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class FontsModuleTest extends CssModuleTestBase {

    public FontsModuleTest(String testName) {
        super(testName);
    }
    
    public void testProperties() {
        assertPropertyValues("font", "caption", "italic 20px sans-serif");
        
        assertPropertyValues("font-size", "20px", "small", "smaller");
        
        assertPropertyValues("font-stretch", "ultra-condensed");
        
        assertPropertyValues("font-variant", 
                "normal", 
                "additional-ligatures", 
                "historical-forms",
                "styleset(10)", 
                "styleset(10,20,30)", 
                "character-variant(1)", 
                "character-variant(10,20,30)", 
                "swash(10)", "swash", 
                "annotation(1)", "annotation", 
                "hojo-kanji");
        
        assertPropertyValues("font-variant-alternates", 
                "normal",
                "contextual",
                "styleset(feature, another)",
                "character-variant(feature)", 
                "annotation(feature)"
                );
        
        assertPropertyValues("font-variant-east-asian", "hojo-kanji");
        
        assertPropertyValues("font-variant-numeric", 
                "lining-nums tabular-nums stacked-fractions",
                "stacked-fractions",
                "tabular-nums stacked-fractions");
        
    }
    
    public void testFontFamily() {
        //test quoted value
        assertPropertyValues("font-family", "\"Times New Roman\"", "'Times New Roman'");
        
        //more quoted values
        assertPropertyValues("font-family", "\"Times New Roman\", \"My Cool Font Family\"");
        //mixed quoted and generic
        assertPropertyValues("font-family", "\"Times New Roman\", Helvetica, Verdana, \"My Cool Font Family\"");
        
        //test unquoted family name - it is recommened so the value is quoted, but not forbidden!
        //http://www.w3.org/TR/css3-fonts/#descdef-font-family
        
        assertPropertyValues("font-family", "Times New Roman");
        assertPropertyValues("font-family", "Times New Roman, My Cool Font Family");
        
        //test mixed
        assertPropertyValues("font-family", "Times New Roman");
        assertPropertyValues("font-family", "Times New Roman, Helvetica");
        assertPropertyValues("font-family", "Times New Roman, Helvetica, \"My Cool Font Family\"");
        
    }
    
    //Bug 217424 - Editor does not know CSS3 rem and vmin units - Unexpected value token
    public void testIssue217424() {
        assertPropertyDeclaration("font-size: 1rem");
        assertPropertyDeclaration("font-size: 8vmin");
        
    }
    
    //Bug 217544 - Value of font property without number marked with Unexpected value token 
    public void testIssue217544() throws ParseException {
        //allow:
        //sup {
        //    font: cursive;
        //}
        //which is not according to the spec
        
        assertCssCode("div { font: cursive }");
    }
}
