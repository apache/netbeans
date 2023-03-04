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
