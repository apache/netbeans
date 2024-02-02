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
package org.netbeans.modules.web.beans.xml.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.namespace.QName;

import org.netbeans.modules.web.beans.xml.Alternatives;
import org.netbeans.modules.web.beans.xml.BeanClass;
import org.netbeans.modules.web.beans.xml.Decorators;
import org.netbeans.modules.web.beans.xml.Interceptors;
import org.netbeans.modules.web.beans.xml.Stereotype;
import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import static org.netbeans.modules.web.beans.xml.WebBeansComponent.WEB_BEANS_NAMESPACE_OLD;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;


/**
 * @author ads
 *
 */
public enum WebBeansElements {
    
    BEANS("beans"),
    DECORATORS(Decorators.DECORATORS),
    INTERCEPTORS( Interceptors.INTERCEPTORS),
    ALTERNATIVES( Alternatives.ALTERNATIVES),
    CLASS( BeanClass.CLASS),
    STEREOTYPE( Stereotype.STEREOTYPE);
    
    WebBeansElements( String name ){
        myName = name;
    }
    
    public String getName() {
        return myName;
    }

    public QName getQName(WebBeansModelImpl model) {
        String ns = WebBeansComponent.WEB_BEANS_NAMESPACE;
        if( model.getRootComponent() instanceof AbstractDocumentComponent) {
            ns = ((AbstractDocumentComponent)model.getRootComponent()).getQName().getNamespaceURI();
        }
        return new QName( ns, getName() );
    }

    
    public static Set<QName> allQNames(WebBeansModelImpl model) {
        if ( myQNames.get() == null ) {
            Set<QName> set = new HashSet<QName>( values().length );
            for (WebBeansElements element : values() ) {
                set.add( element.getQName(model) );
            }
            myQNames.compareAndSet( null, set );
        }
        return myQNames.get();
    }
    
    private String myName;

    private static AtomicReference<Set<QName>> myQNames =
        new AtomicReference<Set<QName>>();
}
