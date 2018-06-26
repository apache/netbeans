/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class AppClientPersistenceProviderTest extends NbTestCase {

    // TODO also test the contents of the classpaths

    private Project project;
    private FileObject root;
    private AppClientPersistenceProvider provider;
    private FileObject persistenceLocation;

    public AppClientPersistenceProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    @Override
    public PrintStream getLog() {
        return System.err;
    }

    @Override
    public void setUp() throws Exception {
        MockLookup.setLayersAndInstances();

        // in an attempt to find the cause of issue 90762
        Logger.getLogger(PersistenceScopesHelper.class.getName()).setLevel(Level.FINEST);
        // setup the project
        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        project = (Project) ProjectSupport.openProject(f);
        Sources src = project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        root = groups[0].getRootFolder();
        provider = project.getLookup().lookup(AppClientPersistenceProvider.class);
        persistenceLocation = project.getProjectDirectory().getFileObject("src/conf");

        FileObject persistenceXml = persistenceLocation.getFileObject("persistence.xml");
        if (persistenceXml != null) {
            persistenceXml.delete();
        }
    }

    public void testPersistenceLocation() throws Exception {
        assertEquals(persistenceLocation, provider.getLocation());
        assertEquals(persistenceLocation, provider.createLocation());
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
        FileObject persistenceXml = persistenceLocation.createData("persistence.xml"); // NOI18N
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
        persistenceLocation.createData("persistence.xml"); // NOI18N
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
