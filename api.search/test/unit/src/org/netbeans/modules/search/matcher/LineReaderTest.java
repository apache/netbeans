/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.matcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
        Charset chs = Charset.forName("UTF-8");
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
