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
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ModuleTest extends NbTestCase {
    private Module fake;
    
    public ModuleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        final File jar = new File(getWorkDir(), "default-package-resource-cached.jar");
        TestFileUtils.writeZipFile(jar, "resource.txt:content", "package/resource.txt:content", 
            "META-INF/MANIFEST.MF:OpenIDE-Module: x.y.z\n\n"
        );
        MockModuleInstaller inst = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mm = new ModuleManager(inst, ev);
        
        fake = new Module(mm, null, null, null) {
            public List<File> getAllJars() {throw new UnsupportedOperationException();}
            public void setReloadable(boolean r) { throw new UnsupportedOperationException();}
            public void reload() throws IOException { throw new UnsupportedOperationException();}
            protected void classLoaderUp(Set<Module> parents) throws IOException {throw new UnsupportedOperationException();}
            protected void classLoaderDown() { throw new UnsupportedOperationException();}
            protected void cleanup() { throw new UnsupportedOperationException();}
            protected void destroy() { throw new UnsupportedOperationException("Not supported yet.");}
            public boolean isFixed() { throw new UnsupportedOperationException("Not supported yet.");}
            public Object getLocalizedAttribute(String attr) { throw new UnsupportedOperationException("Not supported yet.");}

            public Manifest getManifest() {
                try {
                    return new JarFile(jar, false).getManifest();
                } catch (IOException ex) {
                        throw new AssertionFailedError(ex.getMessage());
                }
            }

        };
    }
    
    public void testBuildVersion() {
        fake.getBuildVersion();
    }
    
    public void testImplVersion() {
        fake.getImplementationVersion();
    }

    
}

