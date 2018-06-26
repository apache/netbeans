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

package org.netbeans.modules.web.project;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * An EjbJar implementation
 * 
 * @author Dongmei Cao
 */
public class EjbJarProvider implements EjbJarImplementation2 {

    public static final String EJB_JAR_DD = "ejb-jar.xml";//NOI18N
    private final ProjectWebModule webModule;
    private final ClassPathProviderImpl cpProvider;
    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;

    public EjbJarProvider(ProjectWebModule webModule, ClassPathProviderImpl cpProvider) {
        this.webModule = webModule;
        this.cpProvider = cpProvider;
    }

    public Profile getJ2eeProfile() {
        return this.webModule.getJ2eeProfile();
    }

    public FileObject getMetaInf() {
        return webModule.getWebInf();
    }

    public FileObject getDeploymentDescriptor() {
        return getDeploymentDescriptor(false);
    }

    public FileObject getDeploymentDescriptor(boolean silent) {
        FileObject webInfFo = this.webModule.getWebInf(silent);
        if (webInfFo==null) {
            return null;
        }
        // ejb-jar.xml is optional
        FileObject dd = webInfFo.getFileObject (EJB_JAR_DD);
        return dd;
    }

    public FileObject[] getJavaSources() {
        return this.webModule.getJavaSources();
    }

       public MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit metadataUnit = MetadataUnit.create(
                    cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                    cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                    cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                    // XXX: add listening on deplymentDescriptor
                    ddFile);
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }

}
