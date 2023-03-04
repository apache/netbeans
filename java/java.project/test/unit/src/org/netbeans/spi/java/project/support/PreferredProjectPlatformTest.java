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
package org.netbeans.spi.java.project.support;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;


/**
 *
 * @author Tomas Zezula
 */
public class PreferredProjectPlatformTest extends NbTestCase {
    
    private static final String J2SE = "j2se";  //NOI18N
    private static final String J2ME = "j2me";  //NOI18N
    
    public PreferredProjectPlatformTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockJavaPlatformProvider.class);
    }
    

    public void testPreferredPlatforms() {
        final JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        final JavaPlatform[] javaPlatforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        final JavaPlatform[] nonDefaultJ2SEPlatforms = new JavaPlatform[javaPlatforms.length - 2];
        JavaPlatform j2mePlatform = null;
        for (int i = 0, j = 0; i < javaPlatforms.length; i++ ) {
            if (!javaPlatforms[i].equals(defaultPlatform) &&
                J2SE.equals(javaPlatforms[i].getSpecification().getName())) {
                nonDefaultJ2SEPlatforms[j++] = javaPlatforms[i];
            }
            if (J2ME.equals(javaPlatforms[i].getSpecification().getName())) {
                j2mePlatform = javaPlatforms[i];
            }
        }
        assertEquals(defaultPlatform, PreferredProjectPlatform.getPreferredPlatform(J2SE));
        PreferredProjectPlatform.setPreferredPlatform(nonDefaultJ2SEPlatforms[0]);
        assertEquals(nonDefaultJ2SEPlatforms[0], PreferredProjectPlatform.getPreferredPlatform(J2SE));
        PreferredProjectPlatform.setPreferredPlatform(nonDefaultJ2SEPlatforms[1]);
        assertEquals(nonDefaultJ2SEPlatforms[1], PreferredProjectPlatform.getPreferredPlatform(J2SE));
        PreferredProjectPlatform.setPreferredPlatform(defaultPlatform);
        assertEquals(defaultPlatform, PreferredProjectPlatform.getPreferredPlatform(J2SE));
        assertNull(PreferredProjectPlatform.getPreferredPlatform(J2ME));
        PreferredProjectPlatform.setPreferredPlatform(j2mePlatform);
        assertEquals(j2mePlatform, PreferredProjectPlatform.getPreferredPlatform(J2ME));
    }
    
    
    public static final class MockJavaPlatformProvider implements JavaPlatformProvider {
        
        private final JavaPlatform[] platforms;

        public MockJavaPlatformProvider() {
            this.platforms = new JavaPlatform[4];
            this.platforms[0] = new MockJavaPlatform("default_platform", J2SE);       //NOI18N
            this.platforms[1] = new MockJavaPlatform("nondefault_platform_1", J2SE);  //NOI18N
            this.platforms[2] = new MockJavaPlatform("nondefault_platform_2", J2SE);  //NOI18N
            this.platforms[3] = new MockJavaPlatform("j2me_platform", J2ME);  //NOI18N
        }

        @Override
        public JavaPlatform[] getInstalledPlatforms() {
            return platforms;
        }

        @Override
        public JavaPlatform getDefaultPlatform() {
            return platforms[0];
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }
    
    private static final class MockJavaPlatform extends JavaPlatform {
        
        private final String displayName;
        private final String platformType;
        private final Map<String,String> props;
        
        MockJavaPlatform(
                @NonNull final String name,
                @NonNull final String platformType) {
            super();
            this.displayName = name;
            this.props = new HashMap<String, String>();
            this.props.put("platform.ant.name",name);                   //NOI18N
            this.platformType = platformType;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Map<String, String> getProperties() {
            return props;
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public String getVendor() {
            return "me";    //NOI18N
        }

        @Override
        public Specification getSpecification() {
            return new Specification(platformType, new SpecificationVersion("1.6"));  //NOI18N
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.<FileObject>emptySet();
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPath.EMPTY;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.<URL>emptyList();
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
}
