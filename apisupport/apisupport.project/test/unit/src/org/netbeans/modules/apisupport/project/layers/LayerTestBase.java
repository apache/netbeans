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

package org.netbeans.modules.apisupport.project.layers;

import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.EntityCatalog;
import org.xml.sax.EntityResolver;

/**
 * Superclass for tests that work with XML layers.
 * Does some setup, since TAX requires some special infrastructure.
 * @author Jesse Glick
 * @see "#62363"
 */
@Deprecated
public abstract class LayerTestBase extends NbTestCase {

    public static final class Lkp extends ProxyLookup {
        // Copied from org.netbeans.api.project.TestUtil:
        static {
            // XXX replace with MockServices
            System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
            assertEquals(Lkp.class, Lookup.getDefault().getClass());
            Lookup p = Lookups.forPath("Services/AntBasedProjectTypes/");
            p.lookupAll(AntBasedProjectType.class);
            projects = p;
            setLookup(new Object[0]);
        }
        private static Lkp DEFAULT;
        private static final Lookup projects;
        public Lkp() {
            assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            setLookups(new Lookup[] {
                        Lookups.metaInfServices(l),
                        Lookups.singleton(l)
                    });
        }
        public static void setLookup(Object[] instances) {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                projects
            });
        }
    }
    
    protected LayerTestBase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Lkp.setLookup(new Object[] {
            new TestCatalog(),
        });
    }
    
    /**
     * In the actual IDE, the default NetBeans Catalog will already be "mounted", so just for testing:
     */
    private static final class TestCatalog extends UserCatalog {
        
        public TestCatalog() {}
        
        public EntityResolver getEntityResolver() {
            return EntityCatalog.getDefault();
        }
        
    }
    
}
