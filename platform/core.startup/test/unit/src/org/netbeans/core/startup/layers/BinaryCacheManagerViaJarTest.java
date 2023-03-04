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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.JarClassLoader;
import org.netbeans.Stamps;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/** Test layer cache manager over real JAR file.
 */
public class BinaryCacheManagerViaJarTest extends BinaryCacheManagerTest {
    private JarClassLoader jarCL;
    
    public BinaryCacheManagerViaJarTest(String name) {
        super(name);
    }

    @Override
    protected URL loadResource(String name) throws IOException {
        if (jarCL == null) {
            List<File> arr = Collections.singletonList(NonCacheManagerViaJarTest.generateJAR(this));
            jarCL = new JarClassLoader(arr, new ClassLoader[0]);
        }
        return jarCL.getResource(name);
    }
    
    public void testAllLastModifiedCheck() throws Exception {
        clearWorkDir();
        LayerCacheManager m = createManager();
        // layer2.xml should override layer1.xml where necessary:
        List<URL> urls = Arrays.asList(
                loadResource("data/layer2.xml"),
                loadResource("data/layer1.xml"));
        FileSystem f = BinaryCacheManagerTest.store(m, urls);
        
        Enumeration<? extends FileObject> en = f.getRoot().getChildren(true);
        final long myTime = Stamps.getModulesJARs().lastModified();
        if (myTime <= 0) {
            fail("Something is wrong, stamps are not valid: " + myTime);
        }
        
        CountingSecurityManager.initialize(getWorkDirPath(), CountingSecurityManager.Mode.CHECK_READ, null);
        CountingSecurityManager.acceptAll = true;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            final long time = fo.lastModified().getTime();
            assertEquals("Time stamp is like Stamps: " + fo, myTime, time);
        }
        CountingSecurityManager.assertCounts("No reads at all", 0);
    }
}
