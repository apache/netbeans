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
 * JavaMailSessionBean.java
 *
 * Created on September 17, 2003, 2:50 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.MailSessionResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class JavaMailSessionBean extends MailSessionResource implements java.io.Serializable {
    
    /** Creates a new instance of JavaMailSessionBean */
    public JavaMailSessionBean() {
    }
    
    public String getName() {
        return super.getName();
    }
    
    public String getJndiName(){
        return super.getJndiName();
    }
    
    public static JavaMailSessionBean createBean(MailResource mailresource) {
        JavaMailSessionBean bean = new JavaMailSessionBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the mail-resource dtd.
        bean.setName(mailresource.getJndiName());
        bean.setDescription(mailresource.getDescription());
        bean.setJndiName(mailresource.getJndiName());
        bean.setStoreProt(mailresource.getStoreProtocol());
        bean.setStoreProtClass(mailresource.getStoreProtocolClass());
        bean.setTransProt(mailresource.getTransportProtocol());
        bean.setTransProtClass(mailresource.getTransportProtocolClass());
        bean.setHostName(mailresource.getHost());
        bean.setUserName(mailresource.getUser());
        bean.setFromAddr(mailresource.getFrom());
        bean.setIsDebug(mailresource.getDebug());
        bean.setIsEnabled(mailresource.getEnabled());
           
        PropertyElement[] extraProperties = mailresource.getPropertyElement();
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
    
    public Resources getGraph(){
        Resources res = getResourceGraph();
        return getBeanInGraph(res);
    }    
    
    public Resources getBeanInGraph(Resources res){
        MailResource mlresource = res.newMailResource();
        mlresource.setDescription(getDescription());
        mlresource.setJndiName(getJndiName());
        mlresource.setStoreProtocol(getStoreProt());
        mlresource.setStoreProtocolClass(getStoreProtClass());
        mlresource.setTransportProtocol(getTransProt());
        mlresource.setTransportProtocolClass(getTransProtClass());
        mlresource.setHost(getHostName());
        mlresource.setUser(getUserName());
        mlresource.setFrom(getFromAddr());
        mlresource.setDebug(getIsDebug());
        mlresource.setEnabled(getIsEnabled());
        
        // set properties
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = mlresource.newPropertyElement();
                prop = populatePropertyElement(prop, pair);
                mlresource.addPropertyElement(prop);
            }
        }
        
        res.addMailResource(mlresource);
        return res;
    }
        
}
