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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author Jaroslav Tulach
 */
public class PrintIconTest extends TestBase {

    public PrintIconTest(String testName) {
        super(testName);
    }

    // For some reason, TestBase.extractResource does not work in this test:
    private File extractCountedResource(String resource) throws Exception {
        File f;
        for (int i = 1; ; i++) {
             f = new File(getWorkDir(), i + "_" + resource.replaceFirst(".+/", ""));
             if (!f.isFile()) {
                 break;
             }
        }
        try (OutputStream os = new FileOutputStream(f);
                InputStream is = PrintIconTest.class.getResourceAsStream(resource)) {
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
        }
        return f;
    }

    public void testPrintOutSameIcons() throws Exception {
        File img = extractCountedResource("data/instanceBroken.gif");
        File img2 = extractCountedResource("data/instanceObject.gif");
        File img3 = extractCountedResource("data/instanceBroken.gif");
        File out = extractString("");
        out.delete();
        
        File f = extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nbantext.jar}'/>" +
            "<target name='all' >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "    </firstpool>" +
            "    <secondpool dir='" + img3.getParent() + "'>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </secondpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = readFile(out);
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file, 6, threeParts.length);

        {
            long hash = Long.parseLong(threeParts[0], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURI().toString(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURI().toString(), threeParts[5]);
        }
        
    }
    
    
    public void testDuplicatesFromTheSameSet() throws Exception {
        File img = extractCountedResource("data/instanceBroken.gif");
        File img2 = extractCountedResource("data/instanceObject.gif");
        File img3 = extractCountedResource("data/instanceBroken.gif");
        File out = extractString("");
        out.delete();
        
        File f = extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nbantext.jar}'/>" +
            "<target name='all' >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </firstpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = readFile(out);
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file, 6, threeParts.length);

        {
            long hash = Long.parseLong(threeParts[0], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURI().toString(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURI().toString(), threeParts[5]);
        }
        
    }

    public void testBrokenImageThatCould() throws Exception {
        doBrokenImageTest("data/columnIndex.gif");
    }
    public void testBrokenImageThatCould2() throws Exception {
        doBrokenImageTest("data/Category.png");
    }
    
    private void doBrokenImageTest(String res) throws Exception {
        File img = extractCountedResource(res);
        File img2 = extractCountedResource("data/instanceObject.gif");
        File img3 = extractCountedResource(res);
        File out = extractString("");
        out.delete();
        
        File f = extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nbantext.jar}'/>" +
            "<target name='all' >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </firstpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = readFile(out);
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file + " " + Arrays.toString(threeParts), 6, threeParts.length);

        long prevHash;
        {
            prevHash = Long.parseLong(threeParts[0], 16);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURI().toString(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is the same:\n" + file, prevHash, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURI().toString(), threeParts[5]);
        }
        
    }
    
    public void testPrintExtra() throws Exception {
        File img = extractCountedResource("data/instanceBroken.gif");
        File img2 = extractCountedResource("data/instanceObject.gif");
        File img3 = extractCountedResource("data/instanceBroken.gif");
        File out = extractString("");
        out.delete();
        
        File f = extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nbantext.jar}'/>" +
            "<target name='all' >" +
            "  <printicon difference='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "    </firstpool>" +
            "    <secondpool dir='" + img3.getParent() + "'>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </secondpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = readFile(out);
        
        if (!file.startsWith("-")) {
            fail("Should start with - as one icon is missing in new version:\n" + file);
        } else {
            file = file.substring(1);
        }
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file, 3, threeParts.length);
        
        long hash = Long.parseLong(threeParts[0], 16);
        assertEquals("Hash code is 10ba4f25:\n" + file, 0x10ba4f25L, hash);
        assertEquals("Name is from img2:\n" + file, img2.getName(), threeParts[1]);
        assertEquals("Full name is img2:\n" + file, img2.toURI().toString(), threeParts[2]);
    }
}
