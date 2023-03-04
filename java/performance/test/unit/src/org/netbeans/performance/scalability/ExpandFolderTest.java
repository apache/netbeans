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


package org.netbeans.performance.scalability;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

public class ExpandFolderTest extends NbTestCase implements Callable<Long> {
    private FileObject root;
    private AtomicLong len = new AtomicLong();
    public ExpandFolderTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        CountingSecurityManager.register();
        
        NbTestSuite s = new NbTestSuite();
        s.addTest(create(null, ".*"));
        s.addTest(create("ide[0-9]*|java[0-9]*", ".*"));
        s.addTest(create(".*", ".*"));
        s.addTest(new CompareResults(s));
        return s;
    }

    private static Test create(String clusters, String modules) {
        Configuration config = NbModuleSuite.createConfiguration(ExpandFolderTest.class)
            .clusters(clusters)
            .enableModules(modules)
            .honorAutoloadEager(true)
            .gui(false);
        return NbModuleSuite.create(config);
    }


    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        final File wd = getWorkDir().getCanonicalFile();
        
        File r = new File(wd, "dir");
        r.mkdirs();
        root = FileUtil.toFileObject(r);
        assertNotNull("Cannot find dir for " + wd + " exists: " + wd.exists(), root);

        int extPos = getName().indexOf("Ext");
        String ext = getName().substring(extPos + 3);

        for (int i = 0; i < 1000; i++) {
            new File(r, "empty" + i + "." + ext).createNewFile();
        }
    }
    
    public void testGetNodesForAFolderExtjava() throws Exception {
        CountingSecurityManager.initialize(getWorkDirPath());
        long now = System.currentTimeMillis();
        DataFolder f = DataFolder.findFolder(root);
        Node n = f.getNodeDelegate();
        Node[] arr = n.getChildren().getNodes(true);
        
        assertEquals("1000 nodes", 1000, arr.length);
        
        CountingSecurityManager.assertCounts("About 1000 * 4?", 4000, len);
    }

    public void testGetNodesForAFolderExtxml() throws Exception {
        CountingSecurityManager.initialize(getWorkDirPath());
        long now = System.currentTimeMillis();
        DataFolder f = DataFolder.findFolder(root);
        Node n = f.getNodeDelegate();
        Node[] arr = n.getChildren().getNodes(true);
        
        assertEquals("1000 nodes", 1000, arr.length);
        
        CountingSecurityManager.assertCounts("About 1000 * 11?", 11000, len);
    }

    public Long call() throws Exception {
        return len.longValue();
    }

    private static final class CompareResults extends NbTestCase {
        private final NbTestSuite suite;

        public CompareResults(NbTestSuite suite) {
            super("testCompareResults");
            this.suite = suite;
        }
        
        @Override
        public int countTestCases() {
            return 1;
        }

        @Override
        public void run(TestResult result) {
            result.startTest(this);
            StringBuffer times = new StringBuffer("\n");
            AtomicLong min = new AtomicLong(Long.MAX_VALUE);
            AtomicLong max = new AtomicLong(Long.MIN_VALUE);
            iterateTests(result, times, suite, min, max);
            
            System.err.println(times.toString());
            
            if (max.longValue() > 3 * min.longValue()) {
                result.addFailure(this, new AssertionFailedError(times.toString()));
            }
            result.endTest(this);
        }
        
        private void iterateTests(TestResult result, StringBuffer times, TestSuite suite, AtomicLong min, AtomicLong max) {
            Enumeration en = suite.tests();
            while (en.hasMoreElements()) {
                Test t = (Test)en.nextElement();
                if (t instanceof Callable) {
                    try {
                        Long v = (Long)((Callable) t).call();
                        long time = v.longValue();
                        if (time < min.longValue()) {
                            min.set(time);
                        }
                        if (time > max.longValue()) {
                            max.set(time);
                        }
                        // append(t.toString()).append(" value: ")
                        times.append("Run: ").append(v).append('\n');
                    } catch (Exception ex) {
                        result.addError(this, ex);
                    }
                }
                if (t instanceof TestSuite) {
                    iterateTests(result, times, (TestSuite)t, min, max);
                }
            }
        }
        
    }
}
