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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
