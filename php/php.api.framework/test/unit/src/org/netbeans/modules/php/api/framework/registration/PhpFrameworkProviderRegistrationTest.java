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

package org.netbeans.modules.php.api.framework.registration;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.framework.PhpFrameworks;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class PhpFrameworkProviderRegistrationTest extends NbTestCase {

    private static final String CONSTRUCTOR = "constructor";
    private static final String FACTORY = "factory";


    public PhpFrameworkProviderRegistrationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testRegistration() {
        MyFw.factoryCalls = 0;
        MockLookup.init();
        assertSame("No factory method should not be used yet", 0, MyFw.factoryCalls);
        Collection<? extends PhpFrameworkProvider> all = Lookups.forPath(PhpFrameworks.FRAMEWORK_PATH).lookupAll(PhpFrameworkProvider.class);
        assertSame("Two should be found", 2, all.size());
        // ???
        //assertSame("One factory method should be used", 1, MyFw.factoryCalls);

        Iterator<? extends PhpFrameworkProvider> it = all.iterator();
        assertEquals(CONSTRUCTOR, it.next().getIdentifier());
        assertEquals(FACTORY, it.next().getIdentifier());
    }

    //~ Inner classes

    public static final class MyFwFactory {
        @PhpFrameworkProvider.Registration(position=200)
        public static MyFw getInstance() {
            MyFw.factoryCalls++;
            return new MyFw(FACTORY);
        }
    }

    @PhpFrameworkProvider.Registration(position=100)
    public static final class MyFw extends PhpFrameworkProvider {
        static int factoryCalls = 0;

        public MyFw() {
            super(CONSTRUCTOR, CONSTRUCTOR, null);
        }

        MyFw(String name) {
            super(name, name, null);
        }

        @Override
        public boolean isInPhpModule(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public EditorExtender getEditorExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
