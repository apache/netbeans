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

import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class DeploymentRepositoryImpl extends IdPOMComponentImpl implements DeploymentRepository {

    public DeploymentRepositoryImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public DeploymentRepositoryImpl(POMModel model, POMQName name) {
        this(model, createElementNS(model, name));
    }

    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getUrl() {
        return getChildElementText(getModel().getPOMQNames().URL.getQName());
    }

    @Override
    public void setUrl(String url) {
        setChildElementText(getModel().getPOMQNames().URL.getName(), url,
                getModel().getPOMQNames().URL.getQName());
    }

    @Override
    public String getName() {
        return getChildElementText(getModel().getPOMQNames().NAME.getQName());
    }

    @Override
    public void setName(String name) {
        setChildElementText(getModel().getPOMQNames().NAME.getName(), name,
                getModel().getPOMQNames().NAME.getQName());
    }

    @Override
    public String getLayout() {
        return getChildElementText(getModel().getPOMQNames().LAYOUT.getQName());
    }

    @Override
    public void setLayout(String layout) {
        setChildElementText(getModel().getPOMQNames().LAYOUT.getName(), layout,
                getModel().getPOMQNames().LAYOUT.getQName());
    }


}
