/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
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
