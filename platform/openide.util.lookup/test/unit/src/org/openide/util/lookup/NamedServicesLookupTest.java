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

package org.openide.util.lookup;

import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.ServiceLoaderLineTest;
import org.openide.util.test.MockLookup;


/** Test finding services from manifest.
 * @author Jaroslav Tulach
 */
public class NamedServicesLookupTest extends MetaInfServicesLookupTest {
    static {
        MockLookup.init();
    }
    private ClassLoader previousContextClassLoader;
    
    public NamedServicesLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(previousContextClassLoader);
        super.tearDown();
        ServiceLoaderLineTest.clearLookupsForPath();
    }
    
    

    @Override
    protected String prefix() {
        return "META-INF/namedservices/sub/path/";
    }
    
    @Override
    protected Lookup createLookup(ClassLoader c) {
        Thread.currentThread().setContextClassLoader(c);
        MockLookup.setInstances(c);
        Lookup l = Lookups.forPath("sub/path");
        return l;
    }
    
    //
    // this is not much inheriting test, as we mask most of the tested methods
    // anyway, but the infrastructure to generate the JAR files is useful
    //
    
    public @Override void testLoaderSkew() {}
    public @Override void testStability() throws Exception {}
    public @Override void testMaskingOfResources() throws Exception {}
    public @Override void testOrdering() throws Exception {}
    public @Override void testNoCallToGetResourceForObjectIssue65124() throws Exception {}
    public @Override void testSuperTypes() throws Exception {}
    public @Override void testSubTypes() throws Exception {}
    public @Override void testWrongOrderAsInIssue100320() throws Exception {}
    public @Override void testLookupObject() throws Exception {}
}
