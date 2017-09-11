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

import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class PagedMediaModuleTest extends CssModuleTestBase {
    
    public PagedMediaModuleTest(String name) {
        super(name);
    }

    public void testPageKeywordCompletion() throws ParseException  {
        checkCC("| ", arr("@page"), Match.CONTAINS);
        checkCC("@| ", arr("@page"), Match.CONTAINS);
        checkCC("@pa| ", arr("@page"), Match.CONTAINS);
    }
    
    public void testPagePseudoClassCompletion() throws ParseException  {
        checkCC("@page:| ", arr("first"), Match.CONTAINS);
        checkCC("@page:fi| ", arr("first"), Match.CONTAINS);

        //named page
        checkCC("@page mypage| ", arr(":first"), Match.CONTAINS);
        checkCC("@page mypage:| ", arr("first"), Match.CONTAINS);
        checkCC("@page mypage:fi| ", arr("first"), Match.CONTAINS);
    }
   
    public void testCompletionInPageRule() throws ParseException  {
        //should offer normal properties + page rules
        checkCC("@page { | }", arr("color", "@top-left", "@right-middle"), Match.CONTAINS);
        checkCC("@page { co| }", arr("color"), Match.CONTAINS);
        checkCC("@page { color:| }", arr("red"), Match.CONTAINS);
        checkCC("@page { color:r| }", arr("red"), Match.CONTAINS);
        
        checkCC("@page { @| }", arr("@top-left", "@right-middle"), Match.CONTAINS);
        
        //doesn't work: page rule error recovery needs to be improved -- see the parse tree
//        checkCC("@page { @t| }", arr("@top-left"), Match.CONTAINS);
//        checkCC("@page { @top-left| }", arr("@top-left"), Match.CONTAINS);
    }

    public void testCompletionInPageMarginRule() throws ParseException  {
        //should offer normal properties + page rules
        checkCC("@page { @top-left { | } }", arr("color"), Match.CONTAINS);
        checkCC("@page { @top-left { | } }", arr("@top-left", "@right-middle "), Match.DOES_NOT_CONTAIN);
        
        checkCC("@page { @top-left { co| } }", arr("color"), Match.CONTAINS);
        checkCC("@page { @top-left { color| } }", arr("color"), Match.CONTAINS);
        checkCC("@page { @top-left { color:| } }", arr("red"), Match.CONTAINS);
        checkCC("@page { @top-left { color:re| } }", arr("red"), Match.CONTAINS);
    }
    
   
    public void testProperties() {
        assertPropertyValues("size", "10px 20px");
        
        PropertyDefinition p = Properties.getPropertyDefinition( "size");
        assertNotNull(p);
        assertTrue(new ResolvedProperty(p, "auto").isResolved());
        assertTrue(new ResolvedProperty(p, "portrait").isResolved());
        
        p = Properties.getPropertyDefinition( "orphans");
        assertNotNull(p);
        assertTrue(new ResolvedProperty(p, "2").isResolved());
        
//        p = CssModuleSupport.getPropertyDefinition("fit-position");
//        assertNotNull(p);
//        assertTrue(new PropertyValue(p, "10% 20%").success());
//        assertTrue(new PropertyValue(p, "10px 20px").success());
//        assertTrue(new PropertyValue(p, "auto").success());
//        assertTrue(new PropertyValue(p, "top center").success());
//        assertTrue(new PropertyValue(p, "bottom right").success());
//        assertFalse(new PropertyValue(p, "bottom bottom").success());
        
    }
    
   
}
