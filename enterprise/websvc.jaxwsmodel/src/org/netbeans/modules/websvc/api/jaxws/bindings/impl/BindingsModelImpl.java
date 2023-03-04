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
package org.netbeans.modules.websvc.api.jaxws.bindings.impl;


import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponentFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsModelImpl extends AbstractDocumentModel<BindingsComponent>
        implements BindingsModel{
    
    private BindingsComponentFactory bcf;
    private GlobalBindings gb;
    
    /** Creates a new instance of BindingsModelImpl */
    /*public BindingsModelImpl(javax.swing.text.Document doc) {
         super(doc);
         bcf = new BindingsComponentFactoryImpl(this);
    }*/
    
    public BindingsModelImpl(ModelSource source){
        super(source);
        bcf = new BindingsComponentFactoryImpl(this);
    }
    
    public void setGlobalBindings(GlobalBindings gbindings){
        assert (gbindings instanceof GlobalBindingsImpl) ;
        gb = GlobalBindingsImpl.class.cast(gbindings);
    }
    
    public BindingsComponent createRootComponent(Element root) {
        if (BindingsQName.JAXWS_NS_URI.equals(root.getNamespaceURI())){
            GlobalBindingsImpl gbindings = new GlobalBindingsImpl(this, root);
            setGlobalBindings(gbindings);
        }
        return gb;
    }
    
    public BindingsComponent createComponent(BindingsComponent parent, Element element) {
        return getFactory().create(element, parent);
    }
    
    public BindingsComponent getRootComponent() {
        return gb;
    }
    
    public GlobalBindings getGlobalBindings() {
        return gb;
    }
    
    public BindingsComponentFactory getFactory() {
        return bcf;
    }
    
    protected ComponentUpdater<BindingsComponent> getComponentUpdater() {
        return null;
    }
    
}
