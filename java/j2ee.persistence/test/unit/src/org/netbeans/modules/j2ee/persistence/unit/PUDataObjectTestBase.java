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

package org.netbeans.modules.j2ee.persistence.unit;

import java.net.URI;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Erno Mononen, Andrei Badea
 */
public abstract class PUDataObjectTestBase extends NbTestCase {

    private  static DummyProject project = new DummyProject();
    
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp)Lookup.getDefault()).setLookups(new Object[] { new PUMimeResolver(), new Pool(), new PUFOQI(project) });

        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        assertEquals("The default MIMEResolver is not our resolver!", PUMimeResolver.class, Lookup.getDefault().lookup(MIMEResolver.class).getClass());
        assertEquals("The default DataLoaderPool is not our pool!", Pool.class, Lookup.getDefault().lookup(DataLoaderPool.class).getClass());
    }

    public PUDataObjectTestBase(String name) {
        super(name);
        project.setDirectory(FileUtil.toFileObject(getDataDir()));
    }

    /**
     * Our default lookup.
     */
    public static final class Lkp extends ProxyLookup {

        public Lkp() {
            setLookups(new Object[0]);
        }

        public void setLookups(Object[] instances) {
            ClassLoader l = PersistenceEditorTestBase.class.getClassLoader();
            setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }

    /**
     * DataLoaderPool which is registered in the default lookup and loads
     * PUDataLoader.
     */
    public static final class Pool extends DataLoaderPool {

        @Override
        public Enumeration loaders() {
            return Enumerations.singleton(new PUDataLoader());
        }
    }

    /**
     * MIME Resolver that associates persistence.xml with PUDataLoader.
     */
    public static final class PUMimeResolver extends MIMEResolver {

        @Override
        public String findMIMEType(FileObject fo) {
            if (fo.getName().startsWith("persistence")){
                return PUDataLoader.REQUIRED_MIME;
            }
            return null;
        }
    }

    /**
     * Returns dummy project implementation. Needed since persistence unit needs
     * to be associated with {@link Project} owner. Also see issue #74426.
     */
    private static final class PUFOQI implements FileOwnerQueryImplementation {

        Project dummyProject;
        
        PUFOQI(Project dummy){
            dummyProject = dummy;
        }

        @Override
        public Project getOwner(URI file) {
            return dummyProject;
        }

        @Override
        public Project getOwner(FileObject file) {
            return dummyProject;
        }
    }
    
    private static class DummyProject implements Project{
        private FileObject dir;
        /**
         * Dummy project have to have not null dir for some editor kits, for example XMLKit from xml.text module
         * @param dir 
         */
        public void setDirectory(FileObject dir){
            this.dir = dir;
        }
        @Override
            public Lookup getLookup() { return Lookup.EMPTY; }
        @Override
            public FileObject getProjectDirectory() { return dir; }
    }
}
