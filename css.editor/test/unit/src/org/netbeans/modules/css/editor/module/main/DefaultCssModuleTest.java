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

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
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
    
    public void testCalc() {
        assertPropertyValues("width", "calc(100%/3 - 2*1em - 2*1px)");
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
    
}
