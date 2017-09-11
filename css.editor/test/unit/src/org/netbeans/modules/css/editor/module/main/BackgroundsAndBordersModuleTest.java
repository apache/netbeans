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

import org.netbeans.modules.css.lib.api.properties.NodeUtil;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class BackgroundsAndBordersModuleTest extends CssModuleTestBase {

    public BackgroundsAndBordersModuleTest(String name) {
        super(name);
    }

    public void testBackground_Attachment() throws ParseException {
        PropertyDefinition prop = Properties.getPropertyDefinition( "background-attachment");
        assertNotNull(prop);

        assertTrue(new ResolvedProperty(prop, "scroll").isResolved());
        assertTrue(new ResolvedProperty(prop, "fixed").isResolved());
        assertTrue(new ResolvedProperty(prop, "local").isResolved());

        assertTrue(new ResolvedProperty(prop, "local, local, scroll").isResolved());
        assertTrue(new ResolvedProperty(prop, "fixed,scroll").isResolved());
    }
    
    public void testBackground_Image() throws ParseException {
        PropertyDefinition prop = Properties.getPropertyDefinition( "background-image");
        assertNotNull(prop);

        assertTrue(new ResolvedProperty(prop, "none").isResolved());
        assertTrue(new ResolvedProperty(prop, "url(http://site.org/img.png)").isResolved());
        assertTrue(new ResolvedProperty(prop, "url(picture.jpg)").isResolved());
        
        assertTrue(new ResolvedProperty(prop, "url(picture.jpg), none, url(x.jpg)").isResolved());
   
         //[ top | bottom ]|[[ <percentage> | <length> | left | center | right ][ <percentage> | <length> | top | center | bottom ]?]|[[ center | [ left | right ] [ <percentage> | <length> ]? ][ center | [ top | bottom ] [ <percentage> | <length> ]? ]]
    }
    
    public void testBackground_Position() throws ParseException {
        PropertyDefinition prop = Properties.getPropertyDefinition( "background-position");
        assertNotNull(prop);
        
        assertTrue(new ResolvedProperty(prop, "left      top").isResolved());
        assertTrue(new ResolvedProperty(prop, "left 10px top 15px").isResolved());
    }
    
    public void testIssue201769() {
        PropertyDefinition prop = Properties.getPropertyDefinition( "background-position");
        ResolvedProperty pv = new ResolvedProperty(prop, "center top");
//        PropertyDefinitionTest.dumpResult(pv);
        assertTrue(pv.isResolved());
    }
    
    public void testBackground() {
        PropertyDefinition prop = Properties.getPropertyDefinition( "background");
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
        assertTrue(new ResolvedProperty(prop, "url(image.png) , url(image2.png)").isResolved());

    }
    
    public void testBackgroundPosition() {
        PropertyDefinition prop = Properties.getPropertyDefinition( "background-position");
        assertTrue(new ResolvedProperty(prop, "center").isResolved());
        assertTrue(new ResolvedProperty(prop, "center center").isResolved());
        assertTrue(new ResolvedProperty(prop, "center right 20px").isResolved());
        assertTrue(new ResolvedProperty(prop, "center top 20%").isResolved());
        assertTrue(new ResolvedProperty(prop, "top 20% center").isResolved());
        assertTrue(new ResolvedProperty(prop, "left 20px center").isResolved());
        assertTrue(new ResolvedProperty(prop, "left 20px top 10px").isResolved());
        assertTrue(new ResolvedProperty(prop, "left 20px").isResolved());
        assertTrue(new ResolvedProperty(prop, "left").isResolved());
        
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
        //fail
        assertFalse(new ResolvedProperty(prop, "left 20px right 10px").isResolved());
        
//        ResolvedProperty resolvedProperty = new ResolvedProperty(prop, "left 20px top 10px");
//        NodeUtil.dumpTree(resolvedProperty.getParseTree());
//        ResolvedProperty resolvedProperty = new ResolvedProperty(prop, "left 20px");
//        NodeUtil.dumpTree(resolvedProperty.getParseTree());
        ResolvedProperty resolvedProperty = new ResolvedProperty(prop, "left");
        NodeUtil.dumpTree(resolvedProperty.getParseTree());
    }
    
    public void testVariousProperties() {
        assertPropertyDeclaration("background-image: url(flower.png), url(ball.png), url(grass.png);");
        assertPropertyDeclaration("background-origin: border-box, content-box;");
        assertPropertyDeclaration("background-repeat: no-repeat;");
    }
    
    public void testBoxShadow() {
        assertPropertyDeclaration("box-shadow: inherit");
        assertPropertyDeclaration("box-shadow: none");
        assertPropertyDeclaration("box-shadow: inset rgba(255,255,255,0.3) 2px 2px,  rgba(0,0,0,0.05) 2px 2px;");
        assertPropertyDeclaration("box-shadow: inset rgba(255,255,255,0.3) 0 2px 2px,  rgba(0,0,0,0.05) 0 2px 2px;");
    }
    
    public void testBorderWidthCompletion() {
        //issue: border-width: property value completion contains item "-{0,1}" which comes
        //from the <length> element defined as @length=-? !length
        //desired: the completion should not contain the minus item, at least not with 
        //the multiplicity qualifier
        PropertyDefinition pm = Properties.getPropertyDefinition( "border-width");
        ResolvedProperty rp = assertResolve(pm.getGrammarElement(null), "", false);
        
        assertAlternatives(rp, "thick","thin","inherit","initial","!length","-", "medium", "calc");
        
        //ok - so the minus "-" is still in the alternatives (which is correct),
        //but finally filtered out in the code completion result by the "_operator" postfix
    }
    
    public void testPropertyCategory() {
        PropertyDefinition pd = Properties.getPropertyDefinition( "border-width");
        assertNotNull(pd);
        assertEquals(PropertyCategory.BOX, pd.getPropertyCategory());
    }
    
    
      public void testBorderImage() {
        assertPropertyDeclaration("border-image: url(img/examp/li/borderimage1.png)");
        assertPropertyDeclaration("border-image: 1 / 2px");
        assertPropertyDeclaration("border-image: 1 / 2px / 3");
        assertPropertyDeclaration("border-image: 1 / / 3");
        assertPropertyDeclaration("border-image: 1  / / 3 repeat");
        assertPropertyDeclaration("border-image: 1 repeat");
        assertPropertyDeclaration("border-image: 1 round");
        assertPropertyDeclaration("border-image: url(img/examp/li/borderimage1.png) 9 round");
    }
    
}
