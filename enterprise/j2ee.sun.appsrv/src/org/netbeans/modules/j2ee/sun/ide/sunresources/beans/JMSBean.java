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
/*
 * JMSBean.java
 *
 * Created on November 13, 2003, 3:05 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JavaMsgServiceResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource;

/**
 *
 * @author  nityad
 */
public class JMSBean extends JavaMsgServiceResource implements java.io.Serializable{

    /** Creates a new instance of JMSBean */
    public JMSBean() {
    }

    public String getName() {
        return super.getName();
    }

    public static JMSBean createBean(Resources resources) {
        JMSBean bean = new JMSBean();
        //name attribute in bean is for studio display purpose.
        //It is not part of the resource dtd.
        ConnectorResource connresource = resources.getConnectorResource(0);
        bean.setName(connresource.getJndiName());
        bean.setDescription(connresource.getDescription());
        bean.setIsEnabled(connresource.getEnabled());
        bean.setJndiName(connresource.getJndiName());
        bean.setPoolName(connresource.getPoolName());

        ConnectorConnectionPool connpoolresource = resources.getConnectorConnectionPool(0);
        bean.setResAdapter(connpoolresource.getResourceAdapterName());
        bean.setResType(connpoolresource.getConnectionDefinitionName());

        PropertyElement[] extraProperties = connpoolresource.getPropertyElement();
        Vector vec = new Vector();
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }

        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }

        return bean;
    }

    public static JMSBean createBean(AdminObjectResource aoresource) {
        JMSBean bean = new JMSBean();

        //name attribute in bean is for studio display purpose.
        //It is not part of the resource dtd.
        bean.setName(aoresource.getJndiName());
        bean.setDescription(aoresource.getDescription());
        bean.setIsEnabled(aoresource.getEnabled());
        bean.setJndiName(aoresource.getJndiName());
        bean.setResAdapter(aoresource.getResAdapter());
        bean.setResType(aoresource.getResType());

        PropertyElement[] extraProperties = aoresource.getPropertyElement();
        Vector vec = new Vector();
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }

        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }

        return bean;
    }

    public static JMSBean createBean(JmsResource jmsresource) {
        JMSBean bean = new JMSBean();

        //name attribute in bean is for studio display purpose.
        //It is not part of the resource dtd.
        bean.setName(jmsresource.getJndiName());
        bean.setDescription(jmsresource.getDescription());
        bean.setIsEnabled(jmsresource.getEnabled());
        bean.setJndiName(jmsresource.getJndiName());
        bean.setResAdapter("jmsra"); //NOI18N
        bean.setResType(jmsresource.getResType());

        PropertyElement[] extraProperties = jmsresource.getPropertyElement();
        Vector vec = new Vector();
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }

        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }

        return bean;
    }

    public Resources getConnectorGraph(){
        Resources res = getResourceGraph();
        return getConnectorBeanInGraph(res);
    }

    public Resources getConnectorBeanInGraph(Resources res){
         ConnectorResource connresource = res.newConnectorResource();
         connresource.setDescription(getDescription());
         connresource.setEnabled(getIsEnabled());
         connresource.setJndiName(getJndiName());
         connresource.setPoolName(getJndiName());

         ConnectorConnectionPool connpoolresource = res.newConnectorConnectionPool();
         connpoolresource.setName(getJndiName());
         connpoolresource.setResourceAdapterName(getResAdapter());
         connpoolresource.setConnectionDefinitionName(getResType());
         // set properties
         NameValuePair[] params = getExtraParams();
         if (params != null && params.length > 0) {
             for (int i = 0; i < params.length; i++) {
                 NameValuePair pair = params[i];
                 PropertyElement prop = connpoolresource.newPropertyElement();
                 prop = populatePropertyElement(prop, pair);
                 connpoolresource.addPropertyElement(prop);
             }
         }

         res.addConnectorResource(connresource);
         res.addConnectorConnectionPool(connpoolresource);

         return res;
     }

     public Resources getAdminObjectGraph(){
        Resources res = getResourceGraph();
        return getAdminObjectBeanInGraph(res);
    }

    public Resources getAdminObjectBeanInGraph(Resources res){
         AdminObjectResource aoresource = res.newAdminObjectResource();
         aoresource.setDescription(getDescription());
         aoresource.setEnabled(getIsEnabled());
         aoresource.setJndiName(getJndiName());
         aoresource.setResAdapter(getResAdapter());
         aoresource.setResType(getResType());

         // set properties
         NameValuePair[] params = getExtraParams();
         if (params != null && params.length > 0) {
             for (int i = 0; i < params.length; i++) {
                 NameValuePair pair = params[i];
                 PropertyElement prop = aoresource.newPropertyElement();
                 prop = populatePropertyElement(prop, pair);
                 aoresource.addPropertyElement(prop);
             }
         }

         res.addAdminObjectResource(aoresource);
         return res;
     }
}
