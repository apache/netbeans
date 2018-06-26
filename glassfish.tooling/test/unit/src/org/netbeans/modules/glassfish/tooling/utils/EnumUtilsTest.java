/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.tooling.utils;

import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_4;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test enumeration utilities.
 * <p>
 * @author Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class EnumUtilsTest {
    
    
    /**
     * Test equals method.
     * Expected results:<ul>
     * <li>{@code a > b}: false</li>
     * <li>{@code a == b}: true</li>
     * <li>{@code a < b}: false</li>
     * <li>{@code a, null}: false</li>
     * <li>{@code null, b}: false</li>
     * <li>{@code null, null}: true</li></ul>
     */
    @Test
    public void testEq() {
        assertFalse(EnumUtils.eq(GF_4, GF_3), "Equals for a > b shall be false.");
        assertTrue(EnumUtils.eq(GF_4, GF_4), "Equals for a == b shall be true.");
        assertFalse(EnumUtils.eq(GF_3, GF_4), "Equals for a < b shall be false.");
        assertFalse(EnumUtils.eq(GF_3, null), "Equals for a, null shall be false.");
        assertFalse(EnumUtils.eq(null, GF_3), "Equals for null, b shall be false.");
        assertTrue(EnumUtils.eq(null, null), "Equals for null, null shall be true.");
    }

    /**
     * Test not equals method.
     * Expected results:<ul>
     * <li>{@code a > b}: true</li>
     * <li>{@code a == b}: false</li>
     * <li>{@code a < b}: true</li>
     * <li>{@code a, null}: true</li>
     * <li>{@code null, b}: true</li>
     * <li>{@code null, null}: false</li></ul>
     */
    @Test
    public void testNe() {
        assertTrue(EnumUtils.ne(GF_4, GF_3), "Not equals for a > b shall be true.");
        assertFalse(EnumUtils.ne(GF_4, GF_4), "Not equals for a == b shall be false.");
        assertTrue(EnumUtils.ne(GF_3, GF_4), "Not equals for a < b shall be true.");
        assertTrue(EnumUtils.ne(GF_3, null), "Not equals for a, null shall be true.");
        assertTrue(EnumUtils.ne(null, GF_3), "Not equals for null, b shall be true.");
        assertFalse(EnumUtils.ne(null, null), "Not equals for null, null shall be false.");
    }

    /**
     * Test less than method.
     * Expected results:<ul>
     * <li>{@code a > b}: false</li>
     * <li>{@code a == b}: false</li>
     * <li>{@code a < b}: true</li>
     * <li>{@code a, null}: false</li>
     * <li>{@code null, b}: true</li>
     * <li>{@code null, null}: false</li></ul>
     */
    @Test
    public void testLt() {
        assertFalse(EnumUtils.lt(GF_4, GF_3), "Less than for a > b shall be false.");
        assertFalse(EnumUtils.lt(GF_4, GF_4), "Less than for a == b shall be false.");
        assertTrue(EnumUtils.lt(GF_3, GF_4), "Less than for a < b shall be true.");
        assertFalse(EnumUtils.lt(GF_3, null), "Less than for a, null shall be false.");
        assertTrue(EnumUtils.lt(null, GF_3), "Less than for null, b shall be true.");
        assertFalse(EnumUtils.lt(null, null), "Less than for null, null shall be false.");
    }

    /**
     * Test less than or equal method.
     * Expected results:<ul>
     * <li>{@code a > b}: false</li>
     * <li>{@code a == b}: true</li>
     * <li>{@code a < b}: true</li>
     * <li>{@code a, null}: false</li>
     * <li>{@code null, b}: true</li>
     * <li>{@code null, null}: true</li></ul>
     */
    @Test
    public void testLe() {
        assertFalse(EnumUtils.le(GF_4, GF_3), "Less than or equal for a > b shall be false.");
        assertTrue(EnumUtils.le(GF_4, GF_4), "Less than or equal for a == b shall be true.");
        assertTrue(EnumUtils.le(GF_3, GF_4), "Less than or equal for a < b shall be true.");
        assertFalse(EnumUtils.le(GF_3, null), "Less than or equal for a, null shall be false.");
        assertTrue(EnumUtils.le(null, GF_3), "Less than or equal for null, b shall be true.");
        assertTrue(EnumUtils.le(null, null), "Less than or equal for null, null shall be true.");
    }

   /**
     * Test greater than method.
     * Expected results:<ul>
     * <li>{@code a > b}: true</li>
     * <li>{@code a == b}: false</li>
     * <li>{@code a < b}: false</li>
     * <li>{@code a, null}: true</li>
     * <li>{@code null, b}: false</li>
     * <li>{@code null, null}: false</li></ul>
     */
    @Test
    public void testGt() {
        assertTrue(EnumUtils.gt(GF_4, GF_3), "Greater than for a > b shall be true.");
        assertFalse(EnumUtils.gt(GF_4, GF_4), "Greater than for a == b shall be false.");
        assertFalse(EnumUtils.gt(GF_3, GF_4), "Greater than for a < b shall be false.");
        assertTrue(EnumUtils.gt(GF_3, null), "Greater than for a, null shall be true.");
        assertFalse(EnumUtils.gt(null, GF_3), "Greater than for null, b shall be false.");
        assertFalse(EnumUtils.gt(null, null), "Greater than for null, null shall be false.");
    }

    /**
     * Test greater than or equal method.
     * Expected results:<ul>
     * <li>{@code a > b}: true</li>
     * <li>{@code a == b}: true</li>
     * <li>{@code a < b}: false</li>
     * <li>{@code a, null}: true</li>
     * <li>{@code null, b}: false</li>
     * <li>{@code null, null}: true</li></ul>
     */
    @Test
    public void testGe() {
        assertTrue(EnumUtils.ge(GF_4, GF_3), "Greater than or equal for a > b shall be true.");
        assertTrue(EnumUtils.ge(GF_4, GF_4), "Greater than or equal for a == b shall be true.");
        assertFalse(EnumUtils.ge(GF_3, GF_4), "Greater than or equal for a < b shall be false.");
        assertTrue(EnumUtils.ge(GF_3, null), "Greater than or equal for a, null shall be true.");
        assertFalse(EnumUtils.ge(null, GF_3), "Greater than or equal for null, b shall be false.");
        assertTrue(EnumUtils.ge(null, null), "Greater than or equal for null, null shall be true.");
    }

}
