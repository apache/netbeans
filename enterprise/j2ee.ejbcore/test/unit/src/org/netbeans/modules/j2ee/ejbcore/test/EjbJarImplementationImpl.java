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

package org.netbeans.modules.j2ee.ejbcore.test;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarImplementationImpl implements EjbJarImplementation2 {
    
    private final Profile j2eeProfile;
    private final FileObject ddFileObject;
    private final FileObject[] sources;
    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    
    public EjbJarImplementationImpl(Profile j2eeProfile, FileObject ddFileObject, FileObject[] sources) {
        this.j2eeProfile = j2eeProfile;
        this.ddFileObject = ddFileObject;
        this.sources = sources;
    }

    public Profile getJ2eeProfile() {
        return j2eeProfile;
    }

    public FileObject getMetaInf() {
        return ddFileObject.getParent();
    }
    
    public FileObject getDeploymentDescriptor() {
        return ddFileObject;
    }
    
    public FileObject[] getJavaSources() {
        return sources;
    }
    
    public MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            MetadataUnit metadataUnit = MetadataUnit.create(
                    ClassPathSupport.createClassPath(new FileObject[0]),
                    ClassPath.getClassPath(sources[0], ClassPath.COMPILE),
                    ClassPath.getClassPath(sources[0], ClassPath.SOURCE),
                    ddFileObject == null ? null : FileUtil.toFile(ddFileObject)
                    );
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }
    
}
