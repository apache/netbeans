/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
