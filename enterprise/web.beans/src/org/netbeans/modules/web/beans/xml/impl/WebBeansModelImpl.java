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
package org.netbeans.modules.web.beans.xml.impl;

import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.web.beans.xml.WebBeansModel;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class WebBeansModelImpl extends AbstractDocumentModel<WebBeansComponent> 
    implements WebBeansModel 
{

    public WebBeansModelImpl( ModelSource source ) {
        super(source);
        myFactory = new WebBeansComponentFactoryImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentModel#createRootComponent(org.w3c.dom.Element)
     */
    @Override
    public WebBeansComponent createRootComponent( Element root ) {
        BeansImpl beans = (BeansImpl)getFactory().createComponent( root, null);
        if ( beans!= null ){
            myRoot = beans;
        }
        else {
            return null;
        }
        return getBeans();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentModel#getComponentUpdater()
     */
    @Override
    public ComponentUpdater<WebBeansComponent> getComponentUpdater() {
        if ( mySyncUpdateVisitor== null ){
            mySyncUpdateVisitor  = new SyncUpdateVisitor();
        }
        return mySyncUpdateVisitor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.DocumentModel#createComponent(org.netbeans.modules.xml.xam.dom.DocumentComponent, org.w3c.dom.Element)
     */
    public WebBeansComponent createComponent( WebBeansComponent parent,
            Element element )
    {
        return getFactory().createComponent(element, parent );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.DocumentModel#getRootComponent()
     */
    public WebBeansComponent getRootComponent() {
        if(myRoot == null) {
            refresh();
        }
        return myRoot;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansModel#getBeans()
     */
    public BeansImpl getBeans() {
        return (BeansImpl)getRootComponent();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansModel#getFactory()
     */
    public WebBeansComponentFactoryImpl getFactory() {
        return myFactory;
    }
    
    public Set<QName> getQNames() {
        return WebBeansElements.allQNames(this);
    }
    
    private BeansImpl myRoot;
    
    private SyncUpdateVisitor mySyncUpdateVisitor;
    
    private WebBeansComponentFactoryImpl myFactory;
}
