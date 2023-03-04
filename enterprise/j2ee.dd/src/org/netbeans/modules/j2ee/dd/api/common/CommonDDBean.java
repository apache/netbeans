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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Parent of all DD API interfaces.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface CommonDDBean {
    /**
     * Adds property change listener to particular CommonDDBean object (WebApp object).
     * @param pcl property change listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl);
    /**
     * Removes property change listener from CommonDDBean object.
     * @param pcl property change listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl);
    /**
     * Sets the id attribute for related dd element. E.g.<pre>
&lt;servlet id="xyz"&gt;
  ...
&lt;/servlet&gt;
     *</pre>
     *
     * @param value the value for id attribute
     */    
    public void setId(java.lang.String value);
    /**
     * Returns the id attribute for related dd element.<br>In most cases the id attribute is not specified.
     * @return value of id attribute or null if not specified
     */   
    public java.lang.String getId();
    /**
     * Returns the clonned CommonDDBean object.
     * @return the clonned (not bound to bean graph) CommonDDBean object
     */       
    public Object clone();
    /**
     * Returns the CommonDDBean object or array of CommonDDBean object for given property.<br>
     * E.g. for retrieving the servlet-class value on Servlet object he <b>getValue("ServletClass");</b> can be used.
     * @param propertyName name of the property the value is looking for
     * @return the bean/array of beans related to given property
     */      
    public Object getValue(String propertyName);
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

}
