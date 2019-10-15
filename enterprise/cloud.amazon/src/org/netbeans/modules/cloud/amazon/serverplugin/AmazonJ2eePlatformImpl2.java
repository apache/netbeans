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
package org.netbeans.modules.cloud.amazon.serverplugin;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ImageUtilities;

/**
 *
 */
public class AmazonJ2eePlatformImpl2 extends J2eePlatformImpl2 {

    private AmazonDeploymentManager dm;

    public AmazonJ2eePlatformImpl2(DeploymentManager dm) {
        assert dm instanceof AmazonDeploymentManager;
        this.dm = (AmazonDeploymentManager)dm;
    }
    
    @Override
    public File getServerHome() {
        return null;
    }

    @Override
    public File getDomainHome() {
        return null;
    }

    @Override
    public File getMiddlewareHome() {
        return null;
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        Library l = LibraryManager.getDefault().getLibrary("javaee-api-6.0");
        
        LibraryImplementation library = new J2eeLibraryTypeProvider().createLibrary();

        // set its name
        library.setName("JavaEEAPI");

        library.setContent(J2eeLibraryTypeProvider.
                VOLUME_TYPE_CLASSPATH, l.getContent("classpath"));
        
        return new LibraryImplementation[]{library};
    }

    @Override
    public Set<Type> getSupportedTypes() {
        return Collections.<Type>singleton(Type.WAR);
    }

    @Override
    public Set<Profile> getSupportedProfiles() {
        if (dm.getContainerType().contains("Tomcat 6")) {
            return new HashSet<Profile>(Arrays.<Profile>asList(new Profile[]{Profile.JAVA_EE_5, Profile.J2EE_14}));
        } else if (dm.getContainerType().contains("Tomcat 7")) {
            return new HashSet<Profile>(Arrays.<Profile>asList(new Profile[]{Profile.JAVA_EE_6_FULL, Profile.JAVA_EE_6_WEB, Profile.JAVA_EE_5, Profile.J2EE_14}));
        } else if (dm.getContainerType().contains("Tomcat 8")) {
            return new HashSet<Profile>(Arrays.<Profile>asList(new Profile[]{Profile.JAVA_EE_7_FULL, Profile.JAVA_EE_7_WEB, Profile.JAVA_EE_6_FULL, Profile.JAVA_EE_6_WEB, Profile.JAVA_EE_5, Profile.J2EE_14}));
        }
        else {
            return new HashSet<Profile>(Arrays.<Profile>asList(new Profile[]{Profile.JAVA_EE_6_FULL, Profile.JAVA_EE_6_WEB, Profile.JAVA_EE_5, Profile.J2EE_14}));
        }
    }

    @Override
    public Set<Profile> getSupportedProfiles(Type moduleType) {
        return getSupportedProfiles();
    }
    
    @Override
    public String getDisplayName() {
        return "Amazon Beanstalk Tomcat";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/cloud/amazon/resources/tomcat.png"); // NOI18N
    }

    @Override
    public File[] getPlatformRoots() {
//        Library l = LibraryManager.getDefault().getLibrary("javaee-api-6.0");
//        assert l != null;
//        List<File> res = new ArrayList<File>();
//        for (URL url : l.getContent("classpath")) {
//            FileObject fo = URLMapper.findFileObject(url);
//            fo = FileUtil.getArchiveFile(fo);
//            assert fo != null;
//            res.add(FileUtil.toFile(fo));
//        }
//        return res.toArray(new File[res.size()]);
        return null;
    }

    @Override
    public File[] getToolClasspathEntries(String toolName) {
        return new File[0];
    }

    @Override
    @Deprecated
    public boolean isToolSupported(String toolName) {
        return false;
    }

    @Override
    public Set getSupportedJavaPlatformVersions() {
        return new HashSet<String>(Arrays.asList(new String[] {"1.6","1.5"}));
    }

    @Override
    public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }
    
}
