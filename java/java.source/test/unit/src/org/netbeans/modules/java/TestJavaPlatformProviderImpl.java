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
package org.netbeans.modules.java;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.platform.implspi.JavaPlatformProvider.class)
public class TestJavaPlatformProviderImpl implements JavaPlatformProvider {

    public static volatile boolean ALLOW_INSTALL_FOLDERS;
    
    /** Creates a new instance of TestJavaPlatformProviderImpl */
    public TestJavaPlatformProviderImpl() {
    }

    public JavaPlatform[] getInstalledPlatforms() {
        return new JavaPlatform[] {getDefaultPlatform()};
    }

    private static DefaultPlatform DEFAULT = new DefaultPlatform();

    public JavaPlatform getDefaultPlatform() {
        return DEFAULT;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    private static final class DefaultPlatform extends JavaPlatform {
        private static ClassPath EMPTY = ClassPathSupport.createClassPath(Collections.EMPTY_LIST);

        public String getDisplayName() {
            return "default";
        }

        public Map getProperties() {
            return Collections.emptyMap();
        }

        private static ClassPath  bootClassPath;

        private static synchronized ClassPath getBootClassPath() {
            if (bootClassPath == null) {
                bootClassPath = BootClassPathUtil.getBootClassPath();
            }
            return bootClassPath;
        }

        public ClassPath getBootstrapLibraries() {
            return getBootClassPath();
        }

        public ClassPath getStandardLibraries() {
            return EMPTY;
        }

        public String getVendor() {
            return "";
        }

        private Specification spec = new Specification("j2se", new SpecificationVersion("1.5"));

        public Specification getSpecification() {
            return spec;
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            if (!ALLOW_INSTALL_FOLDERS) {
                return Collections.emptySet();
            }
            FileObject jh = FileUtil.toFileObject(
                FileUtil.normalizeFile(
                    new File(System.getProperty("java.home"))));    //NOI18N
            if (jh == null) {
                return Collections.emptySet();
            }
            if (jh.getFileObject("bin/javac") == null && jh.getParent().getFileObject("bin/javac") != null) {   //NOI18N
                jh = jh.getParent();
            }
            return Collections.singleton(jh);
        }

        public FileObject findTool(String toolName) {
            return null;//no tools supported.
        }

        public ClassPath getSourceFolders() {
            return EMPTY;
        }

        public List getJavadocFolders() {
            return Collections.emptyList();
        }

    }

}
