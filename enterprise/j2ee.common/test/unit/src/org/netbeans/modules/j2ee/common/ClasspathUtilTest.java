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

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class ClasspathUtilTest extends NbTestCase {
    
    /** Creates a new instance of UtilTest */
    public ClasspathUtilTest(String testName) {
        super(testName);
    }

    public void testContainsClass() throws IOException {
        File dataDir = getDataDir();
        File[] classpath1 = new File[] { 
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/libs/org.netbeans.test.dbdriver.jar") 
        }; 
        File[] classpath2 = new File[] { 
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/classes") ,
            new File(dataDir, "testcp/shared/classes"), 
        };
        
        List<URL> urlClasspath1 = new LinkedList<URL>();
        urlClasspath1.add(FileUtil.getArchiveRoot(classpath1[0].toURI().toURL()));
        urlClasspath1.add(FileUtil.getArchiveRoot(classpath1[1].toURI().toURL()));
        
        List<URL> urlClasspath2 = new LinkedList<URL>();
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[0].toURI().toURL()));
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[1].toURI().toURL()));
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[2].toURI().toURL()));
        
        
        assertFalse(ClasspathUtil.containsClass(Arrays.asList(classpath1), "com.mysql.Driver"));
        assertFalse(ClasspathUtil.containsClass(Arrays.asList(classpath2), "com.mysql.Driver"));
        assertFalse(ClasspathUtil.containsClass(urlClasspath1, "com.mysql.Driver"));
        assertFalse(ClasspathUtil.containsClass(urlClasspath2, "com.mysql.Driver"));
        
        // the driver is in the jar file
        assertTrue(ClasspathUtil.containsClass(Arrays.asList(classpath1), "org.netbeans.test.db.driver.TestDriver"));
        assertTrue(ClasspathUtil.containsClass(urlClasspath1, "org.netbeans.test.db.driver.TestDriver"));
        // the driver is among the classes
        assertTrue(ClasspathUtil.containsClass(Arrays.asList(classpath2), "org.netbeans.test.db.driver.TestDriver"));
        assertTrue(ClasspathUtil.containsClass(urlClasspath2, "org.netbeans.test.db.driver.TestDriver"));
    }

    public void testContainsClasses() throws IOException {
        File dataDir = getDataDir();
        File[] classpath1 = new File[] {
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/libs/org.netbeans.test.dbdriver.jar")
        };
        File[] classpath2 = new File[] {
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/classes") ,
            new File(dataDir, "testcp/shared/classes"),
        };

        List<URL> urlClasspath1 = new LinkedList<URL>();
        urlClasspath1.add(FileUtil.getArchiveRoot(classpath1[0].toURI().toURL()));
        urlClasspath1.add(FileUtil.getArchiveRoot(classpath1[1].toURI().toURL()));

        List<URL> urlClasspath2 = new LinkedList<URL>();
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[0].toURI().toURL()));
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[1].toURI().toURL()));
        urlClasspath2.add(FileUtil.getArchiveRoot(classpath2[2].toURI().toURL()));


        assertNull(ClasspathUtil.containsClass(Arrays.asList(classpath1), Collections.singletonMap(true, "com.mysql.Driver")));
        assertNull(ClasspathUtil.containsClass(Arrays.asList(classpath2), Collections.singletonMap(true, "com.mysql.Driver")));
        assertNull(ClasspathUtil.containsClass(urlClasspath1, Collections.singletonMap(true, "com.mysql.Driver")));
        assertNull(ClasspathUtil.containsClass(urlClasspath2, Collections.singletonMap(true, "com.mysql.Driver")));

        // the driver is in the jar file
        assertTrue(ClasspathUtil.containsClass(Arrays.asList(classpath1), Collections.singletonMap(true, "org.netbeans.test.db.driver.TestDriver")));
        assertTrue(ClasspathUtil.containsClass(urlClasspath1, Collections.singletonMap(true, "org.netbeans.test.db.driver.TestDriver")));
        // the driver is among the classes
        assertTrue(ClasspathUtil.containsClass(Arrays.asList(classpath2), Collections.singletonMap(true, "org.netbeans.test.db.driver.TestDriver")));
        assertTrue(ClasspathUtil.containsClass(urlClasspath2, Collections.singletonMap(true, "org.netbeans.test.db.driver.TestDriver")));

        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("org.netbeans.test.db.driver.TestDriver", "org.netbeans.test.db.driver.TestDriver");
        map.put("org.netbeans.test.db.driver.B", "org.netbeans.test.db.driver.B");

        // found test driver not B
        assertEquals("org.netbeans.test.db.driver.TestDriver", ClasspathUtil.containsClass(Arrays.asList(classpath1), map));
        assertEquals("org.netbeans.test.db.driver.TestDriver", ClasspathUtil.containsClass(urlClasspath1, map));

        map.clear();
        map.put("org.netbeans.test.db.driver.B", "org.netbeans.test.db.driver.B");
        map.put("org.netbeans.test.db.driver.TestDriver", "org.netbeans.test.db.driver.TestDriver");

        // found B not test driver
        assertEquals("org.netbeans.test.db.driver.B", ClasspathUtil.containsClass(Arrays.asList(classpath1), map));
        assertEquals("org.netbeans.test.db.driver.B", ClasspathUtil.containsClass(urlClasspath1, map));

        map.clear();
        map.put("org.netbeans.test.db.driver.C", "org.netbeans.test.db.driver.C");
        map.put("org.netbeans.test.db.driver.B", "org.netbeans.test.db.driver.B");
        map.put("org.netbeans.test.db.driver.TestDriver", "org.netbeans.test.db.driver.TestDriver");

        // found B
        assertEquals("org.netbeans.test.db.driver.B", ClasspathUtil.containsClass(Arrays.asList(classpath1), map));
        assertEquals("org.netbeans.test.db.driver.B", ClasspathUtil.containsClass(urlClasspath1, map));
    }
    
}
