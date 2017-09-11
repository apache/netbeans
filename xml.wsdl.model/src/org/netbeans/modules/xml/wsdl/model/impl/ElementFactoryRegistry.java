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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.spi.*;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author rico
 * @author Nam Nguyen
 *
 * Registry for factories of WSDL elements. In order to register an ElementFactory,
 * a QName must be provided of an element for which the factory will create a
 * WSDLComponent.
 */
public class ElementFactoryRegistry {
    
    private static ElementFactoryRegistry registry = null;
    private Map<QName, ElementFactory> factories = null;
    
    private ElementFactoryRegistry() {
        initialize();
    }
    
    public static ElementFactoryRegistry getDefault(){
        if (registry == null) {
            registry = new ElementFactoryRegistry();
        }
        return registry;
    }

    //TODO listen on changes when we have external extensions
    private void initialize(){
        factories = new Hashtable<QName, ElementFactory>();
        Lookup.Result results = Lookup.getDefault().lookup(new Lookup.Template(ElementFactory.class));
        for (Object service : results.allInstances()){
            register((ElementFactory)service);
        }
        
        //try meta-inf services lookup using this class's classloader
        //if no factories are found, This is required for lookup to work
        //from comp app project ant task
        if (factories.size() < 1) {
            Lookup lu2 = Lookups.metaInfServices(this.getClass().getClassLoader());
            Lookup.Result results2 = lu2.lookup(new Lookup.Template(ElementFactory.class));
            for (Object service : results2.allInstances()){
                register((ElementFactory)service);
            }
        }
    }
    
    public void register(ElementFactory factory) {
        for (QName q : factory.getElementQNames()) {
            factories.put(q, factory);
        }
        resetQNameCache();
    }
    
    public void unregister(ElementFactory fac){
        for (QName q : fac.getElementQNames()) {
            factories.remove(q);
        }
        resetQNameCache();
    }
    
    public ElementFactory get(QName type){
        return factories.get(type);
    }
    
    private Set<Class> knownEmbeddedModelTypes = null;
    private Set<QName> knownQNames = null;
    private Set<String> knownNames = null;
    
    public void resetQNameCache() {
        knownEmbeddedModelTypes = null;
        knownQNames = null;
        knownNames = null;
    }
    
    public Set<QName> getKnownQNames() {
        return Collections.unmodifiableSet(knownQNames());
    }
    
    private Set<QName> knownQNames() {
        if (knownQNames == null) {
            knownQNames = new HashSet<QName>();
            for (ElementFactory f : factories.values()) {
                for (QName q : f.getElementQNames()) {
                    if (! knownQNames.add(q)) {
                        String msg = "Duplicate factory for: "+q;
                        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getKnownQNames", msg); //NOI18N
                    }
                }
            }
        }
        return knownQNames;
    }

    public Set<String> getKnownElementNames() {
        return Collections.unmodifiableSet(knownElementNames());
    }
    
    private Set<String> knownElementNames() {
        if (knownNames == null) {
            knownNames = new HashSet<String>();
            for (QName q : knownQNames()) {
                knownNames.add(q.getLocalPart());
            }
        }
        return knownNames;
    }
    
    public void addEmbeddedModelQNames(AbstractDocumentModel embeddedModel) {
        if (knownEmbeddedModelTypes == null) {
            knownEmbeddedModelTypes = new HashSet();
        }
        if (! knownEmbeddedModelTypes.contains(embeddedModel.getClass())) {
            knownQNames().addAll(embeddedModel.getQNames());
            knownNames = null;
        }
    }
}
