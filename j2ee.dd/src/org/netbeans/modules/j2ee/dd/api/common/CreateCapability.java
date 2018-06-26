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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Ability to create a new CommonDDBean objects.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface CreateCapability {
    /**
     * An empty (not bound to bean graph) CommonDDBean object is created corresponding to beanName,
     * regardless the servlet spec. version
     * @param beanName bean name e.g. "Servlet"
     * @return CommonDDBean object corresponding to beanName value
     */
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException ;
    /**
     * An empty bean is created corresponding to beanName regardless the srvlet spec. version.
     * The bean is nested under the actual bean.<br>
     * There is an array of properties that will be initialized.
     * The method is useful for DD elements containing
     * single properties like Servlet, Taglib, ResourceRef etc.<br>
     * Usage<pre>
...
Servlet servlet = (Servlet)webApp.addBean("Servlet",new String[]{"ServletName","ServletClass"},
                                          new Object[]{"TestServlet","mypackage.TestServlet"},"ServletName");
servlet.addBean("InitParam",new String[]{"ParamName","ParamValue"},
                new Object[]{"car","Volvo"},"ParamName");
...
     *</pre>
     * @param beanName bean name
     * @param propertyNames array of properties that should be initialized
     * @param propertyValues array of initialization values, usually strings
     * @param keyProperty the property name that is checked in order to evoid the duplicity, e.g. ServletName.<br>
     * <b>keyProperty should be included to propertyNames array.</b>
     * @return CommonDDBean object that has been nested inside the current element (CommonDDBean object).
     * @exception ClassNotFoundException thrown when the class for beanName cannot be found under the current DD element
     * @exception NameAlreadyUsedException thrown when object with keyProperty value already exists.
     */   
    public CommonDDBean addBean (String beanName, String[] propertyNames, Object [] propertyValues, String keyProperty)
        throws ClassNotFoundException, NameAlreadyUsedException;
    /**
     * An empty bean is created corresponding to beanName, regardless the servlet spec. version.
     * The bean is included under the actual bean. The method is useful for elements containing only
     * non-single properties like WelcomeFileList, JspConfig etc.
     *<pre>
...
JspConfig config = webApp.addBean("JspConfig");
jspConfig.addBean("JspPropertyGroup",new String[]{"UrlPattern","IncludePrelude","IncludeCoda"},
                  new String[]{"*.jsp","/jsp/prelude.html","/jsp/coda.html"},null);
...
     *</pre>
     * @param beanName bean name e.g. "JspConfig"
     * @return CommonDDBean object corresponding to beanName value
     * @exception ClassNotFoundException thrown when the class for beanName cannot be found under the current DD element
     */
    public CommonDDBean addBean (String beanName) throws ClassNotFoundException;
}
