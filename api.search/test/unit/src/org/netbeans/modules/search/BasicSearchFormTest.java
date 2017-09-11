/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  Marian Petras
 */
public class BasicSearchFormTest extends NbTestCase {

    private Method isBackrefSyntaxUsedMethod;

    public BasicSearchFormTest() {
        super("BasicSearchFormTest");
    }

    @Override
    public void tearDown() throws Exception {
        isBackrefSyntaxUsedMethod = null;
    }

    public void testIsBackrefSyntaxUsed() throws Exception {
        assertFalse(callIsBackrefSyntaxUsed(""));

        assertFalse(callIsBackrefSyntaxUsed(" "));
        assertFalse(callIsBackrefSyntaxUsed("a"));
        assertFalse(callIsBackrefSyntaxUsed("0"));
        assertFalse(callIsBackrefSyntaxUsed("1"));
        assertFalse(callIsBackrefSyntaxUsed("5"));
        assertFalse(callIsBackrefSyntaxUsed("9"));
        assertFalse(callIsBackrefSyntaxUsed("\\"));

        assertFalse(callIsBackrefSyntaxUsed(" \\"));
        assertFalse(callIsBackrefSyntaxUsed("a\\"));
        assertFalse(callIsBackrefSyntaxUsed("0\\"));
        assertFalse(callIsBackrefSyntaxUsed("1\\"));
        assertFalse(callIsBackrefSyntaxUsed("5\\"));
        assertFalse(callIsBackrefSyntaxUsed("9\\"));
        assertFalse(callIsBackrefSyntaxUsed("\\\\"));

        assertFalse(callIsBackrefSyntaxUsed("\\ "));
        assertFalse(callIsBackrefSyntaxUsed("\\a"));
        assertTrue(callIsBackrefSyntaxUsed("\\0"));
        assertTrue(callIsBackrefSyntaxUsed("\\1"));
        assertTrue(callIsBackrefSyntaxUsed("\\5"));
        assertTrue(callIsBackrefSyntaxUsed("\\9"));
        assertFalse(callIsBackrefSyntaxUsed("\\\\"));

        assertFalse(callIsBackrefSyntaxUsed("x\\ "));
        assertFalse(callIsBackrefSyntaxUsed("x\\a"));
        assertTrue(callIsBackrefSyntaxUsed("x\\0"));
        assertTrue(callIsBackrefSyntaxUsed("x\\1"));
        assertTrue(callIsBackrefSyntaxUsed("x\\5"));
        assertTrue(callIsBackrefSyntaxUsed("x\\9"));
        assertFalse(callIsBackrefSyntaxUsed("x\\\\"));

        assertFalse(callIsBackrefSyntaxUsed("\\ x"));
        assertFalse(callIsBackrefSyntaxUsed("\\ax"));
        assertTrue(callIsBackrefSyntaxUsed("\\0x"));
        assertTrue(callIsBackrefSyntaxUsed("\\1x"));
        assertTrue(callIsBackrefSyntaxUsed("\\5x"));
        assertTrue(callIsBackrefSyntaxUsed("\\9x"));
        assertFalse(callIsBackrefSyntaxUsed("\\\\x"));

        assertFalse(callIsBackrefSyntaxUsed("alpha\\beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha\\ beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha\\ 9beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha9\\beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha\\beta9gamma"));
        assertFalse(callIsBackrefSyntaxUsed("alpha9beta\\gamma"));

        assertTrue(callIsBackrefSyntaxUsed("\\9beta"));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\"));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\ "));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\\\"));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\gamma"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\ "));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\g"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\\\"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\gamma"));
    }

    private boolean callIsBackrefSyntaxUsed(String text) throws Exception {
        if (isBackrefSyntaxUsedMethod == null) {
            isBackrefSyntaxUsedMethod
                    = BasicSearchForm.class.getDeclaredMethod(
                                                        "isBackrefSyntaxUsed",
                                                        String.class);
            isBackrefSyntaxUsedMethod.setAccessible(true);
        }
        return Boolean.TRUE.equals(isBackrefSyntaxUsedMethod.invoke(null, text));
    }


}
