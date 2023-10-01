/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Pair;

/**
 *
 * @author mfukala@netbeans.org
 */
public class DefaultCssModuleTest extends CssModuleTestBase {

    //parsed from http://www.w3.org/TR/CSS2/propidx.html
    //(Cascading Style Sheets Level 2 Revision 1 (CSS 2.1) Specification)
    private static final String[] ALL_CSS21_PROPERTIES = new String[]{
        "azimuth",
        "background-attachment",
        "background-color",
        "background-image",
        "background-position",
        "background-repeat",
        "background",
        "border-collapse",
        "border-color",
        "border-spacing",
        "border-style",
        "border-top", "border-right", "border-bottom", "border-left",
        "border-top-color", "border-right-color", "border-bottom-color", "border-left-color",
        "border-top-style", "border-right-style", "border-bottom-style", "border-left-style",
        "border-top-width", "border-right-width", "border-bottom-width", "border-left-width",
        "border-width",
        "border",
        "bottom",
        "caption-side",
        "clear",
        "clip",
        "color",
        "content",
        "counter-increment",
        "counter-reset",
        "cue-after",
        "cue-before",
        "cue",
        "cursor",
        "direction",
        "display",
        "elevation",
        "empty-cells",
        "float",
        "font-family",
        "font-size",
        "font-style",
        "font-variant",
        "font-weight",
        "font",
        "height",
        "left",
        "letter-spacing",
        "line-height",
        "list-style-image",
        "list-style-position",
        "list-style-type",
        "list-style",
        "margin-right", "margin-left",
        "margin-top", "margin-bottom",
        "margin",
        "max-height",
        "max-width",
        "min-height",
        "min-width",
        "orphans",
        "outline-color",
        "outline-style",
        "outline-width",
        "outline",
        "overflow",
        "padding-top", "padding-right", "padding-bottom", "padding-left",
        "padding",
        "page-break-after",
        "page-break-before",
        "page-break-inside",
        "pause-after",
        "pause-before",
        "pause",
        "pitch-range",
        "pitch",
        "play-during",
        "position",
        "quotes",
        "richness",
        "right",
        "speak-header",
        "speak-numeral",
        "speak-punctuation",
        "speak",
        "speech-rate",
        "stress",
        "table-layout",
        "text-align",
        "text-decoration",
        "text-indent",
        "text-transform",
        "top",
        "unicode-bidi",
        "vertical-align",
        "visibility",
        "voice-family",
        "volume",
        "white-space",
        "widows",
        "width",
        "word-spacing",
        "z-index"
    };
    
    public DefaultCssModuleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //allow the InstalledFilesLocator to work
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final"));//NOI18N
    }

    public void testDocumentation() {
        HelpResolver resolver = CssModuleSupport.getHelpResolver();
        assertNotNull(resolver);

        PropertyDefinition color = Properties.getPropertyDefinition( "color");
        assertNotNull(color);
        
        String help = resolver.getHelp(null, color);
        assertNotNull(help);
        assertTrue(help.length() > 0);

//        System.out.println(help);
    }

    public void testAllCSS21PropertiesAreAvailable() {
        for(String propName : ALL_CSS21_PROPERTIES) {
            PropertyDefinition prop = Properties.getPropertyDefinition( propName);
            assertNotNull(String.format("No such property %s found!", propName), prop);
            
        }
    }
    
    public void testMathFunctions() {
        assertPropertyValues("width", "calc(100%/3 - 2*1em - 2*1px)");
        assertPropertyValues("width", "calc(var(--width) * 2)");
        assertPropertyValues("width", "clamp(1.8rem, 2.5vw, 2.8rem)");
        assertPropertyValues("width", "max(4vw, 2em, 2rem)");
        assertPropertyValues("width", "min(40%, 400px)");
        assertPropertyValues("width", "abs(20% - 100px)");
        assertPropertyValues("margin", "calc(sign(20% - 100px) * (20% - 100px))");
        assertPropertyValues("transform", "rotate(acos(0.5))");
        assertPropertyValues("transform", "rotate(asin(pi / 5))");
        assertPropertyValues("transform", "rotate(atan(pi / 2))");
        assertPropertyValues("transform", "rotate(atan2(20%, -30%))");
        assertPropertyValues("width", "calc(100px * cos(2 * 0.125))");
        assertPropertyValues("width", "calc(100px * sin(2 * 0.125))");
        assertPropertyValues("width", "calc(100px * tan(1.732 - 1))");
        assertPropertyValues("width", "calc(100px * sqrt(9))");
        assertPropertyValues("width", "round(-105px, 10px)");
        assertPropertyValues("width", "round(to-zero, -105px, 10px)");
        assertPropertyValues("width", "round(up, 101px, var(--interval))");
        assertPropertyValues("width", "round(var(--width), 50px)");
        assertPropertyValues("font-size", "calc(1rem * pow(1.5, 4))");
        assertPropertyValues("line-height", "mod(3.5, 2)");
        assertPropertyValues("width", "calc(var(--size-0) * log(7.389))");
        assertPropertyValues("width", "calc(var(--size-0) * log(8, 2))");
        assertPropertyValues("width", "hypot(var(--size-0))");
        assertPropertyValues("width", "hypot(var(--size-0), var(--size-0))");
        assertPropertyValues("font-size", "calc(1rem * exp(1.25))");
        assertPropertyValues("margin", "rem(10rem, 6rem)");
    }
    
    public void testGetDeclarationForURIQuoted() {
        BaseDocument document = getDocument("div { background: url(\"hello.png\"); } ");
        //                                   0123456789012345678901 23456789012 34567890123456789
        //                                   0         1         2         3
        DefaultCssEditorModule dm = new DefaultCssEditorModule();
        Pair<OffsetRange, FutureParamTask<DeclarationFinder.DeclarationLocation, EditorFeatureContext>> declaration = dm.getDeclaration(document, 25);
        assertNotNull(declaration);
        OffsetRange range = declaration.first();
        assertNotNull(range);
        assertEquals(23, range.getStart());
        assertEquals(32, range.getEnd());
        
        FutureParamTask<DeclarationFinder.DeclarationLocation, EditorFeatureContext> task = declaration.second();
        assertNotNull(task);
        
    }
    
    public void testGetDeclarationForURIUnquoted() {
        BaseDocument document = getDocument("div { background: url(hello.png); } ");
        //                                   01234567890123456789012345678901234567890123456789
        //                                   0         1         2         3
        DefaultCssEditorModule dm = new DefaultCssEditorModule();
        Pair<OffsetRange, FutureParamTask<DeclarationFinder.DeclarationLocation, EditorFeatureContext>> declaration = dm.getDeclaration(document, 25);
        assertNotNull(declaration);
        OffsetRange range = declaration.first();
        assertNotNull(range);
        assertEquals(22, range.getStart());
        assertEquals(31, range.getEnd());
        
        FutureParamTask<DeclarationFinder.DeclarationLocation, EditorFeatureContext> task = declaration.second();
        assertNotNull(task);
        
    }
    
     public void testGetDeclarationForImport() {
        BaseDocument document = getDocument("@import \"hello.css\"; ");
        //                                   01234567 8901234567 8901
        //                                   0         1         2         3
        DefaultCssEditorModule dm = new DefaultCssEditorModule();
        Pair<OffsetRange, FutureParamTask<DeclarationFinder.DeclarationLocation, EditorFeatureContext>> declaration = dm.getDeclaration(document, 10);
        assertNotNull(declaration);
        OffsetRange range = declaration.first();
        assertNotNull(range);
        assertEquals(9, range.getStart());
        assertEquals(18, range.getEnd());
        
        FutureParamTask<DeclarationFinder.DeclarationLocation, EditorFeatureContext> task = declaration.second();
        assertNotNull(task);
        
    }

    public void testVariableCompletionDeclared() throws ParseException {
        checkCC(
            ":root {--c1-fore: coral, --c1-back: blue} p { background: var(|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            ":root {--c1-fore: coral, --c1-back: blue} p { background: var(-|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            ":root {--c1-fore: coral, --c1-back: blue} p { background: var(--|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            ":root {--c1-fore: coral, --c1-back: blue} p { background: var(--c1|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            ":root {--c1-fore: coral, --c1-back: blue} p { background: var(--c1-f|",
            arr("--c1-fore"),
            Match.CONTAINS);
    }

    public void testVariableCompletionUsed() throws ParseException {
        checkCC(
            "h1 {background-color: var(--c1-fore, coral); color: var(--c1-back)} p { background: var(|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            "h1 {background-color: var(--c1-fore, coral); color: var(--c1-back)} p { background: var(-|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            "h1 {background-color: var(--c1-fore, coral); color: var(--c1-back)} p { background: var(--|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            "h1 {background-color: var(--c1-fore, coral); color: var(--c1-back)} p { background: var(--c1|",
            arr("--c1-fore", "--c1-back"),
            Match.CONTAINS);
        checkCC(
            "h1 {background-color: var(--c1-fore, coral); color: var(--c1-back)} p { background: var(--c1-f|",
            arr("--c1-fore"),
            Match.CONTAINS);
    }
}
