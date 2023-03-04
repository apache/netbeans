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

import java.util.Collections;
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ContributorImpl extends POMComponentImpl implements Contributor {

    public ContributorImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ContributorImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().CONTRIBUTOR));
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
    public String getEmail() {
        return getChildElementText(getModel().getPOMQNames().EMAIL.getQName());
    }

    @Override
    public void setEmail(String email) {
        setChildElementText(getModel().getPOMQNames().EMAIL.getName(), email,
                getModel().getPOMQNames().EMAIL.getQName());
    }

    @Override
    public String getOrganization() {
        return getChildElementText(getModel().getPOMQNames().ORGANIZATION.getQName());
    }

    @Override
    public void setOrganization(String organization) {
        setChildElementText(getModel().getPOMQNames().ORGANIZATION.getName(), organization,
                getModel().getPOMQNames().ORGANIZATION.getQName());
    }

    @Override
    public String getOrganizationUrl() {
        return getChildElementText(getModel().getPOMQNames().ORGANIZATIONURL.getQName());
    }

    @Override
    public void setOrganizationUrl(String url) {
        setChildElementText(getModel().getPOMQNames().ORGANIZATIONURL.getName(), url,
                getModel().getPOMQNames().ORGANIZATIONURL.getQName());
    }

    @Override
    public String getTimezone() {
        return getChildElementText(getModel().getPOMQNames().TIMEZONE.getQName());
    }

    @Override
    public void setTimezone(String zone) {
        setChildElementText(getModel().getPOMQNames().TIMEZONE.getName(), zone,
                getModel().getPOMQNames().TIMEZONE.getQName());
    }

    @Override
    public java.util.List<String> getRoles() {
        StringList list = getRolesList();
        return list != null ? list.getListChildren() : null;
    }

    @Override
    public void addRole(String role) {
        StringList list = getRolesList();
        if (list != null) {
            list.addListChild(role);
            return;
        }
        setChild(StringListImpl.class,
                 getModel().getPOMQNames().ROLES.getName(),
                 getModel().getFactory().create(this, getModel().getPOMQNames().ROLES.getQName()),
                 Collections.<Class<? extends POMComponent>>emptySet());
        list = getRolesList();
        if (list != null) {
            list.addListChild(role);
        }
    }

    @Override
    public void removeRole(String role) {
        StringList list = getRolesList();
        if (list != null) {
            list.removeListChild(role);
        }
    }

    private StringList getRolesList() {
        java.util.List<StringList> lists = getChildren(StringList.class);
        for (StringList list : lists) {
            if (getModel().getPOMQNames().ROLES.getName().equals(list.getPeer().getLocalName())) {
                return list;
            }
        }
        return null;
    }

    public static class List extends ListImpl<Contributor> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().CONTRIBUTOR, Contributor.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().CONTRIBUTORS));
        }
    }

}
