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

package org.netbeans.libs.svnclientadapter.svnkit;

import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory;
import org.openide.util.Lookup;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

/**
 *
 * @author tomas
 */
public class SvnKitClientAdapterFactoryTest extends NbTestCase {

    public SvnKitClientAdapterFactoryTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }

    public void testIsAvailable() {
        SvnKitClientAdapterFactory f = getFactory();
        assertNotNull(f);
        assertTrue(f.isAvailable());
        ISVNClientAdapter c = f.createClient();
        assertNotNull(c);
    }

    public void testProvides() {
        SvnKitClientAdapterFactory f = getFactory();
        assertNotNull(f);
        assertEquals(SvnClientAdapterFactory.Client.SVNKIT, f.provides());
    }
    
    public void testGetFactory() {
        Collection<SvnClientAdapterFactory> cl = (Collection<SvnClientAdapterFactory>) Lookup.getDefault().lookupAll(SvnClientAdapterFactory.class);
        for (SvnClientAdapterFactory f : cl) {
            if(f.getClass() == SvnKitClientAdapterFactory.class) {
                return;
            }
        }
        fail("couldn't lookup SvnKitClientAdapterFactory");
    }

    private SvnKitClientAdapterFactory getFactory() {
        return new TestFactory();
    }

    private class TestFactory extends SvnKitClientAdapterFactory {
        @Override
        public Client provides() {
            return super.provides();
        }
        @Override
        public boolean isAvailable() {
            return super.isAvailable();
        }
    }
}