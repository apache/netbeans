/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.lexer;

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.CharRangesDump;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class CharRangesTest extends TestCase {

    public CharRangesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCharRanges() {
        // Check that character ranges of accepted characters for certain
        // methods of java.lang.Character match expectations

        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isWhitespace")).dump();
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isWhitespace")).dumpAsserts();
        List<Integer> charRanges = new CharRangesDump(
                new CharRangesDump.CharacterMethodAcceptor("isWhitespace")).charRanges();
        TestCase.assertEquals(charRanges.get(0).intValue(), 0x9);
        TestCase.assertEquals(charRanges.get(1).intValue(), 0xd);
        TestCase.assertEquals(charRanges.get(2).intValue(), 0x1c);
        TestCase.assertEquals(charRanges.get(3).intValue(), 0x20);
        
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierStart")).dump();
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierStart")).dumpAsserts();
        charRanges = new CharRangesDump(
                new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierStart")).charRanges();
        TestCase.assertEquals(charRanges.get(0).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(1).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(2).intValue(), 0x41);
        TestCase.assertEquals(charRanges.get(3).intValue(), 0x5a);
        TestCase.assertEquals(charRanges.get(4).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(5).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(6).intValue(), 0x61);
        TestCase.assertEquals(charRanges.get(7).intValue(), 0x7a);
        

        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierPart")).dump();
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierPart")).dumpAsserts();
        charRanges = new CharRangesDump(
                new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierPart")).charRanges();
        TestCase.assertEquals(charRanges.get(0).intValue(), 0x0);
        TestCase.assertEquals(charRanges.get(1).intValue(), 0x8);
        TestCase.assertEquals(charRanges.get(2).intValue(), 0xe);
        TestCase.assertEquals(charRanges.get(3).intValue(), 0x1b);
        TestCase.assertEquals(charRanges.get(4).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(5).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(6).intValue(), 0x30);
        TestCase.assertEquals(charRanges.get(7).intValue(), 0x39);
        TestCase.assertEquals(charRanges.get(8).intValue(), 0x41);
        TestCase.assertEquals(charRanges.get(9).intValue(), 0x5a);
        TestCase.assertEquals(charRanges.get(10).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(11).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(12).intValue(), 0x61);
        TestCase.assertEquals(charRanges.get(13).intValue(), 0x7a);
        TestCase.assertEquals(charRanges.get(14).intValue(), 0x7f);
        TestCase.assertEquals(charRanges.get(15).intValue(), 0x9f);

        TestCase.assertEquals((char)-1, 0xFFFF);
    }

}
