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
public class SelectorsModuleTest extends CssModuleTestBase {
    
    public SelectorsModuleTest(String name) {
        super(name);
    }

    //Bug 204504 - Code completion for pseudo-classes doesn't work properly
    public void testIssue204504() throws ParseException  {
        checkCC("p { } \ndiv:|\n", arr("enabled"), Match.CONTAINS);
        checkCC("p { } \ndiv:|", arr("enabled"), Match.CONTAINS);
    }
    
    public void testPseudoClassesCompletionDoesnOfferSelector() throws ParseException  {
        checkCC("div:|", arr("body"), Match.DOES_NOT_CONTAIN);
    }
    
    public void testPseudoClassesCompletion() throws ParseException  {
        checkCC("div:| ", arr("enabled"), Match.CONTAINS);
        checkCC("div:| \n h1 { } ", arr("enabled"), Match.CONTAINS);
        checkCC("div:ena|", arr("enabled"), Match.CONTAINS);
        checkCC("div:ena| h1 { }", arr("enabled"), Match.CONTAINS);
        checkCC("div:enabled| h1 { }", arr("enabled"), Match.CONTAINS);
    }
    
    public void testPseudoElementsCompletion() throws ParseException  {
        checkCC("div::| ", arr("after"), Match.CONTAINS);
        checkCC("div::|  h1 { } ", arr("after"), Match.CONTAINS);
        checkCC("div::af|", arr("after"), Match.CONTAINS);
        checkCC("div::af| h1 { }", arr("after"), Match.CONTAINS);
        checkCC("div::after| h1 { }", arr("after"), Match.CONTAINS);
    }
    
    public void testPseudoClassAfterClassSelector() throws ParseException {
        checkCC(".aclass:| ", arr("active"), Match.CONTAINS);
        checkCC(".aclass:ac| ", arr("active"), Match.CONTAINS);
        
        checkCC("div.aclass:| ", arr("active"), Match.CONTAINS);
        checkCC("div.aclass:ac| ", arr("active"), Match.CONTAINS);
        
    }
    
    public void testNotPseudoClass() throws ParseException {
        checkCC(".aclass:| ", arr("not"), Match.CONTAINS);
        checkCC(".aclass:no| ", arr("not"), Match.CONTAINS);
        
        checkCC("div.aclass:| ", arr("not"), Match.CONTAINS);
        checkCC("div.aclass:n| ", arr("not"), Match.CONTAINS);
        
    }
}
