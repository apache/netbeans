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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.mx.project.suitepy.MxDistribution;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;

public final class ParseSuitesTest extends NbTestCase {

    public ParseSuitesTest(String name) {
        super(name);
    }

    public void testParseThemAll() throws IOException {
        assertSuitePys(getDataDir(), 15);
    }

    public void testParseMultiLineSuite() throws IOException {
        URL url = getClass().getResource("multilinetexts.py");
        MxSuite suite = MxSuite.parse(url);
        assertNotNull("suite parsed", suite);
        MxDistribution tool = suite.distributions().get("TOOLCHAIN");
        assertNotNull("toolchain found", tool);
        assertEquals("No deps", 0, tool.dependencies().size());
    }

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            throw new IOException("Specify parameter to directory with suite.py files you want to verify!");
        }
        assertSuitePys(new File(args[0]), -1);
    }

    private static void assertSuitePys(final File dir, final int expected) throws IOException {
        StringWriter s = new StringWriter();
        PrintWriter pw = new PrintWriter(s);
        int cnt[] = { 0 };
        int error[] = { 0 };
        Files.walkFileTree(dir.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                if (!t.getFileName().toString().equals("suite.py")) {
                    return FileVisitResult.CONTINUE;
                }
                cnt[0]++;
                try {
                    pw.println("Parsing " + t);
                    MxSuite.parse(t.toUri().toURL());
                } catch (IOException ex) {
                    error[0]++;
                    pw.println("Failure " + t);
                    ex.printStackTrace(pw);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
                throw new AssertionError("Failed at " + t, ioe);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        assertEquals(s.toString(), 0, error[0]);
        if (expected >= 0) {
            assertEquals(s.toString(), expected, cnt[0]);
        }
    }
}
