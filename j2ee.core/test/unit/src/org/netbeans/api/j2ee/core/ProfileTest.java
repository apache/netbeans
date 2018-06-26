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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.api.j2ee.core;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ProfileTest extends NbTestCase {

    public ProfileTest(String name) {
        super(name);
    }

    public void testFromPropertiesString() {
        assertEquals(Profile.J2EE_13, Profile.fromPropertiesString("1.3"));
        assertEquals(Profile.J2EE_14, Profile.fromPropertiesString("1.4"));
        assertEquals(Profile.JAVA_EE_5, Profile.fromPropertiesString("1.5"));
        assertEquals(Profile.JAVA_EE_6_FULL, Profile.fromPropertiesString("1.6"));
        assertEquals(Profile.JAVA_EE_6_FULL, Profile.fromPropertiesString("EE_6_FULL"));
        assertEquals(Profile.JAVA_EE_6_WEB, Profile.fromPropertiesString("1.6-web"));
        assertEquals(Profile.JAVA_EE_6_WEB, Profile.fromPropertiesString("EE_6_WEB"));
        assertNull(Profile.fromPropertiesString("something"));
    }

    public void testIsHigherJavaEEVersionJavaEE5() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_5));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_5));

        assertTrue(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_5));
    }

    public void testIsHigherJavaEEVersionJavaEE6full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_6_WEB));

        assertTrue(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertTrue(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertTrue(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertTrue(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_6_WEB));
    }

    public void testIsHigherJavaEEVersionJavaEE7full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_7_WEB));

        assertFalse(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertFalse(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertTrue(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertTrue(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_7_WEB));
    }

}
