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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.netbeans.JarClassLoader;
import org.netbeans.junit.NbTestCase;

/** Test absence of layer cache manager on top of a JAR.
 */
public class NonCacheManagerViaJarTest extends NonCacheManagerTest {
    private JarClassLoader jarCL;
    public NonCacheManagerViaJarTest(String name) {
        super(name);
    }

    @Override
    protected URL loadResource(String name) throws IOException {
        if (jarCL == null) {
            List<File> arr = Collections.singletonList(generateJAR(this));
            jarCL = new JarClassLoader(arr, new ClassLoader[0]);
        }
        return jarCL.getResource(name);
    }

    
    public static File generateJAR(NbTestCase test) throws IOException {
        File config = new File(new File(test.getWorkDir(), "config"), "Modules");
        config.mkdirs();
        File xml = new File(config, test.getName() + ".xml");
        xml.createNewFile();
        File modules = new File(test.getWorkDir(), "modules");
        modules.mkdirs();
        File jar = new File(modules, test.getName() + ".jar");
        if (!jar.exists()) {
            File layers = new File(test.getDataDir(), "layers");
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar));
            dumpDir(jos, "", layers);
            jos.close();
        }
        return jar;
    }
    
    private static void dumpDir(JarOutputStream jos, String path, File dir) throws IOException {
        assertTrue("Dir is dir " + dir, dir.isDirectory());
        for (File ch : dir.listFiles()) {
            if (ch.isDirectory()) {
                dumpDir(jos, path + ch.getName() + "/", ch);
                continue;
            }
            jos.putNextEntry(new JarEntry(path + ch.getName()));
            byte[] arr = new byte[4092];
            FileInputStream is = new FileInputStream(ch);
            for (;;) {
                int len = is.read(arr);
                if (len == -1) {
                    break;
                }
                jos.write(arr, 0, len);
            }
            jos.closeEntry();
            is.close();
        }
    }
}
