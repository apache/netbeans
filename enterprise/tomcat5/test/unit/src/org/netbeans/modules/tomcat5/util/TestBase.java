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

package org.netbeans.modules.tomcat5.util;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author sherold
 */
public class TestBase extends NbTestCase {

    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        
        ((Lkp)Lookup.getDefault()).setRepository(new RepositoryImpl());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Repository.getDefault().getClass());
    }
    
    public TestBase(String name) {
        super(name);
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            setProxyLookups(new Lookup[0]);
        }
        
        private void setProxyLookups(Lookup[] lookups) {
            Lookup[] allLookups = new Lookup[lookups.length + 2];
            ClassLoader l = TestBase.class.getClassLoader();
            allLookups[allLookups.length - 1] = Lookups.metaInfServices(l);
            allLookups[allLookups.length - 2] = Lookups.singleton(l);
            System.arraycopy(lookups, 0, allLookups, 0, lookups.length);
            setLookups(allLookups);
        }
        
        private void setRepository(Repository repository) {
            // must set out repository first
            setProxyLookups(new Lookup[] {
                Lookups.singleton(repository),
            });

            FileObject servicesFolder = FileUtil.getConfigFile("Services");
            if (servicesFolder == null) {
                try {
                    servicesFolder = FileUtil.getConfigRoot().createFolder("Services");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            DataFolder services = DataFolder.findFolder(servicesFolder);
            FolderLookup lookup = new FolderLookup(services);
            setProxyLookups(new Lookup[] {
                Lookups.singleton(repository),
                new FolderLookup(services).getLookup()
            });
        }
    }
}
