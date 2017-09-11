/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
