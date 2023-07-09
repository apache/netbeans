/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jakarta.web.beans.xml.impl;

import org.netbeans.modules.jakarta.web.beans.xml.BeanClass;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class BeanClassImpl extends WebBeansComponentImpl implements BeanClass {

    BeanClassImpl( WebBeansModelImpl model, Element e ) {
        super(model, e);
    }
    
    BeanClassImpl( WebBeansModelImpl model) {
        super(model, createNewElement( CLASS, model));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.BeanClass#getBeanClass()
     */
    public String getBeanClass() {
        return getText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.BeanClass#setBeanClass(java.lang.String)
     */
    public void setBeanClass( String value ) {
        setText(CLASS, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent#accept(org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor)
     */
    public void accept( WebBeansVisitor visitor ) {
        visitor.visit(this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent#getComponentType()
     */
    public Class<? extends WebBeansComponent> getComponentType() {
        return BeanClass.class;
    }

}
