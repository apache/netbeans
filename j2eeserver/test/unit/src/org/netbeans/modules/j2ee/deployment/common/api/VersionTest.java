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
package org.netbeans.modules.j2ee.deployment.common.api;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class VersionTest extends NbTestCase {

    public VersionTest(String name) {
        super(name);
    }

    public void testJsr277() {
        Version version = Version.fromJsr277NotationWithFallback("10.3.4");
        assertEquals("10.3.4", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277NotationWithFallback("something");
        assertEquals("something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277NotationWithFallback("10.3.4.5-something");
        assertEquals("10.3.4.5-something", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("something", version.getQualifier());

        version = Version.fromJsr277NotationWithFallback("10.3.4.5.6");
        assertEquals("10.3.4.5.6", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());
    }

    public void testDotted() {
        Version version = Version.fromDottedNotationWithFallback("10.3.4");
        assertEquals("10.3.4", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromDottedNotationWithFallback("something");
        assertEquals("something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromDottedNotationWithFallback("10.3.4.5-something");
        assertEquals("10.3.4.5-something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromDottedNotationWithFallback("10.3.4.5.6");
        assertEquals("10.3.4.5.6", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("6", version.getQualifier());
    }

    public void testJsr277OrDotted() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertEquals("10.3.4", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277OrDottedNotationWithFallback("something");
        assertEquals("something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4.5-something");
        assertEquals("10.3.4.5-something", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("something", version.getQualifier());

        version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4.5.6");
        assertEquals("10.3.4.5.6", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("6", version.getQualifier());
    }

    public void testAboveOrEqual() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertTrue(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10")));
        assertTrue(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3")));
        assertTrue(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3.4")));
        assertFalse(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.4")));
    }

    public void testBelowOrEqual() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertTrue(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.4.5")));
        assertTrue(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.4")));
        assertTrue(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3.4")));
        assertFalse(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3")));
    }

    public void testEqualsAndHashCode() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertEquals(version, Version.fromJsr277OrDottedNotationWithFallback("10.3.4"));
        assertEquals(version.hashCode(), Version.fromJsr277OrDottedNotationWithFallback("10.3.4").hashCode());

        assertFalse(version.equals(Version.fromJsr277OrDottedNotationWithFallback("10.3")));
    }
}
