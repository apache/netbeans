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

import java.util.*;	
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ActivationImpl extends POMComponentImpl implements Activation {

    public ActivationImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ActivationImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().ACTIVATION));
    }

    // attributes

    // child elements
    @Override
    public ActivationOS getActivationOS() {
        return getChild(ActivationOS.class);
    }

    @Override
    public void setActivationOS(ActivationOS activationOS) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationOS.class, getModel().getPOMQNames().ACTIVATIONOS.getName(), activationOS, empty);
    }

    @Override
    public ActivationProperty getActivationProperty() {
        return getChild(ActivationProperty.class);
    }

    @Override
    public void setActivationProperty(ActivationProperty activationProperty) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationProperty.class, getModel().getPOMQNames().ACTIVATIONPROPERTY.getName(), activationProperty, empty);
    }

    @Override
    public ActivationFile getActivationFile() {
        return getChild(ActivationFile.class);
    }

    @Override
    public void setActivationFile(ActivationFile activationFile) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationFile.class, getModel().getPOMQNames().ACTIVATIONFILE.getName(), activationFile, empty);
    }

    @Override
    public ActivationCustom getActivationCustom() {
        return getChild(ActivationCustom.class);
    }

    @Override
    public void setActivationCustom(ActivationCustom activationCustom) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationCustom.class, getModel().getPOMQNames().ACTIVATIONCUSTOM.getName(), activationCustom, empty);
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
