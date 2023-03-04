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

import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.Server;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ServerImpl extends SettingsComponentImpl implements Server {

    private static final Class<? extends SettingsComponent>[] ORDER = new Class[] {
        Configuration.class
    };

    public ServerImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ServerImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().SERVER));
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
    public String getUsername() {
        return getChildElementText(getModel().getSettingsQNames().USERNAME.getQName());
    }

    @Override
    public void setUsername(String username) {
        setChildElementText(getModel().getSettingsQNames().USERNAME.getName(), username,
                getModel().getSettingsQNames().USERNAME.getQName());
    }

    @Override
    public String getPassphrase() {
        return getChildElementText(getModel().getSettingsQNames().PASSPHRASE.getQName());
    }

    @Override
    public void setPassphrase(String passphrase) {
        setChildElementText(getModel().getSettingsQNames().PASSPHRASE.getName(), passphrase,
                getModel().getSettingsQNames().PASSPHRASE.getQName());
    }

    @Override
    public String getPrivateKey() {
        return getChildElementText(getModel().getSettingsQNames().PRIVATEKEY.getQName());
    }

    @Override
    public void setPrivateKey(String key) {
        setChildElementText(getModel().getSettingsQNames().PRIVATEKEY.getName(), key,
                getModel().getSettingsQNames().PRIVATEKEY.getQName());
    }


    @Override
    public Configuration getConfiguration() {
        return getChild(Configuration.class);
    }

    @Override
    public void setConfiguration(Configuration config) {
        setChild(Configuration.class, getModel().getSettingsQNames().CONFIGURATION.getName(), config,
                getClassesBefore(ORDER, Configuration.class));
    }


    public static class List extends ListImpl<Server> {
        public List(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().SERVER, Server.class);
        }

        public List(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().SERVERS));
        }
    }


}
