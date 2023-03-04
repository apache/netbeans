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
package org.netbeans.api.java.source;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.tools.Diagnostic;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

public class TextBlocksOnAnyJavaTest extends NbTestCase {

    public TextBlocksOnAnyJavaTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(TextBlocksOnAnyJavaTest.class).
            gui(false).
            suite();
    }

    public void testTextBlockParsesWithoutErrors() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject java = fs.getRoot().createData("TextBlock.java");
        Writer os = new OutputStreamWriter(java.getOutputStream());
        os.write("class TextBlock {\n"
                + "  static {\n"
                + "    if (true) throw new IllegalStateException(\"\"\"\n"
                + "    OK\n"
                + "    \"\"\");\n"
                + "  }\n"
                + "}\n");
        os.close();
        MockSourceLevelQuery.register(java, "15");
        JavaSource source = JavaSource.forFileObject(java);
        CountDownLatch cdl = new CountDownLatch(1);
        int[] count = { - 1 };
        String[] msg = { "No message" };
        source.runUserActionTask((comp) -> {
            try {
                comp.toPhase(JavaSource.Phase.RESOLVED);
                List<Diagnostic> diag = comp.getDiagnostics();
                count[0] = diag.size();
                msg[0] = diag.toString();
            } finally {
                cdl.countDown();
            }
        }, true);
        cdl.await(30, TimeUnit.SECONDS);
        assertEquals(msg[0], 0, count[0]);
    }
}
