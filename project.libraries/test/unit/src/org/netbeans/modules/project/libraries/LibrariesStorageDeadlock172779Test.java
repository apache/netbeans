/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.libraries;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.TestEntityCatalog;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class LibrariesStorageDeadlock172779Test extends NbTestCase {
    static final Logger LOG = Logger.getLogger(LibrariesStorageDeadlock166109Test.class.getName());
    private FileObject storageFolder;
    private static CountDownLatch cond;

    public LibrariesStorageDeadlock172779Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(
            new TestEntityCatalog(),
            new LibraryTypeRegistryImpl(),
            new LibrariesTestUtil.MockProjectManager());
        storageFolder = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");
        assertNotNull("storageFolder found", storageFolder);
        LibrariesTestUtil.registerLibraryTypeProvider(TestMutexLibraryTypeProvider.class);
        org.netbeans.modules.project.libraries.LibrariesTestUtil.createLibraryDefinition(storageFolder, "Library1", null);
    }

    public void testDeadlock() throws Exception {        
        cond = new CountDownLatch(1);
        final ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    es.submit(new Runnable() {
                        public void run() {
                            //Trigger init_storage
                            LibraryManager.getDefault().getLibraries();
                        }
                    });
                    try {
                        cond.await();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    //Deadlock
                    LibraryManager.getDefault().getLibraries();
                }
            });                        
        } finally {
            es.shutdown();
        }
    }

    public static final class TestMutexLibraryTypeProvider implements LibraryTypeProvider {

        private static final String LIBRARY_TYPE = "Test";  //NOI18N
        private static final String[] VOLUME_TYPES = new String[] {"bin","src","doc"};  //NOI18N

        public TestMutexLibraryTypeProvider() {
            LOG.info("TestMutexLibraryTypeProvider created");
        }

        public String getLibraryType() {
            return LIBRARY_TYPE;
        }

        public String getDisplayName() {
            return "Test Provider"; //NOI18N
        }

        public void libraryCreated(LibraryImplementation library) {
            cond.countDown();
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    //Deadlock
                }
            });            
        }

        public String[] getSupportedVolumeTypes() {
            return VOLUME_TYPES;
        }

        public LibraryImplementation createLibrary() {
            return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
        }

        public void libraryDeleted(LibraryImplementation library) {
        }      

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public org.openide.util.Lookup getLookup() {
            return Lookup.EMPTY;
        }    

    }

}
