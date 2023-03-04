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
package org.netbeans.modules.maven.model.pom.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.pom.visitor.ChildComponentUpdateVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class POMModelImpl extends POMModel {
    
    private POMComponent rootComponent;
    private POMComponentFactory componentFactory;
    private POMQNames pomqnames;
    private QName PROJECT_NS = POMQName.createQName("project", true); ///NOI18N
    private QName PROJECT = POMQName.createQName("project", false); ///NOI18N
    
    public POMModelImpl(ModelSource source) {
        super(source);
        componentFactory = new POMComponentFactoryImpl(this);
    }
    
    @Override
    public POMComponent getRootComponent() {
        return rootComponent;
    }

    @Override
    public POMComponentFactory getFactory() {
        return componentFactory;
    }

    @Override
    public Project getProject() {
        return (Project) getRootComponent();
    }

    @Override
    public POMComponent createRootComponent(Element root) {
        QName q = root == null ? null : AbstractDocumentComponent.getQName(root);
        if (root != null ) {
            if (PROJECT.equals(q)) {
                pomqnames = new POMQNames(false);
                rootComponent = new ProjectImpl(this, root);
            } else if (PROJECT_NS.equals(q)) {
                pomqnames = new POMQNames(true);
                rootComponent = new ProjectImpl(this, root);
            }
        } 
        
        return getRootComponent();
    }

    @Override
    protected ComponentUpdater<POMComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<POMComponent>();
    }

    @Override
    public POMComponent createComponent(POMComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    @Override
    public POMQNames getPOMQNames() {
        return pomqnames;
    }

}
