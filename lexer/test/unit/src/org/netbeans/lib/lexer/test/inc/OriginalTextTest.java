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

package org.netbeans.lib.lexer.test.inc;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.inc.OriginalText;

/**
 * Test for the text that emulates state of a mutable text input
 * before a particular modification.
 *
 * @author mmetelka
 */
public class OriginalTextTest extends TestCase {

    public OriginalTextTest(String testName) {
        super(testName);
    }

    public void test() throws Exception {
        String orig = "abcdef";
        check(orig, 0, 2, "xyz");
        check(orig, 0, 2, "x");
        check(orig, 0, 0, "");
        check(orig, 0, 0, "klmnopqrst");
        check(orig, orig.length(), 0, "");
        check(orig, orig.length(), 0, "klmnopqrst");
        check(orig, orig.length(), 0, "x");
        check(orig, 3, 0, "x");
        check(orig, 3, 1, "xyz");
        check(orig, 3, 3, "xy");
        check(orig, 1, 0, "x");
        check(orig, 1, 1, "xyz");
        check(orig, 1, 3, "xy");
        check(orig, 4, 0, "x");
        check(orig, 4, 1, "xy");
        check(orig, 4, 2, "x");
    }

    private void check(String text, int removeIndex, int removeLength, String insertText) {
        String modText = text.substring(0, removeIndex) + insertText + text.substring(removeIndex + removeLength);
        OriginalText ot = new OriginalText(modText, removeIndex, text.substring(removeIndex, removeIndex + removeLength), insertText.length());
        assertEquals(text.length(), ot.length());
        for (int i = 0; i < text.length(); i++) {
            assertEquals(String.valueOf(i), text.charAt(i), ot.charAt(i));
        }
        for (int i = 0; i < text.length(); i++) {
            for (int j = i; j < text.length(); j++) {
                assertEquals(text.substring(i, j), String.valueOf(ot.toCharArray(i, j)));
            }
        }
        assertEquals(text, ot.toString());
    }
    
}
