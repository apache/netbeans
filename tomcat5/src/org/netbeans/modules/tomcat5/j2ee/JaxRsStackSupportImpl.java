/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

    private final TomcatPlatformImpl j2eePlatform;

    JaxRsStackSupportImpl(TomcatPlatformImpl j2eePlatform) {
        this.j2eePlatform = j2eePlatform;
    }

    @Override
    public boolean addJsr311Api(Project project) {
        // return true (behaves like added) when JAX-RS is on classpath
        return isBundled(JAX_RS_APPLICATION_CLASS);
    }

    @Override
    public boolean extendsJerseyProjectClasspath(Project project) {
        // declared as extended when JAX-RS is on classpath
        // suppose that TomEE has its own implementation of JAX-RS
        return isBundled(JAX_RS_APPLICATION_CLASS);
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
