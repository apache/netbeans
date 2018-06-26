/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
