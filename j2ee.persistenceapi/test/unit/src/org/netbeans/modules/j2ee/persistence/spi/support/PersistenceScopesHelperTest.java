/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
