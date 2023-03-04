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

package org.netbeans.modules.java.platform;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;

/**
 *
 * @author Tomas Zezula
 */
public class ConvertAsJavaBeanPlatformTest extends NbTestCase {
    
    public ConvertAsJavaBeanPlatformTest(final String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.
                emptyConfiguration().
                addTest(ConvertAsJavaBeanPlatformTest.class).
                clusters("extide"). //NOI18N
                gui(false).
                suite();
    }

    public void testConvertAsBeanPlatform() throws Exception {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        final JavaPlatform[]
                initialState = jpm.getInstalledPlatforms();
        final Set<String> expected = new TreeSet<String>();
        for (JavaPlatform p : initialState) {
            if (p instanceof FallbackDefaultJavaPlatform) {
                continue;
            }
            expected.add(p.getDisplayName());
        }
        final TestPlatform platform = new TestPlatform();
        platform.setDisplayName("TestPlatform");   //NOI18N
        platform.setVendor("me");   //NOI18N
        expected.add(platform.getDisplayName());
        final FileObject platformsFolder = FileUtil.getConfigFile("Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
        InstanceDataObject.create(DataFolder.findFolder(platformsFolder), platform.getDisplayName(), platform, null, true);
        final JavaPlatform[] newState = jpm.getInstalledPlatforms();
        final Set<String> result = new TreeSet<String>();
        for (JavaPlatform p : newState) {
            result.add (p.getDisplayName());
        }
        assertEquals(expected, result);
    }
    
    public static class TestPlatform extends JavaPlatform implements Serializable {
        
        private String name;
        private String vendor;
        private Specification spec;               
        
        public TestPlatform() {            
        }

        @Override
        public String getDisplayName() {
            return name;
        }
        
        public void setDisplayName(final String name) {
            this.name = name;
        }
        
        @Override
        public String getVendor() {
            return vendor;
        }
        
        public void setVendor(final String vendor) {
            this.vendor = vendor;
        }

        @Override
        public Specification getSpecification() {
            return spec;
        }
        
        public void setSpecification(final Specification spec) {
            this.spec = spec;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.<String,String>emptyMap();
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
        
    }
    
}
