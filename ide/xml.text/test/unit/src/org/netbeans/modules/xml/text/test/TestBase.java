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

package org.netbeans.modules.xml.text.test;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 */
public class TestBase extends NbTestCase {

    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        
        ((Lkp)Lookup.getDefault()).addFixed(new RepositoryImpl());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Lookup.getDefault().lookup(Repository.class).getClass());
    }
    
    public static void setLookup(Object[] instance) {
        ((Lkp)Lookup.getDefault()).setLookup(instance);
    }
    
    public TestBase(String name) {
        super(name);
    }
    
    public static final class Lkp extends ProxyLookup {
        
        private InstanceContent fixed = new InstanceContent();
        private Lookup fixedLookup = new AbstractLookup(fixed);
        
        public Lkp() {
            setLookup(new Object[0]);
        }
        
        void setLookup(Object[] instances) {
            ClassLoader l = TestBase.class.getClassLoader();
            setLookups(new Lookup[] {
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                fixedLookup,
                Lookups.fixed(instances),
            });
        }
        
        void addFixed(Object instance) {
            fixed.add(instance);
        }
    }
}
