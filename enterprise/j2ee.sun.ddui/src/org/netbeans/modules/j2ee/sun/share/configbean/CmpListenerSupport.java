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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener;
import org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener.AbstractBeanVisitor;
import org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener.BeanVisitor;
import org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener.EntityAndSessionVisitor;
import org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener.NameVisitor;
import org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener.NameVisitorFactory;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;


/** CMP support on top of the general descriptor listener infrastracture that
 *  was migrated to glassfish.eecommon.
 *
 * @author Peter Williams
 */
public class CmpListenerSupport {

    static final void enableCmpListenerSupport() {
        // update name visitor factory
        DescriptorListener.addNameVisitorFactory(new CmpNameVisitorFactory());

        // update bean visitor mappings
        Map<String, BeanVisitor> cmpVisitorMap = new HashMap<String, BeanVisitor>();
        EntityVisitor entityVisitor = new EntityVisitor();
        CmpEntityVisitor cmpEntityVisitor = new CmpEntityVisitor();
        CmpFieldVisitor cmpFieldVisitor = new CmpFieldVisitor();
        cmpVisitorMap.put("/EjbJar/EnterpriseBeans/Entity", entityVisitor);
        cmpVisitorMap.put("/EjbJar/EnterpriseBeans/Entity/EjbName", cmpEntityVisitor);
        cmpVisitorMap.put("/EjbJar/EnterpriseBeans/Entity/CmpField", cmpFieldVisitor);
        cmpVisitorMap.put("/EjbJar/EnterpriseBeans/Entity/CmpField/FieldName", cmpFieldVisitor);
        DescriptorListener.addBeanVisitorMappings(cmpVisitorMap);
    }

    private static final class CmpNameVisitorFactory implements NameVisitorFactory {

        public NameVisitor createNameVisitor(CommonDDBean bean) {
            NameVisitor result = null;
            if(bean instanceof CmpField) {
                result = new CmpFieldNameVisitor();
            }
            return result;
        }

    }

    private static class CmpFieldNameVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((CmpField) bean).getFieldName();
        }
        public String getNameProperty() {
            return "/" + CmpField.FIELD_NAME;
        }
    }

    private static boolean isCMP(Object ddBean) {
        if(ddBean instanceof Entity) {
            Entity entity = (Entity)ddBean;
            if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType())) {
                return true;
            }
        }
        return false;
    }

    private static final class CmpFieldVisitor extends AbstractBeanVisitor {
        @Override
        public void beanDeleted(GlassfishConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean oldDD) {
            super.beanDeleted(config, xpath, sourceDD, oldDD);
            if(config instanceof SunONEDeploymentConfiguration) {
                SunONEDeploymentConfiguration s1config = (SunONEDeploymentConfiguration) config;
                if(isCMP(sourceDD)) {
                    String ejbName = ((Entity)sourceDD).getEjbName();

                    if (Utils.notEmpty(ejbName) && (oldDD instanceof CmpField)) {
                        String fieldName = ((CmpField)oldDD).getFieldName();
                        if (Utils.notEmpty(fieldName)) {
                            s1config.removeMappingForCmpField(ejbName, fieldName);
                        }
                    }
                }
            }
        }
        @Override
        public void fieldChanged(GlassfishConfiguration config, String xpath,
                Object sourceDD, Object oldValue, Object newValue) {
            super.fieldChanged(config, xpath, sourceDD, oldValue, newValue);
            if(config instanceof SunONEDeploymentConfiguration) {
                SunONEDeploymentConfiguration s1config = (SunONEDeploymentConfiguration) config;
                if(isCMP(sourceDD)) {
                    String ejbName = ((Entity)sourceDD).getEjbName();
                    String oldFieldName = oldValue.toString();
                    String newFieldName = newValue.toString();

                    if (Utils.notEmpty(oldFieldName) && Utils.notEmpty(newFieldName) &&
                            !(oldFieldName.equals(newFieldName))) {
                        s1config.renameMappingForCmpField(ejbName, oldFieldName, newFieldName);
                    }
                }
            }
        }
    }

    private static final class CmpEntityVisitor extends AbstractBeanVisitor {
        @Override
        public void fieldChanged(GlassfishConfiguration config, String xpath,
                Object sourceDD, Object oldValue, Object newValue) {
            super.fieldChanged(config, xpath, sourceDD, oldValue, newValue);
            if(config instanceof SunONEDeploymentConfiguration) {
                SunONEDeploymentConfiguration s1config = (SunONEDeploymentConfiguration) config;
                if(isCMP(sourceDD)) {
                    String oldEjbName = oldValue.toString();
                    String newEjbName = newValue.toString();

                    if (Utils.notEmpty(oldEjbName) && Utils.notEmpty(newEjbName) &&
                            !(oldEjbName.equals(newEjbName))) {
                        s1config.renameMappingForCmp(oldEjbName, newEjbName);
                    }
                }
            }
        }
    }

    private static final class EntityVisitor extends EntityAndSessionVisitor {
        
        @Override
        public void beanDeleted(GlassfishConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean oldDD) {
            super.beanDeleted(config, xpath, sourceDD, oldDD);
            if(config instanceof SunONEDeploymentConfiguration) {
                SunONEDeploymentConfiguration s1config = (SunONEDeploymentConfiguration) config;
                if(isCMP(oldDD)) {
                    String ejbName = ((Entity)oldDD).getEjbName();
                    if(Utils.notEmpty(ejbName)) {
                        s1config.removeMappingForCmp(ejbName);
                    }
                }
            }
        }
    }

}
