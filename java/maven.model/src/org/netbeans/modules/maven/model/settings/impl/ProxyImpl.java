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
package org.netbeans.modules.maven.model.settings.impl;

import org.netbeans.modules.maven.model.settings.Proxy;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProxyImpl extends SettingsComponentImpl implements Proxy {

    public ProxyImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ProxyImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().PROXY));
    }

    // attributes

    // child elements


    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getId() {
        return getChildElementText(getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public void setId(String id) {
        setChildElementText(getModel().getSettingsQNames().ID.getName(), id,
                getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public Boolean getActive() {
        String str = getChildElementText(getModel().getSettingsQNames().ACTIVE.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setActive(Boolean active) {
        setChildElementText(getModel().getSettingsQNames().ACTIVE.getName(),
                active == null ? null : active.toString(),
                getModel().getSettingsQNames().ACTIVE.getQName());
    }

    @Override
    public String getProtocol() {
        return getChildElementText(getModel().getSettingsQNames().PROTOCOL.getQName());
    }

    @Override
    public void setProtocol(String protocol) {
        setChildElementText(getModel().getSettingsQNames().PROTOCOL.getName(), protocol,
                getModel().getSettingsQNames().PROTOCOL.getQName());
    }

    @Override
    public String getUsername() {
        return getChildElementText(getModel().getSettingsQNames().USERNAME.getQName());
    }

    @Override
    public void setUsername(String username) {
        setChildElementText(getModel().getSettingsQNames().USERNAME.getName(), username,
                getModel().getSettingsQNames().USERNAME.getQName());
    }

    @Override
    public String getPassword() {
        return getChildElementText(getModel().getSettingsQNames().PASSWORD.getQName());
    }

    @Override
    public void setPassword(String password) {
        setChildElementText(getModel().getSettingsQNames().PASSWORD.getName(), password,
                getModel().getSettingsQNames().PASSWORD.getQName());
    }

    @Override
    public String getPort() {
        return getChildElementText(getModel().getSettingsQNames().PORT.getQName());
    }

    @Override
    public void setPort(String port) {
        setChildElementText(getModel().getSettingsQNames().PORT.getName(), port,
                getModel().getSettingsQNames().PORT.getQName());
    }

    @Override
    public String getHost() {
        return getChildElementText(getModel().getSettingsQNames().HOST.getQName());
    }

    @Override
    public void setHost(String host) {
        setChildElementText(getModel().getSettingsQNames().HOST.getName(), host,
                getModel().getSettingsQNames().HOST.getQName());
    }

    @Override
    public String getNonProxyHosts() {
        return getChildElementText(getModel().getSettingsQNames().NONPROXYHOSTS.getQName());
    }

    @Override
    public void setNonProxyHosts(String nonProxyHosts) {
        setChildElementText(getModel().getSettingsQNames().NONPROXYHOSTS.getName(), nonProxyHosts,
                getModel().getSettingsQNames().NONPROXYHOSTS.getQName());
    }


    public static class List extends ListImpl<Proxy> {
        public List(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().PROXY, Proxy.class);
        }

        public List(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().PROXIES));
        }
    }


}
