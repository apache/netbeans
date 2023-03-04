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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class J2SEPersistenceProviderTest extends NbTestCase {

    // TODO also test the contents of the classpaths

    private Project project;
    private FileObject root;
    private J2SEPersistenceProvider provider;
    private File persistenceLocation;

    public J2SEPersistenceProviderTest(String testName) {
        super(testName);
    }

    protected @Override int timeOut() {
        return 300000;
    }

    public void setUp() throws Exception {
        // in an attempt to find the cause of issue 90762
        Logger.getLogger(PersistenceScopesHelper.class.getName()).setLevel(Level.FINEST);
        // setup the project
        FileObject scratch = TestUtil.makeScratchDir(this);
        final FileObject projdir = scratch.createFolder("proj");
        MockLookup.setLayersAndInstances();
        // issue 90762: prevent AntProjectHelper from firing changes in a RP thread, which interferes with tests
        // see APH.fireExternalChange
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception{
                J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));
                J2SEProjectGenerator.createProject(FileUtil.toFile(projdir), "proj", "foo.Main", "manifest.mf", null, false);
                J2SEProjectGenerator.setDefaultSourceLevel(null);
                return null;
            }
        });
        project = ProjectManager.getDefault().findProject(projdir);
        Sources src = project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        root = groups[0].getRootFolder();
        provider = project.getLookup().lookup(J2SEPersistenceProvider.class);
        persistenceLocation = new File(FileUtil.toFile(project.getProjectDirectory().getFileObject("src")), "META-INF");
    }

    public void testPersistenceLocation() throws Exception {
        assertEquals(null, provider.getLocation());
        assertEquals(persistenceLocation,  FileUtil.toFile(provider.createLocation()));
        assertEquals(persistenceLocation,  FileUtil.toFile(provider.getLocation()));
    }

    public void testPersistenceScopes() throws Exception {
        class PCL implements PropertyChangeListener {
            private int changeCount;

            public void propertyChange(PropertyChangeEvent event) {
                changeCount++;
            }
        }

        PersistenceScopes persistenceScopes = PersistenceScopes.getPersistenceScopes(project);
        PCL listener = new PCL();
        persistenceScopes.addPropertyChangeListener(listener);

        // no persistence scope
        PersistenceScope persistenceScope = provider.findPersistenceScope(root);
        assertNull(persistenceScope);
        assertEquals(0, listener.changeCount);
        assertEquals(0, persistenceScopes.getPersistenceScopes().length);

        // adding persistence scope
        FileObject persistenceLocationFO = provider.createLocation();
        FileObject persistenceXml = persistenceLocationFO.createData("persistence.xml"); // NOI18N
        persistenceScope = provider.findPersistenceScope(root);
        assertNotNull(persistenceScope);
        assertEquals(persistenceXml, persistenceScope.getPersistenceXml());
        assertEquals(1, listener.changeCount);
        assertSame(persistenceScope, persistenceScopes.getPersistenceScopes()[0]);
        assertNotNull(persistenceScope.getEntityMappingsModel("unit"));

        // testing the persistence scope classpath
        ClassPath scopeCP = persistenceScope.getClassPath();
        assertNotNull(scopeCP);
        persistenceScope = provider.findPersistenceScope(root);
        assertSame("Should return the same classpath object", scopeCP, persistenceScope.getClassPath());

        // removing persistence.xml
        persistenceXml.delete();
        assertNull("Should return a null persistence.xml", persistenceScope.getPersistenceXml());
        assertNull(persistenceScope.getEntityMappingsModel("unit"));
        persistenceScope = provider.findPersistenceScope(root);
        assertNull(persistenceScope);
        assertEquals(2, listener.changeCount);
        assertEquals(0, persistenceScopes.getPersistenceScopes().length);

        // re-adding persistence scope
        persistenceLocationFO.createData("persistence.xml"); // NOI18N
        persistenceScope = provider.findPersistenceScope(root);
        assertTrue("Should always return a valid persistence.xml", persistenceScope.getPersistenceXml().isValid());
    }

    public void testEntityClassScope() throws Exception {
        EntityClassScope entityClassScope = provider.findEntityClassScope(root);
        assertNotNull(entityClassScope);
        assertNotNull(entityClassScope.getEntityMappingsModel(false));
        assertNotNull(entityClassScope.getEntityMappingsModel(true));
    }
}
