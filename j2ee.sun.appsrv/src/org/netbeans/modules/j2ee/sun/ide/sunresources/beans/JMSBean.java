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
