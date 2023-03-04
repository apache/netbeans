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
public class ActivationOSImpl extends POMComponentImpl implements ActivationOS {

    public ActivationOSImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ActivationOSImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().ACTIVATIONOS));
    }

    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
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
    public String getFamily() {
        return getChildElementText(getModel().getPOMQNames().FAMILY.getQName());
    }

    @Override
    public void setFamily(String family) {
        setChildElementText(getModel().getPOMQNames().FAMILY.getName(), family,
                getModel().getPOMQNames().FAMILY.getQName());
    }

    @Override
    public String getArch() {
        return getChildElementText(getModel().getPOMQNames().ARCH.getQName());
    }

    @Override
    public void setArch(String arch) {
        setChildElementText(getModel().getPOMQNames().ARCH.getName(), arch,
                getModel().getPOMQNames().ARCH.getQName());
    }

    @Override
    public String getVersion() {
        return getChildElementText(getModel().getPOMQNames().VERSION.getQName());
    }

    @Override
    public void setVersion(String version) {
        setChildElementText(getModel().getPOMQNames().VERSION.getName(), version,
                getModel().getPOMQNames().VERSION.getQName());
    }

}
