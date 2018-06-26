/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        } else {
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
