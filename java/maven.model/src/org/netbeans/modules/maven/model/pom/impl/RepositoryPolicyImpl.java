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
public class RepositoryPolicyImpl extends POMComponentImpl implements RepositoryPolicy {

    public RepositoryPolicyImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public RepositoryPolicyImpl(POMModel model, POMQName name) {
        this(model, createElementNS(model, name));
    }

    // attributes

    // child elements

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Boolean isEnabled() {
        String str = getChildElementText(getModel().getPOMQNames().ENABLED.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        setChildElementText(getModel().getPOMQNames().ENABLED.getName(),
                enabled == null ? null : enabled.toString(),
                getModel().getPOMQNames().ENABLED.getQName());
    }

    @Override
    public String getUpdatePolicy() {
        return getChildElementText(getModel().getPOMQNames().UPDATEPOLICY.getQName());
    }

    @Override
    public void setUpdatePolicy(String updatePolicy) {
        setChildElementText(getModel().getPOMQNames().UPDATEPOLICY.getName(), updatePolicy,
                getModel().getPOMQNames().UPDATEPOLICY.getQName());
    }

    @Override
    public String getChecksumPolicy() {
        return getChildElementText(getModel().getPOMQNames().CHECKSUMPOLICY.getQName());
    }

    @Override
    public void setChecksumPolicy(String checksumPolicy) {
        setChildElementText(getModel().getPOMQNames().CHECKSUMPOLICY.getName(), checksumPolicy,
                getModel().getPOMQNames().CHECKSUMPOLICY.getQName());
    }


}
