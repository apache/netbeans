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
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class NormalizationTestCase extends RemoteFileTestBase {

    public NormalizationTestCase(String testName) {
        super(testName);
    }
    
    public NormalizationTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testNormalizarion() throws Exception {
        FileObject stdio = getFileObject("/usr/include/stdio.h");
        FileObject usrInclude = stdio.getParent();
        FileObject usr = usrInclude.getParent();
        assertSame(stdio, usrInclude.getFileObject("stdio.h"));
        assertSame(usrInclude, usrInclude.getFileObject("."));
        assertSame(usrInclude, usrInclude.getFileObject("./"));
        assertSame(usrInclude, usrInclude.getFileObject(""));
        assertSame(usr, usrInclude.getFileObject("../"));
        assertSame(rootFO, usrInclude.getFileObject("../../."));
        assertSame(stdio, usr.getFileObject("include/stdio.h"));
        assertSame(stdio, usr.getFileObject("include/././stdio.h"));
        assertSame(stdio, usr.getFileObject("../usr/include/stdio.h"));
        assertSame(stdio, usr.getFileObject("../../../../../usr/include/stdio.h"));
        assertSame(stdio, usr.getFileObject("include/../include/../include/stdio.h"));
    }
    
    private void assertSame(FileObject fo1, FileObject fo2) throws Exception {
        assertEquals(fo1, fo2); // just to distingwish multiple instamce issues from more serious ones
        assertTrue(fo1 == fo2);
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(NormalizationTestCase.class);
    }
}
