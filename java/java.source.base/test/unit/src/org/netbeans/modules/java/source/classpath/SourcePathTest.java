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

package org.netbeans.modules.java.source.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.ClassIndexTestCase;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePathTest extends ClassIndexTestCase {
    
    public SourcePathTest (String name) {
        super (name);
    }
    
    
    public void testSourcePath () throws Exception {
        
        final File wd = this.getWorkDir();
        final File cache = new File (wd,"cache");   //NOI18N
        cache.mkdir();
        TestUtilities.setCacheFolder(cache);
        final File r1 = new File (wd,"root1"); //NOI18N
        r1.mkdir();
        final File r2 = new File (wd,"root2"); //NOI18N
        r2.mkdir();
        
        final ClassPathImplementation baseImpl = ClassPathSupport.createClassPathImplementation(
            Arrays.asList(new PathResourceImplementation[] {
                ClassPathSupport.createResource(Utilities.toURI(r1).toURL()),
                ClassPathSupport.createResource(Utilities.toURI(r2).toURL())
            }));
        final ClassPath base = ClassPathFactory.createClassPath(baseImpl);
        
        final ClassPath sp1 = ClassPathFactory.createClassPath(SourcePath.filtered(baseImpl, true));
        assertEquals (base,sp1);
        
        final ClassPath sp2 = ClassPathFactory.createClassPath(SourcePath.filtered(baseImpl, false));
        assertTrue (sp2.entries().isEmpty());
        
        ensureRootValid(base.entries().get(0).getURL());
        assertEquals(1,sp2.entries().size());
        assertEquals(base.entries().get(0).getURL(), sp2.entries().get(0).getURL());
        ensureRootValid(base.entries().get(1).getURL());
        assertEquals (base,sp2);
        
        final ClassPath sp3 = ClassPathFactory.createClassPath(SourcePath.filtered(baseImpl, false));
        assertEquals (base,sp3);
    }
    
    private static void assertEquals (ClassPath ecp, ClassPath rcp) {
        List<? extends ClassPath.Entry> ee = ecp.entries();
        List<? extends ClassPath.Entry> re = rcp.entries();
        assertEquals(ee.size(), re.size());
        for (int i=0; i<ee.size(); i++) {
            assertEquals(ee.get(i), re.get(i));
        }
    }

}
