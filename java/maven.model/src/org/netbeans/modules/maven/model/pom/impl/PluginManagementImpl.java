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
public class PluginManagementImpl extends POMComponentImpl implements PluginManagement {

    public PluginManagementImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PluginManagementImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().PLUGINMANAGEMENT));
    }

    // attributes

    // child elements
    @Override
    public List<Plugin> getPlugins() {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addPlugin(Plugin plugin) {
        ModelList<Plugin> childs = getChild(PluginImpl.List.class);
        if (childs == null) {
            setChild(PluginImpl.List.class,
                    getModel().getPOMQNames().PLUGINS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().PLUGINS.getQName()),
                    Collections.<Class<? extends POMComponent>>emptyList());
            childs = getChild(PluginImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(plugin);
    }

    @Override
    public void removePlugin(Plugin plugin) {
        remove(plugin, getModel().getPOMQNames().PLUGINS.getName(), PluginImpl.List.class);
    }

    @Override
    public Plugin findPluginById(String groupId, String artifactId) {
        assert groupId != null;
        assert artifactId != null;
        List<Plugin> plugs = getPlugins();
        if (plugs != null) {
            for (Plugin plug : plugs) {
                String plugGroupId = plug.getGroupId();
                if (plugGroupId == null) {
                    plugGroupId = "org.apache.maven.plugins"; //the default groupId
                }
                if (groupId.equals(plugGroupId) && artifactId.equals(plug.getArtifactId())) {
                    return plug;
                }
            }
        }
        return null;
    }


    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
