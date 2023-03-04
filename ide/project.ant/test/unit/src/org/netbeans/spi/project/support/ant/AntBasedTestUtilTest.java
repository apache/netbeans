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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.CollocationQueryImplementation2;

/**
 * Test functionality of AntBasedTestUtil itself.
 * @author Jesse Glick
 */
public class AntBasedTestUtilTest extends NbTestCase {

    static {
        AntBasedTestUtilTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }

    /**
     * Create a new test suite.
     * @param name the suite name
     */
    public AntBasedTestUtilTest(String name) {
        super(name);
    }

    /**
     * Check that reported text diffs are actually accurate.
     * @throws Exception in case of an unexpected error
     */
    public void testCountTextDiffs() throws Exception {
        String f1 =
            "one\n" +
            "two\n" +
            "three\n" +
            "four\n" +
            "five\n" +
            "six\n" +
            "seven\n" +
            "eight\n" +
            "nine\n" +
            "ten\n";
        String f2 =
            "one\n" +
            // two deleted
            // three deleted
            "four\n" +
            "four #2\n" + // added
            "four #3\n" + // added
            "five\n" +
            "six six six\n" + // modified
            "sevvin'!\n" + // modified
            "eight\n" +
            "najn\n" + // modified
            "ten\n" +
            "ten #2\n"; // added
        int[] count = AntBasedTestUtil.countTextDiffs(new StringReader(f1), new StringReader(f2));
        assertEquals("should have three entries", 3, count.length);
        assertEquals("three lines modified", 3, count[0]);
        assertEquals("three lines added", 3, count[1]);
        assertEquals("two lines deleted", 2, count[2]);
    }
    
    public void testTestCollocationQueryImplementation() throws Exception {
        URI root = URI.create("file:/tmp/");
        assertTrue("using absolute root " + root, root.isAbsolute());
        CollocationQueryImplementation2 cqi = AntBasedTestUtil.testCollocationQueryImplementation(root);
        URI f1 = root.resolve("f1");
        URI f2 = root.resolve("f2");
        URI d1f1 = root.resolve("d1/f1");
        URI d2f1 = root.resolve("d2/f1");
        URI s = root.resolve("separate/");
        URI s1 = s.resolve("s1");
        URI s2 = s.resolve("s2");
        URI t = root.resolve("transient/");
        URI t1 = t.resolve("t1");
        URI t2 = t.resolve("t2");
        assertTrue("f1 & f2 collocated", cqi.areCollocated(f1, f2));
        assertTrue("f1 & f2 collocated (reverse)", cqi.areCollocated(f2, f1));
        assertTrue("d1f1 & d2f1 collocated", cqi.areCollocated(d1f1, d2f1));
        assertTrue("s1 & s2 collocated", cqi.areCollocated(s1, s2));
        assertTrue("s & s1 collocated", cqi.areCollocated(s, s1));
        assertFalse("t1 & t2 not collocated", cqi.areCollocated(t1, t2));
        assertFalse("f1 & t1 not collocated", cqi.areCollocated(f1, t1));
        assertFalse("f1 & s1 not collocated", cqi.areCollocated(f1, s1));
        assertFalse("s1 & t1 not collocated", cqi.areCollocated(s1, t1));
        assertEquals("right root for f1", root, cqi.findRoot(f1));
        assertEquals("right root for f2", root, cqi.findRoot(f2));
        assertEquals("right root for d1f1", root, cqi.findRoot(d1f1));
        assertEquals("right root for d2f1", root, cqi.findRoot(d2f1));
        assertEquals("right root for s", s, cqi.findRoot(s));
        assertEquals("right root for s1", s, cqi.findRoot(s1));
        assertEquals("right root for s2", s, cqi.findRoot(s2));
        assertEquals("right root for t", null, cqi.findRoot(t));
        assertEquals("right root for t1", null, cqi.findRoot(t1));
        assertEquals("right root for t2", null, cqi.findRoot(t2));
    }
    
    public void testReplaceInFile() throws Exception {
        clearWorkDir();
        File workdir = getWorkDir();
        File props = new File(workdir, "test.properties");
        Properties p = new Properties();
        p.setProperty("key1", "val1");
        p.setProperty("key2", "val2");
        OutputStream os = new FileOutputStream(props);
        try {
            p.store(os, null);
        } finally {
            os.close();
        }
        assertEquals("two replacements", 2, AntBasedTestUtil.replaceInFile(props, "val", "value"));
        p.clear();
        InputStream is = new FileInputStream(props);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("correct key1", "value1", p.getProperty("key1"));
        assertEquals("correct key2", "value2", p.getProperty("key2"));
    }
    
}
