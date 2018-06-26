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

package org.netbeans.modules.j2ee.sun.dd.api;

import org.netbeans.modules.schema2beans.BaseBean;

/**
 * Parent of all DD API interfaces.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface CommonDDBean {

    public static final int MERGE_INTERSECT = BaseBean.MERGE_INTERSECT;
    public static final int MERGE_UNION	= BaseBean.MERGE_UNION;
    public static final int MERGE_UPDATE = BaseBean.MERGE_UPDATE;
        
    public void merge(CommonDDBean root, int mode); 
    
    /** Adds property change listener to particular CommonDDBean object (WebApp object).
     * 
     * @param pcl property change listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl);
    
    /** Removes property change listener from CommonDDBean object.
     * 
     * @param pcl property change listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl);
    
    /**
     * Returns the CommonDDBean object or array of CommonDDBean object for given property.<br>
     * E.g. for retrieving the servlet-class value on Servlet object he <b>getValue("ServletClass");</b> can be used.
     * @param propertyName name of the property the value is looking for
     * @return the bean/array of beans related to given property
     */      
    public Object getValue(String propertyName);
    
    public Object[] getValues(String name);
    
    public Object getValue(String name, int index);
    
    public void setValue(String name, Object value);
    
    public void setValue(String name, Object[] value);
    
    public void setValue(String name, int index, Object value);
    
    public String getAttributeValue(String name);
    
    public String getAttributeValue(String propName, String name);
    
    public String getAttributeValue(String propName, int index, String name);
    
    public void setAttributeValue(String name, String value);
    
    public void setAttributeValue(String propName, int index, String name, String value);
    
    public void setAttributeValue(String propName, String name, String value);
    
    public String[] findPropertyValue(String propName, Object value);
    
    public int addValue(String name, Object value);
    
    public int removeValue(String name, Object value);
    
    public void removeValue(String name, int index);
    
    public int size(String name);
         
    
    /**
     * Writes the whole DD or its fraction (element related to CommonDDBean) to output stream.<br>
     * For DD root object there is more convenient to use the {@link org.netbeans.modules.j2ee.dd.api.common.RootInterface#write} method.<br>
     * The correct usage with file objects is :<pre>
WebApp ejb;
FileObject fo;
...
//  code that initializes and modifies the ejb object
...
FileLock lock;
try {
    lock=fo.lock();
} catch (FileAlreadyLockedException e) {
    // handling the exception
}
if (lock!=null) {
    try {
        OutputStream os=fo.getOutputStream(lock);
        try {
            ejb.write(os);
        } finally {
            os.close();
        }
    } finally {
        lock.releaseLock();
    }
}
...
     *</pre>
     * @param os output stream for writing
     */  
    public void write(java.io.OutputStream os) throws java.io.IOException;

    public void write(java.io.Writer w) throws java.io.IOException, org.netbeans.modules.j2ee.sun.dd.api.DDException;

    /** Check if there are any non-null subproperties in this bean (other than
     *  the name property, if specified.
     */
    public boolean isTrivial(String nameProperty);
    
    /** Clone this bean.
     */
    public Object clone();
  
    /** Clone this bean, but as an instance from the model tree of a different version.
     */
    public CommonDDBean cloneVersion(String version);
        
    /**
     * Used to convert an graph to the corresponding XML in String form.
     */
    public String dumpBeanNode(); 
    
    public CommonDDBean getPropertyParent(String name);
    
}
