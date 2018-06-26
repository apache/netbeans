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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class JBPluginUtilsTest extends NbTestCase {

    public JBPluginUtilsTest(String testName) {
        super(testName);
    }

    public void testVersion() {
        JBPluginUtils.Version version = new JBPluginUtils.Version("4.1.1.update"); // NOI18N
        assertEquals("4", version.getMajorNumber()); // NOI18N
        assertEquals("1", version.getMinorNumber()); // NOI18N
        assertEquals("1", version.getMicroNumber()); // NOI18N
        assertEquals("update", version.getUpdate()); // NOI18N

        JBPluginUtils.Version versionCmp1 = new JBPluginUtils.Version("4.1.1.update"); // NOI18N
        assertEquals(version, versionCmp1);
        assertEquals(0, version.compareTo(versionCmp1));
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp1));
        assertEquals(version.hashCode(), versionCmp1.hashCode());

        JBPluginUtils.Version versionCmp2 = new JBPluginUtils.Version("4.1.1"); // NOI18N
        assertTrue(version.compareTo(versionCmp2) > 0);
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp2));
    }
}
