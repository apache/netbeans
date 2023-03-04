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
package org.netbeans.modules.tomcat5.j2ee;

import java.net.URL;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * This is TomEE only class.
 *
 * @author Petr Hejl
 */
// XXX web service expert should review this
public class JaxRsStackSupportImpl implements JaxRsStackSupportImplementation {

    private static final String JAX_RS_APPLICATION_CLASS = "javax.ws.rs.core.Application"; //NOI18N
    private static final String JAX_RS_APPLICATION_CLASS_JAKARTAEE = "jakarta.ws.rs.core.Application"; //NOI18N

    private final TomcatPlatformImpl j2eePlatform;

    JaxRsStackSupportImpl(TomcatPlatformImpl j2eePlatform) {
        this.j2eePlatform = j2eePlatform;
    }

    @Override
    public boolean addJsr311Api(Project project) {
        // return true (behaves like added) when JAX-RS is on classpath
        return isBundled(JAX_RS_APPLICATION_CLASS) || isBundled(JAX_RS_APPLICATION_CLASS_JAKARTAEE);
    }

    @Override
    public boolean extendsJerseyProjectClasspath(Project project) {
        // declared as extended when JAX-RS is on classpath
        // suppose that TomEE has its own implementation of JAX-RS
        return isBundled(JAX_RS_APPLICATION_CLASS) || isBundled(JAX_RS_APPLICATION_CLASS_JAKARTAEE);
    }

    @Override
    public void removeJaxRsLibraries(Project project) {
    }

    @Override
    public void configureCustomJersey(Project project) {
    }

    @Override
    public boolean isBundled(String classFqn) {
        j2eePlatform.getLibraries();
        for (LibraryImplementation lib : j2eePlatform.getLibraries()) {
            List<URL> urls = lib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
            for (URL url : urls) {
                FileObject root = URLMapper.findFileObject(url);
                if (FileUtil.isArchiveFile(root)) {
                    root = FileUtil.getArchiveRoot(root);
                }
                String path = classFqn.replace('.', '/') + ".class"; //NOI18N
                if (root.getFileObject(path) != null) {
                    return true;
                }
            }
        }

        return false;
    }
}
