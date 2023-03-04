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
package org.netbeans.modules.j2ee.spi.ejbjar.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 * Contains helper methods for managing {@code EjbReference}.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 * @since 1.25
 */
public final class EjbReferenceSupport {

    /**
     * Creates new {@code EjbReference} for given class within the ejbModule.
     * @param ejbModule in ejb module
     * @param ejbClass for class
     * @return new {@code EjbReference}
     * @throws IOException if there was a problem reading the model from its storage
     */
    public static EjbReference createEjbReference(EjbJar ejbModule, final String ejbClass) throws IOException {

        Map<String, String> ejbInfo = ejbModule.getMetadataModel().runReadAction(
                new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {

            @Override
            public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                Map<String, String> result = new HashMap<String, String>();
                if (ejb != null) {
                    result.put(Ejb.EJB_NAME, ejb.getEjbName());
                    result.put(EjbRef.EJB_REF_TYPE, ejb instanceof Entity ? EjbRef.EJB_REF_TYPE_ENTITY : EjbRef.EJB_REF_TYPE_SESSION);
                    result.put(EntityAndSession.LOCAL, ejb.getLocal());
                    result.put(EntityAndSession.LOCAL_HOME, ejb.getLocalHome());
                    result.put(EntityAndSession.REMOTE, ejb.getRemote());
                    result.put(EntityAndSession.HOME, ejb.getHome());
                }
                return result;
            }
        });
        return EjbReference.create(
                ejbClass,
                ejbInfo.get(EjbRef.EJB_REF_TYPE),
                ejbInfo.get(EntityAndSession.LOCAL),
                ejbInfo.get(EntityAndSession.LOCAL_HOME),
                ejbInfo.get(EntityAndSession.REMOTE),
                ejbInfo.get(EntityAndSession.HOME),
                ejbModule);
    }
}
