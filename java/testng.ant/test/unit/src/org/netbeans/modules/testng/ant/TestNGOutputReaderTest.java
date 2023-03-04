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
package org.netbeans.modules.testng.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.testng.ant.impl.ProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.testng.annotations.Test;
//14226
/**
 *
 * @author lukas
 */
@Test
public class TestNGOutputReaderTest extends NbTestCase {

    public TestNGOutputReaderTest(String name) {
        super(name);
    }

    public void testMsgLogged() throws IOException {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        Project p = new ProjectImpl(root, Lookups.fixed(new LineConvertors.FileLocator() {

            public FileObject find(String filename) {
                return null;
            }
        }));
        TestNGTestSession ts = new TestNGTestSession("UnitTest", p, TestSession.SessionType.TEST);
        TestNGOutputReader r = new TestNGOutputReader(ts);

        BufferedReader br = new BufferedReader(
                new FileReader(new File(getDataDir(), "antOut/log.txt")));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(RegexpUtils.TEST_LISTENER_PREFIX)) {
                r.verboseMessageLogged(line);
            }
        }
        assertEquals(23115, ts.getSessionResult().getElapsedTime());
        assertEquals(0, ts.getSessionResult().getErrors());
        assertEquals(0, ts.getSessionResult().getFailed());
        System.out.println(ts.getSessionResult().getPassed());
        System.out.println(ts.getSessionResult().getTotal());
    }
}
