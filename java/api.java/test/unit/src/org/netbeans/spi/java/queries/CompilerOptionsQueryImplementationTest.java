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
package org.netbeans.spi.java.queries;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CompilerOptionsQueryImplementationTest extends NbTestCase {

    public CompilerOptionsQueryImplementationTest(String name) {
        super(name);
    }

    @Test
    public void testArgumentFiles() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject argfile = FileUtil.createData(wd, "argfile");
        try (OutputStream out = argfile.getOutputStream()) {
            out.write("test \t\t \"quoted1\"   'quoted2'\n   \t\n   @argfile\n".getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(Arrays.asList("test", "quoted1", "quoted2", "@argfile"),
                     Result.doParseLine("@argfile", wd.toURI()));
        assertEquals(Arrays.asList("@argfile"),
                     Result.doParseLine("@argfile", null));
        assertEquals(Arrays.asList("prefix@argfile"),
                     Result.doParseLine("prefix@argfile", wd.toURI()));
        assertEquals(Arrays.asList("@@"),
                     Result.doParseLine("@@", wd.toURI()));
        assertEquals(Arrays.asList("@"),
                     Result.doParseLine("@", wd.toURI()));
        assertEquals(Arrays.asList("test", "quoted1", "quoted2", "@argfile"),
                     Result.doParseLine("@" + FileUtil.toFile(argfile).getAbsolutePath(), wd.toURI()));
        assertEquals(Arrays.asList("@nonexistent"),
                     Result.doParseLine("@nonexistent", wd.toURI()));
    }

}
