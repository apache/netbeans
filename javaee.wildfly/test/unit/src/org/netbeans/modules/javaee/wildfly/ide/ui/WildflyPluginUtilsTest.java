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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.wildfly.ide.ui;

import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class WildflyPluginUtilsTest extends NbTestCase {

    public WildflyPluginUtilsTest(String testName) {
        super(testName);
    }

    public void testVersion() {
        WildflyPluginUtils.Version version = new WildflyPluginUtils.Version("4.1.1.update", false); // NOI18N
        assertEquals("4", version.getMajorNumber()); // NOI18N
        assertEquals("1", version.getMinorNumber()); // NOI18N
        assertEquals("1", version.getMicroNumber()); // NOI18N
        assertEquals("update", version.getUpdate()); // NOI18N

        WildflyPluginUtils.Version versionCmp1 = new WildflyPluginUtils.Version("4.1.1.update", false); // NOI18N
        assertEquals(version, versionCmp1);
        assertEquals(0, version.compareTo(versionCmp1));
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp1));
        assertEquals(version.hashCode(), versionCmp1.hashCode());

        WildflyPluginUtils.Version versionCmp2 = new WildflyPluginUtils.Version("4.1.1", false); // NOI18N
        assertTrue(version.compareTo(versionCmp2) > 0);
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp2));
    }

    public void testComparison() {
        WildflyPluginUtils.Version version1 = new WildflyPluginUtils.Version("9.0.1", true); // NOI18N
        WildflyPluginUtils.Version version2 = new WildflyPluginUtils.Version("10.0.0", true); // NOI18N
        WildflyPluginUtils.Version version3 = new WildflyPluginUtils.Version("8.1.1", true); // NOI18N
        WildflyPluginUtils.Version eap7 = new WildflyPluginUtils.Version("7.0.0", false); // NOI18N

        assertTrue(version1.compareTo(version2) < 0);
        assertTrue(version2.compareTo(version1) > 0);
        assertTrue(version3.compareTo(version1) < 0);
        assertTrue(version1.compareTo(version3) > 0);
        assertTrue(version1.equals(version1));
        assertTrue(version2.equals(version2));
        assertEquals(version1.compareTo(version1), 0);
        assertEquals(version2.compareTo(version2), 0);
        assertEquals(version3.compareTo(version3), 0);
        assertEquals(version2.compareTo(new WildflyPluginUtils.Version("10.0.0", true)), 0); // NOI18N
        assertEquals(eap7.compareTo(new WildflyPluginUtils.Version("10.0.0", true)), 0); // NOI18N
        assertTrue(eap7.compareTo(WildflyPluginUtils.WILDFLY_8_0_0) > 0); // NOI18N
    }

}
