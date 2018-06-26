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

package org.netbeans.modules.j2ee.common;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class J2eeProjectCapabilitiesTest extends NbTestCase {

    public J2eeProjectCapabilitiesTest(String testName) {
        super(testName);
    }

    public void testIsEjbSupported() throws Exception {
        Project p = createProject(Profile.JAVA_EE_5, Type.EJB);
        J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_6_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_6_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_6_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_6_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_5, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_7_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_7_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_7_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());

        p = createProject(Profile.JAVA_EE_7_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
    }

    private Project createProject(final Profile profile, final Type type) throws IOException {
        // just a fake project dir for now:
        FileObject projDir = FileUtil.toFileObject(getWorkDir());
        return new FakeProject(type, profile, projDir);
    }

    public static class FakeProject implements Project {

        private Type type;
        private Profile profile;
        private Lookup l;
        private FileObject projDir;

        public FakeProject(Type type, Profile profile, FileObject projDir) {
            this.type = type;
            this.profile = profile;
            this.projDir = projDir;
            FakeEjbJarsInProject f = new FakeEjbJarsInProject(new FakeEjbJarImplementation2(profile));
            FakeJ2eeModuleProvider f2 = new FakeJ2eeModuleProvider(new FakeJ2eeModuleImpl(type));
            l = Lookups.fixed(f, f2);
        }

        public FileObject getProjectDirectory() {
            return projDir;
        }

        public Lookup getLookup() {
            return l;
        }
    
    }

    private static class FakeEjbJarsInProject implements EjbJarsInProject {

        private EjbJarImplementation2 impl;

        public FakeEjbJarsInProject(EjbJarImplementation2 impl) {
            this.impl = impl;
        }

        public EjbJar[] getEjbJars() {
            return new EjbJar[]{EjbJarFactory.createEjbJar(impl)};
        }

    }

    private static class FakeEjbJarImplementation2 implements EjbJarImplementation2 {

        private Profile profile;

        public FakeEjbJarImplementation2(Profile profile) {
            this.profile = profile;
        }

        public Profile getJ2eeProfile() {
            return profile;
        }

        public FileObject getMetaInf() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject getDeploymentDescriptor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject[] getJavaSources() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MetadataModel<EjbJarMetadata> getMetadataModel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
    }

    private static class FakeJ2eeModuleProvider extends J2eeModuleProvider {

        private J2eeModuleImplementation2 impl;

        public FakeJ2eeModuleProvider(J2eeModuleImplementation2 impl) {
            this.impl = impl;
        }

        @Override
        public J2eeModule getJ2eeModule() {
            return J2eeModuleFactory.createJ2eeModule(impl);
        }

        @Override
        public ModuleChangeReporter getModuleChangeReporter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setServerInstanceID(String severInstanceID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getServerInstanceID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getServerID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class FakeJ2eeModuleImpl implements J2eeModuleImplementation2 {

        private Type type;

        public FakeJ2eeModuleImpl(Type type) {
            this.type = type;
        }

        public Type getModuleType() {
            return type;
        }

        public String getModuleVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getUrl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject getArchive() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Iterator getArchiveContents() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject getContentDirectory() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getResourceDirectory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getDeploymentConfigurationFile(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
 
}
