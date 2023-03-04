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

package org.netbeans.modules.web.project;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
/**
 *
 * @author sp153251
 */
public class WebJPATargetInfo implements JPATargetInfo {

    public WebJPATargetInfo(WebProject project) {
    }

    @Override
    public TargetType getType(FileObject target, final String fqn) {
        EjbJar ejbjar = EjbJar.getEjbJar(target);
        MetadataModel<EjbJarMetadata> metadataModel = ejbjar==null ? null : ejbjar.getMetadataModel();
        boolean isEjb = false;
        if(metadataModel != null){
            try {
                String ret = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                    @Override
                    public String run(EjbJarMetadata metadata) throws Exception {
                        Ejb ejb = metadata.findByEjbClass(fqn);
                        return ejb !=null ? "" : null;
                    }
                });
                isEjb = ret!=null;
            } catch (MetadataModelException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return isEjb ? TargetType.EJB : TargetType.ANY;
    }

}
