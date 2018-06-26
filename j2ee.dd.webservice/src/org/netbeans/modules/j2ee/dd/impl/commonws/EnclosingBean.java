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

/**
 * Superclass for most s2b beans that provides useful methods for creating and finding beans
 * in bean graph.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.commonws;

import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.*;

import org.netbeans.modules.j2ee.dd.impl.webservices.CommonDDAccess;

public abstract class EnclosingBean extends BaseBean implements CommonDDBean, CreateCapability, FindCapability {
    
    /** Creates a new instance of EnclosingBean */
    public EnclosingBean(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    
    /**
    * Method is looking for the nested bean according to the specified property and value 
    *
    * @param beanName e.g. "Servlet" or "ResourceRef"
    * @param propertyName e.g. "ServletName" or ResourceRefName"
    * @param value specific propertyName value e.g. "ControllerServlet" or "jdbc/EmployeeAppDb"
    * @return Bean satisfying the parameter values or null if not found
    */ 
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return (CommonDDBean)CommonDDAccess.findBeanByName(this, beanName, propertyName, value);
    }
    
    /**
    * An empty (not bound to schema2beans graph) bean is created corresponding to beanName 
    * regardless the Servlet Spec. version 
    * @param beanName bean name e.g. Servlet
    * @return CommonDDBean corresponding to beanName value
    */
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return (CommonDDBean)CommonDDAccess.newBean(this, beanName, getPackagePostfix ());
    }
    
    private String getPackagePostfix () {
        return CommonDDAccess.WEBSERVICES_1_1;
    }
    
    public void write (org.openide.filesystems.FileObject fo) throws java.io.IOException {
        // PENDING
        // need to be implemented with Dialog opened when the file object is locked
    }
    
    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, NameAlreadyUsedException {
        if (keyProperty!=null) {
            Object keyValue = null;
            if (propertyNames!=null)
                for (int i=0;i<propertyNames.length;i++) {
                    if (keyProperty.equals(propertyNames[i])) {
                        keyValue=propertyValues[i];
                        break;
                    }
                }
            if (keyValue!=null && keyValue instanceof String) {
                if (findBeanByName(beanName, keyProperty,(String)keyValue)!=null) {
                    throw new NameAlreadyUsedException(beanName,  keyProperty, (String)keyValue);
                }   
            }
        }
        CommonDDBean newBean = createBean(beanName);
        if (propertyNames!=null)
            for (int i=0;i<propertyNames.length;i++) {
                try {
                    ((BaseBean)newBean).setValue(propertyNames[i],propertyValues[i]);
                } catch (IndexOutOfBoundsException ex) {
                    ((BaseBean)newBean).setValue(propertyNames[i],new Object[]{propertyValues[i]});
                }
            }
        CommonDDAccess.addBean(this, newBean, beanName, getPackagePostfix ());
        return newBean;
    }
    
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        try {
            return addBean(beanName,null,null,null);
        } catch (NameAlreadyUsedException ex){}
        return null;
    }
    
    public void merge(RootInterface root, int mode) {
        this.merge((BaseBean)root,mode);
    }
    
}
