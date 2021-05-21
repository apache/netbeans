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
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.TestFileUtils;

public class ProxyClassPackagesTest extends NbTestCase {

    public ProxyClassPackagesTest(String name) {
        super(name);
    }
    
    public void testMetaInfServicesPackageNotListedByDefault() throws Exception {
        File jar = new File(getWorkDir(), "non-empty.jar");
        TestFileUtils.writeZipFile(jar, 
            "META-INF/services/java.lang.Runnable:1"
        );
        JarClassLoader l1 = new JarClassLoader(Collections.nCopies(1, jar), new ClassLoader[] {
            ClassLoader.getSystemClassLoader()
        });
        
        Set<ProxyClassLoader> set = ProxyClassPackages.findCoveredPkg("META-INF.services");
        assertNull("No JAR covers META-INF.services: " + set, set);

        ProxyClassLoader l2 = new ProxyClassLoader(new ProxyClassLoader[] { l1 }, true);

        Enumeration<URL> en = l2.getResources("META-INF/services/java.lang.Runnable");
        assertTrue("Some are there", en.hasMoreElements());
        URL one = en.nextElement();
        assertFalse("And that is all", en.hasMoreElements());
        
        URL alternative = l2.getResource("META-INF/services/java.lang.Runnable");
        assertEquals("Same URL", one, alternative);
        
        ProxyClassPackages.removeCoveredPakcages(l1);
    }
    
    public void testQueriesAllThatDeclareMetaInf() throws Exception {
        File cover = new File(getWorkDir(), "enlists-services.jar");
        TestFileUtils.writeZipFile(cover, 
            "META-INF/services/java.io.Serializable:2",
            "META-INF/MANIFEST.MF:Covered-Packages=META-INF.services"
        );
        final URL[] arr = new URL[] { cover.toURI().toURL() };
        ProxyClassLoader l1 = new ProxyClassLoader(new ClassLoader[] {new URLClassLoader(
            arr, ClassLoader.getSystemClassLoader()
        )}, false) {
            {
                super.addCoveredPackages(Collections.singleton("META-INF.services"));
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                return new URLClassLoader(arr).getResources(name);
            }

            @Override
            public URL findResource(String name) {
                try {
                    Enumeration<URL> en = findResources(name);
                    return en.hasMoreElements() ? en.nextElement() : null;
                } catch (IOException ex) {
                    return null;
                }
            }
            
            
        };
        ProxyClassLoader l2 = new ProxyClassLoader(new ProxyClassLoader[] { l1 }, false);
        
        
        Set<ProxyClassLoader> now = ProxyClassPackages.findCoveredPkg("META-INF.services");
        assertNotNull("One JAR covers META-INF.services: " + now, now);
        assertEquals("One JAR covers META-INF.services: " + now, 1, now.size());
        
        
        Enumeration<URL> en = l2.getResources("META-INF/services/java.io.Serializable");
        assertTrue("Some are there", en.hasMoreElements());
        URL one = en.nextElement();
        assertFalse("And that is all", en.hasMoreElements());

        URL alternative = l2.getResource("META-INF/services/java.io.Serializable");
        assertEquals("Same URL", one, alternative);
        
        ProxyClassPackages.removeCoveredPakcages(l1);
    }
}
