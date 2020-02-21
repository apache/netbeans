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

package org.netbeans.modules.cnd.api.execution;

import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import java.io.File;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 */
public class LinkTestCase extends CndBaseTestCase {

    public LinkTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLink() throws Exception {
        File file = getDataFile("gcc.exe.lnk.data");
        String resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        if ("C:/util/cygwin/etc/alternatives/gcc".equals(resolved)) {
            // normal processing
        } else if ("C:/util/cygwin/bin/gcc-3.exe".equals(resolved) ||
                   "C:/util/cygwin/bin/gcc-4.exe".equals(resolved)) {
            // it is possible on real windows platform where exist "C:\\util\\cygwin\\etc\\alternatives\\gcc"
        } else {
            assertEquals("C:/util/cygwin/etc/alternatives/gcc", resolved); // NOI18N
        }
        file = getDataFile("gcc.lnk.data"); // NOI18N
        resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        assertEquals("C:/util/cygwin/bin/gcc-3.exe", resolved); // NOI18N
    }

    public void testCygwinLink() throws Exception {
        File file = getDataFile("g++.data"); // NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        assertEquals("/etc/alternatives/g++", resolved);
    }

    public void testCygwinLink2() throws Exception {
        File file = getDataFile("c++.exe.data"); // NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        String expected = file.getAbsolutePath().replace('\\', '/');
        int i = expected.lastIndexOf('/'); // NOI18N
        if (i > 0) {
            expected = expected.substring(0, i + 1) + "g++.exe";// NOI18N
        }
        assertEquals(expected, resolved);
    }

    public void testCygwinLink3() throws Exception {
        File file = getDataFile("f77.exe.data");// NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        String expected = file.getAbsolutePath().replace('\\', '/');
        int i = expected.lastIndexOf('/'); // NOI18N
        if (i > 0) {
            expected = expected.substring(0, i + 1) + "g77.exe";// NOI18N
        }
        assertEquals(expected, resolved);
    }

    public void testCygwinLink4() throws Exception {
        File file = getDataFile("cygwin1.7/bin/gcc.exe.data");// NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        String expected = getDataFile("cygwin1.7/etc/alternatives/gcc").getAbsolutePath().replace('\\', '/'); // NOI18N
        assertEquals(expected, resolved);
    }

    public void testCygwinLink5() throws Exception {
        File file = getDataFile("cygwin1.7/etc/alternatives/gcc.data");// NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getAbsolutePath()).replace('\\', '/');
        String expected = getDataFile("cygwin1.7/bin/gcc-4.exe").getAbsolutePath().replace('\\', '/'); // NOI18N
        assertEquals(expected, resolved);
    }
}
