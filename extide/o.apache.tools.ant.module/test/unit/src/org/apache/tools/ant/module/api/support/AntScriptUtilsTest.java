/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tools.ant.module.api.support;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AntScriptUtilsTest extends NbTestCase {

    public AntScriptUtilsTest(String name) {
        super(name);
    }

    private FileObject d;

    protected @Override void setUp() throws Exception {
        super.setUp();
        d = FileUtil.toFileObject(new File(getDataDir(), "antscriptutils"));
        assertNotNull(d);
    }

    public void testGetAntScriptName() throws Exception {
        assertEquals("correct name for test1.xml", "test1", AntScriptUtils.getAntScriptName(d.getFileObject("test1.xml")));
        assertEquals("no name for test2.xml", null, AntScriptUtils.getAntScriptName(d.getFileObject("test2.xml")));
        assertEquals("correct name for test3.xml", "test3", AntScriptUtils.getAntScriptName(d.getFileObject("test3.xml")));
        assertEquals("no name for test5.xml", null, AntScriptUtils.getAntScriptName(d.getFileObject("test5.xml")));
    }

    public void testGetAntScriptTargetNames() throws Exception {
        assertEquals("correct targets for test1.xml",
            Arrays.asList(new String[] {"another", "main", "other"}),
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test1.xml")));
        assertEquals("correct targets for test2.xml",
            Collections.singletonList("sometarget"),
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test2.xml")));
        assertEquals("correct targets for test3.xml",
            Arrays.asList(new String[] {"imported1", "imported2", "main"}),
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test3.xml")));
        try {
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test5.xml"));
            fail();
        } catch (IOException x) {/*OK*/}
    }

}
