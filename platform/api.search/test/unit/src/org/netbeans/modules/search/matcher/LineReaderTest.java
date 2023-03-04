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
package org.netbeans.modules.search.matcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class LineReaderTest extends NbTestCase {

    public LineReaderTest(String name) {
        super(name);
    }

    public void testLineReader() throws IOException {

        FileSystem fs = FileUtil.createMemoryFileSystem();
        Charset chs = StandardCharsets.UTF_8;
        OutputStream os = fs.getRoot().createAndOpen("find.txt");
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, chs.newEncoder());
            try {
                osw.write("Text on line 1.");
                osw.write("Line 1 has some more text included!\r\n");
                osw.write("Not matching line.\r\n");
                osw.write("Line 4 contains text\n");
                osw.write("\n");
            } finally {
                osw.flush();
                osw.close();
            }
        } finally {
            os.flush();
            os.close();
        }

        FileObject fo = fs.getRoot().getFileObject("find.txt");
        LineReader lr = new LineReader(chs.newDecoder(), fo.getInputStream());
        try {
            LineReader.LineInfo li = lr.readNext();
            assertEquals("Text on line 1.Line 1 has some more text included!",
                    li.getString());
            assertEquals(0, li.getFileStart());
            assertEquals(50, li.getFileEnd());
            assertEquals(1, li.getNumber());
            assertEquals(50, li.getLength());

            li = lr.readNext();
            assertEquals("Not matching line.",
                    li.getString());
            assertEquals(52, li.getFileStart());
            assertEquals(70, li.getFileEnd());
            assertEquals(2, li.getNumber());
            assertEquals(18, li.getLength());

            li = lr.readNext();
            assertEquals("Line 4 contains text",
                    li.getString());
            assertEquals(72, li.getFileStart());
            assertEquals(92, li.getFileEnd());
            assertEquals(3, li.getNumber());
            assertEquals(20, li.getLength());

            li = lr.readNext();
            assertEquals("",
                    li.getString());
            assertEquals(93, li.getFileStart());
            assertEquals(93, li.getFileEnd());
            assertEquals(4, li.getNumber());
            assertEquals(0, li.getLength());

            li = lr.readNext();
            assertNull(li);
        } finally {
            lr.close();
        }
    }
}
