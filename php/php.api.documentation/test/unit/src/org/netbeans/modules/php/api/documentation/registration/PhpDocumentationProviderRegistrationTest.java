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

package org.netbeans.modules.php.api.documentation.registration;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class PhpDocumentationProviderRegistrationTest extends NbTestCase {

    private static final String CONSTRUCTOR = "constructor";
    private static final String FACTORY = "factory";


    public PhpDocumentationProviderRegistrationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testRegistration() {
        MyDoc.factoryCalls = 0;
        MockLookup.init();
        assertSame("No factory method should not be used yet", 0, MyDoc.factoryCalls);
        Collection<? extends PhpDocumentationProvider> all = Lookups.forPath(PhpDocumentations.DOCUMENTATION_PATH).lookupAll(PhpDocumentationProvider.class);
        assertSame("Two should be found", 2, all.size());
        // ???
        //assertSame("One factory method should be used", 1, MyDoc.factoryCalls);

        Iterator<? extends PhpDocumentationProvider> it = all.iterator();
        assertSame(CONSTRUCTOR, it.next().getName());
        assertSame(FACTORY, it.next().getName());
    }

    //~ Inner classes

    public static final class MyDocFactory {
        @PhpDocumentationProvider.Registration(position=200)
        public static MyDoc getInstance() {
            MyDoc.factoryCalls++;
            return new MyDoc(FACTORY);
        }
    }

    @PhpDocumentationProvider.Registration(position=100)
    public static final class MyDoc extends PhpDocumentationProvider {
        static int factoryCalls = 0;

        public MyDoc() {
            super(CONSTRUCTOR, "display name");
        }

        MyDoc(String name) {
            super(name, "display name");
        }

        @Override
        public void generateDocumentation(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
