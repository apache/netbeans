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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.projects.WebProjectRestSupport;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 * @author PeterLiu
 */
public class SpringHelper {

    private static final String BEAN_TAG = "bean";      //NOI18N
    private static final String ANNOTATION_DRIVEN_TAG = "tx:annotation-driven"; //NOI18N
    private static final String PROPERTY_TAG = "property";  //NOI18N
    private static final String PROPS_TAG = "props";        //NOI18N
    private static final String PROP_TAG = "prop";      //NOI18N
    private static final String ID_ATTR = "id";         //NOI18N
    private static final String CLASS_ATTR = "class";   //NOI18N
    private static final String NAME_ATTR = "name";     //NOI18N
    private static final String VALUE_ATTR = "value";   //NOI18N
    private static final String REF_ATTR = "ref";       //NOI18N
    private static final String KEY_ATTR = "key";       //NOI18N
    private static final String EMF_ID = "entityManagerFactory";        //NOI18N
    private static final String TXM_ID = "transactionManager";      //NOI18N
    private static final String EMF_CLASS = "org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean";       //NOI18N
    private static final String SIMPLE_EMF_CLASS = "org.springframework.orm.jpa.LocalEntityManagerFactoryBean";       //NOI18N
    private static final String DATA_SOURCE_CLASS = "org.springframework.jdbc.datasource.DriverManagerDataSource";  //NOI18N
    private static final String WEAVER_CLASS = "org.springframework.instrument.classloading.glassfish.GlassFishLoadTimeWeaver"; //NOI18N
    private static final String JPA_ADAPTER_CLASS = "org.springframework.orm.jpa.vendor.TopLinkJpaVendorAdapter";   //NOI18N
    private static final String JTA_TXM_CLASS = "org.springframework.transaction.jta.JtaTransactionManager";        //NOI18N
    private static final String JPA_TXM_CLASS = "org.springframework.orm.jpa.JpaTransactionManager";        //NOI18N
    private static final String DATABASE_PLATFORM_CLASS = "oracle.toplink.essentials.platform.database.DerbyPlatform";     //NOI18N
    private static final String PERSISTENCE_ANNOTATION_POST_PROCESSOR_CLASS = "org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor";     //NOI18N
    private static final String HIBERNATE_TRANSACTION_FACTORY_CLASS = "org.hibernate.transaction.JTATransactionFactory";    //NOI18N
    private static final String HIBERANTE_TRANSACTION_LOOKUP_CLASS = "org.hibernate.transaction.SunONETransactionManagerLookup";        //NOI18N
    private static final String PERSISTENCE_UNIT_NAME_PROP = "persistenceUnitName";     //NOI18N
    private static final String DATA_SOURCE_PROP = "dataSource";        //NOI18N
    private static final String DRIVER_CLASS_NAME_PROP = "driverClassName"; //NOI18N
    private static final String URL_PROP = "url";       //NOI18N
    private static final String USER_NAME_PROP = "username";        //NOI18N
    private static final String PASSWORD_PROP = "password";     //NOI18N
    private static final String LOAD_TIME_WEAVER_PROP = "loadTimeWeaver";        //NOI18N
    private static final String JPA_VENDOR_ADAPTER_PROP = "jpaVendorAdapter";       //NOI18N
    private static final String JPA_PROPERTIES_PROP = "jpaProperties";  //NOI18N
    private static final String DATABASE_PLATFORM_PROP = "databasePlatform";     //NOI18N
    private static final String SHOW_SQL_PROP = "showSql";     //NOI18N
    private static final String GENERATE_DDL_PROP = "generateDdl";      //NOI18N
    private static final String HIBERNATE_TRANSACTION_FACTORY_CLASS_PROP = "hibernate.transaction.factory_class";    //NOI18N
    private static final String HIBERANTE_TRANSACTION_LOOKUP_CLASS_PROP = "hibernate.transaction.manager_lookup_class";        //NOI18N
    private static final String HIBERNATE_PROVIDER = "org.hibernate.ejb.HibernatePersistence";      //NOI18N
    private Project project;
    private PersistenceUnit pu;
    private DOMHelper helper;
    private boolean generateDdl;

    public SpringHelper(Project project, PersistenceUnit pu) {
        this.project = project;
        this.pu = pu;
        this.generateDdl = false;
    }

    public void configure() {
        FileObject fobj = getApplicationContextXml(project);

        if (fobj == null) {
            return;
        }
        helper = new DOMHelper(fobj);

        Element emfElement = helper.findElement(BEAN_TAG, ID_ATTR, EMF_ID);

        if (emfElement != null) {
            return;
        }

        boolean hasJTASupport = RestUtils.hasJTASupport(project);

        if (hasJTASupport) {
            emfElement = createBean(EMF_ID, EMF_CLASS);
            emfElement.appendChild(createProperty(PERSISTENCE_UNIT_NAME_PROP, pu.getName()));
            //emfElement.appendChild(createDataSourceProperty());
            //emfElement.appendChild(createWeaverProperty());
            //emfElement.appendChild(createJpaVendorAdapterProperty());
            Element propElement = createJpaProperties();
            if (propElement != null) {
                emfElement.appendChild(propElement);
            }
        } else {
            emfElement = createBean(EMF_ID, SIMPLE_EMF_CLASS);
            emfElement.appendChild(createProperty(PERSISTENCE_UNIT_NAME_PROP, pu.getName()));
        }

        helper.appendChild(emfElement);

        if (hasJTASupport) {
            helper.appendChild(createBean(TXM_ID, JTA_TXM_CLASS));
        } else {
            Element txmElement = createBean(TXM_ID, JPA_TXM_CLASS);
            txmElement.appendChild(createProperty(EMF_ID, EMF_ID, true));
            helper.appendChild(txmElement);
        }

        helper.appendChild(createBean(null, PERSISTENCE_ANNOTATION_POST_PROCESSOR_CLASS));
        helper.appendChild(helper.createElement(ANNOTATION_DRIVEN_TAG));

        helper.save();
    }

    private FileObject getApplicationContextXml(Project project) {
        return MiscUtilities.getApplicationContextXml(project);
    }

    private Element createBean(String id, String clazz) {
        Element beanElement = helper.createElement(BEAN_TAG);

        if (id != null) {
            beanElement.setAttribute(ID_ATTR, id);
        }

        if (clazz != null) {
            beanElement.setAttribute(CLASS_ATTR, clazz);
        }

        return beanElement;
    }

    private Element createProperty(String name, String value) {
        return createProperty(name, value, false);
    }

    private Element createProperty(String name, String value, boolean useRef) {
        Element propElement = helper.createElement(PROPERTY_TAG);
        propElement.setAttribute(NAME_ATTR, name);

        if (value != null) {
            if (useRef) {
                propElement.setAttribute(REF_ATTR, value);
            } else {
                propElement.setAttribute(VALUE_ATTR, value);
            }
        }
        return propElement;
    }

    private Element createDataSourceProperty() {
        Element propElement = createProperty(DATA_SOURCE_PROP, null);
        Element beanElement = createBean(null, DATA_SOURCE_CLASS);

        String url = "";        //NOI18N
        String username = "";   //NOI18N
        String password = "";   //NOI18N
        String driverClass = "";    //NOI18N

        Datasource ds = pu.getDatasource();

        if (ds != null) {
            url = ds.getUrl();
            username = ds.getUsername();
            password = ds.getPassword();
            driverClass = ds.getDriverClassName();
        }

        beanElement.appendChild(createProperty(DRIVER_CLASS_NAME_PROP, driverClass));
        beanElement.appendChild(createProperty(URL_PROP, url));
        beanElement.appendChild(createProperty(USER_NAME_PROP, username));
        beanElement.appendChild(createProperty(PASSWORD_PROP, password));

        propElement.appendChild(beanElement);

        return propElement;
    }

    private Element createWeaverProperty() {
        Element propElement = createProperty(LOAD_TIME_WEAVER_PROP, null);
        propElement.appendChild(createBean(null, WEAVER_CLASS));

        return propElement;
    }

    private Element createJpaVendorAdapterProperty() {
        Element propElement = createProperty(JPA_VENDOR_ADAPTER_PROP, null);
        Element beanElement = createBean(null, JPA_ADAPTER_CLASS);
        beanElement.appendChild(createProperty(DATABASE_PLATFORM_PROP, DATABASE_PLATFORM_CLASS));
        beanElement.appendChild(createProperty(SHOW_SQL_PROP, "true"));         //NOI18N
        beanElement.appendChild(createProperty(GENERATE_DDL_PROP, String.valueOf(generateDdl)));
        propElement.appendChild(beanElement);

        return propElement;
    }

    private Element createJpaProperties() {
        Element propertyElement = null; 

        if (HIBERNATE_PROVIDER.equals(pu.getProvider())) {
            propertyElement = createProperty(JPA_PROPERTIES_PROP, null);
            Element propsElement = helper.createElement(PROPS_TAG);
            Element propElement = helper.createElement(PROP_TAG, HIBERNATE_TRANSACTION_FACTORY_CLASS);
            propElement.setAttribute(KEY_ATTR, HIBERNATE_TRANSACTION_FACTORY_CLASS_PROP);
            propsElement.appendChild(propElement);

            propElement = helper.createElement(PROP_TAG, HIBERANTE_TRANSACTION_LOOKUP_CLASS);
            propElement.setAttribute(KEY_ATTR, HIBERANTE_TRANSACTION_LOOKUP_CLASS_PROP);
            propsElement.appendChild(propElement);

            propertyElement.appendChild(propsElement);
        }

        return propertyElement;
    }
}
