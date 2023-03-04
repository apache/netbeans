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
public class CiManagementImpl extends POMComponentImpl implements CiManagement {

    public CiManagementImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public CiManagementImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().CIMANAGEMENT));
    }

    // attributes

    // child elements
    @Override
    public List<Notifier> getNotifiers() {
        return getChildren(Notifier.class);
    }

    @Override
    public void addNotifier(Notifier notifier) {
        appendChild(getModel().getPOMQNames().NOTIFIER.getName(), notifier);
    }

    @Override
    public void removeNotifier(Notifier notifier) {
        removeChild(getModel().getPOMQNames().NOTIFIER.getName(), notifier);
    }


    @Override
    public String getSystem() {
        return getChildElementText(getModel().getPOMQNames().CIMANAG_SYSTEM.getQName());
    }

    @Override
    public void setSystem(String system) {
        setChildElementText(getModel().getPOMQNames().CIMANAG_SYSTEM.getName(), system,
                getModel().getPOMQNames().CIMANAG_SYSTEM.getQName());
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
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
