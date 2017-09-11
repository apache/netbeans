/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
public final class DelegateToOwnerLookupTest extends NbTestCase {

    private Project owner;
    private FileObject automaticProjectHome;

    public DelegateToOwnerLookupTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject enclosingProjectHome = FileUtil.createFolder(wd, "enclosing");    //NOI18N
        TestProject.Factory.LOOKUP_FACTORY = new Callable<Lookup>() {
            @Override
            public Lookup call() throws Exception {
                return Lookups.fixed(
                    new SourcesImpl(enclosingProjectHome),
                    new ProjectOpenedHookImpl(),
                    new ProjectInformationImpl(enclosingProjectHome));
            }
        };
        automaticProjectHome = FileUtil.createFolder(enclosingProjectHome, "automatic");   //NOI18N
        FileUtil.createFolder(enclosingProjectHome, TestProject.PROJECT_MARKER);
        owner = FileOwnerQuery.getOwner(enclosingProjectHome);
    }


    public void testDelegateLookup() throws IOException {
        //Assert we have enclosing project
        assertNotNull(owner);
        //Assert we have correct Lookup in enclosing project
        assertNotNull(owner.getLookup().lookup(Sources.class));
        assertNotNull(owner.getLookup().lookup(SourcesImpl.class));
        assertNotNull(owner.getLookup().lookup(ProjectOpenedHook.class));
        assertNotNull(owner.getLookup().lookup(ProjectOpenedHookImpl.class));
        assertNotNull(owner.getLookup().lookup(ProjectInformation.class));
        assertNotNull(owner.getLookup().lookup(ProjectInformationImpl.class));
        //Test delegating lookup
        final Lookup lkp = ProjectConvertors.createDelegateToOwnerLookup(automaticProjectHome);
        assertNotNull(lkp);
        //Test do not have black listed services
        assertNull(lkp.lookup(ProjectOpenedHook.class));
        assertNull(lkp.lookup(ProjectOpenedHookImpl.class));
        assertNull(lkp.lookup(ProjectInformation.class));
        assertNull(lkp.lookup(ProjectInformationImpl.class));
        //Test have other services
        assertNotNull(lkp.lookup(Sources.class));
        assertNotNull(lkp.lookup(SourcesImpl.class));
        //Close lookup
        ((Closeable)lkp).close();
        //Nothing should be in the Lookup
        assertNull(lkp.lookup(ProjectOpenedHook.class));
        assertNull(lkp.lookup(ProjectOpenedHookImpl.class));
        assertNull(lkp.lookup(ProjectInformation.class));
        assertNull(lkp.lookup(ProjectInformationImpl.class));
        assertNull(lkp.lookup(Sources.class));
        assertNull(lkp.lookup(SourcesImpl.class));
    }

    private static final class SourcesImpl implements Sources {
        private final SourceGroup root;


        SourcesImpl(@NonNull final FileObject root) {
            this.root = new SourceGroupImpl(root);
        }

        @Override
        public SourceGroup[] getSourceGroups(String type) {
            if (Sources.TYPE_GENERIC.equals(type)) {
                return new SourceGroup[] {root};
            }
            return new SourceGroup[0];
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }

        private static final class SourceGroupImpl implements SourceGroup {

            private final FileObject root;

            SourceGroupImpl(@NonNull final FileObject root) {
                root.getClass();
                this.root = root;
            }

            @Override
            public FileObject getRootFolder() {
                return root;
            }

            @Override
            public String getName() {
                return root.getNameExt();
            }

            @Override
            public String getDisplayName() {
                return getName();
            }

            @Override
            public Icon getIcon(boolean opened) {
                return null;
            }

            @Override
            public boolean contains(FileObject file) {
                return root.equals(file) || FileUtil.isParentOf(root, file);
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
            }
        }
    }

    private static final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        @Override
        protected void projectOpened() {
        }

        @Override
        protected void projectClosed() {
        }
    }

    private static final class ProjectInformationImpl implements ProjectInformation {
        private final FileObject root;

        ProjectInformationImpl(@NonNull final FileObject root) {
            root.getClass();
            this.root = root;
        }

        @Override
        public String getName() {
            return root.getNameExt();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public Project getProject() {
            return FileOwnerQuery.getOwner(root);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
