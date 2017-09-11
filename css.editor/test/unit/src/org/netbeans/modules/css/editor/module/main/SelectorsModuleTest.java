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
