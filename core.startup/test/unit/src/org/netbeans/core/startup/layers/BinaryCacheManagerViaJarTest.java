/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
