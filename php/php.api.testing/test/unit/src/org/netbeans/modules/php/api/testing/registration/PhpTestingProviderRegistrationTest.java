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

package org.netbeans.modules.php.api.testing.registration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class PhpTestingProviderRegistrationTest extends NbTestCase {

    private static final String CONSTRUCTOR = "constructor";
    private static final String FACTORY = "factory";


    public PhpTestingProviderRegistrationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testRegistration() {
        MyTests.factoryCalls = 0;
        MockLookup.init();
        assertSame("No factory method should not be used yet", 0, MyTests.factoryCalls);
        Collection<? extends PhpTestingProvider> all = Lookups.forPath(PhpTesting.TESTING_PATH).lookupAll(PhpTestingProvider.class);
        assertSame("Two should be found", 2, all.size());
        // ???
        //assertSame("One factory method should be used", 1, MyTests.factoryCalls);

        Iterator<? extends PhpTestingProvider> it = all.iterator();
        assertSame(CONSTRUCTOR, it.next().getIdentifier());
        assertSame(FACTORY, it.next().getIdentifier());
    }

    //~ Inner classes

    public static final class MyTestsFactory {
        @PhpTestingProvider.Registration(position=200)
        public static MyTests getInstance() {
            MyTests.factoryCalls++;
            return new MyTests(FACTORY);
        }
    }

    /**
     *
     */
    @PhpTestingProvider.Registration(position=100)
    public static final class MyTests implements PhpTestingProvider {

        static int factoryCalls = 0;

        private final String identifier;
        private final String displayName;

        public MyTests() {
            this(CONSTRUCTOR);
        }

        MyTests(String name) {
            identifier = name;
            displayName = "display name";
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public boolean isTestFile(PhpModule phpModule, FileObject fileObj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isTestCase(PhpModule phpModule, PhpType.Method method) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void runTests(PhpModule phpModule, TestRunInfo runInfo, TestSession testSession) throws TestRunException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CreateTestsResult createTests(PhpModule phpModule, List<FileObject> files, Map<String, Object> configurationPanelProperties) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCoverageSupported(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Locations.Line parseFileFromOutput(String line) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TestLocator getTestLocator(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ProjectCustomizer.CompositeCategoryProvider createCustomizer(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
