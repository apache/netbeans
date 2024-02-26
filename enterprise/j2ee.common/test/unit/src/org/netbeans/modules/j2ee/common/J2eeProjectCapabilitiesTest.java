/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_6_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_6_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
//        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAVA_EE_7_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_7_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
//        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
//        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAVA_EE_8_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_8_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
//        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
//        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAKARTA_EE_8_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertTrue(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_8_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
//        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
//        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAKARTA_EE_9_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_9_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
//        assertTrue(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAKARTA_EE_9_1_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_9_1_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
//        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_10_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_10_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
//        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_11_FULL, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_11_WEB, Type.EJB);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
//        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_5, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAVA_EE_6_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_6_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_7_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAVA_EE_7_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAVA_EE_8_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAVA_EE_8_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_8_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertTrue(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertTrue(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_8_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertTrue(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertTrue(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertFalse(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAKARTA_EE_9_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_9_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAKARTA_EE_9_1_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_9_1_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_10_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_10_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());
        
        p = createProject(Profile.JAKARTA_EE_11_FULL, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertTrue(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());

        p = createProject(Profile.JAKARTA_EE_11_WEB, Type.WAR);
        cap = J2eeProjectCapabilities.forProject(p);
        assertFalse(cap.isEjb30Supported());
        assertFalse(cap.isEjb31Supported());
        assertFalse(cap.isEjb31LiteSupported());
        assertFalse(cap.isEjb32Supported());
        assertFalse(cap.isEjb32LiteSupported());
        assertFalse(cap.isEjb40Supported());
        assertTrue(cap.isEjb40LiteSupported());
        
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
