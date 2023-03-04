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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarMetadataImpl implements EjbJarMetadata {
    
    private final EjbJar ejbJar;
    private final ClasspathInfo cpInfo;
    
    public EjbJarMetadataImpl(EjbJar ejbJar, ClasspathInfo cpInfo) {
        this.ejbJar = ejbJar;
        this.cpInfo = cpInfo;
    }

    public EjbJar getRoot() {
        return ejbJar;
    }

    public Ejb findByEjbClass(String ejbClass) {
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans != null) {
            for (Ejb ejb : enterpriseBeans.getEjbs()) {
                if (ejbClass.equals(ejb.getEjbClass())) {
                    return ejb;
                }
            }
        }
        return null;
    }

    public FileObject findResource(String resourceName) {
        return cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resourceName);
    }
    
}
