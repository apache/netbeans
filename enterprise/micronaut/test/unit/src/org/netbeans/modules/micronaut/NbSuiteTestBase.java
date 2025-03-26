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
package org.netbeans.modules.micronaut;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.implspi.ActiveDocumentProvider;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class NbSuiteTestBase extends NbTestCase {

    public NbSuiteTestBase(String name) {
        super(name);
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
        public static class InstalledFileLocator extends DummyInstalledFileLocator {
    }

    // must register ADP: otherwise maven fails at the start and will not even run 
    // Prime command.
    @ServiceProvider(service = ActiveDocumentProvider.class)
    public static class ActiveDocumentProviderImpl implements ActiveDocumentProvider {

        @Override
        public Document getActiveDocument() {
            return null;
        }

        @Override
        public Set<? extends Document> getActiveDocuments() {
            return Collections.emptySet();
        }

        @Override
        public void addActiveDocumentListener(ActiveDocumentProvider.ActiveDocumentListener listener) {
        }

        @Override
        public void removeActiveDocumentListener(ActiveDocumentProvider.ActiveDocumentListener listener) {
        }
        
    }

    private static File getTestNBDestDir() throws Exception {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();

        System.setProperty("test.reload.sync", "true");
        // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
    }
    
}
