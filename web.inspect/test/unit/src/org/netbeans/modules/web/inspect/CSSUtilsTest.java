/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests of class {@code CSSUtils}.
 *
 * @author Jan Stola
 */
public class CSSUtilsTest {

    /**
     * Test of {@code isInheritedProperty} method.
     */
    @Test
    public void testIsInheritedProperty1() {
        String name = "color"; // NOI18N
        boolean expResult = true;
        boolean result = CSSUtils.isInheritedProperty(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritedProperty} method.
     */
    @Test
    public void testIsInheritedProperty2() {
        String name = "border"; // NOI18N
        boolean expResult = false;
        boolean result = CSSUtils.isInheritedProperty(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritedProperty} method.
     */
    @Test
    public void testIsInheritedProperty3() {
        String name = "someNonExistentProperty"; // NOI18N
        boolean expResult = false;
        boolean result = CSSUtils.isInheritedProperty(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritValue} method.
     */
    @Test
    public void testIsInheritValue1() {
        String value = "inherit"; // NOI18N
        boolean expResult = true;
        boolean result = CSSUtils.isInheritValue(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritValue} method.
     */
    @Test
    public void testIsInheritValue2() {
        String value = "black"; // NOI18N
        boolean expResult = false;
        boolean result = CSSUtils.isInheritValue(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeSelector} method.
     */
    @Test
    public void testNormalizeSelector1() {
        String selector = "  div "; // NOI18N
        String expResult = "div"; // NOI18N
        String result = CSSUtils.normalizeSelector(selector);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeSelector} method.
     */
    @Test
    public void testNormalizeSelector2() {
        String selector = "  h1     ,   div "; // NOI18N
        String expResult = "h1,div"; // NOI18N
        String result = CSSUtils.normalizeSelector(selector);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeMediaQuery} method.
     */
    @Test
    public void testNormalizeMediaQuery1() {
        String mediaQueryList = "  only    screen "; // NOI18N
        String expResult = "only screen"; // NOI18N
        String result = CSSUtils.normalizeMediaQuery(mediaQueryList);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeMediaQuery} method.
     */
    @Test
    public void testNormalizeMediaQuery2() {
        String mediaQueryList1 = "(min-width: 768px) and (max-width: 979px)"; // NOI18N
        String mediaQueryList2 = "(max-width: 979px) and (min-width: 768px)"; // NOI18N
        String result1 = CSSUtils.normalizeMediaQuery(mediaQueryList1);
        String result2 = CSSUtils.normalizeMediaQuery(mediaQueryList2);
        assertEquals(result1, result2);
    }

    /**
     * Test of {@code normalizeMediaQuery} method.
     */
    @Test
    public void testNormalizeMediaQuery3() {
        String mediaQueryList1 = "only screen and (min-width: 768px) and (max-width: 979px)"; // NOI18N
        String mediaQueryList2 = "only screen and (  max-width:   979px  )   and  (min-width: 768px)"; // NOI18N
        String result1 = CSSUtils.normalizeMediaQuery(mediaQueryList1);
        String result2 = CSSUtils.normalizeMediaQuery(mediaQueryList2);
        assertEquals(result1, result2);
    }

}
