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

package org.netbeans.upgrade;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;

/** Tests copying of attributes during upgrade when .nbattrs file is stored on the
 * local filesystem while the respective fileobject is stored on the XML filesystem.
 *
 * @author sherold
 */
public final class AutoUpgradeTest extends org.netbeans.junit.NbTestCase {
    public AutoUpgradeTest (String name) {
        super (name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
    }
    
    
    public void testDoUpgrade() throws Exception {
        File wrkDir = getWorkDir();
        clearWorkDir();
        File old = new File(wrkDir, "old");
        old.mkdir();
        File config = new File(old, "config");
        config.mkdir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(config);
        // filesystem must not be empty, otherwise .nbattrs file will be deleted :(
        lfs.getRoot().createFolder("test");
        
        String oldVersion = "foo";
        
        URL url = AutoUpgradeTest.class.getResource("layer" + oldVersion + ".xml");
        XMLFileSystem xmlfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = new MultiFileSystem(
                new FileSystem[] { lfs, xmlfs }
        );
        
        String fooBar = "/foo/bar";
        
        FileObject fooBarFO = mfs.findResource(fooBar);
        String attrName = "color";
        String attrValue = "black";
        fooBarFO.setAttribute(attrName, attrValue);
        
        System.setProperty("netbeans.user", new File(wrkDir, "new").getAbsolutePath());
        
        AutoUpgrade.doUpgrade(old, oldVersion);
        
        FileSystem dfs = FileUtil.getConfigRoot().getFileSystem();
        
        MultiFileSystem newmfs = new MultiFileSystem(
                new FileSystem[] { dfs, xmlfs }
        );
        
        FileObject newFooBarFO = newmfs.findResource(fooBar);
        assertNotNull(newFooBarFO);
        assertEquals(attrValue, newFooBarFO.getAttribute(attrName));
    }
    
    public void testComparatorUpgrade() throws Exception {
        // verify version ordering
        List<String> versions = Arrays.asList("12.3,12.4,8.0,12.4.301".split(",")).stream().sorted(AutoUpgrade.APACHE_VERSION_COMPARATOR.reversed()).collect(Collectors.toList());
        assertEquals(4,versions.size());
        assertEquals("12.4.301",versions.get(0));
        assertEquals("12.4",versions.get(1));
        assertEquals("12.3",versions.get(2));
        assertEquals("8.0",versions.get(3));
    }
    
 }
