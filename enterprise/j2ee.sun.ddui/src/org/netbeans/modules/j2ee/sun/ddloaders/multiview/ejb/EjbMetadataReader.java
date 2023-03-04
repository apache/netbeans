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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;

/**
 *
 * @author Peter Williams
 */
public class EjbMetadataReader implements MetadataModelAction<EjbJarMetadata, Map<String, Object>> {

    /** Entry point to generate map from standard descriptor
     */
    public static Map<String, Object> readDescriptor(EjbJar ejbJar) {
        return genProperties(ejbJar);
    }
    
    /** Entry point to generate map from annotation metadata
     */
    public Map<String, Object> run(EjbJarMetadata metadata) throws Exception {
        return genProperties(metadata.getRoot());
    }
    
    /** Maps interesting fields from ejb-jar descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    private static Map<String, Object> genProperties(EjbJar ejbJar) {
        Map<String, Object> data = new HashMap<String, Object>();
        if(ejbJar != null) {
            EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
            if(eb != null) {
                Session [] sessionBeans = eb.getSession();
                if(sessionBeans != null) {
                    for(Session session: sessionBeans) {
                        String ejbName = session.getEjbName();
                        if(Utils.notEmpty(ejbName)) {
                            Map<String, Object> sessionMap = new HashMap<String, Object>();
                            data.put(ejbName, sessionMap);
                            sessionMap.put(DDBinding.PROP_NAME, ejbName);

                            String sessionType = session.getSessionType();
                            if(sessionType != null && sessionType.length() > 0) {
                                sessionMap.put(DDBinding.PROP_SESSION_TYPE, sessionType);
                            }
                        }
                    }
                }
                MessageDriven [] messageBeans = eb.getMessageDriven();
                if(messageBeans != null) {
                    for(MessageDriven message: messageBeans) {
                        String ejbName = message.getEjbName();
                        if(Utils.notEmpty(ejbName)) {
                            Map<String, Object> messageMap = new HashMap<String, Object>();
                            data.put(ejbName, messageMap);
                            messageMap.put(DDBinding.PROP_NAME, ejbName);

//                        ActivationConfig config = message.getActivationConfig();
//                        if(config != null) {
//                            ActivationConfigProperty [] properties = config.getActivationConfigProperty();
//                            if(properties != null) {
//                                for(ActivationConfigProperty property: properties) {
//                                    String name = property.getActivationConfigPropertyName();
//                                    if("destinationType".equals(name)) {
//                                        String destinationType = property.getActivationConfigPropertyValue();
//                                        messageMap.put("DestinationType", destinationType);
//                                    }
//                                }
//                            }
//                        }
                        }
                    }
                }
                Entity [] entityBeans = eb.getEntity();
                if(entityBeans != null) {
                    for(Entity entity: entityBeans) {
                        String ejbName = entity.getEjbName();
                        if(Utils.notEmpty(ejbName)) {
                            Map<String, Object> entityMap = new HashMap<String, Object>();
                            data.put(ejbName, entityMap);
                            entityMap.put(DDBinding.PROP_NAME, ejbName);

                            String persistenceType = entity.getPersistenceType();
                            if(persistenceType != null && persistenceType.length() > 0) {
                                entityMap.put(DDBinding.PROP_PERSISTENCE_TYPE, persistenceType);
                            }
                        }
                    }
                }
            }
        }
        
        return data.size() > 0 ? data : null;
    }

}
