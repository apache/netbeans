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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
