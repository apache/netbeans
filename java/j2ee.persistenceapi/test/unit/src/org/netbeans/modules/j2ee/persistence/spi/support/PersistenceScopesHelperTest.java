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
package org.netbeans.modules.j2ee.persistence.spi.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class PersistenceScopesHelperTest extends NbTestCase {

    private FileObject dataDir;
    private FileObject workDir;

    private PersistenceScope persistenceScope;

    public PersistenceScopesHelperTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        clearWorkDir();
        dataDir = FileUtil.toFileObject(getDataDir());
        workDir = FileUtil.toFileObject(getWorkDir());

        persistenceScope = PersistenceScopeFactory.createPersistenceScope(new PersistenceScopeImpl());
    }

    private static FileObject copyFile(FileObject source, FileObject destFolder) throws IOException {
        return FileUtil.copyFile(source, destFolder, source.getName());
    }

    public void testBasic() throws Exception {
        PersistenceScope persistenceScope2 = PersistenceScopeFactory.createPersistenceScope(new PersistenceScopeImpl());

        FileObject parent = workDir.createFolder("parent");
        File persistenceXmlFile = new File(FileUtil.toFile(parent), "persistence.xml");

        PersistenceScopesHelper helper = new PersistenceScopesHelper();
        PersistenceScopes persistenceScopes = helper.getPersistenceScopes();
        PCL listener = new PCL();
        persistenceScopes.addPropertyChangeListener(listener);

        assertEquals(0, persistenceScopes.getPersistenceScopes().length);
        assertEquals(0, listener.changeCount);

        helper.changePersistenceScope(persistenceScope, persistenceXmlFile);

        assertEquals(0, persistenceScopes.getPersistenceScopes().length);
        assertEquals(0, listener.changeCount);

        // changing the persistence scope to null while persistence.xml is not null is not supported...

        try {
            helper.changePersistenceScope(null, persistenceXmlFile);
            fail();
        } catch (IllegalArgumentException e) { }

        // ... but changing the it to null while persistence.xml is null is

        helper.changePersistenceScope(null, null);

        assertEquals(0, persistenceScopes.getPersistenceScopes().length);
        assertEquals(0, listener.changeCount);

        // changing the persistence scope -- should not trigger any events, persistence.xml does not exist

        helper.changePersistenceScope(persistenceScope2, persistenceXmlFile);

        assertEquals(0, persistenceScopes.getPersistenceScopes().length);
        assertEquals(0, listener.changeCount);

        // setting a null persistence.xml -- should not trigger any events, persistence.xml did not exist before

        helper.changePersistenceScope(persistenceScope2, null);

        assertEquals(0, persistenceScopes.getPersistenceScopes().length);
        assertEquals(0, listener.changeCount);

        // setting back a non-null, but not existing persistence.xml -- should not trigger any events

        helper.changePersistenceScope(persistenceScope2, persistenceXmlFile);

        assertEquals(0, persistenceScopes.getPersistenceScopes().length);
        assertEquals(0, listener.changeCount);

        // creating the persistence.xml file -- should trigger an event

        copyFile(dataDir.getFileObject("persistence.xml"), parent);

        assertEquals(1, listener.changeCount);
        assertSame(persistenceScope2, persistenceScopes.getPersistenceScopes()[0]);

        // changing the persistence scope -- should trigger an event

        helper.changePersistenceScope(persistenceScope, persistenceXmlFile);

        assertEquals(2, listener.changeCount);
        assertSame(persistenceScope, persistenceScopes.getPersistenceScopes()[0]);

        // changing to the same persistence scope -- should not trigger an event, since nothing has changed from the client's point of view

        helper.changePersistenceScope(persistenceScope, persistenceXmlFile);

        assertEquals(2, listener.changeCount);
        assertSame(persistenceScope, persistenceScopes.getPersistenceScopes()[0]);

        // setting a null persistence.xml -- should trigger an event, persistence.xml existed before

        helper.changePersistenceScope(persistenceScope, null);

        assertEquals(3, listener.changeCount);
        assertEquals(0, persistenceScopes.getPersistenceScopes().length);

        // setting back a non-null and existing persistence.xml -- should trigger an event

        helper.changePersistenceScope(persistenceScope, persistenceXmlFile);

        assertEquals(4, listener.changeCount);
        assertSame(persistenceScope, persistenceScopes.getPersistenceScopes()[0]);

        // removing the persistence.xml file -- should trigger an event

        parent.getFileObject("persistence.xml").delete();

        assertEquals(5, listener.changeCount);
        assertEquals(0, persistenceScopes.getPersistenceScopes().length);

        // just making sure the helper is still returning the same instance of PersistenceScopes

        assertSame(persistenceScopes, helper.getPersistenceScopes());
    }

    public void testPropertyChangeWhenFirstTimeSettingExistingPersistenceXml() throws Exception {
        FileObject persistenceXml = copyFile(dataDir.getFileObject("persistence.xml"), workDir);

        PersistenceScopesHelper helper = new PersistenceScopesHelper();
        PersistenceScopes persistenceScopes = helper.getPersistenceScopes();
        PCL listener = new PCL();
        persistenceScopes.addPropertyChangeListener(listener);

        helper.changePersistenceScope(persistenceScope, FileUtil.toFile(persistenceXml));

        assertEquals(1, listener.changeCount);
        assertSame(persistenceScope, helper.getPersistenceScopes().getPersistenceScopes()[0]);
    }

    private static final class PersistenceScopeImpl implements PersistenceScopeImplementation {

        public FileObject getPersistenceXml() {
            return null;
        }

        public ClassPath getClassPath() {
            return null;
        }

        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
            return null;
        }
    }

    private static final class PCL implements PropertyChangeListener {

        private int changeCount;

        public void propertyChange(PropertyChangeEvent event) {
            assertEquals(PersistenceScopes.PROP_PERSISTENCE_SCOPES, event.getPropertyName());
            changeCount++;
        }
    }
}
